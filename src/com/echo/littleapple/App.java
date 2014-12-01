package com.echo.littleapple;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.echo.littleapple.Util.PostResultCallBack;
import com.wandoujia.ads.sdk.Ads;
import com.wandoujia.ads.sdk.loader.Fetcher.AdFormat;
import com.wandoujia.ads.sdk.widget.AppWidget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.echo.littleapple.Util.PostResultCallBack;
import com.echo.littleapple.data.AppConfig;
import com.echo.littleapple.data.GsonRequest;
import com.echo.littleapple.data.RequestManager;

public class App extends Application {

	public static boolean showInterstitialAd = true; // when it is xiaomi,
														// disable it;
	public static boolean autoDownloadAd = false;
	public static boolean newVersionAvailable = false;

	// for preference
	private static final String TYPE_PREFIX = "type:";
	
	private static SharedPreferences sharedPreferences;

	private static Context context;

	private static final String LAST_ENDLESS_DATE = "LAST_ENDLESS_DATE";
	
	// TODO attention: this is the wandoujia ad info, please set change it
	private static final String APPKEY_ID = "100012075";
	private static final String SECRET_KEY = "e74b8f5bbffa9596cafd91c3d124e302";
	public static final String AD_TAG = "d6a209589235286decbc5a9d804eba01";

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
		// Init AdsSdk.
		try {
			Ads.init(this, APPKEY_ID, SECRET_KEY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void showInterstitialAd(Activity activity, final ViewGroup adsWidgetContainer, String adTag) {
		if (!showInterstitialAd) {
			return;
		}
		boolean tmp = Ads.isLoaded(AdFormat.interstitial, adTag);
		if (tmp) {
			adsWidgetContainer.setVisibility(View.VISIBLE);
			AppWidget appWidget = Ads.showAppWidget(activity, null, adTag, Ads.ShowMode.WIDGET,
					new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							adsWidgetContainer.setVisibility(View.GONE);
						}
			});
			if (App.autoDownloadAd) {
				SharedPreferences sharedPreferences = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE);
				long lastEndlessModeMillis = sharedPreferences.getLong(LAST_ENDLESS_DATE, 0);

				//long lastEndlessModeMillis = 0;
				long currentMillis = System.currentTimeMillis();
				long len = currentMillis - lastEndlessModeMillis;
				if (len > 24 * 60 * 60 * 1000) {
					int app_widget_install_button = com.wandoujia.ads.sdk.R.id.app_widget_install_button;
					View view = appWidget.findViewById(app_widget_install_button);
					if (view != null && view instanceof Button) {
						((Button)view).performClick();
						sharedPreferences.edit().putLong(LAST_ENDLESS_DATE, currentMillis).commit();
					}
				}
				
			}
			adsWidgetContainer.addView(appWidget);
		}else {
			Ads.preLoad(activity, AdFormat.interstitial, adTag);
		}
		
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
