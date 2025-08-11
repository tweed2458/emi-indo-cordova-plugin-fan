var exec = require('cordova/exec');

exports.sdkInitialize = function (arg0, arg1, success, error) {
    exec(success, error, 'emiFanPlugin', 'sdkInitialize', [arg0, arg1]);
};

exports.loadBannerAd = function (arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, 'emiFanPlugin', 'loadBannerAd', [arg0, arg1, arg2, arg3]);
};

exports.showBannerAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'showBannerAd', []);
};

exports.hideBannerAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'hideBannerAd', []);
};

exports.removeBannerAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'removeBannerAd', []);
};

exports.loadInterstitialAd = function (arg0, arg1, success, error) {
    exec(success, error, 'emiFanPlugin', 'loadInterstitialAd', [arg0, arg1]);
};

exports.showInterstitialAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'showInterstitialAd', []);
};

exports.loadRewardedVideoAd = function (arg0, arg1, success, error) {
    exec(success, error, 'emiFanPlugin', 'loadRewardedVideoAd', [arg0, arg1]);
};

exports.showRewardedVideoAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'showRewardedVideoAd', []);
};

exports.loadRewardedInterstitialAd = function (arg0, arg1, success, error) {
    exec(success, error, 'emiFanPlugin', 'loadRewardedInterstitialAd', [arg0, arg1]);
};

exports.showRewardedInterstitialAd = function (success, error) {
    exec(success, error, 'emiFanPlugin', 'showRewardedInterstitialAd', []);
};