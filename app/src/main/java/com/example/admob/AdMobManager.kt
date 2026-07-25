package com.example.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdMobConfig {
    const val PUBLISHER_ID = "pub-9902358800495546"
    const val APP_ID = "ca-app-pub-9902358800495546~4527298285"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-9902358800495546/9045127794"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9902358800495546/9045127794"
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

object AdMobManager {
    private const val TAG = "AdMobManager"
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) {
                isInitialized = true
                Log.d(TAG, "AdMob SDK Initialized successfully")
                loadInterstitialAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob init error: ${e.message}")
        }
    }

    fun initializeWithConsent(activity: Activity) {
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.w(TAG, "UMP consent form error: ${formError.errorCode}: ${formError.message}")
                        }
                        if (consentInformation.canRequestAds()) {
                            initialize(activity)
                        }
                    }
                },
                { requestConsentError ->
                    Log.w(TAG, "UMP consent info update error: ${requestConsentError.errorCode}: ${requestConsentError.message}")
                    if (consentInformation.canRequestAds()) {
                        initialize(activity)
                    }
                }
            )

            if (consentInformation.canRequestAds()) {
                initialize(activity)
            }
        } catch (e: Exception) {
            Log.e(TAG, "UMP init error: ${e.message}")
            initialize(activity)
        }
    }

    fun loadInterstitialAd(context: Context) {
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true

        val targetUnitId = AdMobConfig.INTERSTITIAL_AD_UNIT_ID
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context.applicationContext,
            targetUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitial = false
                    Log.e(TAG, "Interstitial ad failed to load ($targetUnitId): ${error.message}")
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity?, onAdClosed: () -> Unit = {}) {
        if (activity == null) {
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity.applicationContext)
                    onAdClosed()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(activity.applicationContext)
            onAdClosed()
        }
    }
}

@Composable
fun AdMobBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    visibility = android.view.View.VISIBLE
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    this.adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            Log.e("AdMobBannerView", "Banner failed ($adUnitId): ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
