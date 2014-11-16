package com.jucyzhang.flappybatta;

import com.echo.littleapple.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class RunnerSprite implements Sprite {

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
	private float tapSpeed;
	private float jumpUpSpeed;
	private float jumpDownSpeed;

	private int hitPaddingLeft;
	private int hitPaddingRight;
	private int hitPaddingBottom;
	private int hitPaddingTop;
	private int maxJumpHeight;

	private boolean isHitted = false;
	private boolean isOnGround = true;
	
	private static final int STATE_NORMAL = 0;
	private static final int STATE_JUMP_UP = 1;
	private static final int STATE_JUMP_DOWN = 2;
	private int currentState = STATE_NORMAL;
	private int jumpToY;

	public RunnerSprite(Context context) {
		int width = ViewUtil.getScreenWidth(context);
		int heith = ViewUtil.getScreenHeight(context);

		Resources resource = context.getResources();
		// TODO change the image, it is too ugly.
		runner = resource.getDrawable(R.drawable.img_coin);

		runnerHeight = ViewUtil.dipResourceToPx(context, R.dimen.runner_height);
		runnerWidth = runnerHeight
				* (runner.getIntrinsicWidth() / runner.getIntrinsicHeight());

		groundHeight = ViewUtil.dipResourceToPx(context, R.dimen.ground_height);
		maxY = heith - groundHeight - runnerHeight;
		currentY = heith - groundHeight - runnerHeight;

		maxJumpHeight = ViewUtil.dipResourceToPx(context,
				R.dimen.runner_max_jump_height);
		minY = heith - groundHeight - runnerHeight - maxJumpHeight;

		int xPosition = ViewUtil.dipResourceToPx(context,
				R.dimen.bird_position_x);
		X = width / 2 - runnerWidth / 2 - xPosition;
		acceleration = ViewUtil.dipResourceToFloat(context,
				R.dimen.bird_acceleration);
		tapSpeed = ViewUtil.dipResourceToFloat(context,
				R.dimen.runner_tap_speed);

		hitPaddingBottom = ViewUtil.dipResourceToPx(context,
				R.dimen.bird_hit_padding_bottom);
		hitPaddingTop = ViewUtil.dipResourceToPx(context,
				R.dimen.bird_hit_padding_top);
		hitPaddingLeft = ViewUtil.dipResourceToPx(context,
				R.dimen.bird_hit_padding_left);
		hitPaddingRight = ViewUtil.dipResourceToPx(context,
				R.dimen.bird_hit_padding_right);

		jumpUpSpeed = ViewUtil.dipResourceToFloat(context, R.dimen.runner_jump_up_speed);
		jumpDownSpeed = ViewUtil.dipResourceToFloat(context, R.dimen.runner_jump_down_speed);
		currentSpeed = 0;
	}

	// TODO optimize
	@Override
	public void onDraw(Canvas canvas, Paint globalPaint, int status) {
		if (status != Sprite.STATUS_NOT_STARTED) {
			
			switch (currentState) {
			case STATE_NORMAL:
				if (!isHitted) {
					currentY += currentSpeed;
					//Log.d("jyj", "jyj currentY, speed, minY, maxY, acceleration: " + currentY + " " + currentSpeed + " " + minY + " " + maxY + " " + acceleration);
					currentSpeed += acceleration;
	
				}
				if (currentY > maxY) {
					currentY = maxY;
					isOnGround = true;
				}
		
				if (currentY < minY) {
					currentY = minY;
				}
				
				break;

			case STATE_JUMP_UP:
				if (!isHitted) {
					//Log.d("jyj", "jyj up y, toY: " + currentY + " " + jumpToY);
					if (currentY > jumpToY) {
						currentY += tapSpeed;
					}else {
						currentY = jumpToY;
						isOnGround = true;
						currentState = STATE_NORMAL;
					}
				}
				break;
			case STATE_JUMP_DOWN:
				if (!isHitted) {
					//Log.d("jyj", "jyj down y, toY: " + currentY + " " + jumpToY);
					if (currentY < jumpToY) {
						currentY -= tapSpeed;
					}else {
						currentY = jumpToY;
						isOnGround = true;
						currentState = STATE_NORMAL;
					}
				}
				break;
			}
		}


		runner.setBounds(X, currentY, X + runnerWidth, currentY + runnerHeight);
		runner.draw(canvas);

	}

	public int getHitTop() {
		return currentY + hitPaddingTop;
	}

	public int getHitBottom() {
		return currentY + runnerHeight - hitPaddingBottom;
	}

	public int getHitLeft() {
		return X + hitPaddingLeft;
	}

	public int getHitRight() {
		return X + runnerWidth - hitPaddingRight;
	}

	public int getWidth() {
		return runnerWidth;
	}

	public int getX() {
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

	public void setHitted(boolean isHitted) {
		this.isHitted = isHitted;
	}

	@Override
	public int getScore() {
		return 0;
	}

	public void onTap() {
		if (isOnGround) {
			currentSpeed = tapSpeed;
			isOnGround = false;

		}
	}

	@Override
	public void setY(int y) {
		currentY = y;
		maxY = y;
		minY = currentY - maxJumpHeight - runnerHeight;
	}
	
	public void setSpeed(float speed){
		this.tapSpeed = speed;
	}
	
	public void jumpToY(int y){
		if (isOnGround && (y != currentY)) {
			
			// TODO when the runner is jumping up or down, allow the user to tap
			// if you do not like this feature, please uncomment the following line
			//isOnGround = false;

			jumpToY = y;
			if (currentY > y) {
				currentState = STATE_JUMP_UP;
			}else {
				currentState = STATE_JUMP_DOWN;
			}
			
			maxY = y;
			minY = maxY - maxJumpHeight - runnerHeight;
		}

	}

}
