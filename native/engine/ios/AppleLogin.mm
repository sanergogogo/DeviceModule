
#import "AppleLogin.h"
#import <AuthenticationServices/AuthenticationServices.h>

@interface AppleLogin ()<ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding>
@end

@implementation AppleLogin


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    NSLog(@"使用苹果登录");
    if (@available(iOS 13.0, *))  {
        ASAuthorizationAppleIDProvider *provider = [[ASAuthorizationAppleIDProvider alloc] init];
        ASAuthorizationAppleIDRequest *request = [provider createRequest];
        request.requestedScopes = @[ASAuthorizationScopeFullName];
        ASAuthorizationController *vc = [[ASAuthorizationController alloc] initWithAuthorizationRequests:@[request]];
        vc.delegate = self;
        vc.presentationContextProvider = self;
        [vc performRequests];
    }
    
}

#pragma mark - ASAuthorizationControllerDelegate

- (void)authorizationController:(ASAuthorizationController *)controller didCompleteWithError:(NSError *)error API_AVAILABLE(ios(13.0)) {
    NSString *errorMsg = nil;
    switch (error.code) {
        case ASAuthorizationErrorCanceled:
            errorMsg = @"用户取消了授权请求";
            break;
        case ASAuthorizationErrorFailed:
            errorMsg = @"授权请求失败";
            break;
        case ASAuthorizationErrorInvalidResponse:
            errorMsg = @"授权请求响应无效";
            break;
        case ASAuthorizationErrorNotHandled:
            errorMsg = @"未能处理授权请求";
            break;
        case ASAuthorizationErrorUnknown:
            errorMsg = @"授权请求失败未知原因";
            break;
    }
    [self dismissViewControllerAnimated:NO completion:nil];

    NSLog(@"loginError:%@", errorMsg);
}

- (void)authorizationController:(ASAuthorizationController *)controller didCompleteWithAuthorization:(ASAuthorization *)authorization API_AVAILABLE(ios(13.0)) {
    
    if ([authorization.credential isKindOfClass:[ASAuthorizationAppleIDCredential class]]) {
        ASAuthorizationAppleIDCredential *credential = (ASAuthorizationAppleIDCredential *)authorization.credential;
        //NSLog(@"授权返回信息：%@", credential.mj_JSONString);
        NSString *userID = credential.user;
//        NSString *state = credential.state;
//        NSPersonNameComponents *fullName = credential.fullName;
//        NSString *email = credential.email;
//        NSString *authorizationCode = [[NSString alloc] initWithData:credential.authorizationCode encoding:NSUTF8StringEncoding];
//        NSString *identityToken = [[NSString alloc] initWithData:credential.identityToken encoding:NSUTF8StringEncoding];
//        ASUserDetectionStatus realUserStatus = credential.realUserStatus;
//        NSArray *authorizedScopes = credential.authorizedScopes;
        
        NSLog(@"userID: %@", userID);
//        NSLog(@"fullName: %@", fullName);
//        NSLog(@"email: %@", email);
//        NSLog(@"authorizationCode: %@", authorizationCode);
//        NSLog(@"identityToken: %@", identityToken);
//        NSLog(@"realUserStatus: %@", @(realUserStatus));
//        NSLog(@"authorizedScopes: %@", authorizedScopes);
//      if (self.block) {
//          self.block(@{@"userID":userID});
//      }
      [self dismissViewControllerAnimated:NO completion:nil];
    }
}

#pragma mark - ASAuthorizationControllerPresentationContextProviding

- (ASPresentationAnchor)presentationAnchorForAuthorizationController:(ASAuthorizationController *)controller API_AVAILABLE(ios(13.0)) {
    return [UIApplication sharedApplication].keyWindow;
}

@end
