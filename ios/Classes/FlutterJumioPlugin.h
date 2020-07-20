#import <Flutter/Flutter.h>
@import JumioCore;
@import Netverify;
@import NetverifyFace;
@import DocumentVerification;
@import BAMCheckout;


@interface FlutterJumioPlugin : NSObject<FlutterPlugin, AuthenticationControllerDelegate, BAMCheckoutViewControllerDelegate, NetverifyViewControllerDelegate, DocumentVerificationViewControllerDelegate>

@property (strong) NetverifyViewController* netverifyViewController;
@property (strong) NetverifyConfiguration* netverifyConfiguration;
@property (strong) AuthenticationController* authenticationController;
@property (strong) AuthenticationConfiguration* authenticationConfiguration;
@property (strong) UIViewController *authenticationScanViewController;
@property (strong) BAMCheckoutViewController* bamViewController;
@property (strong) BAMCheckoutConfiguration* bamConfiguration;
@property (nonatomic, retain) UIViewController *viewController;
@property (strong) DocumentVerificationConfiguration* documentVerifcationConfiguration;
@property (strong) DocumentVerificationViewController* documentVerificationViewController;

@end
