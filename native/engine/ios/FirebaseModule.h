
#import <Foundation/Foundation.h>

@interface FirebaseModule: NSObject

+(void) initSdk;
+(bool) hasFirebase;
+(bool) traceEvent: (NSString *) eventData;

@end
