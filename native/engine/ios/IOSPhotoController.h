
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface IOSPhotoController: UIViewController<UINavigationControllerDelegate, UIImagePickerControllerDelegate>

-(id)initWithBlock:(NSDictionary*)infoDic;
-(void) openPhoto;
-(void)takePhoto;
-(void) imagePickerControllerDidCancel:(UIImagePickerController *_Nullable)picker;
-(void) imagePickerController:(UIImagePickerController *_Nullable)picker didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey, id> *_Nullable)info;
-(void) imageTopicSave:(UIImage *_Nullable)image;
-(UIImage *)imageWithImageRepresentation:(UIImage *)image percent:(float)percent;
-(UIImage *)imageWithImageSimple:(UIImage*)image scaledToSize:(CGSize)newSize;
-(NSString *)toJsonString:(NSDictionary *)dict;

@end

