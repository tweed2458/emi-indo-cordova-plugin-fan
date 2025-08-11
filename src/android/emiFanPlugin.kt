package emi.indo.cordova.plugin.fan
/**
 * Created by EMI INDO So on 1/04/2023
 */

//noinspection SuspiciousImport
import android.R
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.facebook.ads.RewardedInterstitialAd
import com.facebook.ads.RewardedInterstitialAdListener
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Objects
import java.util.UUID


class emiFanPlugin : CordovaPlugin() {

    private var cWebView: CordovaWebView? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedVideoAd: RewardedVideoAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var bannerViewLayout: RelativeLayout? = null
    private var bannerView: AdView? = null
    private var bannerPlacementID: String? = null
    private var interstitialPlacementID: String? = null
    private var rewardedPlacementID: String? = null
    private var rewardedInterstitialPlacementID: String? = null
    private var mActivity: Activity? = null
    private var mContext: Context? = null
    private var orientation = 1
    private var bannerPause = 0

    private var isBannerAutoShow = false
    private var isInterstitialAutoShow = false
    private var isRewardedAutoShow = false
    private var isRewardedIntAutoShow = false

    override fun initialize(cordova: CordovaInterface, webView: CordovaWebView) {
        super.initialize(cordova, webView)
        cWebView = webView
        mActivity = this.cordova.activity
        mContext = (mActivity as AppCompatActivity?)?.applicationContext
        val orientation = (mActivity as AppCompatActivity?)?.resources?.configuration?.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.orientation = Configuration.ORIENTATION_PORTRAIT
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.orientation = Configuration.ORIENTATION_LANDSCAPE
        } else {
            this.orientation = Configuration.ORIENTATION_UNDEFINED
        }
    }



    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        when (action) {
            "sdkInitialize" -> {
                mActivity?.runOnUiThread {
                    
                    val testMode = args.optBoolean(0)
                    val setMixedCOPPA = args.optBoolean(1) // COPPA
                  
                        if (testMode) {
                            addTestDeviceUsingAAID(mActivity!!)
                        }
                       
                        if(setMixedCOPPA) {
                            AdSettings.setMixedAudience(true) 
                        } else {
                            AdSettings.setMixedAudience(false) 
                        }
                    
                        AudienceNetworkAds.initialize(mActivity)
 
                }
                return true
            }
            "loadBannerAd" -> {
                mActivity?.runOnUiThread {
                    try {
                        val placementID = args.optString(0)
                        val position = args.optString(1)
                        val size = args.optString(2)
                        val autoShow = args.optBoolean(3)
                        bannerPlacementID = placementID
                        isBannerAutoShow = autoShow
                        if (bannerPause == 0 && !_isBannerShowing && bannerView == null) {
                            if (bannerViewLayout == null) {
                                bannerViewLayout = RelativeLayout(mActivity)
                                val params = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.MATCH_PARENT,
                                    RelativeLayout.LayoutParams.MATCH_PARENT
                                )
                                bannerViewLayout?.layoutParams = params
                                (Objects.requireNonNull(cWebView?.view) as ViewGroup).addView(
                                    bannerViewLayout
                                )
                            }


                                bannerView = AdView(mActivity, bannerPlacementID, getBannerAdSize(size))
                                val params = RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT
                                )


                            when (position) {
                                "top-left" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                }
                                "top-center" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                    params.addRule(RelativeLayout.CENTER_HORIZONTAL)
                                }
                                "top-right" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                }
                                "left" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                    params.addRule(RelativeLayout.CENTER_VERTICAL)
                                }
                                "center" -> {
                                    params.addRule(RelativeLayout.CENTER_HORIZONTAL)
                                    params.addRule(RelativeLayout.CENTER_VERTICAL)
                                }
                                "right" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                    params.addRule(RelativeLayout.CENTER_VERTICAL)
                                }
                                "bottom-left" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                                }
                                "bottom-center" -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                    params.addRule(RelativeLayout.CENTER_HORIZONTAL)
                                }
                                else -> {
                                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                                }
                            }


                            bannerView?.layoutParams = params

                            setBannerCallBack(callbackContext)

                        }

                    } catch (e: Exception) {
                        callbackContext.error(e.toString())
                    }
                }
                return true
            }
            "showBannerAd" -> {
                mActivity?.runOnUiThread {

                    if (_isBannerShowing && bannerView != null && bannerPause == 1) {

                        bannerView?.visibility = View.VISIBLE
                        _isBannerShowing = true
                        bannerPause = 1

                    } else if (_isBannerShowing && bannerView != null && !isBannerAutoShow) {

                        bannerViewLayout?.addView(bannerView)
                        _isBannerShowing = true
                        bannerPause = 1

                    } else {
                        callbackContext.error("Banner not loaded")
                    }
                }
                return true
            }
            "hideBannerAd" -> {
                mActivity?.runOnUiThread {
                    if (_isBannerShowing) {
                        if (bannerView != null) {
                            bannerView?.visibility = View.GONE
                            _isBannerShowing = true
                            bannerPause = 1
                        }
                    }
                }
                return true
            }
            "removeBannerAd" -> {
                mActivity?.runOnUiThread {
                    if (bannerView != null && bannerPause == 1 && _isBannerShowing) {
                        val parentLayout = bannerView?.parent as? RelativeLayout
                        parentLayout?.removeView(bannerView)

                        bannerView?.destroy()
                        bannerView = null
                        bannerPause = 0
                        _isBannerShowing = false

                        (bannerViewLayout?.parent as? ViewGroup)?.removeView(bannerViewLayout)
                        bannerViewLayout = null

                        cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.remove');")
                    }
                }


                return true
            }
            "loadInterstitialAd" -> {
                mActivity?.runOnUiThread {
                    val placementID = args.optString(0)
                    val autoShow = args.optBoolean(1)
                    try {
                        isInterstitialAutoShow = autoShow
                        interstitialPlacementID = placementID
                        interstitialAd = InterstitialAd(mActivity, interstitialPlacementID)
                        setInterstitialCallBack(callbackContext)
                    } catch (e: Exception) {
                        callbackContext.error(e.toString())
                    }
                }
                return true
            }
            "showInterstitialAd" -> {
                mActivity?.runOnUiThread {
                    try {
                        if (interstitialAd == null || !interstitialAd!!.isAdLoaded) {
                            return@runOnUiThread
                        }
                        if (interstitialAd!!.isAdInvalidated) {
                            return@runOnUiThread
                        }
                        interstitialAd?.show()
                    } catch (e: Exception) {
                        callbackContext.error(e.toString())
                    }
                }
                return true
            }
            "loadRewardedVideoAd" -> {
                mActivity?.runOnUiThread {
                    val placementID = args.optString(0)
                    val autoShow = args.optBoolean(1)
                    try {
                        isRewardedAutoShow = autoShow
                        rewardedPlacementID = placementID
                        rewardedVideoAd = RewardedVideoAd(mActivity, rewardedPlacementID)
                        setRewardedVideoCallBack(callbackContext)
                    } catch (e: Exception) {
                        callbackContext.error(e.toString())
                    }
                }
                return true
            }
            "showRewardedVideoAd" -> {
                mActivity?.runOnUiThread {
                    try {
                        if (rewardedVideoAd == null || !rewardedVideoAd!!.isAdLoaded) {
                            return@runOnUiThread
                        }
                        if (rewardedVideoAd!!.isAdInvalidated) {
                            return@runOnUiThread
                        }
                        rewardedVideoAd?.show()
                    } catch (e: Exception) {
                        callbackContext.success(e.toString())
                    }
                }
                return true
            }
            "loadRewardedInterstitialAd" -> {
                mActivity?.runOnUiThread {
                    val placementID = args.optString(0)
                    val autoShow = args.optBoolean(1)
                    try {
                    isRewardedIntAutoShow = autoShow
                    rewardedInterstitialPlacementID = placementID
                    rewardedInterstitialAd = RewardedInterstitialAd(mActivity, rewardedInterstitialPlacementID)
                    _loadRewardedInterstitialAd(callbackContext)
                    } catch (e: Exception) {
                        callbackContext.error(e.toString())
                    }
                }
                return true
            }
            "showRewardedInterstitialAd" -> {
                mActivity?.runOnUiThread {
                    if (rewardedInterstitialAd == null || !rewardedInterstitialAd!!.isAdLoaded) {
                        return@runOnUiThread
                    }
                    if (rewardedInterstitialAd!!.isAdInvalidated) {
                        return@runOnUiThread
                    }
                    rewardedInterstitialAd?.show(
                        rewardedInterstitialAd!!.buildShowAdConfig().withAppOrientation(orientation)
                            .build()
                    )
                }
                return true
            }
        }
        return false
    }




    private fun setBannerCallBack(callbackContext: CallbackContext) {
        val adListener: AdListener = object : AdListener {
            override fun onError(ad: Ad, adError: AdError) {
                _isBannerShowing = false
                bannerPause = 0
                val result = JSONObject()
                try {
                    result.put("errorMessage", adError.errorMessage)
                    result.put("errorCode", adError.errorCode)
                } catch (e: JSONException) {
                    callbackContext.error(e.message)
                }
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.error, ${result}');")
            }
            override fun onAdLoaded(ad: Ad) {
                _isBannerShowing = true
                if (isBannerAutoShow) {
                    bannerViewLayout?.addView(bannerView)
                }
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.load');")
            }
            override fun onAdClicked(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.click');")
            }
            override fun onLoggingImpression(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.banner.impression');")
            }
        }
        bannerView?.loadAd(bannerView!!.buildLoadAdConfig().withAdListener(adListener).build())
    }




    private fun setRewardedVideoCallBack(callbackContext: CallbackContext) {
        val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onError(ad: Ad, adError: AdError) {
                val result = JSONObject()
                try {
                    result.put("errorMessage", adError.errorMessage)
                    result.put("errorCode", adError.errorCode)
                } catch (e: JSONException) {
                    callbackContext.error(e.message)
                }
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.error, ${result}');")
            }
            override fun onAdLoaded(ad: Ad) {

                if (isRewardedAutoShow){
                    mActivity?.runOnUiThread {
                       rewardedVideoAd?.show()
                    }
                }

                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.load');")
            }
            override fun onAdClicked(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.click');")
            }
            override fun onLoggingImpression(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.impression');")
            }
            override fun onRewardedVideoCompleted() {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.complete');")
            }
            override fun onRewardedVideoClosed() {
                val mainView = getView()
                mainView?.requestFocus()
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.wil.close');")
            }
        }

        rewardedVideoAd?.loadAd(
            rewardedVideoAd!!.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener)
                .build()
        )
    }




    private fun setInterstitialCallBack(callbackContext: CallbackContext) {

        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.displayed');")
            }
            override fun onInterstitialDismissed(ad: Ad) {
                val mainView = getView()
                mainView?.requestFocus()
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.dismissed');")
            }
            override fun onError(ad: Ad, adError: AdError) {
                val result = JSONObject()
                try {
                    result.put("errorMessage", adError.errorMessage)
                    result.put("errorCode", adError.errorCode)
                   // callbackContext.error(result)
                } catch (e: JSONException) {
                    callbackContext.error(e.message)
                }
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.error, ${result}');")
            }
            override fun onAdLoaded(ad: Ad) {
                if (isInterstitialAutoShow){
                    mActivity?.runOnUiThread {

                     interstitialAd?.show()

                    }
                }
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.load');")
            }
            override fun onAdClicked(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.click');")
            }
            override fun onLoggingImpression(ad: Ad) {
                cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.interstitial.impression');")
            }
        }
        interstitialAd?.loadAd(
            interstitialAd!!.buildLoadAdConfig()
                .withAdListener(interstitialAdListener)
                .build()
        )
    }




    private fun _loadRewardedInterstitialAd(callbackContext: CallbackContext) {
        val rewardedInterstitialAdListener: RewardedInterstitialAdListener =
            object : RewardedInterstitialAdListener {
                override fun onError(ad: Ad, error: AdError) {
                    val result = JSONObject()
                    try {
                        result.put("errorMessage", error.errorMessage)
                        result.put("errorCode", error.errorCode)
                    } catch (e: JSONException) {
                        callbackContext.error(e.message)
                    }
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.error, ${result}');")
                }
                override fun onAdLoaded(ad: Ad) {
                    if (isRewardedIntAutoShow){
                        mActivity?.runOnUiThread {
                            rewardedInterstitialAd?.show(
                                rewardedInterstitialAd!!.buildShowAdConfig().withAppOrientation(orientation)
                                    .build()
                            )
                        }
                    }
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.loaded');")
                }
                override fun onAdClicked(ad: Ad) {
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.click');")
                }
                override fun onLoggingImpression(ad: Ad) {
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.impression');")
                }
                override fun onRewardedInterstitialCompleted() {
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.completed');")
                }
                override fun onRewardedInterstitialClosed() {
                    val mainView = getView()
                    mainView?.requestFocus()
                    cWebView!!.loadUrl("javascript:cordova.fireDocumentEvent('on.rewarded.int.closed');")
                }
            }
        rewardedInterstitialAd?.loadAd(
            rewardedInterstitialAd!!.buildLoadAdConfig()
                .withAdListener(rewardedInterstitialAdListener)
                .build()
        )
    }



    private fun getView(): View? {
        return if (View::class.java.isAssignableFrom(CordovaWebView::class.java)) {
            cWebView as View?
        } else mActivity?.window?.decorView?.findViewById(R.id.content)
    }



    private fun getBannerAdSize(size: String): AdSize {
        val sz: AdSize = if ("Standard-Banner" == size) {
            AdSize.BANNER_HEIGHT_50
        } else if ("Large-Banner" == size) {
            AdSize.BANNER_HEIGHT_90
        } else if ("Medium-Rectangle" == size) {
            AdSize.RECTANGLE_HEIGHT_250
        } else {
            AdSize.BANNER_HEIGHT_50
        }
        return sz
    }





    fun getHashedAAID(context: Context, callback: (String?) -> Unit) {
        Thread {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val aaid = adInfo?.id ?: ""
                val uuid = UUID.nameUUIDFromBytes(aaid.toByteArray(Charsets.UTF_8))
                callback(uuid.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null)
            }
        }.start()
    }

    fun addTestDeviceUsingAAID(context: Context) {
        getHashedAAID(context) { hashedId ->
            hashedId?.let {
                val adPrefs = cordova.activity.getSharedPreferences("FBAdPrefs", Context.MODE_PRIVATE)
                val deviceIdHash = adPrefs.getString("deviceIdHash", null)
                deviceIdHash?.let { AdSettings.addTestDevice(it) }
            }
        }
    }




    override fun onDestroy() {
        if (bannerView != null) {
            bannerView?.destroy()
            bannerView = null
            bannerPause = 0
            _isBannerShowing = false
        }

        (bannerViewLayout?.parent as? ViewGroup)?.removeView(bannerViewLayout)
        bannerViewLayout = null


        if (interstitialAd != null) {
            interstitialAd?.destroy()
        }
        if (rewardedVideoAd != null) {
            rewardedVideoAd?.destroy()
            rewardedVideoAd = null
        }
        if (rewardedInterstitialAd != null) {
            rewardedInterstitialAd?.destroy()
            rewardedInterstitialAd = null
        }
        super.onDestroy()
    }



        private val TAG = "emiFanPlugin"
        private var _isBannerShowing = false




    }


