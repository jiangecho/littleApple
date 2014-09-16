package com.echo.littleapple;

import java.util.Random;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	
	private static final int COLUMN = 4;
	private static final int OK = 0;
	private static final int FAIL = 1;
	public static final int TIME_OUT = 2;
	private int row;
	private int cellWidth;
	private int cellHeight;
	private int firstCellHeight;
	
	private int width, height;
	
	private Paint linePaint;
	private Paint applePaint;
	
	private int[][] apples = null;
	private Random random;
	private Rect rect;
	private Bitmap bitmapApple;
	private Bitmap bitmapError;
	
	private GameEventListner listner;
	private int status;
	private static final int STATUS_STOP = 0;
	private static final int STATUS_START = 1;
	private static final int STATUS_FAIL = 2;
	
	private int score;
	
	private int moveStepHeight;
	private int moveYOffset = 0;
	private Handler handler;
	private Handler animationHandler;
	private HandlerThread animationHandlerThread;
	private AnimationTask animationTask;
	
	private SoundPool soundPool;
	private int[] sounds;
	private float audioMaxVolumn;
	private float audioCurrentVolumn;
	private float volumnRatio;
	private Context context;
	
	private HandlerThread soundPoolThread;
	private Handler soundPoolHandler;

	int left, top, right, bottom;
	
	private Canvas canvas;
	private SurfaceHolder holder;
	
	//TODO cell type

	public GameSurfaceView(Context context) {
		this(context, null);
		
	}
	

	public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	public GameSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		
		
		applePaint = new Paint(linePaint);
		
		bitmapApple = BitmapFactory.decodeResource(context.getResources(), R.drawable.apple);
		bitmapError = BitmapFactory.decodeResource(context.getResources(), R.drawable.error);
		rect = new Rect();
		
		random = new Random();
		status = STATUS_STOP;
		
		score = 0;

		handler = new Handler();
		animationHandlerThread = new HandlerThread("animation");
		animationHandlerThread.start();
		animationHandler = new Handler(animationHandlerThread.getLooper());
		
		this.context = context;
		// init sound play
		initSoundPool();
		
		holder = getHolder();
		holder.addCallback(this);
		
		animationTask = new AnimationTask();
	}


	private void doDraw() {
		int i, j;
		
			
		canvas = holder.lockCanvas();

		synchronized (holder) {
			canvas.drawColor(Color.WHITE);
			//draw the first horizontal lines, maybe hidden
			canvas.drawLine(0, moveYOffset + firstCellHeight - cellHeight, width, moveYOffset + firstCellHeight - cellHeight, linePaint);
			//draw horizontal lines
			for (i = 0; i < row ; i++) {
				canvas.drawLine(0, moveYOffset + firstCellHeight + cellHeight * i, width, moveYOffset + firstCellHeight + cellHeight * i, linePaint);
				//canvas.drawLine(0,  firstCellHeight + cellHeight * i, width, firstCellHeight + cellHeight * i, linePaint);
			}
			
			//draw vertical lines
			for (i = 0; i < COLUMN ; i++) {
				canvas.drawLine(cellWidth * i, 0, cellWidth * i, height, linePaint);
			}
	
			// draw applse
			for (i = 0; i < row; i++) {
				for (j = 0; j < COLUMN; j++) {
					if (apples[i][j] == 0) {
						// do nothing
					}else if(apples[i][j] == 1){
						// draw apples
						//left = (j >= 1 ) ? (j - 1) * cellWidth  + cellWidth : 0;
						left = j * cellWidth;
						top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
						right = (j + 1) * cellWidth;
						bottom = moveYOffset + firstCellHeight + i * cellHeight;
						//rect.set(left, top, right, bottom);
						rect.set(left, top, right, bottom);
						canvas.drawBitmap(bitmapApple, null, rect, applePaint);
						
					}else {
						left = j * cellWidth;
						top = moveYOffset + ((i >= 1) ? (firstCellHeight + (i - 1) * cellHeight) : (firstCellHeight - cellHeight)); 
						right = (j + 1) * cellWidth;
						bottom = moveYOffset + firstCellHeight + i * cellHeight;
						//rect.set(left, top, right, bottom);
						rect.set(left, top, right, bottom);
						canvas.drawBitmap(bitmapError, null, rect, applePaint);
						
					}
				}
				
			}
		
		}
		holder.unlockCanvasAndPost(canvas);
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		width = MeasureSpec.getSize(widthMeasureSpec);
		cellWidth = width / COLUMN;
		cellHeight = cellWidth;
		
		height = MeasureSpec.getSize(heightMeasureSpec);
		firstCellHeight = height % cellHeight;
		row  = height / cellHeight;
		
		if (firstCellHeight > 0) {
			row  += 1;
		}
		
		moveStepHeight = cellHeight / 10;
		
		if (apples == null) {
			apples = new int[row][COLUMN];
			randomApples();
		}


	}
	
	public void reset(){
		this.score = 0;
		status = STATUS_STOP;
		randomApples();
		moveYOffset = 0;
		doDraw();
	}
	
	public void recover(){
		status = STATUS_STOP;
		moveYOffset = 0;

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < COLUMN; j++) {
				if (apples[i][j] == 3) {
					apples[i][j] = 0;
				}
			}
		}

		doDraw();
	}
	
	private void randomApples(){
		int columnIndex;
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < COLUMN; j++) {
				apples[i][j] = 0;
			}
		}

		for (int i = 0; i < row - 1; i++) {
			columnIndex = random.nextInt(COLUMN);
			apples[i][columnIndex] = 1;
		}
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//return super.onTouchEvent(event);
		if (status == STATUS_FAIL) {
			return false;
		}
		
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
			return false;
		}else {
			int x = (int) event.getX();
			int y = (int) event.getY();
			
			if (y < height - 2 * cellHeight
					|| y > height - cellHeight) {
				// wrong, do not have any effect
				return false;
			}

			int x_index = x / cellWidth;

			// if move animation is still on going, do more check
			// TODO refactor
			if (moveYOffset > 0 && apples[row -3][x_index] != 1) {
				apples[row - 3][x_index] = 3;
				playGameSoundEffect(FAIL);
				status = STATUS_FAIL;
				doDraw();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (listner != null) {
							listner.onGameOver();
					
						}
						
					}
				}, 300);
				return true;
			}

			//game over
			if (apples[row - 2][x_index] != 1) {
				apples[row - 2][x_index] = 3;
				playGameSoundEffect(FAIL);
				status = STATUS_FAIL;
				doDraw();
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (listner != null) {
							listner.onGameOver();
					
						}
						
					}
				}, 300);
				
				
			} else {
				score ++;
				playGameSoundEffect(OK);
				if (status == STATUS_STOP) {
					status = STATUS_START;
					if (listner != null) {
						listner.onGameStart();
						listner.onScoreUpdate(score);
					}
				}else {
					listner.onScoreUpdate(score);
				}
				
				// move down
				startMoveAnimation();
			}

			// right, move
			// wrong, game over
			return true;
		}
		
	}


	public void setGameEventListener(GameEventListner listner){
		this.listner = listner;
	}
	
	private void startMoveAnimation(){

		animationHandler.post(animationTask);
	}
	
	private void initSoundPool(){
		soundPoolThread = new HandlerThread("test");
		soundPoolThread.start();
		soundPoolHandler = new Handler(soundPoolThread.getLooper(), null);
		soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		
		sounds = new int[3];
		sounds[0] = soundPool.load(context, R.raw.ok, 1);
		sounds[1] = soundPool.load(context, R.raw.fail, 1);
		sounds[2] = soundPool.load(context, R.raw.time_out, 1);

		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		
		volumnRatio = audioCurrentVolumn / audioMaxVolumn;
	}
	
	
	public void playGameSoundEffect(final int type){
		//soundPool.pla
		soundPoolHandler.post(new Runnable() {
			
			@Override
			public void run() {
				soundPool.play(sounds[type], volumnRatio, volumnRatio, 1, 0, 1);
				
			}
		});

	}
	
	
	public interface GameEventListner{
		public void onGameOver();
		public void onGameStart();
		public void onScoreUpdate(int score);
	}
	
	public int getScore(){
		return this.score;
	}
	
	public void setScore(int score){
		this.score = score;
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		doDraw();
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	private class AnimationTask implements Runnable{

		@Override
		public void run() {
			if (moveYOffset < cellHeight) {
				moveYOffset += moveStepHeight;
				doDraw();
				animationHandler.postDelayed(animationTask, 4);
			}else {
				moveYOffset = 0;
				for (int i = row - 2; i > 0; i--) {
					for (int j = 0; j < COLUMN; j++) {
						apples[i][j] = apples[i - 1][j]; 
						apples[i - 1][j] = 0;
					}
				}
				int x_index = random.nextInt(COLUMN);
				apples[0][x_index] = 1;
				doDraw();
			}
		}
		
	}
}
