package com.echo.littleapple;



import com.echo.littleapple.GameView.GameEventListner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class GameActiviy extends Activity implements GameEventListner{

	private static final int TIME_LENGHT = 30 * 1000;
	private static final String BEST_SCORE = "BEST_SCORE";

	private TextView timerTV;
	private GameView gameView;
	private LinearLayout startLayer;

	private LinearLayout resultLayer;
	private TextView resultTV;
	private TextView bestTV;
	private Handler handler;
	
	private CountDownTimer countDownTimer;
	private StringBuffer remindTimeSB;
	private SharedPreferences sharedPreferences;
	private int bestScore;
	
	
	private long lastPressMillis = 0;
	
	private BlockOnTouchEvent blockOnTouchEvent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		blockOnTouchEvent = new BlockOnTouchEvent();
        timerTV = (TextView)findViewById(R.id.timerTV);
        gameView = (GameView)findViewById(R.id.gameView);
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
        bestScore = sharedPreferences.getInt(BEST_SCORE, 0);

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
			timerTV.setText(getResources().getString(R.string.time_out));
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					updateAndShowResultLayer();
				}
			}, 500);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			//timerTV.setText("test " + millisUntilFinished / 1000);
//			String remindTime = "" + millisUntilFinished / 1000 + "." + millisUntilFinished % 1000 / 10;
//			timerTV.setText(remindTime);
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
		}
		
	}
	
	
	public void onStartButtonClick(View view){
		startLayer.setVisibility(View.INVISIBLE);
		gameView.reset();
	}

	public void onRestartButtonClick(View view){
		resultLayer.setVisibility(View.INVISIBLE);
		timerTV.setText("30.00");
		gameView.reset();
	}

	
	private void updateAndShowResultLayer(){
		int score = gameView.getScore();
		String value = getResources().getString(R.string.result, score);
		resultTV.setText(value);
		
		if (score > bestScore) {
			bestScore = score;
			sharedPreferences.edit().putInt(BEST_SCORE, bestScore).commit();
		}
		value = getString(R.string.best, bestScore);
		bestTV.setText(value);

		resultLayer.setVisibility(View.VISIBLE);
		
	}

	@Override
	public void onGameStart() {
		//gameView.reset();
		countDownTimer.start();
	}

	@Override
	public void onGameOver(int score) {
		//TODO stop timer
		// show result
		countDownTimer.cancel();
		
		//TODO best score
		updateAndShowResultLayer();
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
}
