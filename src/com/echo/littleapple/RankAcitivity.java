package com.echo.littleapple;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RankAcitivity extends Activity{
	private ListView rankListView;
	private TextView rankTextView;
	private ProgressBar progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_page);
		rankTextView = (TextView) findViewById(R.id.rankTextView);
		rankListView = (ListView) findViewById(R.id.rankListView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
