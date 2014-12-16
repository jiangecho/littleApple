package com.echo.littleapple;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.trinea.android.common.util.PreferencesUtils;

import com.echo.littleapple.GameSurfaceView.GameEventListner;

public class GameActiviy extends Activity implements GameEventListner {

	private static final int TIME_LENGHT = 30 * 1000;
	private static final int TIME_LENGHT_20 = 20 * 1000;


	private TextView timerTV;
	private GameSurfaceView gameView;
	private LinearLayout modeSelectLayer, typeSelectLayer, runnerSelectLayer;

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
	//private SharedPreferences sharedPreferences;
	private int bestScore = 0;
	private int currentScore = 0;

	private long currentSpeedScore;
	private long speedBestScore;

	private long lastPressMillis = 0;

	private BlockOnTouchEvent blockOnTouchEvent;

	private static final String[] colors = { "#773460", "#FE436A", "#823935",
			"#113F3D", "#26BCD5", "#F40D64", "#458994", "#93E0FF", "#D96831",
			"#AEDD81", "#593D43" };
	private Random random;

	private String nickyName;
	private String scoreString;
	private static final String NICKY_NAME = "nickyname";


	public static final int MODE_CLASSIC = 0;
	public static final int MODE_GRAVITY = 1;
	public static final int MODE_TERRIBLE = 2;



	private static final int SPEED_SUCCESS_SCORE = 100;
	private static final int SPEED_MAX_TIME_LENGHT = 60 * 1000;
	private long escapeMillis;

	private int mode = MODE_CLASSIC;
	private int type = Constant.TYPE_CLASSIC_30S;

	public static final int LEVEL_EASY = 0;
	public static final int LEVEL_NORMAL = 1;
	public static final int LEVEL_HARD = 2;

	private int level = LEVEL_NORMAL;
	private String currentLevelString;

	private Button startSpeedButton, startMindeButton;
	private LinearLayout classicAndGravityTypesLayout, terribleTypesLayout;
	
	private Button moreGameButton;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_main);
		blockOnTouchEvent = new BlockOnTouchEvent();
		timerTV = (TextView) findViewById(R.id.timerTV);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.gameView);
		gameView = new GameSurfaceView(this);
		frameLayout.addView(gameView);

		promptTV = (TextView) findViewById(R.id.promptTV);
		gameView.setGameEventListener(this);
		modeSelectLayer = (LinearLayout) findViewById(R.id.mode_select_layer);
		typeSelectLayer = (LinearLayout) findViewById(R.id.type_select_layer);
		runnerSelectLayer = (LinearLayout) findViewById(R.id.runner_select_layer);
		typeSelectLayer.setOnTouchListener(blockOnTouchEvent);
		modeSelectLayer.setOnTouchListener(blockOnTouchEvent);
		runnerSelectLayer.setOnTouchListener(blockOnTouchEvent);

		startSpeedButton = (Button) findViewById(R.id.startSpeedButton);
		startMindeButton = (Button) findViewById(R.id.startMineButton);

		classicAndGravityTypesLayout = (LinearLayout) findViewById(R.id.classic_gravity_types);
		terribleTypesLayout = (LinearLayout) findViewById(R.id.terrible_types);

		resultLayer = (LinearLayout) findViewById(R.id.resultLayer);
		resultLayer.setOnTouchListener(blockOnTouchEvent);

		resultTV = (TextView) findViewById(R.id.resultTV);
		bestTV = (TextView) findViewById(R.id.bestTV);
		
		currentModeLevelTextView = (TextView) findViewById(R.id.current_mode_level_tv);
		currentModeTypeLevelTextView = (TextView) findViewById(R.id.current_mode_type_level_tv);

		moreGameButton = (Button) findViewById(R.id.moreGamesButton);
		boolean showGameCenter = PreferencesUtils.getBoolean(this, Constant.SHOW_GAME_CENTER);
		if (!App.showGameCenter && !showGameCenter) {
			moreGameButton.setVisibility(View.GONE);
		}

		typeIntroTextView = (TextView) findViewById(R.id.type_intro_tv);
		currentLevelString = getString(R.string.level_normal);
		handler = new Handler();
		remindTimeSB = new StringBuffer();

