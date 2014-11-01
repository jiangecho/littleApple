package com.jucyzhang.flappybatta;

import com.echo.littleapple.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

public class RunnerSprite implements Sprite{
	
	private Drawable runner;
	private final int X;
	private int currentY;
	private int maxY;
	private int minY;
	
	private int runnerHeight;
	private int runnerWidth;
	private int groundHeight;

	private float currentSpeed;

	private final float acceleration;
	private final float tapSpeed;
	
	private int hitPaddingLeft;
	private int hitPaddingRight;
	private int hitPaddingBottom;
	private int hitPaddingTop;
	private int maxJumpHeight;
	
	private boolean isHitted = false;
	
	public RunnerSprite(Context context) {
		int width = ViewUtil.getScreenWidth(context);
		int heith = ViewUtil.getScreenHeight(context);
		
		Resources resource = context.getResources();
		// TODO change the image, it is too ugly.
		runner = resource.getDrawable(R.drawable.img_coin);
		
		runnerHeight = ViewUtil.dipResourceToPx(context, R.dimen.runner_height);
		runnerWidth = runnerHeight * (runner.getIntrinsicWidth() / runner.getIntrinsicHeight());
		
		groundHeight = ViewUtil.dipResourceToPx(context, R.dimen.ground_height);
		maxY = heith - groundHeight;
		currentY = heith - groundHeight - runnerHeight;
		
		maxJumpHeight = ViewUtil.dipResourceToPx(context, R.dimen.runner_max_jump_height);
		minY = heith - groundHeight - runnerHeight - maxJumpHeight;

		int xPosition = ViewUtil.dipResourceToPx(context, R.dimen.bird_position_x);
		X = width / 2 - runnerWidth / 2 - xPosition;
		acceleration = ViewUtil.dipResourceToFloat(context, R.dimen.bird_acceleration);
		tapSpeed = ViewUtil.dipResourceToFloat(context, R.dimen.runner_tap_speed);
		
		hitPaddingBottom = ViewUtil.dipResourceToPx(context, R.dimen.bird_hit_padding_bottom);
		hitPaddingTop = ViewUtil.dipResourceToPx(context, R.dimen.bird_hit_padding_top);
		hitPaddingLeft = ViewUtil.dipResourceToPx(context, R.dimen.bird_hit_padding_left);
		hitPaddingRight = ViewUtil.dipResourceToPx(context, R.dimen.bird_hit_padding_right);
		
		currentSpeed = 0;
	}

	@Override
	public void onDraw(Canvas canvas, Paint globalPaint, int status) {
		if (status != Sprite.STATUS_NOT_STARTED) {
			if (!isHitted) {
				currentY += currentSpeed;
				synchronized (this) {
					currentSpeed += acceleration;
				}
				
			}
		}
		
		if (currentY + runnerHeight > maxY) {
			currentY = maxY - runnerHeight;
		}
		
		if (currentY < minY) {
			currentY = minY;
		}
		
		runner.setBounds(X, currentY, X + runnerWidth, currentY + runnerHeight);
		runner.draw(canvas);
		
	}
	
	public int getHitTop(){
		return currentY + hitPaddingTop;
	}
	
	public int getHitBottom(){
		return currentY + runnerHeight - hitPaddingBottom;
	}
	
	public int getHitLeft(){
		return X + hitPaddingLeft;
	}
	
	public int getHitRight(){
		return X + runnerWidth - hitPaddingRight;
	}
	
	public int getWidth(){
		return runnerWidth;
	}
	
	public int getX(){
		return X;
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public boolean isHit(Sprite sprite) {
		return isHitted;
	}
	
	public void setHitted(boolean isHitted){
		this.isHitted = isHitted;
	}

	@Override
	public int getScore() {
		return 0;
	}
	
	public void onTap(){
		if (currentY == maxY - runnerHeight) {
			synchronized (this) {
				currentSpeed = tapSpeed;
			}
			
		}
	}

	@Override
	public void setY(int y) {
		currentY = y - runnerHeight;
		maxY = y; 
		minY = currentY - maxJumpHeight; 
	}

}
