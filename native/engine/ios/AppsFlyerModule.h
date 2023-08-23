
#import <Foundation/Foundation.h>

@interface AppsFlyerModule: NSObject

+(void) initSdk;
+(void) start;
+(BOOL) handleOpenUrl:(NSURL *)url options:(NSDictionary *)options;
+(BOOL) handleOpenUrl2:(NSURL *)url sourceApplication:(NSString*)sourceApplication annotation:(id)annotation;
+(bool) hasAppsFlyer;
+(bool) traceEvent: (NSString *) eventData;

@end
