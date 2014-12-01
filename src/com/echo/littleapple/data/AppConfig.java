package com.echo.littleapple.data;

public class AppConfig {
	private int showInterstitialAdVersion; // only for xiaomi
	private int autoAdVersion;
	private int versionCode;

	private boolean changeAd;
	public boolean isChangeAd() {
		return changeAd;
	}

	private String appkeyId;;
	private String secretKey;
	private String adTag;

	public int getShowInterstitialAdVersion() {
		return showInterstitialAdVersion;
	}

	public int getAutoAdVersion() {
		return autoAdVersion;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public String getAppkeyId() {
		return appkeyId;
	}

	public String getSecretKey() {
		return secretKey;
	}


	public String getAdTag() {
		return adTag;
	}


	// public AppConfig(int showInterstitialAdVersion, int autoAdVersion,
	// int versionCode) {
	// this.showInterstitialAdVersion = showInterstitialAdVersion;
	// this.autoAdVersion = autoAdVersion;
	// this.versionCode = versionCode;
	// }

}
