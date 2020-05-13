package com.pgmaru.pentix;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    public WebView webView;
    WebAppInterface webAppInterface;

    // Client used to sign in with Google APIs
    public GoogleSignInClient mGoogleSignInClient = null;

    // Client variables
    public AchievementsClient mAchievementsClient;
    public LeaderboardsClient mLeaderboardsClient;
    public EventsClient mEventsClient;
    public PlayersClient mPlayersClient;

    public String displayName;

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    public InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        webView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webAppInterface = new WebAppInterface(this);
        webView.addJavascriptInterface(webAppInterface, "Android");

        // url
        webView.loadUrl("https://truemaxdh.github.io/EnjoyCoding/game_pentix/www/");
        webView.setWebViewClient(new WebViewClient());
    }

    /*
    // Back button activity
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    */

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        mEventsClient = Games.getEventsClient(this, googleSignInAccount);
        mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);

        // Set the greeting appropriately on main menu
        mPlayersClient.getCurrentPlayer()
                .addOnCompleteListener(new OnCompleteListener<Player>() {
                    @Override
                    public void onComplete(Task<Player> task) {
                        if (task.isSuccessful()) {
                            displayName = task.getResult().getDisplayName();
                        } else {
                            displayName = "???";
                        }
                        //Toast.makeText(getApplicationContext(), "Hello," + displayName, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void onDisconnected() {
        mAchievementsClient = null;
        mLeaderboardsClient = null;
        mPlayersClient = null;
        //Toast.makeText(this, "GoodBye!", Toast.LENGTH_LONG).show();
    }

    public void signInSilently() {
        //Toast.makeText(this, "signInSilently", Toast.LENGTH_LONG).show();
        mGoogleSignInClient.silentSignIn().addOnCompleteListener((Executor) this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            onConnected(task.getResult());
                        } else {
                            onDisconnected();
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        //Toast.makeText(this, "OnResume", Toast.LENGTH_LONG).show();
        if (mGoogleSignInClient != null) {
            signInSilently();
        }
    }

    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    public void showAchievements() {
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "The interstitial wasn't loaded yet.", Toast.LENGTH_LONG);
        }*/
        mAchievementsClient.getAchievementsIntent()
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_UNUSED);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    //Toast.makeText(getApplicationContext(), "Failed to get Achievements.", Toast.LENGTH_LONG);
                }
            });
    }

    public void showLeaderboard() {
        /*if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(this, "The interstitial wasn't loaded yet.", Toast.LENGTH_LONG);
        }*/
        mLeaderboardsClient.getAllLeaderboardsIntent()
            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                @Override
                public void onSuccess(Intent intent) {
                    startActivityForResult(intent, RC_UNUSED);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    //Toast.makeText(getApplicationContext(), "Failed to get Leaderboard.", Toast.LENGTH_LONG);
                }
            });
    }
}