//		sharedPreferences = getSharedPreferences(getPackageName(),
//				Context.MODE_PRIVATE);
//		speedBestScore = sharedPreferences.getLong(CLASSIC_SPEED_BEST_SCORE,
//				Long.MAX_VALUE);
		speedBestScore = App.getBestScore(Constant.TYPE_CLASSIC_SPEED);
		nickyName = App.getString(NICKY_NAME);
		if (nickyName == null) {
			nickyName = "User" + System.currentTimeMillis();
			LayoutInflater layoutInflater = LayoutInflater.from(this);
			final View view = layoutInflater.inflate(R.layout.nicky_name, null);
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(
							getResources().getString(R.string.input_nicky_name))
					.setView(view)
					.setPositiveButton(
							getResources().getString(R.string.confirm),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									EditText editText = (EditText) view
											.findViewById(R.id.editText);
									String tmp = editText.getText().toString()
											.replaceAll("\\s+", "");
									tmp.replaceAll("_", "");
									tmp.replaceAll(";", "");
									if (tmp.length() > 0) {
										nickyName = tmp + "_"
												+ System.currentTimeMillis();
										App.putString(NICKY_NAME, nickyName);
									}

								}
							}).create();
			dialog.setCancelable(false);
			dialog.show();
		}

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
		gameView.initSoundPool();
	}

	private class MyCountDownTimer extends CountDownTimer {
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

			if (type == Constant.TYPE_CLASSIC_SPEED) {
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

			// if (type == Constant.TYPE_CLASSIC_30S || type == Constant.TYPE_GRAVITY_30S
			// || type == Constant.TYPE_CLASSIC_DISCONTINUOUS
			// || type == Constant.TYPE_GRAVITY_DISCONTINUOUS
			// || type == Constant.TYPE_GRAVITY_MINE || type == Constant.TYPE_CLASSIC_DOUBLE
			// || type == Constant.TYPE_GRAVITY_DOUBLE
			// || type == Constant.TYPE_TERIBLE_RELAY) {
			// remindTimeSB.setLength(0);
			// remindSeconds = (int) (millisUntilFinished / 1000);
			// remindMillis = (int) (millisUntilFinished % 1000 / 10);
			// if (remindSeconds < 10) {
			// remindTimeSB.append("0");
			// }
			// remindTimeSB.append(remindSeconds);
			// remindTimeSB.append(".");
			//
			// if (remindMillis < 10) {
			// remindTimeSB.append("0");
			// }
			// remindTimeSB.append(remindMillis);
			//
			// timerTV.setText(remindTimeSB);
			//
			// // TERRIBLE type
			// if (type == Constant.TYPE_TERIBLE_RELAY
			// && millisUntilFinished < TIME_LENGHT_20) {
			// gameView.setMode(MODE_GRAVITY);
			// }
			// } else
			if (type == Constant.TYPE_CLASSIC_SPEED) {
				// do nothing
				escapeMillis = SPEED_MAX_TIME_LENGHT - millisUntilFinished;
			} else {
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

				// TERRIBLE type
				if (type == Constant.TYPE_TERIBLE_RELAY
						&& millisUntilFinished < TIME_LENGHT_20) {
					gameView.setMode(MODE_GRAVITY);
				}

			}

		}

	}

	// select mode
	public void onClassicButtonClick(View view) {
		this.mode = MODE_CLASSIC;
		modeSelectLayer.setVisibility(View.INVISIBLE);

		startSpeedButton.setVisibility(View.VISIBLE);
		startMindeButton.setVisibility(View.GONE);

		currentModeString = getString(R.string.mode_classic);
		currentModeLevelTextView.setText(getString(R.string.current_mode_level,
				currentModeString, currentLevelString));
		typeSelectLayer.setVisibility(View.VISIBLE);
		classicAndGravityTypesLayout.setVisibility(View.VISIBLE);
		terribleTypesLayout.setVisibility(View.GONE);

		gameView.setMode(mode);
	}

	public void onGravityButtonClick(View view) {
		this.mode = MODE_GRAVITY;
		modeSelectLayer.setVisibility(View.INVISIBLE);

		startSpeedButton.setVisibility(View.GONE);
		startMindeButton.setVisibility(View.VISIBLE);

		currentModeString = getString(R.string.mode_gravity);
		currentModeLevelTextView.setText(getString(R.string.current_mode_level,
				currentModeString, currentLevelString));
		typeSelectLayer.setVisibility(View.VISIBLE);
		classicAndGravityTypesLayout.setVisibility(View.VISIBLE);
		terribleTypesLayout.setVisibility(View.GONE);

		gameView.setMode(mode);
	}

	public void onTerribleGameButtonClick(View view) {
		this.mode = MODE_TERRIBLE;
		currentModeString = getString(R.string.mode_terrible);
		currentModeLevelTextView.setText(getString(R.string.current_mode_level,
				currentModeString, currentLevelString));
		modeSelectLayer.setVisibility(View.INVISIBLE);
		typeSelectLayer.setVisibility(View.VISIBLE);
		classicAndGravityTypesLayout.setVisibility(View.GONE);
		terribleTypesLayout.setVisibility(View.VISIBLE);
	}

	public void onStartRelayButtonClick(View view) {
		startRelay();
	}

	public void onStartLoomButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}
		type = Constant.TYPE_TERRIBLE_LOOM;
		mode = MODE_TERRIBLE;
		gameView.setMode(MODE_TERRIBLE);
		gameView.setType(Constant.TYPE_TERRIBLE_LOOM);

		bestScore = (int) App.getBestScore(Constant.TYPE_TERRIBLE_LOOM);
		typeIntroTextView.setText(R.string.loom_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		currentScore = 0;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		currentModeString = getString(R.string.mode_terrible);
		currentTypeString = getString(R.string.type_loom);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
		typeSelectLayer.setVisibility(View.INVISIBLE);
		modeSelectLayer.setVisibility(View.INVISIBLE);

	}
	
	public void onStartMoveButtonClick(View view){
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}
		type = Constant.TYPE_TERRIBLE_MOVE;
		mode = MODE_TERRIBLE;
		gameView.setMode(MODE_TERRIBLE);
		gameView.setType(Constant.TYPE_TERRIBLE_MOVE);

		bestScore = (int) App.getBestScore(Constant.TYPE_TERRIBLE_MOVE);
		typeIntroTextView.setText(R.string.move_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		currentScore = 0;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		currentModeString = getString(R.string.mode_terrible);
		currentTypeString = getString(R.string.type_move);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
		typeSelectLayer.setVisibility(View.INVISIBLE);
		modeSelectLayer.setVisibility(View.INVISIBLE);
		
	}

	public void onStartTerribleDoubleButtonClick(View view){
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}
		type = Constant.TYPE_TERRIBLE_DOUBLE;
		mode = MODE_TERRIBLE;
		gameView.setMode(MODE_TERRIBLE);
		gameView.setType(Constant.TYPE_TERRIBLE_DOUBLE);

		bestScore = (int) App.getBestScore(Constant.TYPE_TERRIBLE_DOUBLE);
		typeIntroTextView.setText(R.string.terrible_double_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		currentScore = 0;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		currentModeString = getString(R.string.mode_terrible);
		currentTypeString = getString(R.string.type_terrible_double);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
		typeSelectLayer.setVisibility(View.INVISIBLE);
		modeSelectLayer.setVisibility(View.INVISIBLE);
		
	}

	public void onStartTerrible2ButtonClick(View view){
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}
		type = Constant.TYPE_TERRIBLE_2;
		mode = MODE_TERRIBLE;
		gameView.setMode(MODE_TERRIBLE);
		gameView.setType(Constant.TYPE_TERRIBLE_2);

		bestScore = (int) App.getBestScore(Constant.TYPE_TERRIBLE_2);
		typeIntroTextView.setText(R.string.terrible_2_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		currentScore = 0;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		currentModeString = getString(R.string.mode_terrible);
		currentTypeString = getString(R.string.type_terrible_2);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
		typeSelectLayer.setVisibility(View.INVISIBLE);
		modeSelectLayer.setVisibility(View.INVISIBLE);
		
	}


	public void onSettingButtonClick(View view) {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		final View alertView = layoutInflater.inflate(R.layout.level_setting,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.level_setting);
		builder.setView(alertView);
		AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
				getString(R.string.level_easy),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						level = LEVEL_EASY;
						currentLevelString = getString(R.string.level_easy);
						gameView.setLevel(GameActiviy.LEVEL_EASY);

					}
				});

		dialog.setButton(AlertDialog.BUTTON_POSITIVE,
				getString(R.string.level_hard),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						level = LEVEL_HARD;
						currentLevelString = getString(R.string.level_hard);
						gameView.setLevel(GameActiviy.LEVEL_HARD);
					}
				});

		dialog.show();
	}

	public void onRunnerButtonClick(View view) {

		modeSelectLayer.setVisibility(View.INVISIBLE);
		runnerSelectLayer.setVisibility(View.VISIBLE);

	}

	public void onSingleRowButtonClick(View view) {
		Intent intent = new Intent(this,
				com.jucyzhang.flappybatta.RunnerGameActivity.class);
		intent.putExtra("NICKYNAME", nickyName);
		startActivity(intent);
	}

	public void onDoubleRowButtonClick(View view) {
		Intent intent = new Intent(this,
				com.jucyzhang.flappybatta.TwoRunnerGameActivity.class);
		intent.putExtra("NICKYNAME", nickyName);
		startActivity(intent);
	}

	public void onUpDownButtonClick(View view) {
		Intent intent = new Intent(this,
				com.jucyzhang.flappybatta.UpDownRnnerGameActivity.class);
		intent.putExtra("NICKYNAME", nickyName);
		startActivity(intent);
	}

	public void onUpDownButtonPlusClick(View view) {
		Intent intent = new Intent(this,
				com.jucyzhang.flappybatta.UpDownRnnerGameActivity.class);
		intent.putExtra("NICKYNAME", nickyName);
		intent.putExtra(com.jucyzhang.flappybatta.UpDownRnnerGameActivity.ENABLE_PLUS_MODE, true);
		startActivity(intent);
	}

	public void onBirdButtonClick(View view) {
		Intent intent = new Intent(this,
				com.jucyzhang.flappybatta.GameActivity.class);
		// Intent intent = new Intent(this,
		// com.jucyzhang.flappybatta.RunnerGameActivity.class);
		intent.putExtra("NICKYNAME", nickyName);
		startActivity(intent);
	}

	// select type
	public void onStart30sButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}

		}

		switch (mode) {
		case MODE_CLASSIC:
			type = Constant.TYPE_CLASSIC_30S;
			bestScore = (int) App.getBestScore(Constant.TYPE_CLASSIC_30S);

			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = Constant.TYPE_GRAVITY_30S;
			bestScore = (int) App.getBestScore(Constant.TYPE_GRAVITY_30S);

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
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));

	}

	public void onStartEndlessButtonClick(View view) {
		switch (mode) {
		case MODE_CLASSIC:
			type = Constant.TYPE_CLASSIC_ENDLESS;
			bestScore = (int) App.getBestScore(Constant.TYPE_CLASSIC_ENDLESS);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = Constant.TYPE_GRAVITY_ENDLESS;
			bestScore = (int) App.getBestScore(Constant.TYPE_GRAVITY_ENDLESS);
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
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));

	}

	public void onStartSpeedButtonClick(View view) {
		type = Constant.TYPE_CLASSIC_SPEED;
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != SPEED_MAX_TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(SPEED_MAX_TIME_LENGHT,
						100);
			}
		}

		// speed type is only available in CLASSIC MODE
		speedBestScore = App.getBestScore(Constant.TYPE_CLASSIC_SPEED);
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
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
	}

	public void onStartDiscontinuousButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}

		}

		switch (mode) {
		case MODE_CLASSIC:
			type = Constant.TYPE_CLASSIC_DISCONTINUOUS;
			App.getBestScore(Constant.TYPE_CLASSIC_DISCONTINUOUS);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = Constant.TYPE_GRAVITY_DISCONTINUOUS;
			bestScore = (int) App.getBestScore(Constant.TYPE_GRAVITY_DISCONTINUOUS);
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
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
	}

	public void onStartDoubleButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}

		}

		switch (mode) {
		case MODE_CLASSIC:
			type = Constant.TYPE_CLASSIC_DOUBLE;
			bestScore = (int) App.getBestScore(Constant.TYPE_CLASSIC_DOUBLE);
			typeIntroTextView.setText(R.string.classic_intro);
			typeIntroTextView.setVisibility(View.VISIBLE);
			break;
		case MODE_GRAVITY:
			type = Constant.TYPE_GRAVITY_DOUBLE;
			bestScore = (int) App.getBestScore(Constant.TYPE_GRAVITY_DOUBLE);
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
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));

	}

	public void onStartMineButtonClick(View view) {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}

		}

		// mine type only support gravity mode
		type = Constant.TYPE_GRAVITY_MINE;
		bestScore = (int) App.getBestScore(Constant.TYPE_GRAVITY_MINE);

		currentScore = 0;
		gameView.setType(type);
		typeSelectLayer.setVisibility(View.INVISIBLE);
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		typeIntroTextView.setText(R.string.gravity_mine_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);

		currentTypeString = getString(R.string.type_mine);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
	}

	public void onRestartButtonClick(View view) {
		timerTV.setVisibility(View.VISIBLE);
		typeIntroTextView.setVisibility(View.VISIBLE);
		resultLayer.setVisibility(View.INVISIBLE);
		if (type == Constant.TYPE_CLASSIC_30S || type == Constant.TYPE_GRAVITY_30S
				|| type == Constant.TYPE_GRAVITY_MINE
				|| type == Constant.TYPE_CLASSIC_DISCONTINUOUS
				|| type == Constant.TYPE_GRAVITY_DISCONTINUOUS) {
			timerTV.setText("30.00");
		} else if (type == Constant.TYPE_CLASSIC_SPEED) {
			timerTV.setText("0");
		} else if (type == Constant.TYPE_CLASSIC_ENDLESS || type == Constant.TYPE_GRAVITY_ENDLESS) {
			timerTV.setText("0");
		} else if (type == Constant.TYPE_TERIBLE_RELAY) {
			timerTV.setText("40:00");
			gameView.setMode(MODE_CLASSIC);
		}

		currentScore = 0;
		currentSpeedScore = 0;
		escapeMillis = 0;
		gameView.reset();
	}

	// TODO bugs
	public void onRankButtonClick(View view) {
		Intent intent = new Intent(this, NewRankAcitivity.class);
		intent.putExtra(Constant.TYPE, type);
		startActivity(intent);
	}

	public void onBackButtonClick(View view) {
		if (runnerSelectLayer.getVisibility() == View.VISIBLE) {
			modeSelectLayer.setVisibility(View.VISIBLE);
			runnerSelectLayer.setVisibility(View.INVISIBLE);
		} else {
			if (typeSelectLayer.getVisibility() == View.VISIBLE) {
				typeSelectLayer.setVisibility(View.INVISIBLE);
				modeSelectLayer.setVisibility(View.VISIBLE);
			} else {
				gameView.reset();
				typeSelectLayer.setVisibility(View.VISIBLE);
				resultLayer.setVisibility(View.INVISIBLE);
			}

		}

	}
	
	public void onMoreGamesButtonClick(View view){
		Intent intent = new Intent(this, GameCenterActivity.class);
		startActivity(intent);
	}

	private void startRelay() {
		if (countDownTimer == null) {
			countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
		} else {
			if (countDownTimer.durationMillis != TIME_LENGHT) {
				countDownTimer = new MyCountDownTimer(TIME_LENGHT, 100);
			}
		}

		type = Constant.TYPE_TERIBLE_RELAY;
		mode = MODE_TERRIBLE;
		gameView.setMode(MODE_CLASSIC);
		gameView.setType(Constant.TYPE_TERIBLE_RELAY);

		bestScore = (int) App.getBestScore(Constant.TYPE_TERIBLE_RELAY);
		typeIntroTextView.setText(R.string.relay_intro);
		typeIntroTextView.setVisibility(View.VISIBLE);
		currentScore = 0;
		timerTV.setVisibility(View.VISIBLE);
		timerTV.setText("30:00");
		currentModeString = getString(R.string.mode_terrible);
		currentTypeString = getString(R.string.type_relay);
		currentModeTypeLevelTextView.setText(getString(
				R.string.current_mode_type_level, currentModeString,
				currentTypeString, currentLevelString));
		typeSelectLayer.setVisibility(View.INVISIBLE);
		modeSelectLayer.setVisibility(View.INVISIBLE);
	}

	// TODO bug
	private void updateAndShowResultLayer() {
		String resultInfo = null;
		String bestScoreInfo = null;
		String promptInfo = null;

		if (random == null) {
			random = new Random();
		}

		timerTV.setVisibility(View.INVISIBLE);

		int colorIndex = random.nextInt(colors.length);
		resultLayer.setBackgroundColor(Color.parseColor(colors[colorIndex]));
		;

		switch (type) {
		// case Constant.TYPE_CLASSIC_DOUBLE:
		// case Constant.TYPE_GRAVITY_DOUBLE:
		// case Constant.TYPE_GRAVITY_MINE:
		// case Constant.TYPE_CLASSIC_DISCONTINUOUS:
		// case Constant.TYPE_GRAVITY_DISCONTINUOUS:
		// case Constant.TYPE_CLASSIC_ENDLESS:
		// case Constant.TYPE_GRAVITY_ENDLESS:
		// // transfer to classic
		// case Constant.TYPE_CLASSIC_30S:
		// case Constant.TYPE_GRAVITY_30S:
		// case Constant.TYPE_TERIBLE_RELAY:
		// resultInfo = getResources().getString(R.string.classic_result,
		// currentScore);
		//
		// if (currentScore > 100) {
		// promptInfo = getResources().getString(R.string.str_high_score);
		// } else {
		// promptInfo = getResources().getString(R.string.strf);
		// }
		//
		// bestScoreInfo = getString(R.string.best, bestScore);
		//
		// break;

		case Constant.TYPE_CLASSIC_SPEED:
			StringBuffer sb = new StringBuffer();
			if (currentScore >= SPEED_SUCCESS_SCORE) {
				sb.append(escapeMillis / 1000);
				sb.append(".");
				sb.append((escapeMillis % 1000) / 10);
				resultInfo = getResources().getString(R.string.speed_result,
						sb.toString());

				if (escapeMillis > 25 * 1000) {
					promptInfo = getResources().getString(
							R.string.str_high_score);
				} else {
					promptInfo = getResources().getString(R.string.strf);
				}
			} else {
				resultInfo = getResources().getString(R.string.speed_fail);
				promptInfo = getResources().getString(R.string.strf);
			}

			if (speedBestScore > SPEED_MAX_TIME_LENGHT) {
				bestScoreInfo = getString(R.string.speed_best,
						getString(R.string.speed_rank_none));

			} else {
				sb.setLength(0);
				sb.append(speedBestScore / 1000);
				sb.append(".");
				sb.append((speedBestScore % 1000) / 10);
				bestScoreInfo = getString(R.string.speed_best, sb.toString());
			}

			break;
		default:
			resultInfo = getResources().getString(R.string.classic_result,
					currentScore);

			if (currentScore > 100) {
				promptInfo = getResources().getString(R.string.str_high_score);
			} else {
				promptInfo = getResources().getString(R.string.strf);
			}

			bestScoreInfo = getString(R.string.best, bestScore);

			break;
		}

		resultTV.setText(resultInfo);
		promptTV.setText(promptInfo);
		bestTV.setText(bestScoreInfo);

		resultLayer.setVisibility(View.VISIBLE);

	}

	@Override
	public void onGameStart() {
		// TODO start the timer or not should depend on the mode
		if (countDownTimer != null) {
			countDownTimer.start();
		}

		typeIntroTextView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onGameOver() {

		typeIntroTextView.setVisibility(View.INVISIBLE);
		// TODO endless mode
		if (type == Constant.TYPE_CLASSIC_ENDLESS || type == Constant.TYPE_GRAVITY_ENDLESS) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.recover);
			builder.setMessage(R.string.recover_prompt);
			AlertDialog dialog = builder.create();
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
					getString(R.string.go_dead),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							updateBestScore();
							updateAndShowResultLayer();
							submitScore();
						}
					});

			dialog.setButton(AlertDialog.BUTTON_POSITIVE,
					getString(R.string.recover),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO show appwall
							// TODO just for test
							gameView.recover();

						}
					});
			dialog.show();
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			// dialog.setOnKeyListener(new OnKeyListener() {
			//
			// @Override
			// public boolean onKey(DialogInterface dialog, int keyCode,
			// KeyEvent event) {
			// return true;
			// }
			// });

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
		if (type == Constant.TYPE_CLASSIC_SPEED) {
			timerTV.setText("" + score);
			if (score == SPEED_SUCCESS_SCORE) {
				gameView.playGameSoundEffect(GameSurfaceView.TIME_OUT);
				gameView.stop();
				timerTV.setText(getResources()
						.getString(R.string.speed_success));

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
		} else if (type == Constant.TYPE_CLASSIC_ENDLESS || type == Constant.TYPE_GRAVITY_ENDLESS) {
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
			} else if (typeSelectLayer.getVisibility() == View.VISIBLE) {
				typeSelectLayer.setVisibility(View.INVISIBLE);
				modeSelectLayer.setVisibility(View.VISIBLE);
			} else {
				long currentMillis = System.currentTimeMillis();
				if (currentMillis - lastPressMillis < 2000) {
					finish();
				} else {
					if (App.newVersionAvailable) {
						showUpdateDialog();
					} else {
						// lastPressMillis = currentMillis;
						showExitDialog();

					}
				}

			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);

		}

	}

	private class BlockOnTouchEvent implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}

	}

	public void onShareButtonClick(View view) {
	}

	// TODO optimize do not need to check the mode, can just use type
	private void updateBestScore() {
		
		if (type == Constant.TYPE_CLASSIC_SPEED) {
			if (currentSpeedScore > 0 && currentSpeedScore < speedBestScore) {
				speedBestScore = currentSpeedScore;
				App.updateBestScore(type, speedBestScore);
			} 
		}else {
			if (currentScore > bestScore) {
				bestScore = currentScore;
				App.updateBestScore(type, bestScore);
			}
		}
	}

	private void submitScore() {

		if (nickyName == null) {
			return;
		}

		if (type == Constant.TYPE_CLASSIC_SPEED) {
			if (currentSpeedScore == 0) {
				return;
			} else {
				scoreString = currentSpeedScore + "";
			}
		} else {
			if (currentScore == 0) {
				return;
			} else {
				scoreString = currentScore + "";
			}
		}
		
		App.submitScore(nickyName, scoreString, type);

	}

