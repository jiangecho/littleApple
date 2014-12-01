package com.echo.littleapple;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.echo.littleapple.Util.PostResultCallBack;
import com.echo.littleapple.data.AppConfig;
import com.echo.littleapple.data.GsonRequest;
import com.echo.littleapple.data.RequestManager;

public class App extends Application {

	public static boolean showInterstitialAd = true; // when it is xiaomi, disable it;
	public static boolean autoDownloadAd = false;
	public static boolean newVersionAvailable = false;

	// for preference
	private static final String TYPE_PREFIX = "type:";
	
	private static SharedPreferences sharedPreferences;
	
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();

		context = getApplicationContext();
		sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		getOnlineConfig();
		
	}


	private void getOnlineConfig() {

		GsonRequest<AppConfig> request = new GsonRequest<AppConfig>(Constant.CONFIG_URL, AppConfig.class, null, new Response.Listener<AppConfig>() {

					@Override
					public void onResponse(AppConfig response) {
						int currentVersionCode = 0;
						try {
							currentVersionCode = getPackageManager().getPackageInfo(
									getPackageName(), 0).versionCode;
						} catch (NameNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (currentVersionCode <= response.getAutoAdVersion()) {
							autoDownloadAd = true;
						}
						
						// attention the following line only for xiaomi
						if (currentVersionCode <= response.getShowInterstitialAdVersion()) {
							showInterstitialAd = true;
						}
						// end
						
						if (currentVersionCode < response.getVersionCode()) {
							newVersionAvailable = true;
						}

						initAd();

					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

					}
				});
		request.setResponseCharset("UTF-8");
		RequestManager.addRequest(request, this);
	}

	private void initAd() {
		// TODO depend on different ad platform
	}

	public static void submitScore(final String nickyName,
			final String scoreString, final int type,
			final PostResultCallBack callBack) {
		if (nickyName == null || nickyName.trim().equals("")
				|| scoreString == null || scoreString.trim().equals("")) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO the uri should base on the mode
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("nickyname",
						nickyName));
				nameValuePairs
						.add(new BasicNameValuePair("score", scoreString));
				nameValuePairs.add(new BasicNameValuePair("type", type + ""));
				Util.httpPost(Constant.SUBMIT_SCORE_URL, nameValuePairs, callBack);
			}
		}).start();
	}

	public static void submitScore(final String nickyName,
			final String scoreString, final int type) {
		submitScore(nickyName, scoreString, type, null);
	}

	public static long getBestScore(int type){
		long score;
		if (type == Constant.TYPE_CLASSIC_ENDLESS) {
			score = sharedPreferences.getLong(TYPE_PREFIX + type, Long.MAX_VALUE);
		}else {
			score = sharedPreferences.getLong(TYPE_PREFIX + type, 0);
		}
		return score;
	}
	
	// attention: is not backward compatible
	public static void updateBestScore(int type, long score){
		long bestScore = getBestScore(type);
		if (score < bestScore) {
			return;
		}
		sharedPreferences.edit().putLong(TYPE_PREFIX + type, score).commit();
	}
	
	public static void putString(String key, String value){
		sharedPreferences.edit().putString(key, value).commit();
	}
	
	public static String getString(String key){
		return sharedPreferences.getString(key, null);
	}
	
	public static void putInt(String key, int value) {
		sharedPreferences.edit().putInt(key, value).commit();
	}
	
	public static int getInt(String key){
		return sharedPreferences.getInt(key, 0);
	}

	public static Context getContext() {
		return context;
	}
	
}
