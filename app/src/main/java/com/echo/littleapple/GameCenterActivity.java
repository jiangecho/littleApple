package com.echo.littleapple;

import java.util.ArrayList;
import java.util.List;

import android.widget.*;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.echo.littleapple.data.BitmapLruCache;
import com.echo.littleapple.data.GameItem;
import com.echo.littleapple.data.GameItemsRequestData;
import com.echo.littleapple.data.GsonRequest;
import com.echo.littleapple.data.RequestManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class GameCenterActivity extends Activity {

	private ListView dropDownListView;
	private GameListAdapter adapter;
	
	private ArrayList<GameItem> games;
	private ImageLoader imageLoader;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_center);

		games = new ArrayList<GameItem>();
		imageLoader = RequestManager.getImageLoader();

		dropDownListView = (ListView) findViewById(R.id.game_list);
		adapter = new GameListAdapter(this);
		dropDownListView.setAdapter(adapter);
//		dropDownListView.onBottom();
//		dropDownListView.setOnBottomListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO the following lines are just for test
//				//adapter.notifyDataSetChanged();
//				//dropDownListView.onBottomComplete();
//			}
//		});
		
		
		// TODO download the game list.
		loadData(Constant.GAME_LIST_URL);
		
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
	
	// TODO 
	public void onDownloadButtonClick(View view){
		Button button = (Button) view;
		int position = (Integer) button.getTag();
        Uri uri = Uri.parse(games.get(position).getGameDownloadUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
        Toast toast = Toast.makeText(this,
                        getString(R.string.update_prompt),
                        Toast.LENGTH_SHORT);
        toast.show();
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
				holder.gameIconimImageView = (NetworkImageView) convertView.findViewById(R.id.game_icon);
				holder.gameNameTextView = (TextView) convertView.findViewById(R.id.game_name);
				holder.gameSummaryTextView = (TextView) convertView.findViewById(R.id.game_summary);
				holder.gameTitleTextView = (TextView) convertView.findViewById(R.id.game_title);
				holder.downloadButton = (Button) convertView.findViewById(R.id.download_button);
				convertView.setTag(holder);
			}else {
				holder = (Holder) convertView.getTag();
			}

			GameItem gameItem = games.get(position);
			// TODO set the game icon
			holder.gameIconimImageView.setImageUrl(gameItem.getGameIconUrl(), imageLoader);
			holder.gameNameTextView.setText(gameItem.getGameName());
			holder.gameSummaryTextView.setText(gameItem.getGameSummary());
			holder.gameTitleTextView.setText(gameItem.getGameTitle());
			holder.downloadButton.setTag(position);
			
			return convertView;
		}
		
		class Holder{
			NetworkImageView gameIconimImageView;
			TextView gameNameTextView;
			TextView gameSummaryTextView;
			TextView gameTitleTextView;
			Button downloadButton;
		}
	}
	
	private void loadData(String url){
		GsonRequest<GameItemsRequestData> request = new GsonRequest<GameItemsRequestData>(url, GameItemsRequestData.class, null, new Response.Listener<GameItemsRequestData>() {

			@Override
			public void onResponse(GameItemsRequestData response) {
				// TODO Auto-generated method stub
				// TODO asyntask to update the ui
				List<GameItem> responseGames = response.getGameItems();
				for (GameItem gameItem : responseGames) {
					games.add(gameItem);
				}
				adapter.notifyDataSetChanged();
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				// TODO Auto-generated method stub
				Log.d("JYJ", "error");
			}
		});
		request.setResponseCharset("UTF-8");
		executeRequest(request);
	}
	
	private void executeRequest(Request<?> request){
		RequestManager.addRequest(request, this);
	}

	
}
