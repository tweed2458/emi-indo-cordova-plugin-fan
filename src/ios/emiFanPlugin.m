/********* emiFanPlugin.m Cordova Plugin Implementation *******/
/**
 * Created by EMI INDO So on 29/12/2023
 */
#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <AdSupport/AdSupport.h>
#import <Foundation/Foundation.h>
#import <AppTrackingTransparency/AppTrackingTransparency.h>

#import "emiFanPlugin.h"

@implementation emiFanPlugin

@synthesize adView;
@synthesize adSize;
@synthesize interstitialAd;
@synthesize rewardedVideoAd;
NSString *Position = @"bottom-center";

BOOL isBannerAutoShow = NO;
BOOL isInterstitialAutoShow = NO;
BOOL isRewardedAutoShow = NO;


//- (void)pluginInitialize {}

- (void)fireEvent:(NSString *)obj event:(NSString *)eventName withData:(NSString *)jsonStr {
    NSString *js;
    if (obj && [obj isEqualToString:@"window"]) {
        js = [NSString stringWithFormat:@"var evt = document.createEvent(\"UIEvents\"); evt.initUIEvent(\"%@\", true, false, window, 0); window.dispatchEvent(evt);", eventName];
    } else if (jsonStr && jsonStr.length > 0) {
        js = [NSString stringWithFormat:@"javascript:cordova.fireDocumentEvent('%@', %@);", eventName, jsonStr];
    } else {
        js = [NSString stringWithFormat:@"javascript:cordova.fireDocumentEvent('%@');", eventName];
    }
    [self.commandDelegate evalJs:js];
}


- (void)sdkInitialize:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;

    BOOL setMixedCOPPA = [[command.arguments objectAtIndex:0] boolValue];
    BOOL setTrackingEnabled = [[command.arguments objectAtIndex:1] boolValue];
    
    @try {
        
        dispatch_async(dispatch_get_main_queue(), ^{
        if (@available(iOS 14, *)) {
                   [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
                      
                           [FBAudienceNetworkAds initializeWithSettings:nil completionHandler:nil];
                           [FBAdSettings setAdvertiserTrackingEnabled:setTrackingEnabled];
                           [FBAdSettings setMixedAudience:setMixedCOPPA];
                     
                   }];
               } else {
                   
                   [FBAudienceNetworkAds initializeWithSettings:nil completionHandler:nil];
                   [FBAdSettings setAdvertiserTrackingEnabled:setTrackingEnabled];
                   [FBAdSettings setMixedAudience:setMixedCOPPA];
                   
               }
        });
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}




- (void)loadBannerAd:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* placementID = [command.arguments objectAtIndex:0];
    NSString* position = [command.arguments objectAtIndex:1];
    NSString* size = [command.arguments objectAtIndex:2];
    BOOL autoShow = [[command.arguments objectAtIndex:3] boolValue];
    
    
    UIView *parentView = [self.webView superview];
    @try {
        
        isBannerAutoShow = autoShow;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            /*
            FBAdView *adRequest = [[FBAdView alloc] init];
            NSError *error;
            NSData *adRequestData = [NSKeyedArchiver archivedDataWithRootObject:adRequest requiringSecureCoding:NO error:&error];
            if (error) {
                NSLog(@"Error archiving adRequest object: %@", error);
                [self fireEvent:@"" event:@"on.banner.bidpayload.error" withData:nil];
                return;
            }
            NSString *bidPayload = [[NSString alloc] initWithData:adRequestData encoding:NSUTF8StringEncoding];
            */
            Position = position;
            FBAdSize adSize = [self __AdSizeFromString:size];
            self.adView.hidden = YES;
            self.adView = [[FBAdView alloc] initWithPlacementID:placementID adSize:adSize rootViewController:self.viewController];
            self.adView.delegate = self;
            [self addBannerViewToView:command];
            [self.adView loadAd];
            [parentView addSubview:self.adView];
            [parentView bringSubviewToFront:self.adView];
        });
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (FBAdSize) __AdSizeFromString:(NSString*)size {
    if ([size isEqualToString:@"Standard-Banner"]) {
        return kFBAdSizeHeight50Banner;
    } else if ([size isEqualToString:@"Large-Banner"]) {
        return kFBAdSizeHeight90Banner;
    } else if ([size isEqualToString:@"Medium-Rectangle"]) {
        return kFBAdSizeHeight250Rectangle;
    } else {
        return kFBAdSizeHeight50Banner;
    }
}


