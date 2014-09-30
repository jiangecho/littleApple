package com.echo.littleapple;



import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import org.apache.http.util.ByteArrayBuffer;

import com.wandoujia.ads.sdk.AdListener;
import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.Ads.ShowMode;
import com.wandoujia.ads.sdk.R.string;
import com.wandoujia.ads.sdk.loader.Fetcher.AdFormat;
import com.wandoujia.ads.sdk.widget.AdBanner;
import com.wandoujia.ads.sdk.widget.AppWidget;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.echo.littleapple.GameSurfaceView.GameEventListner;


public class GameActiviy extends Activity implements GameEventListner{

	private static final int TIME_LENGHT = 30 * 1000;
	private static final String CLASSIC_30S_BEST_SCORE = "CLASSIC_30S_BEST_SCORE";
	//fix bug
	private static final String CLASSIC_SPEED_BEST_SCORE = "CLASSIC_SPEED_BEST_SCORE_EX";
	private static final String CLASSIC_ENDLESS_BEST_SCORE = "CLASSIC_ENDLESS_BEST_SCORE";
	private static final String CLASSIC_DISCONTINUOUS_BEST_SCORE = "CLASSIC_DISCONTINUOUS_BEST_SCORE ";
	private static final String CLASSIC_DOUBLE_BEST_SCORE = "CLASSIC_DOUBLE_BEST_SCORE ";

	private static final String GRAVITY_30S_BEST_SCORE = "GRAVITY_30S_BEST_SCORE";
	private static final String GRAVITY_MINE_BEST_SCORE = "GRAVITY_MINE_BEST_SCORE";
	private static final String GRAVITY_ENDLESS_BEST_SCORE = "GRAVITY_ENDLESS_BEST_SCORE";
	private static final String GRAVITY_DISCONTINUOUS_BEST_SCORE = "GRAVITY_DISCONTINUOUS_BEST_SCORE ";
	private static final String GRAVITY_DOUBLE_BEST_SCORE = "GRAVITY_DOUBLE_BEST_SCORE ";

	private static final String APP_URL = "http://1.littleappleapp.sinaapp.com/littleApple.apk";

	private static final String APP_WALL_ID = "2001e0364714d23e2f420e0f99e89020";
	private static final String LAST_ENDLESS_DATE = "LAST_ENDLESS_DATE";
	private TextView timerTV;
	private GameSurfaceView gameView;
	private LinearLayout modeSelectLayer, typeSelectLayer;

	private LinearLayout resultLayer;
	private TextView resultTV;
	private TextView bestTV;
	private TextView promptTV;
	private Handler handler;

	private TextView currentModeLevelTextView;
	private TextView currentModeTypeLevelTextView;
	private String currentModeString, currentTypeString;
	
	private TextView typeIntroTextView;
	
	private MyCountDownTimer countDownTimer;
	private StringBuffer remindTimeSB;
	private SharedPreferences sharedPreferences;
	private int bestScore = 0;
	private int currentScore = 0;
	
	private long currentSpeedScore;
	private long speedBestScore;
	
	
	private long lastPressMillis = 0;
	
	private BlockOnTouchEvent blockOnTouchEvent;
	
	private static final String[] colors = {"#773460" ,"#FE436A" ,"#823935" ,"#113F3D" ,"#26BCD5" ,"#F40D64" ,"#458994" ,"#93E0FF" ,"#D96831" ,"#AEDD81" ,"#593D43"};
	private static final String SAY_HELLO = "2.0ad";
	//TODO modify for different app store
	private static final String RECOVER_AD = "auto";
	private Random random;
	
	private String nickyName;
	private String scoreString;
	private final String submitUri = "http://littleappleapp.sinaapp.com/submit_score.php";;
	private static final String NICKY_NAME = "nickyname";
	private static final String HAVE_SUBMITED = "haveSubmited";
	private boolean haveSubmited = false;
	
	private Util.PostResultCallBack postResultCallBack;

	public static final int MODE_CLASSIC = 0;
	public static final int MODE_GRAVITY = 1;

	//attention: the type value should not be the same!!!!!
	// when add new type, no matter which mode it is,
	// we should add it at the end
	public static final int TYPE_CLASSIC_30S = 0;
	public static final int TYPE_CLASSIC_SPEED = 1;
	public static final int TYPE_CLASSIC_ENDLESS = 2;
	public static final int TYPE_CLASSIC_DISCONTINUOUS = 3;
	public static final int TYPE_CLASSIC_DOUBLE = 4;

