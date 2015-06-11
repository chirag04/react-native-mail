#import <MessageUI/MessageUI.h>
#import "RNMail.h"
#import "RCTConvert.h"

@implementation RNMail
{
    NSMutableDictionary *_callbacks;
}

- (instancetype)init
{
    if ((self = [super init])) {
        _callbacks = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(mail:(NSDictionary *)options
                  callback: (RCTResponseSenderBlock)callback)
{
    if ([MFMailComposeViewController canSendMail])
    {
        MFMailComposeViewController *mail = [[MFMailComposeViewController alloc] init];
        mail.mailComposeDelegate = self;
        _callbacks[RCTKeyForInstance(mail)] = callback;
        
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
        
        UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [root presentViewController:mail animated:YES completion:nil];
    } else {
        callback(@[@"not_available"]);
    }
}

#pragma mark MFMailComposeViewControllerDelegate Methods

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    NSString *key = RCTKeyForInstance(controller);
    RCTResponseSenderBlock callback = _callbacks[key];
    if (callback) {
        switch (result) {
            case MFMailComposeResultSent:
                callback(@[[NSNull null] , @"sent"]);
                break;
            case MFMailComposeResultSaved:
                callback(@[[NSNull null] , @"saved"]);
                break;
            case MFMailComposeResultCancelled:
                callback(@[[NSNull null] , @"cancelled"]);
                break;
            case MFMailComposeResultFailed:
                callback(@[@"failed"]);
                break;
            default:
                callback(@[@"error"]);
                break;
        }
        [_callbacks removeObjectForKey:key];
    } else {
        RCTLogWarn(@"No callback registered for mail: %@", controller.title);
    }
    UIViewController *ctrl = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    [ctrl dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark Private

static NSString *RCTKeyForInstance(id instance)
{
    return [NSString stringWithFormat:@"%p", instance];
}

@end
