#import "FirebaseModule.h"
#import <Foundation/Foundation.h>

#import "FirebaseCore.h"
#import <FirebaseAnalytics/FIRAnalytics.h>

@implementation FirebaseModule

static bool inited = false;

+(void)initSdk {
    [FIRApp configure];
    inited = true;
}

+(bool) hasFirebase {
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
    int event_type = [[json objectForKey:@"event_type"] intValue];
    
    NSMutableDictionary *params = [[NSMutableDictionary alloc] init];
    if (event_type == 1) {
        NSString* product_id = [NSString stringWithFormat:@"%@",[json objectForKey:@"product_id"]];
        [params setValue:[json objectForKey:@"value"] forKey:@"value"];
        [params setValue:[json objectForKey:@"currency"] forKey:@"currency"];
        [params setValue:[json objectForKey:@"order_id"] forKey:@"transaction_id"];
    } else {
        params = nil;
    }
    [FIRAnalytics logEventWithName:event_name
                        parameters:params];
    
    return true;
}

@end