- (void)addBannerViewToView:(CDVInvokedUrlCommand *)command {
  [self.viewController.view addSubview:adView];
  if ([Position isEqualToString:@"bottom-center"]) {
    [self.viewController.view addConstraints:@[
      [NSLayoutConstraint
          constraintWithItem:adView
                   attribute:NSLayoutAttributeBottom
                   relatedBy:NSLayoutRelationEqual
                      toItem:self.viewController.view.safeAreaLayoutGuide
                   attribute:NSLayoutAttributeBottom
                  multiplier:1
                    constant:0],
      [NSLayoutConstraint constraintWithItem:adView
                                   attribute:NSLayoutAttributeCenterX
                                   relatedBy:NSLayoutRelationEqual
                                      toItem:self.viewController.view
                                   attribute:NSLayoutAttributeCenterX
                                  multiplier:1
                                    constant:0]
    ]];
  } else {
    [self.viewController.view addConstraints:@[
      [NSLayoutConstraint
          constraintWithItem:adView
                   attribute:NSLayoutAttributeTop
                   relatedBy:NSLayoutRelationEqual
                      toItem:self.viewController.view.safeAreaLayoutGuide
                   attribute:NSLayoutAttributeTop
                  multiplier:1
                    constant:0],
      [NSLayoutConstraint constraintWithItem:adView
                                   attribute:NSLayoutAttributeCenterX
                                   relatedBy:NSLayoutRelationEqual
                                      toItem:self.viewController.view
                                   attribute:NSLayoutAttributeCenterX
                                  multiplier:1
                                    constant:0]
    ]];
  }
}




- (void)showBannerAd:(CDVInvokedUrlCommand *)command {
  CDVPluginResult *pluginResult;
  NSString *callbackId = command.callbackId;
  @try {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.adView) {
            self.adView.hidden = NO;
           [self fireEvent:@"" event:@"on.banner.show" withData:nil];
        }
    });
   }@catch (NSException *exception) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
  }
  [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}




- (void)hideBannerAd:(CDVInvokedUrlCommand *)command {
  CDVPluginResult *pluginResult;
  NSString *callbackId = command.callbackId;
  @try {
      dispatch_async(dispatch_get_main_queue(), ^{
          if (self.adView) {
          self.adView.hidden = YES;
         [self fireEvent:@"" event:@"on.banner.hide" withData:nil];
      }
    });
   }@catch (NSException *exception) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
  }
  [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}


- (void)removeBannerAd:(CDVInvokedUrlCommand *)command {
  CDVPluginResult *pluginResult;
  NSString *callbackId = command.callbackId;
  @try {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.adView) {
            self.adView.hidden = YES;
           [self.adView removeFromSuperview];
            self.adView = nil;
           [self fireEvent:@"" event:@"on.banner.remove" withData:nil];
        }
    });
   }@catch (NSException *exception) {
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
  }
  [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}



- (void)loadInterstitialAd:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* placementID = [command.arguments objectAtIndex:0];
    BOOL autoShow = [[command.arguments objectAtIndex:1] boolValue];
    @try {
        
        isInterstitialAutoShow = autoShow;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            /*
            FBInterstitialAd *adRequest = [[FBInterstitialAd alloc] init];
            NSError *error;
            NSData *adRequestData = [NSKeyedArchiver archivedDataWithRootObject:adRequest requiringSecureCoding:NO error:&error];
            if (error) {
                NSLog(@"Error archiving adRequest object: %@", error);
                [self fireEvent:@"" event:@"on.interstitial.bidpayload.error" withData:nil];
               // return;
            }
            NSString *bidPayload = [[NSString alloc] initWithData:adRequestData encoding:NSUTF8StringEncoding];
            
         */
              
             self.interstitialAd = [[FBInterstitialAd alloc] initWithPlacementID:placementID];
             self.interstitialAd.delegate = self;
            [self.interstitialAd loadAd];
         
            
        });
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    
    
    
}



- (void)showInterstitialAd:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
        if (self.interstitialAd && self.interstitialAd.isAdValid) {
            [self.interstitialAd showAdFromRootViewController:self.viewController];
        } 
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}






- (void)loadRewardedVideoAd:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* placementID = [command.arguments objectAtIndex:0];
    BOOL autoShow = [[command.arguments objectAtIndex:1] boolValue];
    @try {
        
        isRewardedAutoShow = autoShow;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            /*
            FBRewardedVideoAd *adRequest = [[FBRewardedVideoAd alloc] init];
            NSError *error;
            NSData *adRequestData = [NSKeyedArchiver archivedDataWithRootObject:adRequest requiringSecureCoding:NO error:&error];
            if (error) {
                NSLog(@"Error archiving adRequest object: %@", error);
                [self fireEvent:@"" event:@"on.reward.bidpayload.error" withData:nil];
                return;
            }
            NSString *bidPayload = [[NSString alloc] initWithData:adRequestData encoding:NSUTF8StringEncoding];
            */
            self.rewardedVideoAd = [[FBRewardedVideoAd alloc] initWithPlacementID:placementID];
            self.rewardedVideoAd.delegate = self;
            [self.rewardedVideoAd loadAd];
        });
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)showRewardedVideoAd:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    @try {
          if (self.rewardedVideoAd && self.rewardedVideoAd.isAdValid) {
            [self.rewardedVideoAd showAdFromRootViewController:self.viewController];
          }
        }@catch (NSException *exception) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.description];
        }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)adViewDidClick:(FBAdView *)adView
{
    [self fireEvent:@"" event:@"on.banner.click" withData:nil];
}
- (void)adViewDidFinishHandlingClick:(FBAdView *)adView
{
    [self fireEvent:@"" event:@"on.banner.finish.click" withData:nil];
}
- (void)adViewWillLogImpression:(FBAdView *)adView
{
    [self fireEvent:@"" event:@"on.banner.impression" withData:nil];
}
- (void)adView:(FBAdView *)adView didFailWithError:(NSError *)error
{
    NSDictionary *errorData = @{
           @"code": @(error.code),
           @"message": error.localizedDescription
       };
       NSError *jsonError;
       NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorData options:0 error:&jsonError];
       if (!jsonError) {
           NSString *jsonDataString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
           [self fireEvent:@"" event:@"on.banner.error" withData:jsonDataString];
       } else {
          // NSLog(@"Error converting error data to JSON: %@", jsonError);
       }
}

