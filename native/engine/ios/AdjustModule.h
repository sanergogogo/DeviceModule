
#import <Foundation/Foundation.h>

@interface AdjustModule: NSObject

+(void) initSdk;
+(bool) hasAdjust;
+(bool) traceEvent: (NSString *) eventData;

@end
