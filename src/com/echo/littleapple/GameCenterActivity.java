package com.echo.littleapple;

import java.util.ArrayList;
import java.util.List;






import cn.trinea.android.common.view.DropDownListView;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class GameCenterActivity extends Activity {

	private DropDownListView dropDownListView;
	private GameListAdapter adapter;
	private List<GameItem> games;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_center);

		games = new ArrayList<GameItem>();
		// TODO the following line is just for testing
		games.add(new GameItem("0" , "gameIconUrl", "summary", "title", "download url"));

		dropDownListView = (DropDownListView) findViewById(R.id.game_list);
		adapter = new GameListAdapter(this);
		dropDownListView.setAdapter(adapter);
		dropDownListView.setOnBottomListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO the following lines are just for test
				games.add(new GameItem("0" , "gameIconUrl", "summary", "title", "download url"));
				adapter.notifyDataSetChanged();
				dropDownListView.onBottomComplete();
			}
		});
		
		
		// TODO download the game list.
		// volley?
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	class GameItem{
		String gameName;
		String gameIconUrl;
		String gameSummary;
		String gameTitle;
		String gameDownloadUrl;

		public GameItem(String gameName, String gameIconUrl, String gameSummary, String gameTitle, String gameDownloadUrl) {
			super();
			this.gameName = gameName;
			this.gameIconUrl = gameIconUrl;
			this.gameSummary = gameSummary;
			this.gameTitle = gameTitle;
			this.gameDownloadUrl = gameDownloadUrl;
		}
		
	}
	
	class GameListAdapter extends BaseAdapter{

		private Context context;
		public GameListAdapter(Context context) {
			super();
			this.context = context;
		}

		@Override
		public int getCount() {
			return games.size();
		}

		@Override
		public Object getItem(int position) {
			return games.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Holder holder;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.game_list_item, null);
				holder = new Holder();
				holder.gameIconimImageView = (ImageView) convertView.findViewById(R.id.game_icon);
				holder.gameNameTextView = (TextView) convertView.findViewById(R.id.game_name);
				holder.gameSummaryTextView = (TextView) convertView.findViewById(R.id.game_summary);
				holder.gameTitleTextView = (TextView) convertView.findViewById(R.id.game_title);
				convertView.setTag(holder);
			}else {
				holder = (Holder) convertView.getTag();
			}

			GameItem gameItem = games.get(position);
			// TODO set the game icon
			holder.gameNameTextView.setText(gameItem.gameName);
			holder.gameSummaryTextView.setText(gameItem.gameSummary);
			holder.gameTitleTextView.setText(gameItem.gameTitle);
			
			return convertView;
		}
		
		class Holder{
			ImageView gameIconimImageView;
			TextView gameNameTextView;
			TextView gameSummaryTextView;
			TextView gameTitleTextView;
		}
	}

	
}
