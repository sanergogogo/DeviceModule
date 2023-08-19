#import "DeviceModule.h"
#import <Foundation/Foundation.h>

#import "sys/utsname.h"
#import <UIKit/UIKit.h>
#import <AudioToolbox/AudioToolbox.h>
#import <CommonCrypto/CommonDigest.h>
#import <AdSupport/ASIdentifierManager.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import "CHKeychain/SAMKeychainQuery.h"
#import "IOSPhotoController.h"

#include "platform/apple/JsbBridge.h"

#define KEY_UUID @"com.device.module.uuid";

@implementation DeviceModule

+(NSString *)getDeviceUuid {
    NSString* appName = [[[NSBundle mainBundle] infoDictionary] objectForKey:(NSString*)kCFBundleNameKey];
    NSError *error = nil;
    SAMKeychainQuery *query = [[SAMKeychainQuery alloc] init];
    query.service = appName;
    query.account = KEY_UUID;
    [query fetch:&error];
    NSString* appUUID = query.password;
    if (appUUID == nil) {
        appUUID = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        query.password = appUUID;
        query.synchronizationMode = SAMKeychainQuerySynchronizationModeNo;
        [query save:&error];
    }
    
    return appUUID;
}

+(NSString *) getDeviceAdid {
    __block NSString *idfa = @"";
    if (@available(iOS 14, *)) {
        // iOS14及以上版本需要先请求权限
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            // 获取到权限后，依然使用老方法获取idfa
            if (status == ATTrackingManagerAuthorizationStatusAuthorized) {
                idfa = [[ASIdentifierManager sharedManager].advertisingIdentifier UUIDString];
                NSLog(@"%@",idfa);
            } else {
                NSLog(@"请在设置-隐私-跟踪中允许App请求跟踪");
                idfa = [DeviceModule getDeviceUuid];

            }
        }];
    } else {
        // iOS14以下版本依然使用老方法
        // 判断在设置-隐私里用户是否打开了广告跟踪
        if ([[ASIdentifierManager sharedManager] isAdvertisingTrackingEnabled]) {
            idfa = [[ASIdentifierManager sharedManager].advertisingIdentifier UUIDString];
            NSLog(@"%@",idfa);
        } else {
            NSLog(@"请在设置-隐私-广告中打开广告跟踪功能");
            idfa = [DeviceModule getDeviceUuid];

        }
    }

    return idfa;
}

+(NSString *)getDeviceName {
    struct utsname systemInfo;
    uname(&systemInfo);
    // 获取设备标识Identifier
    NSString *platform = [NSString stringWithCString:systemInfo.machine encoding:NSUTF8StringEncoding];
    
    // 直接返回表示，由js方做进一步处理，方便新机型出来的时候适配
    return platform;
}

+(int)getNetworkStrength {
    return 0;
}

+(void) vibrate: (NSNumber*) duration {
    int val = [duration intValue];
    if (@available(iOS 10.0, *)) {
        UIImpactFeedbackGenerator *feedBackGenertor = [[UIImpactFeedbackGenerator alloc]initWithStyle:(UIImpactFeedbackStyle)val];
        [feedBackGenertor impactOccurred];
    } else {
        if (val == 0) {
            // 普通短震，3D Touch 中 Peek 震动反馈
            //AudioServicesPlaySystemSound(1519);
        } else if (val == 1) {
            // 普通短震，3D Touch 中 Pop 震动反馈
            //AudioServicesPlaySystemSound(1520);
        } else {
            // 连续三次短震
            AudioServicesPlaySystemSound(1521);
        }
    }
}

+(void) setKeepScreenOn: (BOOL) keepScreenOn {
    [[UIApplication sharedApplication] setIdleTimerDisabled:keepScreenOn];
}

+(void) shareText: (NSString *) text andScheme: (NSString *) scheme {
    //如果想分享图片 就把图片添加进去 文字什么的同上
    NSArray *activityItems = @[text];
    // 创建分享vc
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
    // 设置不出现在活动的项目
    //activityVC.excludedActivityTypes = @[UIActivityTypePrint,UIActivityTypeMessage,UIActivityTypeMail, UIActivityTypePrint,UIActivityTypeAddToReadingList,UIActivityTypeOpenInIBooks, UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact,UIActivityTypeSaveToCameraRoll];
    
    UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
    [ctrol presentViewController:activityVC animated:YES completion:nil];
     // 分享之后的回调
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        if (completed) {
            NSLog(@"completed");
            //分享 成功
            NSDictionary *dict=@{@"success" : @YES};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        } else  {
            NSLog(@"cancled");
            //分享 取消
            NSDictionary *dict=@{@"success" : @NO};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        }
    };
}

+(void) shareImage: (NSString *) uri andScheme: (NSString *) scheme {
    //分享的图片
    UIImage *imageToShare = [UIImage imageWithContentsOfFile:uri];
    
    //如果想分享图片 就把图片添加进去 文字什么的同上
    NSArray *activityItems = @[imageToShare];
    // 创建分享vc
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
    // 设置不出现在活动的项目
    //activityVC.excludedActivityTypes = @[UIActivityTypePrint,UIActivityTypeMessage,UIActivityTypeMail, UIActivityTypePrint,UIActivityTypeAddToReadingList,UIActivityTypeOpenInIBooks, UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact,UIActivityTypeSaveToCameraRoll];
    
    UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
    [ctrol presentViewController:activityVC animated:YES completion:nil];
     // 分享之后的回调
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        if (completed) {
            NSLog(@"completed");
            //分享 成功
            NSDictionary *dict=@{@"success" : @YES};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        } else  {
            NSLog(@"cancled");
            //分享 取消
            NSDictionary *dict=@{@"success" : @NO};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        }
    };
}

