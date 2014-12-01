package com.echo.littleapple;

public class Constant {

	public static final String TYPE = "TYPE";
	public static final String TOTAL_AWARD = "TOTAL_AWARD";
	public static final String LAST_AWARD_WEEK = "LAST_AWARD_WEEK";
	public static final String APP_URL = "http://1.littleappleapp.sinaapp.com/littleApple.apk";
	public static final String CONFIG_URL = "http://littleappleapp.sinaapp.com/config/config.json";
	public static final String SUBMIT_SCORE_URL = "http://littleappleapp.sinaapp.com/submit_score.php";
	public static final String GAME_LIST_URL = "http://littleappleapp.sinaapp.com/games/game_list.json";
	// attention: the type value should not be the same!!!!!
	// when add new type, no matter which mode it is,
	// we should add it at the end
	public static final int TYPE_CLASSIC_30S = 0;
	public static final int TYPE_CLASSIC_SPEED = 1;
	public static final int TYPE_CLASSIC_ENDLESS = 2;
	public static final int TYPE_CLASSIC_DISCONTINUOUS = 3;
	public static final int TYPE_CLASSIC_DOUBLE = 4;

	public static final int TYPE_GRAVITY_30S = 5;
	public static final int TYPE_GRAVITY_MINE = 6;
	public static final int TYPE_GRAVITY_ENDLESS = 7;
	public static final int TYPE_GRAVITY_DISCONTINUOUS = 8;
	public static final int TYPE_GRAVITY_DOUBLE = 9;
	// add new type here

	public static final int TYPE_TERIBLE_RELAY = 10;
	public static final int TYPE_TERRIBLE_LOOM = 11;
	public static final int TYPE_TERRIBLE_MOVE = 12;
	public static final int TYPE_TERRIBLE_DOUBLE = 13;
	
	
	public static final int TYPE_FLAPPY_BIRD = 20;
	public static final int TYPE_FLAPPY_RUNNER = 21;
	public static final int TYPE_FLAPPY_RUNNER_DOUBLE = 22;
	public static final int TYPE_FLAPPY_RUNNER_UP_DOWN = 23;
}