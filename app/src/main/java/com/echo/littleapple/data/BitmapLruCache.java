
package com.echo.littleapple.data;

import com.android.volley.toolbox.ImageLoader;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Created by Issac on 7/19/13.
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

	@SuppressLint("NewApi")
	@Override
    protected int sizeOf(String key, Bitmap bitmap) {
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
    		return bitmap.getByteCount();
		}else {
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}
