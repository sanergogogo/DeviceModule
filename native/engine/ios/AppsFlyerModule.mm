#import "AppsFlyerModule.h"
#import <Foundation/Foundation.h>

#import <AppsFlyerLib/AppsFlyerLib.h>

@implementation AppsFlyerModule

static bool inited = false;

const NSDictionary *kEventMap=@{
    @"purchase" : @"mfmtqc",
    @"normal_event" : @"wxyb47"
};

+(void)initSdk {
    [[AppsFlyerLib shared] setAppsFlyerDevKey:@"<AF_DEV_KEY>"];
    [[AppsFlyerLib shared] setAppleAppID:@"<APPLE_APP_ID>"];
    [AppsFlyerLib shared].isDebug = true;
    inited = true;
}

+(void)start {
    if (!inited) {
        return;
    }
    [[AppsFlyerLib shared] start];
}

+(BOOL) handleOpenUrl:(NSURL *)url options:(NSDictionary *)options {
    if (!inited) {
        return FALSE;
    }
    [[AppsFlyerLib shared] handleOpenUrl:url options:options];
    return YES;
}

+(BOOL) handleOpenUrl2:(NSURL *)url sourceApplication:(NSString*)sourceApplication annotation:(id)annotation {
    if (!inited) {
        return FALSE;
    }
    [[AppsFlyerLib shared] handleOpenURL:url sourceApplication:sourceApplication withAnnotation:annotation];
    return YES;
}

+(bool) hasAppsFlyer {
    return inited;
}

+(bool) traceEvent:(NSString *)eventData {
    if (!inited) {
        return false;
    }
    
    NSError *jsonError;
    NSData *objectData = [eventData dataUsingEncoding:NSUTF8StringEncoding];
    NSDictionary *json = [NSJSONSerialization JSONObjectWithData:objectData
                                          options:NSJSONReadingMutableContainers
                                            error:&jsonError];
    if (!json) {
        NSLog(@"%s: error: %@", __func__, jsonError.localizedDescription);
        return false;
    }
    
    NSString* event_name = [NSString stringWithFormat:@"af_%@",[json objectForKey:@"event_name"]];
    int event_type = [[json objectForKey:@"event_type"] intValue];
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    if (event_type == 1) {
        [params setValue:[json objectForKey:@"value"] forKey:AFEventParamRevenue];
        [params setValue:[json objectForKey:@"currency"] forKey:AFEventParamCurrency];
        [params setValue:[json objectForKey:@"order_id"] forKey:AFEventParamOrderId];
        [params setValue:[json objectForKey:@"product_id"] forKey:AFEventParamContentId];
    } else {
        params = nil;
    }
    
    [[AppsFlyerLib shared]  logEvent: event_name withValues: params];
    
    return true;
}

@end
