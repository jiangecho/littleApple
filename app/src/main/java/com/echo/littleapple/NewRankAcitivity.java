package com.echo.littleapple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.umeng.analytics.MobclickAgent;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class NewRankAcitivity extends Activity {
	private ProgressBar progressBar;
	private TextView networkInfoTextView;

	private RankListAdapter rankAdapter;
	private AwardListAdaper awardAdaper;
	private List<RankItem> rankListItems;
	private List<AwardItem> awardListItems;

	private TextView myLastWeekAwardTextView;
	private TextView myTotalAwardTextView;
	private Button shareButton;
	private ListView lastWeekAwardListView;
	private ListView currentWeekRankListListView;
	private TextView lastWeekNoAwardLisTextView;
	private TextView currentWeekNoRankLisTextView;
	
	private TextView nickynameTextView;

	private TextView newsTextView;

	private ScrollView scrollView;
	private String myNickyName;

	private boolean loadDataSuccess = false;

	/**
	 * $last_week_award = "last_week_award:"; $my_last_week_rank =
	 * "my_last_week_rank:"; $my_current_week_rank = "my_current_week_rank:";
	 * $last_week_award_list = "last_week_award_list:"; $current_week_rank_list
	 * = "current_week_rank_list:";
	 */
	private static final String LAST_WEEK_AWARD_STATUS = "last_week_award:";
	private static final String MY_LAST_WEEK_RANK = "my_last_week_rank:";
	private static final String MY_CURRENT_WEEK_RANK = "my_current_week_rank:";
	private static final String LAST_WEEK_AWARD_LIST = "last_week_award_list:";
	private static final String CURRENT_WEEK_RANK_LIST = "current_week_rank_list:";
	private static final String AWARD_STATUS = "award_status:";
	private static final String AWARD_VALUES = "award_values:";
	private static final String NEWS = "news:";

	private static final int NOT_START = 0;
	private static final int ON_GOING = 1;
	private static final int END = 2;

	private static final String TRUE = "true";

	// 0, not start; 1, on going; 2, end
	private int lastWeekAwardStatus = 0;
	private int myLastWeekRank, myCurrentWeekRank;

	private static final int NO_AWARD = 0;
	private static final int PENDING_ACCEPT_AWARD = 1;
	private static final int ACCEPT_AWARD = 2;
	// private int awardStatus = NO_AWARD;

	private int[] awardValues;
	private int myAward;

	private String news;

	private int type;
	private static final String RANK_INFO_URL = "http://littleappleapp.sinaapp.com/get_rank_info_v_2_7.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_rank_page);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		networkInfoTextView = (TextView) findViewById(R.id.networkInfoTV);
		scrollView = (ScrollView) findViewById(R.id.scrollView);

		myLastWeekAwardTextView = (TextView) findViewById(R.id.my_award_of_last_week);
		myTotalAwardTextView = (TextView) findViewById(R.id.my_total_award);
		shareButton = (Button) findViewById(R.id.sharetButton);
		lastWeekAwardListView = (ListView) findViewById(R.id.last_week_award_list);
		currentWeekRankListListView = (ListView) findViewById(R.id.current_week_rank_list);
		nickynameTextView = (TextView) findViewById(R.id.nickyname_textview);

		lastWeekNoAwardLisTextView = (TextView) findViewById(R.id.last_week_no_award_list);
		currentWeekNoRankLisTextView = (TextView) findViewById(R.id.current_week_no_rank_list);

		newsTextView = (TextView) findViewById(R.id.newsTV);

		scrollView.setVisibility(View.GONE);

		rankListItems = new ArrayList<NewRankAcitivity.RankItem>();
		rankAdapter = new RankListAdapter(this, R.layout.rank_item,
				rankListItems);
		currentWeekRankListListView.setAdapter(rankAdapter);

		awardListItems = new ArrayList<NewRankAcitivity.AwardItem>();
		awardAdaper = new AwardListAdaper(this, R.layout.award_item,
				awardListItems);
		lastWeekAwardListView.setAdapter(awardAdaper);

		myNickyName = getSharedPreferences(getPackageName(),
				Context.MODE_PRIVATE).getString("nickyname", null);
		
		if (myNickyName != null) {
			int index = myNickyName.indexOf("_");
			if (index > 0) {
				nickynameTextView.setText(myNickyName.subSequence(0, index));
			}else {
				nickynameTextView.setText(myNickyName);
			}
			
		}

		type = getIntent().getIntExtra(Constant.TYPE,
				Constant.TYPE_CLASSIC_30S);

		new LoadDataTask().execute();

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private class RankItem {
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

	private class AwardItem {
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

	private class RankListAdapter extends ArrayAdapter<RankItem> {

		private List<RankItem> items;

		public RankListAdapter(Context context, int resource,
				List<RankItem> items) {
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
				view = LayoutInflater.from(NewRankAcitivity.this).inflate(
						R.layout.rank_item, null);
			} else {
				view = convertView;
			}
			rankTextView = (TextView) view.findViewById(R.id.rank);
			nickyNameTextView = (TextView) view
					.findViewById(R.id.nickyNameTextView);
			scoreTextView = (TextView) view.findViewById(R.id.scoreTextView);

			RankItem item = getItem(position);
			rankTextView.setText("" + item.rank);
			nickyNameTextView.setText(item.nickyName);

			if (type == Constant.TYPE_CLASSIC_SPEED) {

				StringBuffer sb = new StringBuffer();
				sb.append(Long.parseLong(item.score) / 1000);
				sb.append(".");
				if ((Long.parseLong(item.score) % 1000) < 100) {
					sb.append("0");
				}
				sb.append((Long.parseLong(item.score) % 1000) / 10);
				scoreTextView.setText(sb.toString());
			} else {
				scoreTextView.setText(item.score);
			}

			// TODO set listview's height
			return view;
		}

	}

	private class AwardListAdaper extends ArrayAdapter<AwardItem> {

		public AwardListAdaper(Context context, int resource,
				List<AwardItem> items) {
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
				view = LayoutInflater.from(NewRankAcitivity.this).inflate(
						R.layout.award_item, null);
			} else {
				view = convertView;
			}
			nickyNameTextView = (TextView) view.findViewById(R.id.nickyname);
			scoreTextView = (TextView) view.findViewById(R.id.score);
			awardTextView = (TextView) view.findViewById(R.id.award);

			AwardItem item = getItem(position);

			nickyNameTextView.setText(item.nickyName);
			// scoreTextView.setText(item.score);
			awardTextView.setText("" + item.award);

			if (type == Constant.TYPE_CLASSIC_SPEED) {

				StringBuffer sb = new StringBuffer();
				sb.append(Long.parseLong(item.score) / 1000);
				sb.append(".");
				if ((Long.parseLong(item.score) % 1000) < 100) {
					sb.append("0");
				}
				sb.append((Long.parseLong(item.score) % 1000) / 10);
				scoreTextView.setText(sb.toString());
			} else {
				scoreTextView.setText(item.score);
			}

			// TODO set listview's height
			return view;
		}

	}

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {

			List<NameValuePair> nameValuePairs = null;
			if (myNickyName != null) {
				nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("nickyname",
						myNickyName));
				nameValuePairs.add(new BasicNameValuePair("type", type + ""));
			}

			String content = Util.httpPost(RANK_INFO_URL, nameValuePairs, null);
			String line, tmp;
			if (content == null) {
				loadDataSuccess = false;
				return null;
			}

			BufferedReader reader = new BufferedReader(
					new StringReader(content));

			String[] items = null;
			String[] itemFields = null;

			String rank;
			String nickyName;
			String score;
			String award;
			// TODO start with award, rank and so to mark the line's meaning;
			try {
				line = reader.readLine();
				while (line != null) {
					if (line.startsWith(NEWS)) {
						news = line.substring(NEWS.length()).trim();
					} else if (line.startsWith(AWARD_VALUES)) {
						tmp = line.substring(AWARD_VALUES.length()).trim();
						itemFields = tmp.split(" ");
						awardValues = new int[itemFields.length];
						for (int i = 0; i < itemFields.length; i++) {
							awardValues[i] = Integer.parseInt(itemFields[i]);
						}
					} else if (line.startsWith(LAST_WEEK_AWARD_STATUS)) {
						tmp = line.substring(LAST_WEEK_AWARD_STATUS.length())
								.trim();
						// TODO need check the return value from the server
						try {
							lastWeekAwardStatus = Integer.parseInt(tmp);
						} catch (Exception e) {
							// TODO: handle exception
						}

					} else if (line.startsWith(MY_LAST_WEEK_RANK)) {
						tmp = line.substring(MY_LAST_WEEK_RANK.length()).trim();
						try {
							myLastWeekRank = Integer.parseInt(tmp);
						} catch (Exception e) {
							myCurrentWeekRank = -1;
						}
					} else if (line.startsWith(MY_CURRENT_WEEK_RANK)) {
						tmp = line.substring(MY_CURRENT_WEEK_RANK.length())
								.trim();
						try {
							myCurrentWeekRank = Integer.parseInt(tmp);
						} catch (Exception e) {
							myCurrentWeekRank = -1;
						}
					} else if (line.startsWith(LAST_WEEK_AWARD_LIST)) {
						tmp = line.substring(LAST_WEEK_AWARD_LIST.length())
								.trim();
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
								if (i < awardValues.length) {
									award = awardValues[i] + "";
								} else {
									award = awardValues[awardValues.length - 1]
											+ "";
								}
								awardListItems.add(new AwardItem(nickyName,
										score, award));
							}
						}

					} else if (line.startsWith(CURRENT_WEEK_RANK_LIST)) {
						tmp = line.substring(CURRENT_WEEK_RANK_LIST.length())
								.trim();
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
								rankListItems.add(new RankItem(rank, nickyName,
										score));

							}
						}
					}
					// else if (line.startsWith(AWARD_STATUS)) {
					// tmp = line.substring(AWARD_STATUS.length());
					// try {
					// awardStatus = Integer.parseInt(tmp);
					// } catch (Exception e) {
					// awardStatus = NO_AWARD;
					// }
					// }
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
			// TODO do more check
			if (loadDataSuccess) {
				if (news != null && news.length() > 0) {
					newsTextView.setText(news);
					newsTextView.setVisibility(View.VISIBLE);
				}

				int lastWeekAward = 0;
				int totalAward = 0;
				String lastAwardWeek = null;
				String awardWeek = null;

				totalAward = App.getInt(Constant.TOTAL_AWARD);
				if (myLastWeekRank >= 0 && myLastWeekRank < awardListItems.size()) {
					if (myLastWeekRank < awardValues.length) {
						lastWeekAward = awardValues[myLastWeekRank];
					}else {
						lastWeekAward = awardValues[awardValues.length - 1];
					}

				} 
				myLastWeekAwardTextView.setText(getString(
						R.string.my_award_of_last_week, lastWeekAward));
				
				if (lastWeekAward > 0) {
					Calendar calendar = Calendar.getInstance();
					int year = calendar.get(Calendar.YEAR);
					int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
					awardWeek = year + " " + weekOfYear;

					lastAwardWeek = App.getString(Constant.LAST_AWARD_WEEK);
					if (lastAwardWeek == null || !lastAwardWeek.equals(awardWeek)) {
						App.putString(Constant.LAST_AWARD_WEEK, awardWeek);
						totalAward += lastWeekAward;
						App.putInt(Constant.TOTAL_AWARD, totalAward);
					}
					
				}

				myTotalAwardTextView.setText(getString(R.string.my_total_award, totalAward));

				if (awardListItems.size() == 0) {
					if (lastWeekAwardStatus == NOT_START) {
						lastWeekNoAwardLisTextView
								.setText(getString(R.string.last_week_no_competition));
					} else {
						lastWeekNoAwardLisTextView
								.setText(getString(R.string.competition_over));
					}
					lastWeekNoAwardLisTextView.setVisibility(View.VISIBLE);
				} else {
					lastWeekNoAwardLisTextView.setVisibility(View.GONE);
				}

				if (rankListItems.size() == 0) {
					currentWeekNoRankLisTextView.setVisibility(View.VISIBLE);
				} else {
					currentWeekNoRankLisTextView.setVisibility(View.GONE);
				}

				ListViewEXUtil
						.setListViewHeightBasedOnChild(currentWeekRankListListView);
				rankAdapter.notifyDataSetChanged();
				ListViewEXUtil
						.setListViewHeightBasedOnChild(lastWeekAwardListView);
				awardAdaper.notifyDataSetChanged();
				scrollView.setVisibility(View.VISIBLE);

				// switch (awardStatus) {
				// case NO_AWARD:
				// acceptAwardButton.setText(getString(R.string.no_award));
				// acceptAwardButton.setEnabled(false);
				// break;
				// case PENDING_ACCEPT_AWARD:
				// //do nothing
				// break;
				// case ACCEPT_AWARD:
				// acceptAwardButton.setText(getString(R.string.have_accept_award));
				// acceptAwardButton.setEnabled(false);
				//
				// default:
				// break;
				// }

			} else {
				networkInfoTextView.setVisibility(View.VISIBLE);

			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	public void onShareButtonClick(View view){
		String imgPath = Util.takeScreenShot(view);
		if (imgPath == null) {
			Toast.makeText(this, "SD卡不存在", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, getString(R.string.capture_screen_ok),
					Toast.LENGTH_SHORT).show();
			showShare(imgPath);
		}
	}

	// TODO more strict
	public boolean isPhoneNumber(String phoneNum) {
		if (phoneNum != null && phoneNum.length() == 11) {
			return true;
		} else {
			return false;
		}

	}

	private void showShare(String imgPath) {
		ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		oks.disableSSOWhenAuthorize();

		oks.setNotification(R.drawable.ic_launcher,
				getString(R.string.app_name));
		oks.setTitle(getString(R.string.share_comment));
		oks.setTitleUrl(Constant.APP_URL);
		oks.setText(getString(R.string.share_title, Constant.APP_URL));
		oks.setImagePath(imgPath);
		oks.setUrl(Constant.APP_URL);
		oks.setComment(getString(R.string.share_comment));
		oks.setSite(getString(R.string.app_name));
		oks.setSiteUrl(Constant.APP_URL);

		oks.show(this);
	}

}
