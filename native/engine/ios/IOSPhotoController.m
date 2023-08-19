
#import <CoreFoundation/CoreFoundation.h>
#import <Foundation/Foundation.h>
#import "IOSPhotoController.h"
#include "platform/apple/JsbBridge.h"

@interface IOSPhotoController ()
@property (nonatomic,assign) int cropType; //0 不裁剪 1:按宽高裁剪
@property (nonatomic,assign) float aspectRatioX;
@property (nonatomic,assign) float aspectRatioY;
@property (nonatomic,strong) NSString* filename;//图片名字
@property (nonatomic,assign) int maxWidth;
@property (nonatomic,assign) int maxHeight;
@end

@implementation IOSPhotoController

-(id)initWithBlock:(NSDictionary*)infoDic
{
    if (self = [super init]) {
        NSLog(@"初始化");
        if ([infoDic objectForKey:@"filename"]) {
            self.filename = [NSString stringWithFormat:@"%@",[infoDic objectForKey:@"filename"]];
            NSLog(@"filename:%@",[NSString stringWithFormat:@"%@",[infoDic objectForKey:@"filename"]]);
        } else {
            self.filename = @"crop.jpg";
        }
        if ([infoDic objectForKey:@"type"]) {
            self.cropType = [[infoDic objectForKey:@"type"] intValue];
            NSLog(@"clipType:%d",[[infoDic objectForKey:@"type"] intValue]);
        } else {
            self.cropType = 0;
        }
        if ([infoDic objectForKey:@"aspectRatioX"]) {
            self.aspectRatioX = [[infoDic objectForKey:@"aspectRatioX"] floatValue];

            NSLog(@"aspectRatioX:%f",[[infoDic objectForKey:@"aspectRatioX"] floatValue]);
        } else {
            self.aspectRatioX = 1;
        }
        
        if ([infoDic objectForKey:@"aspectRatioY"]) {
            self.aspectRatioY = [[infoDic objectForKey:@"aspectRatioY"] floatValue];

            NSLog(@"aspectRatioY:%f",[[infoDic objectForKey:@"aspectRatioY"] floatValue]);
        } else {
            self.aspectRatioY = 1;
        }
        
        if ([infoDic objectForKey:@"maxWidth"]) {
            self.maxWidth = [[infoDic objectForKey:@"maxWidth"] intValue];
            NSLog(@"maxWidth:%d",[[infoDic objectForKey:@"maxWidth"] intValue]);
        } else {
            self.maxWidth = 500;
        }
        
        if ([infoDic objectForKey:@"maxHeight"]) {
            self.maxHeight = [[infoDic objectForKey:@"maxHeight"] intValue];
            NSLog(@"maxHeight:%d",[[infoDic objectForKey:@"maxHeight"] intValue]);
        } else {
            self.maxHeight = 500;
        }
        self.cropType = 1;
    }
   return self;
}

//开始拍照
-(void)takePhoto
{
    UIImagePickerControllerSourceType sourceType = UIImagePickerControllerSourceTypeCamera;
    if ([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypeCamera])
    {
        UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
        //设置拍照后的图片可被编辑
        if (self.cropType == 0) {
            imagePicker.allowsEditing = NO;
        } else {
            imagePicker.allowsEditing = YES;
        }
        imagePicker.sourceType = sourceType;
        imagePicker.cameraViewTransform = CGAffineTransformMakeScale(1.5, 1.5);

        UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
        imagePicker.delegate = self;
        [ctrol presentViewController:imagePicker animated:YES completion:nil];
        //[imagePicker release];
    } else {
        NSLog(@"模拟其中无法打开照相机,请在真机中使用");
    }
}

