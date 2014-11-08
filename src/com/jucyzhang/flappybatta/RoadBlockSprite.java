package com.jucyzhang.flappybatta;

import com.echo.littleapple.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class RoadBlockSprite implements Sprite {

	private boolean scored = false;
	private int blockWidth, blockHeight;
	private int width, height;
	private int X;
	private int currentX, currentY;
	private int groundHeight;
	private float speed;

	// X, the x position of the runner sprite
	public static RoadBlockSprite obtainRandom(Context context, int X) {

		return new RoadBlockSprite(context, X);
	}

	private RoadBlockSprite(Context context, int X) {

		groundHeight = ViewUtil.dipResourceToPx(context, R.dimen.ground_height);

		// TODO random width and height
		int defaultHeight = ViewUtil.dipResourceToPx(context,
				R.dimen.block_width);
		blockHeight = defaultHeight - RANDOM.nextInt((int) (defaultHeight / 2));
		blockWidth = defaultHeight / 3 + RANDOM.nextInt(defaultHeight / 2);

		speed = ViewUtil.dipResourceToFloat(context, R.dimen.block_speed);

		width = ViewUtil.getScreenWidth(context);
		height = ViewUtil.getScreenHeight(context);
		currentX = width + RANDOM.nextInt(width / 5);
		currentY = height - groundHeight - blockHeight;
		this.X = X;

	}

	// TODO drawable
	@Override
	public void onDraw(Canvas canvas, Paint globalPaint, int status) {
		if (status == STATUS_NOT_STARTED) {
			return;
		}

		if (status == STATUS_NORMAL) {
			currentX -= speed;
		}

		globalPaint.setColor(Color.BLACK);
		canvas.drawRect(currentX, currentY, currentX + blockWidth, currentY
				+ blockHeight, globalPaint);
	}

	@Override
	public boolean isAlive() {
		return currentX + blockWidth > 0;
	}

	@Override
	public boolean isHit(Sprite sprite) {
		if (sprite instanceof RunnerSprite) {
			RunnerSprite r = (RunnerSprite) sprite;
			// int rTop = r.getHitTop();
			int rBottom = r.getHitBottom();
			int rLeft = r.getHitLeft();
			int rRight = r.getHitRight();

			if ((rBottom > currentY && rBottom <= currentY + blockHeight)
					&& (((rRight > currentX) && (rLeft < currentX))
							|| ((rLeft > currentX) && rRight < currentX
									+ blockWidth) || ((rRight > currentX
							+ blockWidth) && (rLeft < currentX + blockWidth
							- ((RunnerSprite) sprite).getWidth() / 2)))) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}

	@Override
	public int getScore() {
		if (!scored && currentX < X) {
			scored = true;
			return 5;
		} else {
			return 0;
		}
	}

	@Override
	public void setY(int y) {
		currentY = y - blockHeight;

	}

}
