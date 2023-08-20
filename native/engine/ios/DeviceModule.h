
#import <Foundation/Foundation.h>

@interface DeviceModule: NSObject

+(NSString *) getDeviceUuid;
+(NSString *) getDeviceAdid;
+(NSString *) getDeviceName;
+(int) getNetworkStrength;
+(void) vibrate: (NSNumber*) duration;
+(void) setKeepScreenOn: (BOOL) keepScreenOn;
+(void) shareText: (NSString *) text andScheme: (NSString *) scheme;
+(void) shareImage: (NSString *) uri andScheme: (NSString *) scheme;
+(void) shareVideo: (NSString *) uri andScheme: (NSString *) scheme;
+(void) shareFile: (NSString *) uri andScheme: (NSString *) scheme;
+(bool) appInstalled:(NSString *) scheme;
+(bool) saveImageToAlbum:(NSString *) fullPathForFilename;
+(bool) selectImageFromAlbum: (NSString*) fullPathForFilename;
+(bool) changeOrientation: (int) orientation;
+(bool)doFacebookLogin;
+(bool)doAppleLogin;

@end