	public static final int TYPE_GRAVITY_30S = 5;
	public static final int TYPE_GRAVITY_MINE = 6;
	public static final int TYPE_GRAVITY_ENDLESS = 7;
	public static final int TYPE_GRAVITY_DISCONTINUOUS = 8;
	public static final int TYPE_GRAVITY_DOUBLE = 9;
	// add new type here

	public static final String TYPE = "TYPE";
	
	private static final int SPEED_SUCCESS_SCORE = 100;
	private static final int SPEED_MAX_TIME_LENGHT = 60 * 1000;
	private long escapeMillis;
	
	private int mode = MODE_CLASSIC;
	private int type = TYPE_CLASSIC_30S;

	public static final int LEVEL_EASY = 0;
	public static final int LEVEL_NORMAL = 1;
	public static final int LEVEL_HARD = 2;
	
	private int level = LEVEL_NORMAL;
	private String currentLevelString;

	
	private Button startSpeedButton, startMindeButton;
	
	private boolean newVersionAvailable = false;

	private AdBanner adBanner;
	private View adBannerView;

	private boolean sayHello = false;
	private boolean isShowingAppWall = false;
	private boolean appWallReady = false;

    private ViewGroup adsWidgetContainer;
    private LinearLayout adsContainerContainer;
    
    private boolean recoverAdEnable = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.fragment_main);
		blockOnTouchEvent = new BlockOnTouchEvent();
        timerTV = (TextView)findViewById(R.id.timerTV);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.gameView);
        gameView = new GameSurfaceView(this);
        frameLayout.addView(gameView);

        promptTV = (TextView) findViewById(R.id.promptTV);
        gameView.setGameEventListener(this);
        modeSelectLayer = (LinearLayout) findViewById(R.id.mode_select_layer);
        typeSelectLayer = (LinearLayout)findViewById(R.id.type_select_layer);
        typeSelectLayer.setOnTouchListener(blockOnTouchEvent);
        modeSelectLayer.setOnTouchListener(blockOnTouchEvent);
        
        startSpeedButton = (Button) findViewById(R.id.startSpeedButton);
        startMindeButton = (Button) findViewById(R.id.startMineButton);

        resultLayer = (LinearLayout)findViewById(R.id.resultLayer);
        resultLayer.setOnTouchListener(blockOnTouchEvent);
        
        resultTV = (TextView) findViewById(R.id.resultTV);
        bestTV = (TextView) findViewById(R.id.bestTV);

        adsWidgetContainer = (ViewGroup) findViewById(R.id.ads_widget_container);
        adsContainerContainer = (LinearLayout) findViewById(R.id.ads_container_container);
        adsContainerContainer.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
        
		currentModeLevelTextView = (TextView) findViewById(R.id.current_mode_level_tv);
		currentModeTypeLevelTextView = (TextView) findViewById(R.id.current_mode_type_level_tv);
        
        typeIntroTextView = (TextView) findViewById(R.id.type_intro_tv);
        currentLevelString = getString(R.string.level_normal);
        handler = new Handler();
        remindTimeSB = new StringBuffer();
        
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        speedBestScore = sharedPreferences.getLong(CLASSIC_SPEED_BEST_SCORE, Long.MAX_VALUE);
        
        nickyName = sharedPreferences.getString(NICKY_NAME, null);
        if (nickyName == null) {
        	//nickyName = "User" + System.currentTimeMillis();
        	LayoutInflater layoutInflater = LayoutInflater.from(this);
        	final View view = layoutInflater.inflate(R.layout.nicky_name, null);
        	AlertDialog dialog = new AlertDialog.Builder(this)
        		.setTitle(getResources().getString(R.string.input_nicky_name))
        		.setView(view)
        		.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText editText = (EditText) view.findViewById(R.id.editText);
						String tmp = editText.getText().toString().replaceAll("\\s+", "");
						tmp.replaceAll("_", "");
						tmp.replaceAll(";", "");
						if (tmp.length() > 0) {
							nickyName = tmp + "_" + System.currentTimeMillis();
							sharedPreferences.edit().putString(NICKY_NAME, nickyName).commit();					
							//when the user reset the nicky name, re-submit the best score
							sharedPreferences.edit().putBoolean(HAVE_SUBMITED, false).commit();
							haveSubmited = false;
						}

					}
				})
				.create();
        	dialog.show();
		}
        
        haveSubmited = sharedPreferences.getBoolean(HAVE_SUBMITED, false);
        
        postResultCallBack = new CallBack();
        
        asyncGetOnlineConfig();


     // Init AdsSdk.
        try {
            Ads.init(this, "100010461", "7b95eea6b51978614c4ff137c2ad7c9f");
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        sayHello = sharedPreferences.getBoolean(SAY_HELLO, false);
        if (!sayHello) {
        	asyncGetOnlineConfig();
		}else {
			Ads.preLoad(this, AdFormat.interstitial, "1a3b067d93c5a677f37685fdf4c76b49");
		}
        
        Ads.preLoad(this, AdFormat.appwall, "GAME", APP_WALL_ID, new AdListener() {
			
			@Override
			public void onLoadFailure() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAdReady() {
				// TODO Auto-generated method stub
				appWallReady = true;
			}
			
			@Override
			public void onAdPresent() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAdDismiss() {
				// TODO Auto-generated method stub
				
			}
		});
        Ads.preLoad(this, AdFormat.appwall, "APP", APP_WALL_ID);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	private class MyCountDownTimer extends CountDownTimer{
		private int remindSeconds;
		private int remindMillis;
		public long durationMillis;

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			durationMillis = millisInFuture;
		}

		@Override
		public void onFinish() {
			gameView.playGameSoundEffect(GameSurfaceView.TIME_OUT);
			gameView.stop();
			timerTV.setText(getResources().getString(R.string.time_out));
			
			if (type == TYPE_CLASSIC_SPEED) {
				currentSpeedScore = escapeMillis;
			}
			updateBestScore();
			submitScore();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					updateAndShowResultLayer();
				}
			}, 500);
		}

		@Override
		public void onTick(long millisUntilFinished) {

			if (type == TYPE_CLASSIC_30S || type == TYPE_GRAVITY_30S 
					|| type == TYPE_CLASSIC_DISCONTINUOUS || type == TYPE_GRAVITY_DISCONTINUOUS
					|| type == TYPE_GRAVITY_MINE
					|| type == TYPE_CLASSIC_DOUBLE || type == TYPE_GRAVITY_DOUBLE) {
				remindTimeSB.setLength(0);
				remindSeconds = (int) (millisUntilFinished / 1000);
				remindMillis = (int) (millisUntilFinished % 1000 / 10);
				if (remindSeconds < 10) {
					remindTimeSB.append("0");
				}
				remindTimeSB.append(remindSeconds);
				remindTimeSB.append(".");
				
				if (remindMillis < 10) {
					remindTimeSB.append("0");
				}
				remindTimeSB.append(remindMillis);
				
				timerTV.setText(remindTimeSB);
			}else if(type == TYPE_CLASSIC_SPEED){
				//do nothing
				escapeMillis = SPEED_MAX_TIME_LENGHT - millisUntilFinished;
		}
		
	}
	
	}
	
	//select mode
	public void onClassicButtonClick(View view){
		this.mode = MODE_CLASSIC;
		modeSelectLayer.setVisibility(View.INVISIBLE);

		startSpeedButton.setVisibility(View.VISIBLE);
		startMindeButton.setVisibility(View.GONE);
		
		currentModeString = getString(R.string.mode_classic);
		currentModeLevelTextView.setText(getString(R.string.current_mode_level, currentModeString, currentLevelString));
		typeSelectLayer.setVisibility(View.VISIBLE);
		
		gameView.setMode(mode);
	}
	
	public void onGravityButtonClick(View view){
		this.mode = MODE_GRAVITY;
		modeSelectLayer.setVisibility(View.INVISIBLE);

		startSpeedButton.setVisibility(View.GONE);
		startMindeButton.setVisibility(View.VISIBLE);

		currentModeString = getString(R.string.mode_gravity);
		currentModeLevelTextView.setText(getString(R.string.current_mode_level, currentModeString, currentLevelString));
		typeSelectLayer.setVisibility(View.VISIBLE);

		gameView.setMode(mode);
	}
	
	public void onSettingButtonClick(View view){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View alertView = layoutInflater.inflate(R.layout.level_setting, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.level_setting);
		builder.setView(alertView);
		AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.level_easy), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
						level = LEVEL_EASY;
						currentLevelString = getString(R.string.level_easy);
						gameView.setLevel(GameActiviy.LEVEL_EASY);
				
			}
		});

		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.level_hard), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