//	private class CallBack implements Util.PostResultCallBack {
//
//		@Override
//		public void onSuccess() {
//			if (!haveSubmited) {
//				haveSubmited = true;
//				sharedPreferences.edit().putBoolean(HAVE_SUBMITED, true)
//						.commit();
//			}
//
//		}
//
//		@Override
//		public void onFail() {
//			haveSubmited = false;
//			sharedPreferences.edit().putBoolean(HAVE_SUBMITED, false).commit();
//
//		}
//
//	}

	private void showUpdateDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.update_title));
		builder.setMessage(getString(R.string.update_text));
		AlertDialog dialog = builder.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
				getString(R.string.update_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		dialog.setButton(AlertDialog.BUTTON_POSITIVE,
				getString(R.string.update_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(Constant.APP_URL);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
						Toast toast = Toast.makeText(GameActiviy.this,
								getString(R.string.update_prompt),
								Toast.LENGTH_SHORT);
						toast.show();
						finish();
					}
				});
		dialog.show();
	}

	private void showExitDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
				getString(R.string.exit_title))
		// .setMessage(getString(R.string.exit_text))
				.create();
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
				getString(R.string.exit_cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		dialog.setButton(AlertDialog.BUTTON_POSITIVE,
				getString(R.string.exit_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		dialog.show();
	}
}
