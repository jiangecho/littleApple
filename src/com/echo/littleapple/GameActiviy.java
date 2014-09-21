package com.echo.littleapple;



import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.echo.littleapple.GameSurfaceView.GameEventListner;


public class GameActiviy extends Activity implements GameEventListner{

	private static final int TIME_LENGHT = 30 * 1000;
	private static final String CLASSIC_30S_BEST_SCORE = "CLASSIC_30S_BEST_SCORE";
	private static final String CLASSIC_SPEED_BEST_SCORE = "CLASSIC_SPEED_BEST_SCORE";
	private static final String CLASSIC_ENDLESS_BEST_SCORE = "CLASSIC_ENDLESS_BEST_SCORE";
	private static final String CLASSIC_DISCONTINUOUS_BEST_SCORE = "CLASSIC_DISCONTINUOUS_BEST_SCORE ";
	private static final String CLASSIC_DOUBLE_BEST_SCORE = "CLASSIC_DOUBLE_BEST_SCORE ";

	private static final String GRAVITY_30S_BEST_SCORE = "GRAVITY_30S_BEST_SCORE";
	private static final String GRAVITY_MINE_BEST_SCORE = "GRAVITY_MINE_BEST_SCORE";
	private static final String GRAVITY_ENDLESS_BEST_SCORE = "GRAVITY_ENDLESS_BEST_SCORE";
	private static final String GRAVITY_DISCONTINUOUS_BEST_SCORE = "GRAVITY_DISCONTINUOUS_BEST_SCORE ";
	private static final String GRAVITY_DOUBLE_BEST_SCORE = "GRAVITY_DOUBLE_BEST_SCORE ";

	private static final String APP_URL = "http://1.littleappleapp.sinaapp.com/littleApple.apk";

	private TextView timerTV;
	private GameSurfaceView gameView;
	private LinearLayout modeSelectLayer, typeSelectLayer;

	private LinearLayout resultLayer;
	private TextView resultTV;
	private TextView bestTV;
	private TextView promptTV;
	private Handler handler;
	
	private TextView currentModeTextView;
	private TextView currentModeTypeTextView;
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
	private Random random;
	
	private String nickyName;
	private String scoreString;
	private String submitUri;
	private static final String NICKY_NAME = "nickyname";
	private static final String HAVE_SUBMITED = "haveSubmited";
	private boolean haveSubmited = false;
	
	private Util.PostResultCallBack postResultCallBack;
	
	public static final int MODE_CLASSIC = 0;
	public static final int MODE_GRAVITY = 1;

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

	public static final String TYPE = "TYPE";
	
	private static final int SPEED_SUCCESS_SCORE = 100;
	private static final int SPEED_MAX_TIME_LENGHT = 60 * 1000;
	private long escapeMillis;
	
	private int mode = MODE_CLASSIC;
	private int type = TYPE_CLASSIC_30S;

	public static final int LEVEL_EASY = 0;
	public static final int LEVEL_NORMAL = 1;
	public static final int LEVEL_HARD = 2;

	
	private Button startSpeedButton, startMindeButton;

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
        
        currentModeTextView = (TextView) findViewById(R.id.current_mode_tv);
        currentModeTypeTextView = (TextView) findViewById(R.id.current_mode_type_tv);
        
        typeIntroTextView = (TextView) findViewById(R.id.type_intro_tv);
        
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
		currentModeTextView.setText(getString(R.string.current_mode, currentModeString));
		typeSelectLayer.setVisibility(View.VISIBLE);
		
