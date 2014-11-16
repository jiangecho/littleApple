package com.jucyzhang.flappybatta;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echo.littleapple.App;
import com.echo.littleapple.Constant;
import com.echo.littleapple.NewRankAcitivity;
import com.echo.littleapple.R;

public class UpDownRnnerGameActivity extends Activity implements Callback,
		OnTouchListener {
	/**
	 * set to true in order to print fps in screen.
	 */
	private static final boolean SHOW_FPS = false;
	/**
	 * set to false to disable coins.
	 */
	private static final boolean ENABLE_COIN = false;
	private ImageView ivBackground;
	private SurfaceView surfaceView;
	private SurfaceHolder holder;
	private LinkedList<Sprite> sprites;
	private SoundPool soundPool;

	private static final String TAG = "GameActivity";
	private Drawable coin;
	private static final long GAP = 20;
	private static final long SECOND_FLOOR_NEW_BLOCKER_COUNT = 60;
	private static final long FIRST_FLOOR_NEW_BLOCKER_COUNT = 100;
	private static final long NEW_COIN_COUNT = 60;
	private static final int[] BACKGROUND = new int[] {
			R.drawable.bg_general_day, R.drawable.bg_general_night };

	private Paint globalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private boolean surfaceCreated = false;;
	private Thread drawingTheard;
	private int[] soundIds;

	private static final int SOUND_DIE = 0;
	private static final int SOUND_HIT = 1;
	private static final int SOUND_POINT = 2;
	private static final int SOUND_SWOOSHING = 3;
	private static final int SOUND_WING = 4;

	private RunnerSprite runnerSprite;
	// private RunnerSprite runnerSprite2;
	private ScoreSprite scoreSprite;
	private GroundSprite groundSprite;
	private SplashSprite splashSprite;
	private FpsSprite fpsSprite;

	private int secondFloorBlockerCount = 0;
	private int firstFloorBlockerCount = 0;
	private int coinCount = 0;
	private volatile int currentPoint = 0;
	private volatile int currentStatus = Sprite.STATUS_NOT_STARTED;

	private Random random = new Random();

	private LinearLayout resultLayer;
	private Button shareButton;

	private TextView resultTV;
	private TextView bestTV;
	private TextView currentModeTypeLevelTV;

	private String nickyName;
	private int bestScore;
	private static final int type = Constant.TYPE_FLAPPY_RUNNER_UP_DOWN;

	private int groundHeight;
	private int groundTopHeight;
	private int firstFloorBottomY;
	private int secondFloorBottomY;
	private int currentFloor = 2;
	private int firstFloorHeight;
	private int secondFloorHeight;

	private int FULL_BLOCK_FREQUCEN = 5;

	private int runnerHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_game);
		ivBackground = (ImageView) findViewById(R.id.iv_background);
		shareButton = (Button) findViewById(R.id.sharetButton);
		shareButton.setVisibility(View.GONE);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		surfaceView.setKeepScreenOn(true);
		resultLayer = (LinearLayout) findViewById(R.id.resultLayer);
		surfaceView.setSoundEffectsEnabled(false);
		holder = surfaceView.getHolder();
		surfaceView.setZOrderOnTop(true);
		// surfaceView.setOnClickListener(this);
		surfaceView.setOnTouchListener(this);
		holder.addCallback(this);
		holder.setFormat(PixelFormat.TRANSLUCENT);

		resultTV = (TextView) findViewById(R.id.resultTV);
		bestTV = (TextView) findViewById(R.id.bestTV);
		currentModeTypeLevelTV = (TextView) findViewById(R.id.current_mode_type_level_tv);
		currentModeTypeLevelTV.setVisibility(View.INVISIBLE);

		nickyName = getIntent().getStringExtra("NICKYNAME");

		int height = ViewUtil.getScreenHeight(this);
		groundHeight = ViewUtil.dipResourceToPx(this, R.dimen.ground_height);
		groundTopHeight = groundHeight
				- ViewUtil.dipResourceToPx(this, R.dimen.ground_margin);

		secondFloorBottomY = height;
		firstFloorBottomY = height / 5 * 2;
		firstFloorHeight = firstFloorBottomY - groundTopHeight;
		secondFloorHeight = secondFloorBottomY - firstFloorBottomY
				- groundHeight;

		runnerHeight = ViewUtil.dipResourceToPx(this, R.dimen.runner_height);
		
		bestScore = (int) App.getBestScore(type);

		loadRes();
		restart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopDrawingThread();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopDrawingThread();
		soundPool.release();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceCreated = true;
		startDrawingThread();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;
		stopDrawingThread();
	}

	private void restart() {
		if (!isFinishing()) {
			ivBackground.setImageResource(BACKGROUND[random
					.nextInt(BACKGROUND.length)]);
			soundPool.play(soundIds[SOUND_SWOOSHING], 0.5f, 0.5f, 1, 0, 1);
			sprites = new LinkedList<Sprite>();
			runnerSprite = new RunnerSprite(this);
			scoreSprite = new ScoreSprite(this);
			groundSprite = new GroundSprite(this);
			splashSprite = null;
			sprites.add(scoreSprite);
			sprites.add(groundSprite);
			groundSprite = new GroundSprite(this);
			groundSprite.setY(firstFloorBottomY - groundTopHeight);
			sprites.add(groundSprite);
			if (SHOW_FPS) {
				fpsSprite = new FpsSprite(this);
				sprites.add(fpsSprite);
			} else {
				fpsSprite = null;
			}

			if (random.nextBoolean()) {
				runnerSprite.setY(firstFloorBottomY - groundTopHeight
						- runnerHeight);
				currentFloor = 1;
			} else {
				currentFloor = 2;
			}
			sprites.add(runnerSprite);

			// sprites.add(runnerSprite2);
			HintSprite hintSprite = new HintSprite(this);
			sprites.add(hintSprite);
			secondFloorBlockerCount = 0;
			firstFloorBlockerCount = 0;
			coinCount = (int) (NEW_COIN_COUNT / 2);
			currentPoint = 0;
			currentStatus = Sprite.STATUS_NOT_STARTED;
			// if (alertDialog != null && alertDialog.isShowing()) {
			// alertDialog.dismiss();
			// alertDialog = null;
			// }
			if (surfaceCreated) {
				startDrawingThread();
			}
		}
	}

	private void loadRes() {
		Resources res = getResources();
		coin = res.getDrawable(R.drawable.img_coin);
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		AssetManager assetManager = res.getAssets();
		soundIds = new int[5];
		try {
			soundIds[SOUND_DIE] = soundPool.load(
					assetManager.openFd("sfx_die.ogg"), 1);
			soundIds[SOUND_HIT] = soundPool.load(
					assetManager.openFd("sfx_hit.ogg"), 1);
			soundIds[SOUND_POINT] = soundPool.load(
					assetManager.openFd("sfx_point.ogg"), 1);
			soundIds[SOUND_SWOOSHING] = soundPool.load(
					assetManager.openFd("sfx_swooshing.ogg"), 1);
			soundIds[SOUND_WING] = soundPool.load(
					assetManager.openFd("sfx_wing.ogg"), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startDrawingThread() {
		stopDrawingThread();
		drawingTheard = new DrawingThread();
		drawingTheard.start();
	}

	private void stopDrawingThread() {
		if (drawingTheard != null) {
			drawingTheard.interrupt();
			try {
				drawingTheard.join();
			} catch (InterruptedException e) {
			}
			drawingTheard = null;
		}
	}

	@SuppressLint("WrongCall")
	private class DrawingThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (!Thread.interrupted()) {
				long startTime = System.currentTimeMillis();
				Canvas canvas = holder.lockCanvas();
				if (canvas == null) {
					Log.e(TAG, "canvas == null");
					continue;
				}
				try {
					cleanCanvas(canvas);
					if (sprites.size() == 0) {
						break;
					}
					Iterator<Sprite> iterator = sprites.iterator();
					while (iterator.hasNext()) {
						Sprite sprite = iterator.next();
						if (sprite.isAlive()) {
							sprite.onDraw(canvas, globalPaint, currentStatus);
						} else {
							iterator.remove();
							// Log.d(TAG, "remove sprite");
						}
					}
				} finally {
					holder.unlockCanvasAndPost(canvas);
				}
				long duration = (System.currentTimeMillis() - startTime);
				long gap = GAP - duration;
				if (gap > 0) {
					try {
						sleep(gap);
					} catch (Exception e) {
						break;
					}
				}
				if (currentStatus == Sprite.STATUS_NOT_STARTED) {
					continue;
				}
				if (currentStatus == Sprite.STATUS_GAME_OVER) {
					if ((runnerSprite.isHit(runnerSprite) || runnerSprite
							.isHit(runnerSprite)) && !splashSprite.isAlive()) {
						onGameOver();
						sprites.clear();
						continue;
					} else {
						continue;
					}
				}
				boolean hit = false;
				for (Sprite sprite : sprites) {
					if (sprite.isHit(runnerSprite)
					/* || sprite.isHit(runnerSprite2) */) {
						onHit();
						hit = true;
						break;
					}
				}
				if (hit) {
					sprites.addLast(splashSprite = new SplashSprite());
					currentStatus = Sprite.STATUS_GAME_OVER;
					runnerSprite.setHitted(true);
					// runnerSprite2.setHitted(true);
					continue;
				}
				if (secondFloorBlockerCount > SECOND_FLOOR_NEW_BLOCKER_COUNT) {
					secondFloorBlockerCount = 0;
					RoadBlockSprite sprite;
					if (random.nextInt(FULL_BLOCK_FREQUCEN) == 3) {
						sprite = RoadBlockSprite.obtainRandom(getBaseContext(),
								runnerSprite.getX(), secondFloorHeight);
					} else {
						sprite = RoadBlockSprite.obtainRandom(getBaseContext(),
								runnerSprite.getX());
					}
					sprites.addFirst(sprite);
					// Log.d(TAG, "new sprite");
				} else {
					secondFloorBlockerCount++;
				}

				if (firstFloorBlockerCount > FIRST_FLOOR_NEW_BLOCKER_COUNT) {
					firstFloorBlockerCount = 0;
					RoadBlockSprite sprite;
					if (random.nextInt(FULL_BLOCK_FREQUCEN) == 3) {
						sprite = RoadBlockSprite.obtainRandom(getBaseContext(),
								runnerSprite.getX(), firstFloorHeight);
					} else {
						sprite = RoadBlockSprite.obtainRandom(getBaseContext(),
								runnerSprite.getX());
					}
					// TODO error
					sprite.setY(firstFloorBottomY - groundTopHeight
							- sprite.getHeight());
					sprites.addFirst(sprite);
					// Log.d(TAG, "new sprite");
				} else {
					firstFloorBlockerCount++;
				}
				if (ENABLE_COIN) {
					if (coinCount > NEW_COIN_COUNT) {
						coinCount = 0;
						CoinSprite sprite = new CoinSprite(getBaseContext(),
								coin);
						sprites.addFirst(sprite);
						// Log.d(TAG, "new coin");
					} else {
						coinCount++;
					}
				}
				for (Sprite sprite : sprites) {
					int point = sprite.getScore();
					if (point > 0) {
						onGetPoint(point);
					}
				}
			}
			Log.d("DrawingThread", "quit");
		}
	}

	private void onGetPoint(int point) {
		currentPoint += point;
		scoreSprite.setCurrentScore(currentPoint);
		// runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// if (!isFinishing()) {
		// soundPool.play(soundIds[SOUND_POINT], 0.5f, 0.5f, 1, 0, 1);
		// }
		// }
		// });
	}

	private void showAndUpdateResultLayer() {

		resultLayer.setBackgroundColor(Color.parseColor("#773460"));
		resultLayer.setVisibility(View.VISIBLE);
		if (currentPoint > bestScore) {
			bestScore = currentPoint;
			App.updateBestScore(type, bestScore);
		}
		bestTV.setText(getString(R.string.best, bestScore));
		resultTV.setText(getString(R.string.flappy_runner_result, currentPoint));
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void onGameOver() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!isFinishing()) {
					soundPool.play(soundIds[SOUND_DIE], 0.5f, 0.5f, 1, 0, 1);
					showAndUpdateResultLayer();
					submitScore();
				}
			}
		});
	}

	private void onHit() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!isFinishing()) {
					soundPool.play(soundIds[SOUND_HIT], 0.5f, 0.5f, 1, 0, 1);
				}
			}
		});
	}

	private void playSwooshing() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!isFinishing()) {
					soundPool.play(soundIds[SOUND_SWOOSHING], 0.5f, 0.5f, 1, 0,
							1);
				}
			}
		});
	}

	private void cleanCanvas(Canvas canvas) {
		canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
	}

	// @Override
	// public void onClick(View v) {
	// switch (currentStatus) {
	// case Sprite.STATUS_NOT_STARTED:
	// currentStatus = Sprite.STATUS_NORMAL;
	// case Sprite.STATUS_NORMAL:
	// runnerSprite.onTap();
	// soundPool.play(soundIds[SOUND_WING], 0.5f, 0.5f, 1, 0, 1);
	// break;
	//
	// default:
	// break;
	// }
	// }

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		int action = event.getAction();

		if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN
				|| (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {

			int pointerIndex = event.getActionIndex();
			// Log.d("jyj", "jyj action, pointerIndex: " + action +", " +
			// pointerIndex);
			switch (currentStatus) {
			case Sprite.STATUS_NOT_STARTED:
				currentStatus = Sprite.STATUS_NORMAL;
			case Sprite.STATUS_NORMAL:
				if (event.getY(pointerIndex) < firstFloorBottomY) {
					if (currentFloor == 1) {
						runnerSprite.onTap();
					} else {
						if(runnerSprite.jumpToY(firstFloorBottomY - groundTopHeight - runnerHeight)){
							currentFloor = 1;
						}
					}
				} else {
					if (currentFloor == 2) {
						runnerSprite.onTap();
					} else {
						if(runnerSprite.jumpToY(secondFloorBottomY - groundHeight - runnerHeight)){
							currentFloor = 2;
						}
					}
				}

				soundPool.play(soundIds[SOUND_WING], 0.5f, 0.5f, 1, 0, 1);
				break;

			default:
				break;
			}

			return true;

		} else {
			return false;
		}
	}

	public void onRestartButtonClick(View view) {
		resultLayer.setVisibility(View.INVISIBLE);
		// surfaceView.setVisibility(View.VISIBLE);
		playSwooshing();
		restart();
	}

	public void onRankButtonClick(View view) {
		Intent intent = new Intent(this, NewRankAcitivity.class);
		intent.putExtra(Constant.TYPE, type);
		startActivity(intent);
	}

	public void onBackButtonClick(View view) {
		playSwooshing();
		finish();
	}

	private void submitScore() {

		if (nickyName == null || currentPoint == 0) {
			return;
		}

		App.submitScore(nickyName, currentPoint + "", type);
	}

}
