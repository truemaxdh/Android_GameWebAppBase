package com.pgmaru.pentix;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import static com.google.android.gms.ads.MobileAds.initialize;

public class WebAppInterface {
    Context mContext;
    MainActivity mMain;

    /** Instantiate the interface and set the context */
    WebAppInterface(Context c) {
        mContext = c;
        mMain = (MainActivity)c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    /** AdMob Init */
    @JavascriptInterface
    public void adMobInit(String adMobAppId, String useJSCallbackYN) {
        if (useJSCallbackYN.equals("Y")) {
            initialize(mContext, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    mMain.webView.loadUrl("javascript:AdMob.onInitComplete();");
                }
            });
        } else {
            initialize(mContext, adMobAppId);
        }        
    }
    
    /** AdMob Init IntertitialAd */
    @JavascriptInterface
    public void adMobInitIntertitial(String adUnitId) {
        try {
            mMain.mInterstitialAd = new InterstitialAd(mContext);
            mMain.mInterstitialAd.setAdUnitId(adUnitId);
        } catch(Exception e) {
            showToast(e.toString());
        }
    }

    /** AdMob Load InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialLoad() {
        try {
            mMain.mInterstitialAd.loadAd(new AdRequest.Builder().build());
        } catch(Exception e) {
            showToast(e.toString());
        }
    }

    /** AdMob Show InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialShow() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mMain.mInterstitialAd.isLoaded()) {
                        mMain.mInterstitialAd.show();
                    } else {
                        showToast("The interstitial wasn't loaded yet.");
                    }
                }
            });
        } catch(Exception e) {
            showToast(e.toString());
        }
    }

    /** AdMob Set Callbacks of InterstitialAd */
    @JavascriptInterface
    public void adMobIntertitialSetToUseJSCallback() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdLoaded();");
                        }

                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdFailedToLoad();");
                        }

                        @Override
                        public void onAdOpened() {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdOpened();");
                        }

                        @Override
                        public void onAdClicked() {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdClicked();");
                        }

                        @Override
                        public void onAdLeftApplication() {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdLeftApplication();");
                        }

                        @Override
                        public void onAdClosed() {
                            mMain.webView.loadUrl("javascript:AdMob.Interstitial.onAdClosed();");
                        }
                    });
                }
            });
        } catch (Exception e) {
            showToast(e.toString());
        }
    }
    
    /** Signin to google services from the web page */
    @JavascriptInterface
    public void GoogleSignIn_getClient() {
        // Create the client used to sign in to Google services.
        if (mMain.mGoogleSignInClient == null) {
            //showToast("GoogleSignIn.getClient");
            mMain.mGoogleSignInClient = GoogleSignIn.getClient(mContext,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        }
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public void signInToGS() {
        //showToast("startSignInIntent");
        mMain.startSignInIntent();
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public void signInSilently() {
        //showToast("signInSilently");
        mMain.signInSilently();
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public String getLastSignedInAccount() {
        String dispName = GoogleSignIn.getLastSignedInAccount(mContext).getDisplayName();
        //showToast(dispName);
        return dispName;
    }

    /** Show achievements from the web page */
    @JavascriptInterface
    public void showAchievements() {
        mMain.showAchievements();
    }

    /** Show leaderboard from the web page */
    @JavascriptInterface
    public void showLeaderboard() {
        mMain.showLeaderboard();
    }

    /** Unlock Achievement from the web page */
    @JavascriptInterface
    public void unlockAchievement(String achievementId) {
        mMain.mAchievementsClient.unlock(achievementId);
    }

    /** Submit Score from the web page */
    @JavascriptInterface
    public void submitScore(String leaderboardId, int score) {
        mMain.mLeaderboardsClient.submitScore(leaderboardId, score);
    }

    /** Exit app from the web page */
    @JavascriptInterface
    public void exitApp(String leaderboardId, int score) {
        mMain.finish();
    }

    public void showSubMenu() {
        mMain.webView.loadUrl("javascript:showSubMenu();");
    }


}
