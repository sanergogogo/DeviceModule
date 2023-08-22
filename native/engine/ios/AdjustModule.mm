#import "AdjustModule.h"
#import <Foundation/Foundation.h>

#import "Adjust.h"

@implementation AdjustModule

static bool inited = false;

const NSDictionary *kEventMap=@{
    @"purchase" : @"mfmtqc",
    @"normal_event" : @"wxyb47"
};

+(void)initSdk {
    NSString *yourAppToken = @"4zf1u1v99thc";
    NSString *environment = ADJEnvironmentSandbox;
    ADJConfig* adjustConfig = [ADJConfig configWithAppToken:yourAppToken
                                      environment:environment];
    [adjustConfig setLogLevel:ADJLogLevelVerbose];
    [Adjust appDidLaunch:adjustConfig];
    inited = true;
}

+(bool) hasAdjust {
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
    
    NSString* event_name = [NSString stringWithFormat:@"%@",[json objectForKey:@"event_name"]];
    NSString* event_token = [NSString stringWithFormat:@"%@",[kEventMap objectForKey:event_name]];
    int event_type = [[json objectForKey:@"event_type"] intValue];
    
    ADJEvent *event = [ADJEvent eventWithEventToken:event_token];
    if (event_type == 1) {
        double value = [[json objectForKey:@"value"] doubleValue];
        NSString* currency = [NSString stringWithFormat:@"%@",[json objectForKey:@"currency"]];
        NSString* order_id = [NSString stringWithFormat:@"%@",[json objectForKey:@"order_id"]];
        NSString* product_id = [NSString stringWithFormat:@"%@",[json objectForKey:@"product_id"]];
        [event setRevenue:value currency:currency];
        [event setTransactionId:order_id];
    }
    [Adjust trackEvent:event];
    
    return true;
}

@end
