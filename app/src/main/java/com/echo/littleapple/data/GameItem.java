package com.echo.littleapple.data;

public class GameItem {
	String gameName;
	String gameIconUrl;
	String gameSummary;
	String gameTitle;
	String gameDownloadUrl;

	public String getGameName() {
		return gameName;
	}

	public String getGameIconUrl() {
		return gameIconUrl;
	}

	public String getGameSummary() {
		return gameSummary;
	}

	public String getGameTitle() {
		return gameTitle;
	}

	public String getGameDownloadUrl() {
		return gameDownloadUrl;
	}

	public void setGameDownloadUrl(String gameDownloadUrl) {
		this.gameDownloadUrl = gameDownloadUrl;
	}

	// TODO just for test
//	public GameItem(String gameName, String gameIconUrl, String gameSummary,
//			String gameTitle, String gameDownloadUrl) {
//		super();
//		this.gameName = gameName;
//		this.gameIconUrl = gameIconUrl;
//		this.gameSummary = gameSummary;
//		this.gameTitle = gameTitle;
//		this.gameDownloadUrl = gameDownloadUrl;
//	}

}