level = LEVEL_HARD;
						currentLevelString = getString(R.string.level_hard);
				gameView.setLevel(GameActiviy.LEVEL_HARD);
			}
		});
		
		dialog.show();
	}
	
	//select type
	public void onStart30sButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		}else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
				
		}
		
		switch (mode) {
		case MODE_CLASSIC:
			type = TYPE_CLASSIC_30S;
			bestScore = sharedPreferences.getInt(CLASSIC_30S_BEST_SCORE, 0);
			
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_30S;
			bestScore = sharedPreferences.getInt(GRAVITY_30S_BEST_SCORE, 0);
			
			typeIntroTextView.setText(R.string.gravity_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		}
		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		
		currentTypeString = getString(R.string.type_time_30);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));

	}

	public void onStartEndlessButtonClick(View view) {
		switch (mode) {
		case MODE_CLASSIC:
			type = TYPE_CLASSIC_ENDLESS;
			bestScore = sharedPreferences.getInt(CLASSIC_ENDLESS_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_ENDLESS;
			bestScore = sharedPreferences.getInt(GRAVITY_ENDLESS_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.gravity_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		countDownTimer = null;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("0");
		typeSelectLayer.setVisibility(View.INVISIBLE);

		currentTypeString = getString(R.string.type_endless);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));

	}

	public void onStartSpeedButtonClick(View view) {
		type = TYPE_CLASSIC_SPEED;
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT, 100);
		}else {
			if (countDownTimer.durationMillis != SPEED_MAX_TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT, 100);
			}
		}

		// speed type is only available in CLASSIC MODE
		speedBestScore = sharedPreferences.getLong(CLASSIC_SPEED_BEST_SCORE, Long.MAX_VALUE);
		currentSpeedScore = 0;
		currentScore = 0;
		escapeMillis = 0;

		gameView.setType(type);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("0");
		typeSelectLayer.setVisibility(View.INVISIBLE);
		typeIntroTextView.setText(R.string.gravity_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		
		currentTypeString = getString(R.string.type_speed);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));
	}

	public void onStartDiscontinuousButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		}else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
				
		}

		switch (mode) {
		case MODE_CLASSIC:
			type = TYPE_CLASSIC_DISCONTINUOUS;
			bestScore = sharedPreferences.getInt(CLASSIC_DISCONTINUOUS_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_DISCONTINUOUS;
			bestScore = sharedPreferences.getInt(GRAVITY_DISCONTINUOUS_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.gravity_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");

		currentTypeString = getString(R.string.type_discontinuous);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));
	}

	public void onStartDoubleButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		}else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
				
		}

		switch (mode) {
		case MODE_CLASSIC:
			type = TYPE_CLASSIC_DOUBLE;
			bestScore = sharedPreferences.getInt(CLASSIC_DOUBLE_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_DOUBLE;
			bestScore = sharedPreferences.getInt(GRAVITY_DOUBLE_BEST_SCORE, 0);
			typeIntroTextView.setText(R.string.gravity_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");

		currentTypeString = getString(R.string.type_double);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));

	}

	public void onStartMineButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		}else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
				
		}

		// mine type only support gravity mode
		type = TYPE_GRAVITY_MINE;
		bestScore = sharedPreferences.getInt(GRAVITY_MINE_BEST_SCORE, 0);

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		typeIntroTextView.setText(R.string.gravity_mine_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		
		currentTypeString = getString(R.string.type_mine);
		currentModeTypeLevelTextView.setText(getString(R.string.current_mode_type_level, currentModeString, 
				currentTypeString, currentLevelString));
	}

	public void onRestartButtonClick(View view){
		timerTV.setVisibility(View.VISIBLE);
		typeIntroTextView.setVisibility(View.VISIBLE);
		resultLayer.setVisibility(View.INVISIBLE);
		if (type == TYPE_CLASSIC_30S || type == TYPE_GRAVITY_30S
				|| type == TYPE_GRAVITY_MINE
				|| type == TYPE_CLASSIC_DISCONTINUOUS || type == TYPE_GRAVITY_DISCONTINUOUS) {
			timerTV.setText("30.00");
		}else if(type == TYPE_CLASSIC_SPEED){
			timerTV.setText("0");
		}else if(type == TYPE_CLASSIC_ENDLESS || type == TYPE_GRAVITY_ENDLESS){
			timerTV.setText("0");
		}

		currentScore = 0;
		currentSpeedScore = 0;
		escapeMillis = 0;
		gameView.reset();
	}
	
	//TODO bugs
	public void onRankButtonClick(View view){
		Intent intent = new Intent(this, NewRankAcitivity.class);
		intent.putExtra(TYPE, type);
		startActivity(intent);
	}

	public void onBackButtonClick(View view){
		if (typeSelectLayer.getVisibility() == View.VISIBLE) {
			typeSelectLayer.setVisibility(View.INVISIBLE);
			modeSelectLayer.setVisibility(View.VISIBLE);
		}else {
			gameView.reset();
			typeSelectLayer.setVisibility(View.VISIBLE);
			resultLayer.setVisibility(View.INVISIBLE);
			
		}
	}
	
	
	//TODO bug
	private void updateAndShowResultLayer(){
		String resultInfo = null;
		String bestScoreInfo = null;
		String promptInfo = null;
		
		if (random == null) {
			random = new Random();
		}

		timerTV.setVisibility(View.INVISIBLE);

		int colorIndex = random.nextInt(colors.length);
		resultLayer.setBackgroundColor(Color.parseColor(colors[colorIndex]));;

		switch (type) {
		case TYPE_CLASSIC_DOUBLE:
		case TYPE_GRAVITY_DOUBLE:
		case TYPE_GRAVITY_MINE:
		case TYPE_CLASSIC_DISCONTINUOUS:
		case TYPE_GRAVITY_DISCONTINUOUS:
		case TYPE_CLASSIC_ENDLESS:
		case TYPE_GRAVITY_ENDLESS:
			//transfer to classic
		case TYPE_CLASSIC_30S:
		case TYPE_GRAVITY_30S:
			resultInfo= getResources().getString(R.string.classic_result, currentScore);
		
			if (currentScore > 100) {
				promptInfo = getResources().getString(R.string.str_high_score);
		}else {
				promptInfo = getResources().getString(R.string.strf);
		}

	        bestScoreInfo = getString(R.string.best, bestScore);

			break;

		case TYPE_CLASSIC_SPEED:
			StringBuffer sb = new StringBuffer();
			if (currentScore >= SPEED_SUCCESS_SCORE) {
				sb.append(escapeMillis / 1000);
				sb.append(".");
				sb.append((escapeMillis % 1000) / 10);
				resultInfo= getResources().getString(R.string.speed_result, sb.toString());
				
				if (escapeMillis > 25 * 1000) {
					promptInfo = getResources().getString(R.string.str_high_score);
				}else {
					promptInfo = getResources().getString(R.string.strf);
				}
			}else {
				resultInfo = getResources().getString(R.string.speed_fail);
				promptInfo = getResources().getString(R.string.strf);
			}
			
			if (speedBestScore > SPEED_MAX_TIME_LENGHT) {
				bestScoreInfo = getString(R.string.speed_best, getString(R.string.speed_rank_none));
				
			}else {
				sb.setLength(0);
				sb.append(speedBestScore / 1000);
				sb.append(".");
				sb.append((speedBestScore % 1000) / 10);
				bestScoreInfo = getString(R.string.speed_best, sb.toString());
			}


			break;
		}

		resultTV.setText(resultInfo);
		promptTV.setText(promptInfo);
        bestTV.setText(bestScoreInfo);
        
		resultLayer.setVisibility(View.VISIBLE);
		showAd();

	}

	@Override
	protected void onStart() {
		if (!sayHello) {
			showBannerAd();
		}
		if (adBanner != null) {
			adBanner.startAutoScroll();
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (adBanner != null) {
			adBanner.stopAutoScroll();
		}
		super.onStop();
	}

	@Override
	public void onGameStart() {
		//TODO start the timer or not should depend on the mode
		if (countDownTimer != null) {
			countDownTimer.start();
		}
		
		typeIntroTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onGameOver() {
		
		typeIntroTextView.setVisibility(View.INVISIBLE);
		//TODO endless mode
		if (type == TYPE_CLASSIC_ENDLESS || type == TYPE_GRAVITY_ENDLESS) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.recover);
			builder.setMessage(R.string.recover_prompt);
			AlertDialog dialog = builder.create();
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.go_dead), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					updateBestScore();
					updateAndShowResultLayer();
					submitScore();
				}
			});

			dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.recover), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO show appwall
					//TODO just for test
					if (appWallReady) {
						showAd();
					}
					isShowingAppWall = true;
					
				}
			});
			dialog.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					
					if (isShowingAppWall) {
						isShowingAppWall = false;
						gameView.recover();
					}
				}
			});
			dialog.show();
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
//			dialog.setOnKeyListener(new OnKeyListener() {
//				
//				@Override
//				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//					return true;
//				}
//			});
			
			return;
		}
		
		if (countDownTimer != null) {
			countDownTimer.cancel();
		}

		updateBestScore();
		updateAndShowResultLayer();
		submitScore();
	}

	@Override
	public void onScoreUpdate(int score) {
		currentScore = score;
		if (type == TYPE_CLASSIC_SPEED) {
			timerTV.setText("" + score);
			if (score == SPEED_SUCCESS_SCORE) {
				gameView.playGameSoundEffect(GameSurfaceView.TIME_OUT);
				gameView.stop();
				timerTV.setText(getResources().getString(R.string.speed_success));

				currentSpeedScore = escapeMillis;
				
				countDownTimer.cancel();
				updateBestScore();
				submitScore();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						updateAndShowResultLayer();
					}
				}, 200);
			}
		}else if (type == TYPE_CLASSIC_ENDLESS || type == TYPE_GRAVITY_ENDLESS) {
			timerTV.setText("" + score);
		}
	}

	
	public void showAd(){
		boolean tmp = Ads.isLoaded(AdFormat.interstitial, "1a3b067d93c5a677f37685fdf4c76b49");
		if (tmp) {
			adsWidgetContainer.setVisibility(View.VISIBLE);
			AppWidget appWidget = Ads.showAppWidget(this, null, "1a3b067d93c5a677f37685fdf4c76b49", Ads.ShowMode.WIDGET,
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							adsWidgetContainer.setVisibility(View.GONE);
						}
			});
			if ((type == TYPE_CLASSIC_ENDLESS || type == TYPE_GRAVITY_ENDLESS) && recoverAdEnable) {
				long lastEndlessModeMillis = sharedPreferences.getLong(LAST_ENDLESS_DATE, 0);
				long currentMillis = System.currentTimeMillis();
				long len = currentMillis - lastEndlessModeMillis;
				if (len > 24 * 60 * 60 * 1000) {
					sharedPreferences.edit().putLong(LAST_ENDLESS_DATE, currentMillis).commit();
					int app_widget_install_button = 2131230861;
					View view = appWidget.findViewById(app_widget_install_button);
					if (view != null) {
						String string = ((Button)view).getText().toString();
						if (string != null) {
							if (string.equals("立即安装")) {
								((Button)view).performClick();
							}
						}
					}
				}
				
			}
			adsWidgetContainer.addView(appWidget);
		}
	}
	
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(adsWidgetContainer.getVisibility() == View.VISIBLE){
				return true;
			} else if (resultLayer.getVisibility() == View.VISIBLE) {
				gameView.reset();
				resultLayer.setVisibility(View.INVISIBLE);
				typeSelectLayer.setVisibility(View.VISIBLE);
			}else if(typeSelectLayer.getVisibility() == View.VISIBLE){
				typeSelectLayer.setVisibility(View.INVISIBLE);
				modeSelectLayer.setVisibility(View.VISIBLE);
			} else {
				long currentMillis = System.currentTimeMillis();
				if (currentMillis - lastPressMillis < 2000) {
				 	finish();
				}else {
					if (newVersionAvailable) {
						showUpdateDialog();
					}else {
//						lastPressMillis = currentMillis;
						showExitDialog();
						
					}
				}
				
			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
			
		}
		
	}
	
	private class BlockOnTouchEvent implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
		
	}
	
	public void onShareButtonClick(View view){
		String imgPath = takeScreenShot(view);
		if (imgPath == null) {
			Toast.makeText(this, "SD卡不存在", Toast.LENGTH_SHORT).show();
		}else {
			Toast.makeText(this, getString(R.string.capture_screen_ok), Toast.LENGTH_SHORT).show();
			showShare(imgPath);
		}
	}
	
	private String takeScreenShot(View view){
		View rootView = view.getRootView();
		rootView.setDrawingCacheEnabled(true);
		rootView.buildDrawingCache(true);
		Bitmap bitmap = rootView.getDrawingCache(true);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return null;
		}
		File  path = Environment.getExternalStorageDirectory();
		File file = new File(path, "screenshot.png");

		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rootView.destroyDrawingCache();
		return file.getAbsolutePath();
		
	}
	
   private void showShare(String imgPath) {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        oks.disableSSOWhenAuthorize();
       
        oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        oks.setTitle(getString(R.string.app_name));
        oks.setTitleUrl(APP_URL);
        oks.setText(getString(R.string.share_title, APP_URL));
        oks.setImagePath(imgPath);
        oks.setUrl(APP_URL);
        oks.setComment(getString(R.string.share_comment, currentScore));
        oks.setSite(getString(R.string.app_name));
        oks.setSiteUrl(APP_URL);

        oks.show(this);
   }
   

	  private void showBannerAd() {
		    ViewGroup containerView = (ViewGroup) findViewById(R.id.banner_ad_container);
		    if (adBannerView != null && containerView.indexOfChild(adBannerView) >= 0) {
		      containerView.removeView(adBannerView);
		    }



		    adBanner = Ads.showBannerAd(this, (ViewGroup) findViewById(R.id.banner_ad_container),
		       "61312ac6f2b2f4d277ebabd02f88f00b");
		    adBannerView = adBanner.getView();
		  }
	  
	  
   private void checkUpdate(int versionCode){
	   try {
		   int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		   if (versionCode > currentVersionCode) {
			   newVersionAvailable = true;
		   }

	   } catch (NameNotFoundException e) {
		   e.printStackTrace();
	   }
   }
   
	 private void getOnlineConfig(){
		try {
			URL url = new URL("http://littleappleapp.sinaapp.com/config.txt");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			InputStream inputStream = urlConnection.getInputStream();
			byte[] bytes = new byte[1024];
			int count;
			ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(1024);
			count = inputStream.read(bytes);
			while (count != -1) {
				byteArrayBuffer.append(bytes, 0, count);
				count = inputStream.read(bytes);
			}
			String config = new String(byteArrayBuffer.toByteArray());
			if (config.contains(SAY_HELLO)) {
				sayHello = true;
				sharedPreferences.edit().putBoolean(SAY_HELLO, true).commit();
				Ads.preLoad(this, AdFormat.interstitial, "1a3b067d93c5a677f37685fdf4c76b49");
			}
			
			if (config.contains(RECOVER_AD)) {
				recoverAdEnable = true;
			}

			String[] tmp = config.split("\\s+");

			for (String str : tmp) {
				if (str.startsWith("version")) {
					int versionCode = Integer.parseInt(str.substring("version".length()));
					checkUpdate(versionCode);
					break;
				}
			}

			urlConnection.disconnect();
			inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
	 } 
	 
	 private void asyncGetOnlineConfig(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				getOnlineConfig();
			}
		}).start(); 
	 }
   
   // TODO optimize do not need to check the mode, can just use type
   private void updateBestScore(){
	  if (mode == MODE_CLASSIC) {
		  switch (type) {
		  	case TYPE_CLASSIC_30S:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(CLASSIC_30S_BEST_SCORE, bestScore).commit();
				}
		  		break;
			case TYPE_CLASSIC_ENDLESS:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(CLASSIC_ENDLESS_BEST_SCORE, bestScore).commit();
				}
				break;
			case TYPE_CLASSIC_SPEED:
		  		if (currentSpeedScore > 0 && currentSpeedScore < speedBestScore) {
					speedBestScore = currentSpeedScore;
					sharedPreferences.edit().putLong(CLASSIC_SPEED_BEST_SCORE, speedBestScore).commit();
				}
				break;
			case TYPE_CLASSIC_DISCONTINUOUS:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(CLASSIC_DISCONTINUOUS_BEST_SCORE, bestScore).commit();
				}
				break;
			case TYPE_CLASSIC_DOUBLE:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(CLASSIC_DOUBLE_BEST_SCORE, bestScore).commit();
				}
				break;
		}
		
	  }else if(mode == MODE_GRAVITY){
		  switch (type) {
		  	case TYPE_GRAVITY_30S:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(GRAVITY_30S_BEST_SCORE, bestScore).commit();
				}
		  		break;
			case TYPE_GRAVITY_ENDLESS:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(GRAVITY_ENDLESS_BEST_SCORE, bestScore).commit();
				}
				break;
			case TYPE_GRAVITY_MINE:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(GRAVITY_MINE_BEST_SCORE, bestScore).commit();
				}
				break;
			case TYPE_GRAVITY_DISCONTINUOUS:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(GRAVITY_DISCONTINUOUS_BEST_SCORE, bestScore).commit();
				}
				break;
			case TYPE_GRAVITY_DOUBLE:
		  		if (currentScore > bestScore) {
					bestScore = currentScore;
					sharedPreferences.edit().putInt(GRAVITY_DOUBLE_BEST_SCORE, bestScore).commit();
				}
				break;
		}
	  }
   }

   //TODO bug, different uri
   private void submitScore(){
	   
	   if (nickyName == null) {
		   return;
	   }
	   
	   switch (type) {
		case TYPE_CLASSIC_30S:
		   if (currentScore == 0) {
		return;
	   }
		   scoreString = currentScore + "";
			break;
	
		case TYPE_CLASSIC_SPEED:
			if (escapeMillis > SPEED_MAX_TIME_LENGHT || currentScore < SPEED_SUCCESS_SCORE) {
				return;
			}
			
			scoreString = escapeMillis + "";
			break;
			//TODO 
		case TYPE_CLASSIC_ENDLESS:
		   if (currentScore == 0) {
			   return;
		   }
		   scoreString = currentScore + "";
			break;
		}
	   
	   if (type == TYPE_CLASSIC_SPEED) {
		   if (currentSpeedScore == 0) {
			   return;
		   }else {
			scoreString = currentSpeedScore + "";
		}
	   }else {
		if (currentScore == 0) {
			return;
		}else {
			scoreString = currentScore + "";
		}
	   }

	   new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO the uri should base on the mode
			  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			  nameValuePairs.add(new BasicNameValuePair("nickyname", nickyName));
			  nameValuePairs.add(new BasicNameValuePair("score", scoreString));
			  nameValuePairs.add(new BasicNameValuePair("type", type + ""));
			  Util.httpPost(submitUri, nameValuePairs, postResultCallBack);
		}
	}).start();

   }
   
   private class CallBack implements Util.PostResultCallBack{

	@Override
	public void onSuccess() {
		if (!haveSubmited) {
			haveSubmited = true;
			sharedPreferences.edit().putBoolean(HAVE_SUBMITED, true).commit();
		}
		
	}

	@Override
	public void onFail() {
		haveSubmited = false;
		sharedPreferences.edit().putBoolean(HAVE_SUBMITED, false).commit();

	}
	   
   }
   
   private void showUpdateDialog(){
	  AlertDialog.Builder builder = new AlertDialog.Builder(this); 
	  builder.setTitle(getString(R.string.update_title));
	  builder.setMessage(getString(R.string.update_text));
	  AlertDialog dialog = builder.create();
	  dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.update_cancel), new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			finish();
		}
	});
	  dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.update_ok), new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Uri uri = Uri.parse(APP_URL);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			Toast toast = Toast.makeText(GameActiviy.this, getString(R.string.update_prompt) , Toast.LENGTH_SHORT);
			toast.show();
			finish();
		}
	});
	  dialog.show();
   }
   
   private void showExitDialog(){
	  AlertDialog dialog = new AlertDialog.Builder(this)
	  	.setTitle(getString(R.string.exit_title))
	  	.setMessage(getString(R.string.exit_text))
	  	.create();
	  dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.exit_cancel), new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			finish();
		}
	});
	  dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.exit_ok), new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			Ads.showAppWall(GameActiviy.this, APP_WALL_ID);
		}
	});
	  dialog.show();
   }
}
