package com.echo.littleapple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class NewRankAcitivity extends Activity{
	private ProgressBar progressBar;
	private TextView networkInfoTextView;
	
	private RankListAdapter rankAdapter;
	private AwardListAdaper awardAdaper;
	private List<RankItem> rankListItems;
	private List<AwardItem> awardListItems;
	
	private TextView myLastWeekRankTextView;
	private Button acceptAwardButton;
	private TextView myCurrentWeekRankTextView;
	private ListView lastWeekAwardListView;
	private ListView currentWeekRankListListView;
	
	private ScrollView scrollView;
	private String myNickyName;
	
	private boolean loadDataSuccess = false;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_rank_page);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		networkInfoTextView = (TextView) findViewById(R.id.networkInfoTV);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		
		myLastWeekRankTextView = (TextView) findViewById(R.id.my_rank_of_last_week);
		myCurrentWeekRankTextView = (TextView) findViewById(R.id.my_rank_of_current_week);
		acceptAwardButton = (Button) findViewById(R.id.accept_award);
		lastWeekAwardListView = (ListView) findViewById(R.id.last_week_award_list);
		currentWeekRankListListView = (ListView) findViewById(R.id.current_week_rank_list);

		scrollView.setVisibility(View.GONE);

		rankListItems = new ArrayList<NewRankAcitivity.RankItem>();
		rankAdapter = new RankListAdapter(this, R.layout.rank_item, rankListItems);
		currentWeekRankListListView.setAdapter(rankAdapter);
		
		awardListItems = new ArrayList<NewRankAcitivity.AwardItem>();
		awardAdaper = new AwardListAdaper(this, R.layout.award_item, awardListItems);
		lastWeekAwardListView.setAdapter(awardAdaper);
		
		myNickyName = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE)
				.getString("nickyname", null);
		
		new LoadDataTask().execute();
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private class RankItem{
		public String rank;
		public String nickyName;
		public String score;
		public String timeStamp;

		public RankItem(String rank, String nickyName, String score) {
			this.rank = rank;
			this.nickyName = nickyName;
			this.score = score;
		}
		
	}

	private class AwardItem{
		public String nickyName;
		public String score;
		public String award;

		public AwardItem(String nickyName, String score, String award) {
			super();
			this.nickyName = nickyName;
			this.score = score;
			this.award = award;
		}

	}
	
	private class RankListAdapter extends ArrayAdapter<RankItem>{

		private List<RankItem> items;

		public RankListAdapter(Context context, int resource, List<RankItem> items) {
			super(context, resource, items);
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public RankItem getItem(int position) {
			return super.getItem(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			TextView rankTextView, nickyNameTextView, scoreTextView;
			if (convertView == null) {
				view = LayoutInflater.from(NewRankAcitivity.this).inflate(R.layout.rank_item, null);
			}else {
				view = convertView;
			}
			rankTextView = (TextView) view.findViewById(R.id.rank);
			nickyNameTextView = (TextView) view.findViewById(R.id.nickyNameTextView);
			scoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
			
			RankItem item = getItem(position);
			rankTextView.setText("" + item.rank);
			nickyNameTextView.setText(item.nickyName);
			scoreTextView.setText(item.score);

			//TODO set listview's height
			return view;
		}
		
	}
	
	private class AwardListAdaper extends ArrayAdapter<AwardItem>{

		public AwardListAdaper(Context context, int resource, List<AwardItem> items) {
			super(context, resource, items);
		}

		@Override
		public int getCount() {
			return super.getCount();
		}

		@Override
		public AwardItem getItem(int position) {
			return super.getItem(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view;
			TextView nickyNameTextView, scoreTextView, awardTextView;
			if (convertView == null) {
				view = LayoutInflater.from(NewRankAcitivity.this).inflate(R.layout.award_item, null);
			}else {
				view = convertView;
			}
			nickyNameTextView = (TextView) view.findViewById(R.id.nickyname);
			scoreTextView = (TextView) view.findViewById(R.id.score);
			awardTextView = (TextView) view.findViewById(R.id.award);
			
			AwardItem item = getItem(position);

			nickyNameTextView.setText(item.nickyName);
			scoreTextView.setText(item.score);
			awardTextView.setText("" + item.award);

			//TODO set listview's height
			return view;
		}
		
		
	}
	
	private class LoadDataTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			String uri = "http://littleappleapp.sinaapp.com/new_rank_str.php";
			List<NameValuePair> nameValuePairs = null;
			if (myNickyName != null) {
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("nickyname", myNickyName));
			}

			String content = Util.httpPost(uri, nameValuePairs, null);
			String line;
			BufferedReader reader = new BufferedReader(new StringReader(content));

			// TODO start with award, rank and so to mark the line's meaning;
			try {
				line = reader.readLine();
				while(line != null){
					line = reader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
				loadDataSuccess = false;
			}
			
			loadDataSuccess = true;

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressBar.setVisibility(View.GONE);
			//TODO 
			if (loadDataSuccess) {
				rankAdapter.notifyDataSetChanged();
				scrollView.setVisibility(View.VISIBLE);
			}else {
				networkInfoTextView.setVisibility(View.VISIBLE);
				
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
	}

}
