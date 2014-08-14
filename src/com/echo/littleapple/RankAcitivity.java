package com.echo.littleapple;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RankAcitivity extends Activity{
	private ListView rankListView;
	private TextView rankTextView;
	private ProgressBar progressBar;
	
	private RankItemAdapter rankAdapter;
	private List<RankItem> items;
	private String myNickyName;
	private boolean inRank;
	private RankItem meItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_page);
		rankTextView = (TextView) findViewById(R.id.rankTextView);
		rankListView = (ListView) findViewById(R.id.rankListView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		rankListView.setVisibility(View.GONE);
		items = new ArrayList<RankAcitivity.RankItem>();
		rankAdapter = new RankItemAdapter(this, R.layout.rank_item, items);
		rankListView.setAdapter(rankAdapter);
		
		myNickyName = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
				.getString("nickyname", null);
		
		inRank = false;
		new LoadDataTask().execute();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private class RankItem{
		public int rank;
		public String nickyName;
		public String score;
		public String timeStamp;

		public RankItem(int rank, String nickyName, String score) {
			this.rank = rank;
			this.nickyName = nickyName;
			this.score = score;
		}
		
		
	}
	
	private class RankItemAdapter extends ArrayAdapter<RankItem>{

		private List<RankItem> items;

		public RankItemAdapter(Context context, int resource, List<RankItem> items) {
			super(context, resource, items);
			this.items = items;
		}

		@Override
		public int getCount() {
			return inRank ? items.size() : items.size() + 1;
		}

		@Override
		public RankItem getItem(int position) {
			return super.getItem(position);
		}

		@Override
		public int getPosition(RankItem item) {
			return super.getPosition(item);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView rankTextView, nickyNameTextView, scoreTextView;
			if (convertView == null) {
				view = LayoutInflater.from(RankAcitivity.this).inflate(R.layout.rank_item, null);
			}else {
				view = convertView;
			}
			rankTextView = (TextView) view.findViewById(R.id.rank);
			nickyNameTextView = (TextView) view.findViewById(R.id.nickyNameTextView);
			scoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
			
			if (position < items.size() - 1) {
				RankItem item = getItem(position);
				rankTextView.setText("" + item.rank);
				nickyNameTextView.setText(item.nickyName);
				scoreTextView.setText(item.score);
			}else if (position == items.size() - 1) {
				if (inRank) {
					RankItem item = getItem(position);
					rankTextView.setText("" + item.rank);
					nickyNameTextView.setText(item.nickyName);
					scoreTextView.setText(item.score);
	
				}else {
					rankTextView.setText("");
					nickyNameTextView.setText(".....");
					scoreTextView.setText("");

//						RankItem meItem = getItem(getCount() - 1);
//						rankTextView.setText("" + meItem.rank);
//						nickyNameTextView.setText(meItem.nickyName);
//						scoreTextView.setText(meItem.score);
					
				}
				
			}else {
				// postion == items.size() 
				RankItem item = getItem(items.size() - 1);
				rankTextView.setText("" + item.rank);
				nickyNameTextView.setText(item.nickyName);
				scoreTextView.setText(item.score);

			}

			return view;
		}
		
	}
	
	private class LoadDataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			String uri = "http://littleappleapp.sinaapp.com/rank_str.php";
			List<NameValuePair> nameValuePairs = null;
			if (myNickyName != null) {
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("nickyname", myNickyName));
			}

			String ranks = Util.httpPost(uri, nameValuePairs);
			items.clear();
			ranks = ranks.substring(0, ranks.length() - 1);
			String[] itemStrings = ranks.split(";");
			String[] tmpStrings;
			String nickyName;
			for (int i = 0; i < itemStrings.length - 1; i ++) {
				tmpStrings = itemStrings[i].trim().split(" ");
				if (myNickyName != null && tmpStrings[0].equals(myNickyName)) {
					nickyName = getResources().getString(R.string.me);
					inRank = true;
				}else {
					if (tmpStrings[0].contains("_")) {
						nickyName = tmpStrings[0].substring(0, tmpStrings[0].indexOf("_"));
					}else {
						nickyName = tmpStrings[0];
					}
				}
				items.add(new RankItem(i + 1, nickyName, tmpStrings[1]));
			}
			
			if (myNickyName != null) {
				int myRank = Integer.parseInt(itemStrings[itemStrings.length - 1].trim());
				int myScore = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getInt("BEST_SCORE", 0);
				nickyName = getResources().getString(R.string.me);
				if (inRank) {
					
				}else if(myRank == items.size() + 1){
					inRank = true;
					items.add(new RankItem(myRank, nickyName, myScore + ""));
				}else {
					items.add(new RankItem(myRank, nickyName, myScore + ""));
				}
			}
			
			// my rank
//			if (myNickyName != null) {
//				tmpStrings = itemStrings[itemStrings.length - 1].trim().split(" ");
//				nickyName = getResources().getString(R.string.me);
//				items.add(new RankItem(Integer.parseInt(tmpStrings[0]), nickyName, tmpStrings[2]));
//			}
			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			rankAdapter.notifyDataSetChanged();
			rankListView.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
	}

}
