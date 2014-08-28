package com.echo.littleapple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
	private TextView lastWeekNoAwardLisTextView;
	private TextView currentWeekNoRankLisTextView;
	
	private ScrollView scrollView;
	private String myNickyName;
	
	private boolean loadDataSuccess = false;
	
	/**
	$last_week_award = "last_week_award:";
	$my_last_week_rank = "my_last_week_rank:";
	$my_current_week_rank = "my_current_week_rank:";
	$last_week_award_list = "last_week_award_list:";
	$current_week_rank_list = "current_week_rank_list:";
	*/
	private static final String LAST_WEEK_AWARD_STATUS = "last_week_award:";
	private static final String MY_LAST_WEEK_RANK = "my_last_week_rank:";
	private static final String MY_CURRENT_WEEK_RANK = "my_current_week_rank:";
	private static final String LAST_WEEK_AWARD_LIST = "last_week_award_list:";
	private static final String CURRENT_WEEK_RANK_LIST = "current_week_rank_list:";
	private static final String AWARD_STATUS = "award_status:";
	private static final String AWARD_VALUES = "award_values:";
	
	private static final int NOT_START = 0;
	private static final int ON_GOING = 1;
	private static final int END = 2;
	
	private static final String TRUE = "true";
	
	//0, not start; 1, on going; 2, end
	private int lastWeekAwardStatus = 0;
	private int myLastWeekRank, myCurrentWeekRank;

	private static final int NO_AWARD = 0;
	private static final int PENDING_ACCEPT_AWARD = 1;
	private static final int ACCEPT_AWARD = 2;
	//private int awardStatus = NO_AWARD;
	
	private int[] awardValues;
	private int myAward;


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
		
		lastWeekNoAwardLisTextView = (TextView) findViewById(R.id.last_week_no_award_list);
		currentWeekNoRankLisTextView = (TextView) findViewById(R.id.current_week_no_rank_list);

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
		public boolean isEnabled(int position) {
			return false;
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
		public boolean isEnabled(int position) {
			return false;
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
			String line, tmp;
			if (content == null) {
				loadDataSuccess = false;
				return null;
			}

			BufferedReader reader = new BufferedReader(new StringReader(content));

			String[] items = null;
			String[] itemFields = null;
                        
			String rank;
			String nickyName;
			String score;
			String award;
			// TODO start with award, rank and so to mark the line's meaning;
			try {
				line = reader.readLine();
				while(line != null){
					if (line.startsWith(AWARD_VALUES)) {
						tmp = line.substring(AWARD_VALUES.length()).trim();
						itemFields = tmp.split(" ");
						awardValues = new int[3];
						for (int i = 0; i < 3; i++) {
							awardValues[i] = Integer.parseInt(itemFields[i]);
						}
					}else if(line.startsWith(LAST_WEEK_AWARD_STATUS)){
						tmp = line.substring(LAST_WEEK_AWARD_STATUS.length()).trim();
						//TODO need check the return value from the server
						try {
							lastWeekAwardStatus = Integer.parseInt(tmp);
						} catch (Exception e) {
							// TODO: handle exception
						}

					}else if(line.startsWith(MY_LAST_WEEK_RANK)){
						tmp = line.substring(MY_LAST_WEEK_RANK.length()).trim();
						try {
							myLastWeekRank = Integer.parseInt(tmp);
						} catch (Exception e) {
							myCurrentWeekRank = -1;
						}
					}else if (line.startsWith(MY_CURRENT_WEEK_RANK)) {
						tmp = line.substring(MY_CURRENT_WEEK_RANK.length()).trim();
						try {
							myCurrentWeekRank = Integer.parseInt(tmp);
						} catch (Exception e) {
							myCurrentWeekRank = -1;
						}
					}else if (line.startsWith(LAST_WEEK_AWARD_LIST)) {
						tmp = line.substring(LAST_WEEK_AWARD_LIST.length()).trim();
						items = tmp.split(";");
						itemFields = null;
						// the last one is empty
						int index;
						for (int i = 0; i < items.length; i++) {
							itemFields = items[i].trim().split(" ");
							if (itemFields != null && itemFields.length == 2) {
								nickyName = itemFields[0];
								index = nickyName.indexOf("_");
								if (index > 0) {
									nickyName = nickyName.substring(0, index);
								}
								score = itemFields[1];
								award = awardValues[i] + "";
								awardListItems.add(new AwardItem(nickyName, score, award));
							}
						}
						
					}else if(line.startsWith(CURRENT_WEEK_RANK_LIST)){
						tmp = line.substring(CURRENT_WEEK_RANK_LIST.length()).trim();
						items = tmp.split(";");
						itemFields = null;
						int index;

                        for (int i = 0; i < items.length; i++) {
							itemFields = items[i].trim().split(" ");
							if (itemFields != null && itemFields.length == 2) {
								rank = (i + 1) + "";
								nickyName = itemFields[0];
								score = itemFields[1];
								
								index = nickyName.indexOf("_");
								if (index > 0) {
									nickyName = nickyName.substring(0, index);
								}
								rankListItems.add(new RankItem(rank, nickyName, score));

							}
						}
					}
//					else if (line.startsWith(AWARD_STATUS)) {
//						tmp = line.substring(AWARD_STATUS.length());
//						try {
//							awardStatus = Integer.parseInt(tmp);
//						} catch (Exception e) {
//							awardStatus = NO_AWARD;
//						}
//					}
					line = reader.readLine();
				}
				reader.close();
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
			//TODO do more check
			if (loadDataSuccess) {
				if (lastWeekAwardStatus == ON_GOING) {
					if (myLastWeekRank != -1 && myLastWeekRank  <= awardListItems.size()) {
						acceptAwardButton.setEnabled(true);
					}else {
						acceptAwardButton.setText(getString(R.string.no_award));
						acceptAwardButton.setEnabled(false);
					}
				}else {
					acceptAwardButton.setText(getString(R.string.no_award));
					acceptAwardButton.setEnabled(false);

				}
				if (myCurrentWeekRank >= 0) {
					myCurrentWeekRankTextView.setText(getString(R.string.my_rank_of_current_week, myCurrentWeekRank));
				}else {
					myCurrentWeekRankTextView.setText(getString(R.string.current_week_no_rank));
				}

				if (myLastWeekRank >= 0) {
					myLastWeekRankTextView.setText(getString(R.string.my_rank_of_last_week, myLastWeekRank));
				}else {
					myLastWeekRankTextView.setText(getString(R.string.last_week_no_rank));
				}

				if (awardListItems.size() == 0) {
					if (lastWeekAwardStatus == NOT_START) {
						lastWeekNoAwardLisTextView.setText(getString(R.string.last_week_no_competition));
					}else {
						lastWeekNoAwardLisTextView.setText(getString(R.string.competition_over));
					}
					lastWeekNoAwardLisTextView.setVisibility(View.VISIBLE);
				}else {
					lastWeekNoAwardLisTextView.setVisibility(View.GONE);
				}

				if (rankListItems.size() == 0) {
					currentWeekNoRankLisTextView.setVisibility(View.VISIBLE);
				}else {
					currentWeekNoRankLisTextView.setVisibility(View.GONE);
				}

				ListViewEXUtil.setListViewHeightBasedOnChild(currentWeekRankListListView);
				rankAdapter.notifyDataSetChanged();
				ListViewEXUtil.setListViewHeightBasedOnChild(lastWeekAwardListView);
				awardAdaper.notifyDataSetChanged();
				scrollView.setVisibility(View.VISIBLE);
				
//				switch (awardStatus) {
//				case NO_AWARD:
//					acceptAwardButton.setText(getString(R.string.no_award));
//					acceptAwardButton.setEnabled(false);
//					break;
//				case PENDING_ACCEPT_AWARD:
//					//do nothing
//					break;
//				case ACCEPT_AWARD:
//					acceptAwardButton.setText(getString(R.string.have_accept_award));
//					acceptAwardButton.setEnabled(false);
//
//				default:
//					break;
//				}
				
			}else {
				networkInfoTextView.setVisibility(View.VISIBLE);
				
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
	}
	
	
	public void onAcceptAwardButtonClick(View v){
        	LayoutInflater layoutInflater = LayoutInflater.from(this);
        	final View view = layoutInflater.inflate(R.layout.accept_award_dialog, null);
        	int award = 0;
        	if (lastWeekAwardStatus == ON_GOING) {
        		switch (myLastWeekRank) {
				case 1:
					award = 50;
					break;
				case 2:
					award = 30;
					break;
				case 3:
					award = 20;
					break;

				default:
					break;
				}
				
			}
        	AlertDialog dialog = new AlertDialog.Builder(this)
        		.setTitle(getString(R.string.award_value, award))
        		.setView(view)
        		.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						EditText editText = (EditText) view.findViewById(R.id.editText);
						final String phoneNum = editText.getText().toString().trim();
						if (isPhoneNumber(phoneNum)) {
							new Thread(new Runnable() {
								
								@Override
								public void run() {
	                                String uri = "http://littleappleapp.sinaapp.com/accept_award.php";
	                                List<NameValuePair> nameValuePairs = null;
	                                if (myNickyName != null) {
	                                        nameValuePairs = new ArrayList<NameValuePair>();
	                                        nameValuePairs.add(new BasicNameValuePair("nickyname", myNickyName));
	                                        nameValuePairs.add(new BasicNameValuePair("phone_number", phoneNum));
	                                        nameValuePairs.add(new BasicNameValuePair("award", awardValues[myLastWeekRank - 1] + ""));
	                                }
	
	                                String content = Util.httpPost(uri, nameValuePairs, null);
	                                if (content == null) {
	                                        return ;
	                                }
										
									}
								}).start();
							acceptAwardButton.setText(getString(R.string.have_accept_award));
							Toast.makeText(NewRankAcitivity.this, getString(R.string.accept_award_success), Toast.LENGTH_LONG).show();
							
						}else {
							Toast.makeText(NewRankAcitivity.this, getString(R.string.invalid_phone_number), Toast.LENGTH_LONG)
								.show();;
						}

					}
				})
				.create();
        	dialog.show();
	}
	
	//TODO more strict
	public boolean isPhoneNumber(String phoneNum) {
		if (phoneNum != null && phoneNum.length() == 11) {
			return true;
		}else {
			return false;
		}
		
	}

}
