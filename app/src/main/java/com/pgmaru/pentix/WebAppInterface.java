package com.pgmaru.pentix;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
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
    public void adMobInit(String adUnitId) {
        initialize(mContext);
        mMain.mInterstitialAd = new InterstitialAd(mContext);
        mMain.mInterstitialAd.setAdUnitId(adUnitId);
        // "ca-app-pub-3940256099942544/1033173712" : Test
        //ca-app-pub-7307479428475282~3962043059 : Real

    }

    /** AdMob Load InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialLoad() {
        mMain.mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    /** AdMob Show InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialShow() {
        if (mMain.mInterstitialAd.isLoaded()) {
            mMain.mInterstitialAd.show();
        } else {
            showToast("The interstitial wasn't loaded yet.");
        }
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public void signInToGS() {
        // Create the client used to sign in to Google services.
        if (mMain.mGoogleSignInClient == null) {
            mMain.mGoogleSignInClient = GoogleSignIn.getClient(mContext,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
        }

        //if (GoogleSignIn.getLastSignedInAccount(mContext) == null)
            //mMain.startSignInIntent();
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
}