+(void) shareVideo: (NSString *) uri andScheme: (NSString *) scheme {
    //分享的图片
    NSURL* urlToShare =  [NSURL fileURLWithPath:uri];
    
    //如果想分享图片 就把图片添加进去 文字什么的同上
    NSArray *activityItems = @[urlToShare];
    // 创建分享vc
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
    // 设置不出现在活动的项目
    //activityVC.excludedActivityTypes = @[UIActivityTypePrint,UIActivityTypeMessage,UIActivityTypeMail, UIActivityTypePrint,UIActivityTypeAddToReadingList,UIActivityTypeOpenInIBooks, UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact,UIActivityTypeSaveToCameraRoll];
    
    UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
    [ctrol presentViewController:activityVC animated:YES completion:nil];
     // 分享之后的回调
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        if (completed) {
            NSLog(@"completed");
            //分享 成功
            NSDictionary *dict=@{@"success" : @YES};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        } else  {
            NSLog(@"cancled");
            //分享 取消
            NSDictionary *dict=@{@"success" : @NO};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        }
    };
}

+(void) shareFile: (NSString *) uri andScheme: (NSString *) scheme {
    //分享的图片
    NSURL* urlToShare =  [NSURL fileURLWithPath:uri];
    
    //如果想分享图片 就把图片添加进去 文字什么的同上
    NSArray *activityItems = @[urlToShare];
    // 创建分享vc
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]initWithActivityItems:activityItems applicationActivities:nil];
    // 设置不出现在活动的项目
    //activityVC.excludedActivityTypes = @[UIActivityTypePrint,UIActivityTypeMessage,UIActivityTypeMail, UIActivityTypePrint,UIActivityTypeAddToReadingList,UIActivityTypeOpenInIBooks, UIActivityTypeCopyToPasteboard,UIActivityTypeAssignToContact,UIActivityTypeSaveToCameraRoll];
    
    UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
    [ctrol presentViewController:activityVC animated:YES completion:nil];
     // 分享之后的回调
    activityVC.completionWithItemsHandler = ^(UIActivityType  _Nullable activityType, BOOL completed, NSArray * _Nullable returnedItems, NSError * _Nullable activityError) {
        if (completed) {
            NSLog(@"completed");
            //分享 成功
            NSDictionary *dict=@{@"success" : @YES};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        } else  {
            NSLog(@"cancled");
            //分享 取消
            NSDictionary *dict=@{@"success" : @NO};
            NSString* jsonstr = [DeviceModule toJsonString:dict];
            
            JsbBridge* m = [JsbBridge sharedInstance];
            [m sendToScript:@"share" arg1:jsonstr];
        }
    };
}

+(bool) checkAppInstalled:(NSString *) scheme {
    NSURL* url;
    if ([scheme containsString:@"://"]) {
        url = [NSURL URLWithString:[NSString stringWithFormat:@"%@",scheme]];
    } else {
        url = [NSURL URLWithString:[NSString stringWithFormat:@"%@://",scheme]];
    }
    if ([[UIApplication sharedApplication] canOpenURL:url]){
        return true;
    } else {
        return false;
    }
}

+(bool)saveImageToAlbum:(NSString *) fullPathForFilename {
    NSLog(@"ios saveImageToAlbum");
    NSLog(@"ios %@", fullPathForFilename);
    UIImage *img=[[UIImage alloc]initWithContentsOfFile:fullPathForFilename];
    UIImageWriteToSavedPhotosAlbum(img, nil, nil, nil);
    return true;
}

+(bool)selectImageFromAlbum: (NSString*) jsonstr {
    NSLog(@"ios selectImageFromAlbum %@", jsonstr);
    NSError *jsonError;
    NSData *objectData = [jsonstr dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                          options:NSJSONReadingMutableContainers
                                            error:&jsonError];
    if (!json) {
        NSLog(@"%s: error: %@", __func__, jsonError.localizedDescription);
        return false;
    }
    
    //如果app默认不是横屏则不需要此行代码
    //[[NSNotificationCenter defaultCenter] postNotificationName:@"changeOrientation" object:@"1"];// 横竖屏切换通知

    IOSPhotoController *helper = [[IOSPhotoController alloc]initWithBlock:json];
    [helper openPhoto];
    
    return true;
}

+(bool)changeOrientation: (int) orientation {
    NSLog(@"Thread:%@",[NSThread currentThread]);
    [[NSNotificationCenter defaultCenter] postNotificationName:@"changeOrientation" object:[NSString stringWithFormat:@"%d", orientation]];// 横竖屏切换通知
    
    return true;
}


+(NSString*)toJsonString:(NSDictionary*) dict {
    NSError * err;
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:&err];
    if (!jsonData) {
        NSLog(@"%s: error: %@", __func__, err.localizedDescription);
        return @"{}";
    } else {
        return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
}

@end
