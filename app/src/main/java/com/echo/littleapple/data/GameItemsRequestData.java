package com.echo.littleapple.data;

import java.util.ArrayList;

public class GameItemsRequestData {
	private int total;
	private int countPerPage;
	private int page;
	private int pageCount;
	private ArrayList<GameItem> gameItems;

	public int getTotal() {
		return total;
	}

	public int getCountPerPage() {
		return countPerPage;
	}

	public int getPage() {
		return page;
	}

	public int getPageCount() {
		return pageCount;
	}

	public ArrayList<GameItem> getGameItems() {
		return gameItems;
	}

	// TODO just for test
//	public GameItemsRequestData() {
//		this.total = 10;
//		this.countPerPage = 10;
//		this.page = 0;
//		this.pageCount = 1;
//		gameItems = new ArrayList<GameItem>();
//		gameItems.add(new GameItem("name", "http://www.baidu.com", "summary", "title", "http://www.baidu.com/download"));
//		gameItems.add(new GameItem("name1", "http://www.baidu.com", "summary", "title", "http://www.baidu.com/download"));
//		gameItems.add(new GameItem("name2", "http://www.baidu.com", "summary", "title", "http://www.baidu.com/download"));
//	}
	
	

}
