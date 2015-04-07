package com.echo.littleapple;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.view.View;

//TODO please refer to: http://blog.csdn.net/flying_tao/article/details/6553601
public class Util {

	// TODO can be used to submit data or get data from the server
	public static String httpPost(String uri,
			List<NameValuePair> nameValuePairs, PostResultCallBack callBack) {

		HttpPost httpPost = new HttpPost(uri);
		String resultString = null;
		try {
			if (nameValuePairs != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						HTTP.UTF_8));
			}

			HttpResponse httpResponse = new DefaultHttpClient()
					.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(httpResponse.getEntity());
				if (callBack != null) {
					callBack.onSuccess();
				}
			} else {
				if (callBack != null) {
					callBack.onFail();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			if (callBack != null) {
				callBack.onFail();
			}
		}
		return resultString;
	}

	public interface PostResultCallBack {
		public void onSuccess();

		public void onFail();

	}


	public static String takeScreenShot(View view) {
		View rootView = view.getRootView();
		rootView.setDrawingCacheEnabled(true);
		rootView.buildDrawingCache(true);
		Bitmap bitmap = rootView.getDrawingCache(true);
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return null;
		}
		File path = Environment.getExternalStorageDirectory();
		File file = new File(path, "screenshot.png");

		if (file.exists()) {
			file.delete();
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		rootView.destroyDrawingCache();
		return file.getAbsolutePath();
	}



}
