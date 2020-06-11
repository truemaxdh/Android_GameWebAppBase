package com.pgmaru.common;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
    public void adMobInit(String useJSCallbackYN) {
        try {
            if (useJSCallbackYN.equals("Y")) {
                initialize(mContext, new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        mMain.webView.loadUrl("javascript:AdMob.onInitComplete();");
                    }
                });
            } else {
                initialize(mContext);
            }
        } catch (Exception e) {
            showToast(e.toString());
        }
    }
    
    /** AdMob Init InterstitialAd */
    @JavascriptInterface
    public void adMobInitInterstitial(String adUnitId) {
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
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
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
                    //showToast("The interstitial wasn't loaded yet.");
                }
                }
            });
        } catch(Exception e) {
            showToast(e.toString());
        }
    }

    /** AdMob Set Callbacks of InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialSetToUseJSCallback() {
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
    
    /** AdMob Create and show banner */
    @JavascriptInterface
    public void adMobInitBanner() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mMain.initAdmobBanner();
                    } catch(Exception e) {
                        showToast(e.toString());
                    }
                }
            });
        } catch(Exception e) {
            showToast(e.toString());
        }
    }
    
    /** Signin to google services from the web page */
    @JavascriptInterface
    public void GoogleSignIn_getClient() {
        try {
            // Create the client used to sign in to Google services.
            if (mMain.mGoogleSignInClient == null) {
                //showToast("GoogleSignIn.getClient");
                mMain.mGoogleSignInClient = GoogleSignIn.getClient(mContext,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());
            }
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public void signInToGS() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.startSignInIntent();
                }
            });
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Signin to google services from the web page */
    @JavascriptInterface
    public void signInSilently() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.signInSilently();
                }
            });
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** get Last SignedIn Account to google services from the web page */
    @JavascriptInterface
    public String getLastSignedInAccount() {
        String dispName = "";
        try {
            GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(mContext);
            if (gsa != null)
                dispName = gsa.getDisplayName();
        } catch(Exception e) {
            showToast(e.toString());
        }
        //showToast(dispName);
        return dispName;
    }

    /** Show achievements from the web page */
    @JavascriptInterface
    public void showAchievements() {
        try {
            mMain.showAchievements();
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Show leaderboard from the web page */
    @JavascriptInterface
    public void showLeaderboard(String leaderboardId) {
        try {
            mMain.mLeaderboardId = leaderboardId;
            mMain.showLeaderboard(mMain.mLeaderboardId);
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Unlock Achievement from the web page */
    @JavascriptInterface
    public void unlockAchievement(String achievementId) {
        try {
            mMain.mAchievementId = achievementId;
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.mAchievementsClient.unlock(mMain.mAchievementId);
                }
            });
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Submit Score from the web page */
    @JavascriptInterface
    public void submitScore(String leaderboardId, int score) {
        try {
            mMain.mLeaderboardId = leaderboardId;
            mMain.mScore = score;
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMain.mLeaderboardsClient.submitScore(mMain.mLeaderboardId, mMain.mScore);
                }
            });
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** Exit app from the web page */
    @JavascriptInterface
    public void exitApp() {
        try {
            mMain.finish();
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    public void showSubMenu() {
        try {
            mMain.webView.loadUrl("javascript:showSubMenu();");
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    public void jscallback_gamerProfile(String isConnected, String dispName) {
        try {
            mMain.webView.loadUrl("javascript:setGamerProfile('" + isConnected + "', '" + dispName + "');");
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

}
