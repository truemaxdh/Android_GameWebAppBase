package com.pgmaru.pentix;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
  public WebView webView;
  WebAppInterface webAppInterface;

  // Client used to sign in with Google APIs
  public GoogleSignInClient mGoogleSignInClient = null;

  // Client variables
  public AchievementsClient mAchievementsClient;
  public LeaderboardsClient mLeaderboardsClient;
  public EventsClient mEventsClient;
  //public PlayersClient mPlayersClient;
  public GamesClient mGamesClient;

  
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

  // Back button activity
  public void onBackPressed() {
    try {
      webAppInterface.showSubMenu();
    } catch(Exception e) {
      super.onBackPressed();
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task =
          GoogleSignIn.getSignedInAccountFromIntent(intent);

      try {
        GoogleSignInAccount account = task.getResult(ApiException.class);
        onConnected(account);
      } catch (ApiException apiException) {
        String message = apiException.getMessage();
        if (message == null || message.isEmpty()) {
          message = "other_error";
        }

        onDisconnected();

        new AlertDialog.Builder(this)
            .setMessage(message)
            .setNeutralButton("OK", null)
            .show();
      }
    }
  }

  private void onConnected(GoogleSignInAccount googleSignInAccount) {
    mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
    mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
    mEventsClient = Games.getEventsClient(this, googleSignInAccount);
    //mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
    mGamesClient = Games.getGamesClient(this, googleSignInAccount);

    mGamesClient.setViewForPopups(webView);

    // Set the greeting
    mPlayersClient.getCurrentPlayer()
      .addOnCompleteListener(new OnCompleteListener<Player>() {
        @Override
        public void onComplete(Task<Player> task) {
          String dispName;
          if (task.isSuccessful()) {
            dispName = task.getResult().getDisplayName();
          } else {
            dispName = "unknown";
          }
          webAppInterface.jscallback_gamerProfile("connected",dispName);
        }
      });
  }

  private void onDisconnected() {
    mAchievementsClient = null;
    mLeaderboardsClient = null;
    //mPlayersClient = null;
    mGamesClient = null;

    webAppInterface.jscallback_gamerProfile("disconnected","");
  }

  public void signInSilently() {
    mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
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

    ///webAppInterface.showToast("onResume!!");
    // Since the state of the signed in user can change when the activity is not active
    // it is recommended to try and sign in silently from when the app resumes.
    if (mGoogleSignInClient != null) {
      signInSilently();
    }
  }

  public void startSignInIntent() {
    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
  }

  public void showAchievements() {
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
          webAppInterface.showToast("getAchievementsIntent failed");
        }
      });
  }

  public void showLeaderboard(String leaderboardId) {
    mLeaderboardsClient.getLeaderboardsIntent(leaderboardId)
      .addOnSuccessListener(new OnSuccessListener<Intent>() {
        @Override
        public void onSuccess(Intent intent) {
          startActivityForResult(intent, RC_UNUSED);
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
          webAppInterface.showToast("getAllLeaderboardsIntent failed");
        }
      });
  }
}
