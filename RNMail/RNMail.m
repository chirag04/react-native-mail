#import <MessageUI/MessageUI.h>
#import "RNMail.h"
#import "RCTConvert.h"

@implementation RNMail

// Expose this module to the React Native bridge
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(mail:(NSDictionary *)options
                  callback: (RCTResponseSenderBlock)callback)
{
    self.done = callback;
    if ([MFMailComposeViewController canSendMail])
    {
        MFMailComposeViewController *mail = [[MFMailComposeViewController alloc] init];
        mail.mailComposeDelegate = self;
        
        if (options[@"subject"]){
            NSString *subject = [RCTConvert NSString:options[@"subject"]];
            [mail setSubject:subject];
        }
        
        if (options[@"body"]){
            NSString *body = [RCTConvert NSString:options[@"body"]];
            [mail setMessageBody:body isHTML:NO];
        }
        
        if (options[@"recipients"]){
            NSArray *recipients = [RCTConvert NSArray:options[@"recipients"]];
            [mail setToRecipients:recipients];
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
            [root presentViewController:mail animated:YES completion:NULL];
        });
    }
    else
    {
        callback(@[@"not_available"]);
    }
}

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    switch (result) {
        case MFMailComposeResultSent:
            self.done(@[[NSNull null] , @"sent"]);
            break;
        case MFMailComposeResultSaved:
            self.done(@[[NSNull null] , @"saved"]);
            break;
        case MFMailComposeResultCancelled:
            self.done(@[[NSNull null] , @"cancelled"]);
            break;
        case MFMailComposeResultFailed:
            self.done(@[@"failed"]);
            break;
        default:
            self.done(@[@"error"]);
            break;
    }
    UIViewController *ctrl = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    [ctrl dismissViewControllerAnimated:YES completion:NULL];
}

@end
