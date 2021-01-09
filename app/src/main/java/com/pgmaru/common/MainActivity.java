package com.pgmaru.common;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.EventsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.achievement.Achievement;
import com.google.android.gms.games.achievement.AchievementBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.ads.MobileAds.initialize;

public class MainActivity extends AppCompatActivity {
  public WebView webView;
  public String mAchievementId;
  public int mScore;
  WebAppInterface webAppInterface;

  // Client used to sign in with Google APIs
  public GoogleSignInClient mGoogleSignInClient = null;

  // Client variables
  public AchievementsClient mAchievementsClient;
  public LeaderboardsClient mLeaderboardsClient;
  public EventsClient mEventsClient;
  public PlayersClient mPlayersClient;
  public GamesClient mGamesClient;

  public String strJSONAchievements = "";
  
  private static final int RC_UNUSED = 5001;
  private static final int RC_SIGN_IN = 9001;

  // achievements and scores we're pending to push to the cloud
  // (waiting for the user to sign in, for instance)
  private final AccomplishmentsOutbox mOutbox = new AccomplishmentsOutbox();

  public InterstitialAd mInterstitialAd;

  public String mLeaderboardId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main);
    if (getString(R.string.setting_keep_screen_on).equals("1")) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //
    // Google sign in
    //
    mGoogleSignInClient = GoogleSignIn.getClient(this,
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

    //
    // Admob
    //
    initialize(this);
    mInterstitialAd = new InterstitialAd(this);
    mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial_unit_id));
    AdView adView = (AdView)findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);

    //
    // Webview
    //
    webView = (WebView)findViewById(R.id.webView);
    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);

    webAppInterface = new WebAppInterface(this);
    webView.addJavascriptInterface(webAppInterface, "Android");

    // url
    webView.loadUrl(getString(R.string.http_url));
    webView.setWebViewClient(new WebViewClient());
  }

  /*// Create AdMob Banner View
  public void initAdmobBanner() {
    AdView adView = (AdView)findViewById(R.id.adView);
    //adView.setAdSize(AdSize.BANNER);
    //adView.setAdUnitId(mAdUnitId);
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);
  }*/
  
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
    mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
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
    mPlayersClient = null;
    mGamesClient = null;

    webAppInterface.jscallback_gamerProfile("disconnected","");
  }

  private boolean isSignedIn() {
    return GoogleSignIn.getLastSignedInAccount(this) != null;
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

  public void startSignInIntent() {
    startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
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

  public void signOut() {
    //Log.d(TAG, "signOut()");

    if (!isSignedIn()) {
      //Log.w(TAG, "signOut() called, but was not signed in!");
      return;
    }

    mGoogleSignInClient.signOut().addOnCompleteListener(this,
            new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                boolean successful = task.isSuccessful();
                //Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));

                onDisconnected();
              }
            });
  }

  public void loadAchievements() {
    mAchievementsClient.load(true)
      .addOnSuccessListener(new OnSuccessListener<AnnotatedData<AchievementBuffer>>() {
        @Override
        public void onSuccess(AnnotatedData<AchievementBuffer> achievementBufferAnnotatedData) {
          strJSONAchievements = achievementsToJSONArray(achievementBufferAnnotatedData.get()).toString();
          webAppInterface.jscallback_loadAchievements(strJSONAchievements);
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          webAppInterface.showToast("loadAchievements failed");
          webAppInterface.jscallback_loadAchievements(strJSONAchievements);
        }
      });
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
    mLeaderboardsClient.getLeaderboardIntent(leaderboardId)
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

  public static JSONArray achievementsToJSONArray(AchievementBuffer buffer ) {
    JSONArray result = new JSONArray();
    int bufSize = buffer.getCount();
    for( int i = 0; i < bufSize; i++ ) {
      Achievement achievement = buffer.get( i );
      JSONObject json = new JSONObject();
      try {
        json.put( "AchievementId", achievement.getAchievementId() );
        json.put( "State", achievement.getState() );
        json.put( "Type", achievement.getType() );
        json.put( "Description", achievement.getDescription() );
        json.put( "Name", achievement.getName() );
        /* Is incremental */
        if( achievement.getType() == Achievement.TYPE_INCREMENTAL ) {
          json.put( "CurrentSteps", achievement.getCurrentSteps() );
          json.put( "TotalSteps", achievement.getTotalSteps() );
        }

        result.put( json.toString() );
      } catch( JSONException e ) {
        e.printStackTrace();
      }
    }
    buffer.release();
    return result;
  }

  private class AccomplishmentsOutbox {
    boolean mPrimeAchievement = false;
    boolean mHumbleAchievement = false;
    boolean mLeetAchievement = false;
    boolean mArrogantAchievement = false;
    int mBoredSteps = 0;
    int mEasyModeScore = -1;
    int mHardModeScore = -1;

    boolean isEmpty() {
      return !mPrimeAchievement && !mHumbleAchievement && !mLeetAchievement &&
              !mArrogantAchievement && mBoredSteps == 0 && mEasyModeScore < 0 &&
              mHardModeScore < 0;
    }

  }


  /**
   * vibrate
   * @param millisec
   */
  public void vibrate(int millisec) {
    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createOneShot(millisec, VibrationEffect.DEFAULT_AMPLITUDE));
    } else {
      vibrator.vibrate(millisec);
    }
  }
}