- (void)adViewDidLoad:(FBAdView *)adView
{
    if (isBannerAutoShow){
          dispatch_async(dispatch_get_main_queue(), ^{
              if (self.adView) {
                  self.adView.hidden = NO;
              }
          });
    }
    
    [self fireEvent:@"" event:@"on.banner.load" withData:nil];
}






- (void)interstitialAdDidLoad:(FBInterstitialAd *)interstitialAd
{
    [self fireEvent:@"" event:@"on.interstitial.load" withData:nil];
    
    if (isInterstitialAutoShow){
        if (self.interstitialAd && self.interstitialAd.isAdValid) {
            [self.interstitialAd showAdFromRootViewController:self.viewController];
        }
    }
    
}
- (void)interstitialAdWillLogImpression:(FBInterstitialAd *)interstitialAd
{
  [self fireEvent:@"" event:@"on.interstitial.impression" withData:nil];
}
- (void)interstitialAdDidClick:(FBInterstitialAd *)interstitialAd
{
  [self fireEvent:@"" event:@"on.interstitial.click" withData:nil];
}
- (void)interstitialAdWillClose:(FBInterstitialAd *)interstitialAd
{
  [self fireEvent:@"" event:@"on.interstitial.wil.close" withData:nil];
}
- (void)interstitialAdDidClose:(FBInterstitialAd *)interstitialAd
{
  [self fireEvent:@"" event:@"on.interstitial.did.close" withData:nil];
}
- (void)interstitialAd:(FBInterstitialAd *)interstitialAd didFailWithError:(NSError *)error
{
    NSDictionary *errorData = @{
           @"code": @(error.code),
           @"message": error.localizedDescription
       };
       NSError *jsonError;
       NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorData options:0 error:&jsonError];
       if (!jsonError) {
           NSString *jsonDataString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
           [self fireEvent:@"" event:@"on.interstitial.error" withData:jsonDataString];
       } else {
          // NSLog(@"Error converting error data to JSON: %@", jsonError);
       }
}






- (void)rewardedVideoAdDidLoad:(FBRewardedVideoAd *)rewardedVideoAd
{
  [self fireEvent:@"" event:@"on.rewarded.load" withData:nil];
    
    if(isRewardedAutoShow){
        if (self.rewardedVideoAd && self.rewardedVideoAd.isAdValid) {
          [self.rewardedVideoAd showAdFromRootViewController:self.viewController];
        }
    }
    
}
- (void)rewardedVideoAd:(FBRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error
{
    NSDictionary *errorData = @{
           @"code": @(error.code),
           @"message": error.localizedDescription
       };
       NSError *jsonError;
       NSData *jsonData = [NSJSONSerialization dataWithJSONObject:errorData options:0 error:&jsonError];
       if (!jsonError) {
           NSString *jsonDataString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
           [self fireEvent:@"" event:@"on.rewarded.error" withData:jsonDataString];
       } else {
          // NSLog(@"Error converting error data to JSON: %@", jsonError);
       }
}


- (void)rewardedVideoAdDidClick:(FBRewardedVideoAd *)rewardedVideoAd
{
  [self fireEvent:@"" event:@"on.rewarded.click" withData:nil];
}
- (void)rewardedVideoAdDidClose:(FBRewardedVideoAd *)rewardedVideoAd
{
  [self fireEvent:@"" event:@"on.rewarded.did.close" withData:nil];
}
- (void)rewardedVideoAdVideoComplete:(FBRewardedVideoAd *)rewardedVideoAd;
{
  [self fireEvent:@"" event:@"on.rewarded.complete" withData:nil];
}
- (void)rewardedVideoAdWillClose:(FBRewardedVideoAd *)rewardedVideoAd
{
  [self fireEvent:@"" event:@"on.rewarded.wil.close" withData:nil];
}
- (void)rewardedVideoAdWillLogImpression:(FBRewardedVideoAd *)rewardedVideoAd
{
  [self fireEvent:@"" event:@"on.rewarded.impression" withData:nil];
}




#pragma mark Cleanup
- (void)dealloc
{
 self.adView = nil;
 self.interstitialAd = nil;
 self.rewardedVideoAd = nil;
}
@end
