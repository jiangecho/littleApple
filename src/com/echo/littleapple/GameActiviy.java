package com.echo.littleapple;



import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.mipmap;
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
	private static final String BEST_SCORE = "BEST_SCORE";
	private static final String SPEED_BEST_SCORE = "SPEED_BEST_SCORE";
	private static final String ENDLESS_BEST_SCORE = "ENDLESS_BEST_SCORE";
	private static final String APP_URL = "http://1.littleappleapp.sinaapp.com/littleApple.apk";

	private TextView timerTV;
	private GameSurfaceView gameView;
	private LinearLayout startLayer;

	private LinearLayout resultLayer;
	private TextView resultTV;
	private TextView bestTV;
	private TextView promptTV;
	private Handler handler;
	
	private CountDownTimer countDownTimer;
	private StringBuffer remindTimeSB;
	private SharedPreferences sharedPreferences;
	private int bestScore = 0;
	private int currentScore = 0;
	
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
	public static final int MODE_SPEED = 1;
	public static final int MODE_ENDLESS = 2;
	public static final int MODE_GRAVITY = 3;
	public static final String MODE = "MODE";
	
	private static final int SPEED_SUCCESS_SCORE = 100;
	private static final int SPEED_MAX_TIME_LENGHT = 60 * 1000;
	private long escapeMillis;
	
	private int mode = MODE_CLASSIC;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		blockOnTouchEvent = new BlockOnTouchEvent();
        timerTV = (TextView)findViewById(R.id.timerTV);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.gameView);
        gameView = new GameSurfaceView(this);
        frameLayout.addView(gameView);

        promptTV = (TextView) findViewById(R.id.promptTV);
        gameView.setGameEventListener(this);
        startLayer = (LinearLayout)findViewById(R.id.startLayer);
        startLayer.setOnTouchListener(blockOnTouchEvent);

        resultLayer = (LinearLayout)findViewById(R.id.resultLayer);
        resultLayer.setOnTouchListener(blockOnTouchEvent);
        
        resultTV = (TextView) findViewById(R.id.resultTV);
        bestTV = (TextView) findViewById(R.id.bestTV);
        
        handler = new Handler();
        remindTimeSB = new StringBuffer();
        
        sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        speedBestScore = sharedPreferences.getLong(SPEED_BEST_SCORE, Long.MAX_VALUE);
        
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

		public MyCountDownTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			gameView.playGameSoundEffect(GameSurfaceView.TIME_OUT);
			timerTV.setText(getResources().getString(R.string.time_out));
			
			switch (mode) {
			case MODE_CLASSIC:
				if (currentScore > bestScore ) {
					bestScore = currentScore ;
					sharedPreferences.edit().putInt(BEST_SCORE, bestScore).commit();
				}
				
				break;

			case MODE_SPEED:
				if (escapeMillis < speedBestScore) {
					speedBestScore = escapeMillis;
					sharedPreferences.edit().putLong(SPEED_BEST_SCORE, speedBestScore).commit();
				}

				break;
			}
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

			if (mode == MODE_CLASSIC) {
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
			}else if(mode == MODE_SPEED){
				//do nothing
				escapeMillis = SPEED_MAX_TIME_LENGHT - millisUntilFinished;
			}
			
		}
		
	}
	
	
	public void onStartButtonClick(View view){
		if (mode != MODE_CLASSIC) {
			mode = MODE_CLASSIC;
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		}else {
			if (countDownTimer == null) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}
		
        bestScore = sharedPreferences.getInt(BEST_SCORE, 0);
		startLayer.setVisibility(View.INVISIBLE);
		//gameView.reset();
	}

	public void onSpeedStartButtonClick(View view){
		if (mode != MODE_SPEED) {
			mode = MODE_SPEED;
			countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT, 100);
		}else {
			if (countDownTimer == null) {
				countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT, 100);
			}
		}

        bestScore = sharedPreferences.getInt(SPEED_BEST_SCORE, 0);
		timerTV.setText("0");
		startLayer.setVisibility(View.INVISIBLE);
		//gameView.reset();
	}
	
	public void onEndlessStartButtonClick(View view){
		mode = MODE_ENDLESS;
        bestScore = sharedPreferences.getInt(ENDLESS_BEST_SCORE, 0);
		countDownTimer = null;
		timerTV.setText("0");
		gameView.setMode(MODE_GRAVITY);
		startLayer.setVisibility(View.INVISIBLE);
	}


	public void onRestartButtonClick(View view){
		resultLayer.setVisibility(View.INVISIBLE);
		if (mode == MODE_CLASSIC) {
			timerTV.setText("30.00");
		}else if(mode == MODE_SPEED){
			timerTV.setText("0");
		}else if(mode == MODE_ENDLESS){
			timerTV.setText("0");
		}
		gameView.reset();
	}
	
	public void onRankButtonClick(View view){
		Intent intent = new Intent(this, NewRankAcitivity.class);
		intent.putExtra(MODE, mode);
		startActivity(intent);
	}

	public void onBackButtonClick(View view){
		gameView.reset();
		startLayer.setVisibility(View.VISIBLE);
		resultLayer.setVisibility(View.INVISIBLE);
	}

	
	private void updateAndShowResultLayer(){
		String resultInfo = null;
		String bestScoreInfo = null;
		String promptInfo = null;
		
		if (random == null) {
			random = new Random();
		}

		int colorIndex = random.nextInt(colors.length);
		resultLayer.setBackgroundColor(Color.parseColor(colors[colorIndex]));;

		switch (mode) {
		case MODE_ENDLESS:
			//transfer to classic
		case MODE_CLASSIC:
			resultInfo= getResources().getString(R.string.classic_result, currentScore);
			
			if (currentScore > 100) {
				promptInfo = getResources().getString(R.string.str_high_score);
			}else {
				promptInfo = getResources().getString(R.string.strf);
			}
	
	        bestScoreInfo = getString(R.string.best, bestScore);
			
			break;

		case MODE_SPEED:
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
		//gameView.reset();
		if (countDownTimer != null) {
			countDownTimer.start();
		}
	}

	@Override
	public void onGameOver() {
		
		//TODO endless mode
		if (mode == MODE_ENDLESS) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.recover);
			builder.setMessage(R.string.recover_prompt);
			AlertDialog dialog = builder.create();
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.go_dead), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (currentScore > bestScore ) {
						bestScore = currentScore;
						sharedPreferences.edit().putInt(ENDLESS_BEST_SCORE, bestScore).commit();
					}
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

		switch (mode) {
		case MODE_CLASSIC:
			if (currentScore > bestScore ) {
				bestScore = currentScore;
				sharedPreferences.edit().putInt(BEST_SCORE, bestScore).commit();
			}
			
			break;

		case MODE_SPEED:
			timerTV.setText(getResources().getString(R.string.speed_fail));
			if (currentScore >= SPEED_SUCCESS_SCORE) {
				if (escapeMillis < speedBestScore) {
					speedBestScore = escapeMillis;
					sharedPreferences.edit().putLong(SPEED_BEST_SCORE, speedBestScore).commit();
				}
				
			}
			break;
		}
		
		updateAndShowResultLayer();
		submitScore();
	}
	
	@Override
	public void onScoreUpdate(int score) {
		currentScore = score;
		if (mode == MODE_SPEED) {
			timerTV.setText("" + score);
			if (score == SPEED_SUCCESS_SCORE) {
				gameView.playGameSoundEffect(GameSurfaceView.TIME_OUT);
				timerTV.setText(getResources().getString(R.string.speed_success));

				if (escapeMillis < speedBestScore) {
					speedBestScore = escapeMillis;
					sharedPreferences.edit().putLong(SPEED_BEST_SCORE, speedBestScore).commit();
				}
				
				countDownTimer.cancel();
				submitScore();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						updateAndShowResultLayer();
					}
				}, 500);
			}
		}else if (mode == MODE_ENDLESS) {
			timerTV.setText("" + score);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long currentMillis = System.currentTimeMillis();
			if (currentMillis - lastPressMillis < 2000) {
			 	finish();
			 	return true;
			}else {
				lastPressMillis = currentMillis;
				Toast toast = Toast.makeText(this, getResources().getString(R.string.press_twice_to_exit) , Toast.LENGTH_SHORT);
				toast.show();
				return false;
			}
			
		}
		
		return super.onKeyUp(keyCode, event);
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
   
   private void submitScore(){

	   if (nickyName == null) {
		   return;
	   }
	   
	   switch (mode) {
		case MODE_CLASSIC:
		   if (currentScore == 0) {
			   return;
		   }
		   scoreString = currentScore + "";
		   submitUri = "http://littleappleapp.sinaapp.com/new_insert.php";
			break;
	
		case MODE_SPEED:
			if (escapeMillis > SPEED_MAX_TIME_LENGHT || currentScore < SPEED_SUCCESS_SCORE) {
				return;
			}
			
			submitUri = "http://littleappleapp.sinaapp.com/new_insert_speed.php";
			scoreString = escapeMillis + "";
			break;
			//TODO 
		case MODE_ENDLESS:
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
