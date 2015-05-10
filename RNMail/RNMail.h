#import <UIKit/UIKit.h>
#import "RCTBridgeModule.h"

@interface RNMail : NSObject <RCTBridgeModule, MFMailComposeViewControllerDelegate>

@property (nonatomic, strong) RCTResponseSenderBlock done;

@end