		gameView.setMode(mode);
	}
	
	public void onGravityButtonClick(View view){
		this.mode = MODE_GRAVITY;
		modeSelectLayer.setVisibility(View.INVISIBLE);

		startSpeedButton.setVisibility(View.GONE);
		startMindeButton.setVisibility(View.VISIBLE);

		currentModeString = getString(R.string.mode_gravity);
		currentModeTextView.setText(getString(R.string.current_mode, currentModeString));
		typeSelectLayer.setVisibility(View.VISIBLE);

		gameView.setMode(mode);
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
			
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_30S;
			bestScore = sharedPreferences.getInt(GRAVITY_30S_BEST_SCORE, 0);
			
			break;
		}
		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setText("30:00");
		
		currentTypeString = getString(R.string.type_time_30);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
		
	}

	public void onStartEndlessButtonClick(View view) {
		switch (mode) {
		case MODE_CLASSIC:
			type = TYPE_CLASSIC_ENDLESS;
			bestScore = sharedPreferences.getInt(CLASSIC_ENDLESS_BEST_SCORE, 0);
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_ENDLESS;
			bestScore = sharedPreferences.getInt(GRAVITY_ENDLESS_BEST_SCORE, 0);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		countDownTimer = null;
		timerTV.setText("0");
		typeSelectLayer.setVisibility(View.INVISIBLE);

		currentTypeString = getString(R.string.type_endless);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
		
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

		gameView.setType(type);
		timerTV.setText("0");
		typeSelectLayer.setVisibility(View.INVISIBLE);
		
		currentTypeString = getString(R.string.type_speed);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
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
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_DISCONTINUOUS;
			bestScore = sharedPreferences.getInt(GRAVITY_DISCONTINUOUS_BEST_SCORE, 0);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setText("30:00");

		currentTypeString = getString(R.string.type_discontinuous);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
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
			break;
		case MODE_GRAVITY:
			type = TYPE_GRAVITY_DOUBLE;
			bestScore = sharedPreferences.getInt(GRAVITY_DOUBLE_BEST_SCORE, 0);
			break;
		}

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setText("30:00");

		currentTypeString = getString(R.string.type_double);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
		
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
		timerTV.setText("30:00");
		
		currentTypeString = getString(R.string.type_mine);
		currentModeTypeTextView.setText(getString(R.string.current_mode_type, currentModeString, currentTypeString));
	}

	public void onRestartButtonClick(View view){
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
		gameView.reset();
	}
	
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
		
	}

	@Override
	public void onGameStart() {
		//TODO start the timer or not should depend on the mode
		if (countDownTimer != null) {
			countDownTimer.start();
		}
		
		//typeIntroTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onGameOver() {
		
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
					gameView.recover();
					
				}
			});
			dialog.show();
			dialog.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					return true;
				}
			});
			
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if (resultLayer.getVisibility() == View.VISIBLE) {
				gameView.reset();
				resultLayer.setVisibility(View.INVISIBLE);
				typeSelectLayer.setVisibility(View.VISIBLE);
			}else if(typeSelectLayer.getVisibility() == View.VISIBLE){
				typeSelectLayer.setVisibility(View.INVISIBLE);
				modeSelectLayer.setVisibility(View.VISIBLE);
			}else {
				long currentMillis = System.currentTimeMillis();
				if (currentMillis - lastPressMillis < 2000) {
				 	finish();
				}else {
					lastPressMillis = currentMillis;
					Toast toast = Toast.makeText(this, getResources().getString(R.string.press_twice_to_exit) , Toast.LENGTH_SHORT);
					toast.show();
				}
				
			}
			return true;
		}else {
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
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
       
        // 分享时Notification的图标和文字
        oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.app_name));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(APP_URL);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("哈哈，来挑战我吧！你是我的小苹果:" + APP_URL);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(imgPath);
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(APP_URL);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("呵呵，我吃了" + currentScore  + "个小苹果！");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(APP_URL);

        // 启动分享GUI
        oks.show(this);
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
		  		if (currentSpeedScore > speedBestScore) {
					speedBestScore = currentSpeedScore;
					sharedPreferences.edit().putLong(CLASSIC_SPEED_BEST_SCORE, bestScore).commit();
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
	   
	   // TODO just for debug
	   if (true) {
		return;
	   }

	   if (nickyName == null) {
		   return;
	   }
	   
	   switch (type) {
//	   	case TYPE_CLASSIC_GRAVITY:
//		   if (currentScore == 0) {
//			   return;
//		   }
//		   scoreString = currentScore + "";
//		   submitUri = "http://littleappleapp.sinaapp.com/new_insert_gravity.php";
//			break;

		case TYPE_CLASSIC_30S:
		   if (currentScore == 0) {
			   return;
		   }
		   scoreString = currentScore + "";
		   submitUri = "http://littleappleapp.sinaapp.com/new_insert.php";
			break;
	
		case TYPE_CLASSIC_SPEED:
			if (escapeMillis > SPEED_MAX_TIME_LENGHT || currentScore < SPEED_SUCCESS_SCORE) {
				return;
			}
			
			submitUri = "http://littleappleapp.sinaapp.com/new_insert_speed.php";
			scoreString = escapeMillis + "";
			break;
			//TODO 
		case TYPE_CLASSIC_ENDLESS:
		   if (currentScore == 0) {
			   return;
		   }
		   scoreString = currentScore + "";
		   submitUri = "http://littleappleapp.sinaapp.com/new_insert_endless.php";
			break;
		}


	   new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO the uri should base on the mode
			  List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			  nameValuePairs.add(new BasicNameValuePair("nickyname", nickyName));
			  nameValuePairs.add(new BasicNameValuePair("score", scoreString));
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
}
