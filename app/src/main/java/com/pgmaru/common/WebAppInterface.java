package com.pgmaru.common;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
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

    /** AdMob Load InterstitialAd */
    @JavascriptInterface
    public void adMobInterstitialLoad() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  //mMain.mInterstitialAd.l.loadAd(new AdRequest.Builder().build());
                  InterstitialAd.load(
                    mMain,
                    getString(R.string.admob_interstitial_unit_id),
                    mMain.adRequest,
                    new InterstitialAdLoadCallback() {
                      @Override
                      public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mMain.mInterstitialAd = interstitialAd;
                        //Log.i(TAG, "onAdLoaded");
                        //Toast.makeText(MyActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(
                            new FullScreenContentCallback() {
                              @Override
                              public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mMain.mInterstitialAd = null;
                                //Log.d("TAG", "The ad was dismissed.");
                              }

                              @Override
                              public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mMain.mInterstitialAd = null;
                                //Log.d("TAG", "The ad failed to show.");
                              }

                              @Override
                              public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                //Log.d("TAG", "The ad was shown.");
                              }
                            });
                      }

                      @Override
                      public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //Log.i(TAG, loadAdError.getMessage());
                        mMain.mInterstitialAd = null;

                        /*String error =
                            String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Toast.makeText(
                                MyActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
                            .show();*/
                      }
                    });
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
                //if (mMain.mInterstitialAd..isLoaded()) {
                    //mMain.mInterstitialAd.show(mMain);
                //} else {
                    //showToast("The interstitial wasn't loaded yet.");
                //}
                  // Show the ad if it's ready. Otherwise toast and restart the game.
                  if (mMain.mInterstitialAd != null) {
                    mMain.mInterstitialAd.show(mMain);
                  } else {
                      //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
                      //startGame();
                  }
                }
            });
        } catch(Exception e) {
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

    /** Signout from google services from the web page */
    @JavascriptInterface
    public void signOutFromGS() {
        try {
            mMain.signOut();
        } catch (Exception e) {
            showToast(e.toString());
        }
    }

    /** get Last SignedIn Account to google services from the web page */
    @JavascriptInterface
    public void reqGamerProfile() {
        try {
            mMain.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String isConnected = "disconnected";
                    String dispName = "";
                    try {
                        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(mContext);
                        if (gsa != null) {
                            dispName = gsa.getDisplayName();
                            isConnected = "connected";
                        }
                    } catch(Exception e) {
                        showToast(e.toString());
                    }
                    //showToast(dispName);
                    jscallback_gamerProfile(isConnected, dispName);
                }
            });
        } catch(Exception e) {
            showToast(e.toString());
        }
        
        
        
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

    /** Load Accomplished Achievement from the web page */
    @JavascriptInterface
    public void loadAchievements() {
        try {
            mMain.loadAchievements();
        } catch (Exception e) {
            //showToast(e.toString());
            jscallback_loadAchievements("");
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

    /** Vibrate from the web page */
    @JavascriptInterface
    public void vibrate(int millisec) {
        try {
            mMain.vibrate(millisec);
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


    public void jscallback_loadAchievements(String strAchievements) {
        try {
            mMain.webView.loadUrl("javascript:setAchievements('" + strAchievements + "');");
        } catch (Exception e) {
            showToast(e.toString());
        }
    }
}
