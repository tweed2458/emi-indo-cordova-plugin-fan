/********* emiAdmobPlugin.m Cordova Plugin Implementation *******/
/**
 * Created by EMI INDO So on 29/12/2023
 */
#import <UIKit/UIKit.h>
#import <Cordova/CDV.h>
@import FBAudienceNetwork;
@interface emiFanPlugin : CDVPlugin <FBAdViewDelegate, FBInterstitialAdDelegate, FBRewardedVideoAdDelegate> {
}
@property (nonatomic, strong) FBAdView *adView;
@property (assign) FBAdSize adSize;
@property (nonatomic, strong) FBInterstitialAd *interstitialAd;
@property (nonatomic, strong) FBRewardedVideoAd *rewardedVideoAd;
@property (nonatomic, copy) NSString *callbackId;
- (void)sdkInitialize:(CDVInvokedUrlCommand*)command;
- (void)loadBannerAd:(CDVInvokedUrlCommand*)command;
- (void)showBannerAd:(CDVInvokedUrlCommand*)command;
- (void)hideBannerAd:(CDVInvokedUrlCommand*)command;
- (void)removeBannerAd:(CDVInvokedUrlCommand*)command;
- (void)loadInterstitialAd:(CDVInvokedUrlCommand*)command;
- (void)showInterstitialAd:(CDVInvokedUrlCommand*)command;
- (void)loadRewardedVideoAd:(CDVInvokedUrlCommand*)command;
- (void)showRewardedVideoAd:(CDVInvokedUrlCommand*)command;
- (void) fireEvent:(NSString *)obj event:(NSString *)eventName withData:(NSString *)jsonStr;
@end