-(void) openPhoto
{
    UIImagePickerController *imagePicker = [[UIImagePickerController alloc]init];
    //Yes为裁剪后的图片，no为原图
    if (self.cropType == 0) {
        imagePicker.allowsEditing = NO;
    } else {
        imagePicker.allowsEditing = YES;
    }
    
    imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    UIViewController* ctrol = [UIApplication sharedApplication].keyWindow.rootViewController;
    imagePicker.delegate = self;
    [ctrol presentViewController:imagePicker animated:YES completion:nil];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey, id> *)info
{
    UIImage *image;
    if (self.cropType == 0) {
        image = [info objectForKey:UIImagePickerControllerOriginalImage];///原图
    } else {
        image = [info objectForKey:UIImagePickerControllerEditedImage];
        
//        //先把图片转成NSData
//        NSLog(@"选择图片原图---width:%.0f,height:%.0f",image.size.width,image.size.height);
//
//        float withRatio =
//        CGSize tmpCGSize;
//        int screenWidth = [[UIScreen mainScreen]bounds].size.width;
//        tmpCGSize.width = image.size.width;
//        tmpCGSize.height = image.size.width * self.clipRatio;
//        image = [self imageWithImageSimple:image scaledToSize:tmpCGSize];
//        image = [self imageWithImageRepresentation:image percent:0.01];
//        NSLog(@"选择图片裁剪---width:%.0f,height:%.0f",tmpCGSize.width,tmpCGSize.height);
    }
    
    [self imageTopicSave:image];
    
    //如果app默认不是横屏则不需要此行代码
    //[[NSNotificationCenter defaultCenter] postNotificationName:@"changeOrientation" object:@"0"];// 横竖屏切换通知

    //关闭相册界面
    [picker dismissViewControllerAnimated:NO completion:nil];
    [self release];
}

///取消选择图片（拍照）
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    //如果app默认不是横屏则不需要此行代码
    //[[NSNotificationCenter defaultCenter] postNotificationName:@"changeOrientation" object:@"0"];// 横竖屏切换通知

    [picker dismissViewControllerAnimated:YES completion:nil];
    [self release];
}

-(UIImage *)imageWithImageRepresentation:(UIImage *)image percent:(float)percent {
    NSData *imageData = UIImageJPEGRepresentation(image, percent);
    UIImage *newImage = [UIImage imageWithData:imageData];
    return newImage;
}

-(UIImage*)imageWithImageSimple:(UIImage*)image scaledToSize:(CGSize)newSize {
    // Create a graphics image context
    UIGraphicsBeginImageContext(newSize);
    // new size
    //CGFloat wid = newSize.width;
    [image drawInRect:CGRectMake(0,0,newSize.width,newSize.height)];
    // Get the new image from the context
    UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
    
    // End the context
    UIGraphicsEndImageContext();
    // Return the new image.
    return newImage;
}

///保存图片到本地相册
-(void)imageTopicSave:(UIImage *)image{
    if (image == nil) {
        return;
    }
    
    NSData *data;
    if (UIImagePNGRepresentation(image) == nil)
    {
        data = UIImageJPEGRepresentation(image, 1.0);
    }
    else
    {
        data = UIImagePNGRepresentation(image);
    }
    
    //图片保存的路径
    //这里将图片放在沙盒的documents文件夹中
    NSString * DocumentsPath = [NSHomeDirectory() stringByAppendingPathComponent:@"Documents"];
    //文件管理器
    NSFileManager *fileManager = [NSFileManager defaultManager];
    //把刚刚图片转换的data对象拷贝至沙盒中 并保存为image.png
    [fileManager createDirectoryAtPath:DocumentsPath withIntermediateDirectories:YES attributes:nil error:nil];
    NSString *TempString=[NSString stringWithFormat:@"/%@",self.filename];
    
    [fileManager createFileAtPath:[DocumentsPath stringByAppendingString:TempString] contents:data attributes:nil];
            
    //得到选择后沙盒中图片的完整路径
    NSString* filePath = [[NSString alloc]initWithFormat:@"%@%@",DocumentsPath,TempString];
    
    BOOL result = [UIImagePNGRepresentation(image) writeToFile:filePath atomically:YES];
    
    if (result) {
        NSDictionary *dict=@{@"filepath" : filePath};
        NSString* jsonstr = [self toJsonString:dict];
        
        JsbBridge* m = [JsbBridge sharedInstance];
        [m sendToScript:@"selectImageFromAlbum" arg1:jsonstr];
        
        NSLog(@"图片保存成功 路径是---：%@", filePath);
    } else {
        JsbBridge* m = [JsbBridge sharedInstance];
        [m sendToScript:@"selectImageFromAlbum" arg1: @"{}"];
        NSLog(@"图片保存失败 路径是---：%@", filePath);
    }
}

-(NSString *)toJsonString:(NSDictionary *)dict {
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
