package com.echo.littleapple.data;

public class AppConfig {
	private int showInterstitialAdVersion; // only for xiaomi
	private int autoAdVersion;
	private int versionCode;
	private int showGameCenterVersion;

	public int getShowInterstitialAdVersion() {
		return showInterstitialAdVersion;
	}

	public int getAutoAdVersion() {
		return autoAdVersion;
	}

	public int getVersionCode() {
		return versionCode;
	}
	
	public int getShowGameCenterVersion(){
		return showGameCenterVersion;
	}

//	public AppConfig(int showInterstitialAdVersion, int autoAdVersion,
//			int versionCode) {
//		this.showInterstitialAdVersion = showInterstitialAdVersion;
//		this.autoAdVersion = autoAdVersion;
//		this.versionCode = versionCode;
//	}

}
