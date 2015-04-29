package anipr.stampitgo.android;

import java.io.File;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.squareup.picasso.Picasso;

public class Rewards extends Fragment {
	private GridView rewardsGrid;
	private Context mContext;
	private Cursor adapterCursor;
	private SwipeRefreshLayout mrewardpageSwipeRefreshLayout;
	private RewardAdapter mRewardAdapter;
	private SQLiteDatabase db;
	private FrameLayout coverFrame;
	private String tag = getClass().getSimpleName();
	private String[] args = { DbHelper.REWARDSTORECODE, DbHelper.REWARDSTORE,
			DbHelper.REWARDID, DbHelper.REWARDNAME, DbHelper.REWARDDETAILS,
			DbHelper.EXPIRESON, DbHelper.REWARDSTATE, DbHelper.USED_ON };
	private RewardsUpdate mRewardsUpdate;
	private int i = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.rewards, container, false);
		rewardsGrid = (GridView) rootView.findViewById(R.id.rewardsGrid);
		coverFrame = (FrameLayout) rootView
				.findViewById(R.id.rewards_coverframe);
		mrewardpageSwipeRefreshLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.rewardpageSwipeRefreshLayout);
		mrewardpageSwipeRefreshLayout
				.setOnRefreshListener(mrewardsPageOnRefreshListener);
		mrewardpageSwipeRefreshLayout.setColorSchemeResources(R.color.dark_red,
				R.color.light_red1, R.color.light_red2, R.color.light_red3);

		mRewardsUpdate = new RewardsUpdate();

		return rootView;

	}

	void drawRewardPage() {
		db = DbHelper.getInstance(getActivity().getApplicationContext())
				.getWritableDatabase();
		adapterCursor = db.query(DbHelper.REWARDS_TABLE, args, null, null,
				null, null, DbHelper.REWARDSTATE + "," + DbHelper.EXPIRESON
						+ " Desc," + DbHelper.INSERT_TIME + " Desc");
		coverFrame.setVisibility(View.GONE);
		if (AppController.cookie != null) {
			if (!((AppController.cookie).equals(AppController.GUEST_USER))) {
				mRewardAdapter = new RewardAdapter(getActivity(), adapterCursor);
				rewardsGrid.setAdapter(mRewardAdapter);
				if (adapterCursor.moveToFirst()) {
					rewardsGrid
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									Intent i = new Intent(mContext,
											RedeemReward.class);
									Cursor rewardCursor = adapterCursor;
									if (null != rewardCursor
											&& rewardCursor
													.moveToPosition(position)) {
										i.putExtra(
												DbHelper.REWARDSTORE,
												rewardCursor.getString(rewardCursor
														.getColumnIndex(DbHelper.REWARDSTORE)));
										i.putExtra(
												DbHelper.REWARDDETAILS,
												rewardCursor.getString(rewardCursor
														.getColumnIndex(DbHelper.REWARDDETAILS)));
										i.putExtra(
												DbHelper.REWARDSTATE,
												rewardCursor.getString(rewardCursor
														.getColumnIndex(DbHelper.REWARDSTATE)));
										i.putExtra(
												DbHelper.USED_ON,
												rewardCursor.getString(rewardCursor
														.getColumnIndex(DbHelper.USED_ON)));
										i.putExtra(
												DbHelper.REWARDID,
												rewardCursor.getString(rewardCursor
														.getColumnIndex(DbHelper.REWARDID)));
										rewardCursor.close();
										mContext.startActivity(i);
										getActivity()
												.overridePendingTransition(
														R.anim.right_in,
														R.anim.left_out);
									}
								}
							});
				} else {
					Log.d("Rewards", "No Rewards");
					coverFrame.setVisibility(View.VISIBLE);
					coverFrame = (FrameLayout) getActivity().findViewById(
							R.id.rewards_coverframe);
					TextView errorText = (TextView) getActivity().findViewById(
							R.id.rewards_error_msg);
					errorText.setText(getString(R.string.logged_in_no_rewards));
					ImageButton button = (ImageButton) getActivity()
							.findViewById(R.id.rewards_frame_button);
					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							i = 1;
							refresh();
						}
					});
				}
			} else {
				Log.d("Rewards", "No logged user");
				coverFrame.setVisibility(View.VISIBLE);
				coverFrame = (FrameLayout) getActivity().findViewById(
						R.id.rewards_coverframe);
				TextView errorText = (TextView) getActivity().findViewById(
						R.id.rewards_error_msg);
				errorText.setText("Login to check Rewards");
			}
			if (mrewardpageSwipeRefreshLayout.isRefreshing()) {
				mrewardpageSwipeRefreshLayout.setRefreshing(false);
			}

		} else {
			AppController.cookie = AppController.GUEST_USER;
		}
	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mRewardsUpdate);
		super.onPause();
	}

	@Override
	public void onResume() {
		try {
			db = DbHelper.getInstance(getActivity().getApplicationContext())
					.getWritableDatabase();
			Log.i("Rewards", "DB Reopened");
			super.onResume();
			adapterCursor = db.query(DbHelper.REWARDS_TABLE, args, null, null,
					null, null, DbHelper.REWARDSTATE + "," + DbHelper.EXPIRESON
							+ " Desc," + DbHelper.INSERT_TIME + " Desc");

			mContext = getActivity();
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.stampitgo.rewards_update");
			LocalBroadcastManager.getInstance(mContext).registerReceiver(
					mRewardsUpdate, filter);
			drawRewardPage();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected OnRefreshListener mrewardsPageOnRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			ComUtility c = new ComUtility(getActivity());
			if (c.isConnectingToInternet()) {
				c.getRewards();
				i = 1;
			} else {
				Snackbar.with(getActivity().getApplicationContext())
						// context
						.type(SnackbarType.MULTI_LINE)
						.text("Could not Refresh.Internet Connection not found")
						.actionLabel("Okay")
						.actionColor(
								getActivity().getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(getActivity());

				if (mrewardpageSwipeRefreshLayout.isRefreshing()) {
					mrewardpageSwipeRefreshLayout.setRefreshing(false);
				}
			}
		}
	};

	public class RewardAdapter extends BaseAdapter {

		private final int VIEW_TYPE_USED = 0;
		private final int VIEW_TYPE_ACTIVE = 1;
		private Cursor rewardCursor;

		public RewardAdapter(Context context, Cursor c) {
			rewardCursor = c;
		}

		@Override
		public int getItemViewType(int position) {
			db = DbHelper.getInstance(getActivity().getApplicationContext())
					.getWritableDatabase();
			if (rewardCursor.moveToPosition(position)) {
				if (rewardCursor.getString(
						rewardCursor.getColumnIndex(DbHelper.REWARDSTATE))
						.equals("true")
						|| rewardCursor.getString(
								rewardCursor
										.getColumnIndex(DbHelper.REWARDSTATE))
								.equals(RedeemReward.EXPIRED)) {
					return VIEW_TYPE_USED;
				} else {
					return VIEW_TYPE_ACTIVE;
				}
			} else {
				return VIEW_TYPE_USED;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		private class ViewHolder {
			ImageView logo;
			TextView storeName, rewardMessage, expiryDate;
		}

		private void refresh() {
			rewardCursor = db.query(DbHelper.REWARDS_TABLE, args, null, null,
					null, null, DbHelper.REWARDSTATE + "," + DbHelper.EXPIRESON
							+ " Desc," + DbHelper.INSERT_TIME + " Desc");
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return rewardCursor.getCount();
		}

		@Override
		public Object getItem(int position) {
			return rewardCursor.moveToPosition(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			if (convertView == null) {
				int viewType = getItemViewType(rewardCursor.getPosition());
				if (viewType == VIEW_TYPE_ACTIVE) {
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.reward_list_item, parent, false);
				} else {
					convertView = LayoutInflater.from(mContext).inflate(
							R.layout.reward_list_item_used, parent, false);
				}
				viewHolder = new ViewHolder();
				viewHolder.logo = (ImageView) convertView
						.findViewById(R.id.rewardListStoreLogo);
				viewHolder.storeName = (TextView) convertView
						.findViewById(R.id.rewardListRestaurantLabel);
				viewHolder.expiryDate = (TextView) convertView
						.findViewById(R.id.expieryDay);
				viewHolder.rewardMessage = (TextView) convertView
						.findViewById(R.id.rewardListRewardMsg);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			try {
				String storeCode = rewardCursor.getString(rewardCursor
						.getColumnIndex(DbHelper.REWARDSTORECODE));
				String storeName = rewardCursor.getString(rewardCursor
						.getColumnIndex(DbHelper.REWARDSTORE));
				String rewardName = rewardCursor.getString(rewardCursor
						.getColumnIndex(DbHelper.REWARDNAME));
				String expiryDate = ""
						+ rewardCursor.getString(rewardCursor
								.getColumnIndex(DbHelper.EXPIRESON));
				String rewardState = rewardCursor.getString(rewardCursor
						.getColumnIndex(DbHelper.REWARDSTATE));
				String usedOn = rewardCursor.getString(rewardCursor
						.getColumnIndex(DbHelper.USED_ON));
				File logoFile = new File(mContext.getApplicationInfo().dataDir
						+ "/app_" + AppController.LOGO_DIR, storeCode);
				if (logoFile.exists()) {
					Picasso.with(mContext).load(logoFile).into(viewHolder.logo);
				} else {
					String query = "select " + DbHelper.LOGO_URL + " from "
							+ DbHelper.RESTAURANT_TABLE + " where "
							+ DbHelper.STORECODE + " = ? ;";
					Cursor c = db.rawQuery(query, new String[] {});
					if (c.moveToFirst()) {
						Log.e("image downlaod", "Downloading image" + storeName);
						new DownloadImagesVolley(mContext, c.getString(c
								.getColumnIndex(DbHelper.LOGO_URL)), storeCode,
								AppController.LOGO_DIR).makeRequest();
					}
					c.close();

				}
				float i = getResources().getDisplayMetrics().density;
				if (i <= 1.5) { // HDPI
					if (storeName.length() > 16) {
						String sName = storeName.substring(0, 13) + "...";
						storeName = sName;
					}
					if (rewardName.length() > 13) {
						String rName = rewardName.substring(0, 10) + "..";
						rewardName = rName;
					}

				} else {// XHdpi,XXHdpi
					if (storeName.length() > 20) {
						String sName = storeName.substring(0, 17) + "...";
						storeName = sName;
					}
					if (rewardName.length() > 17) {
						String rName = rewardName.substring(0, 14) + "..";
						rewardName = rName;
					}

				}

				viewHolder.storeName.setText(storeName);
				viewHolder.rewardMessage.setText(rewardName);
				Calendar expirydate = Calendar.getInstance();
				try {

					expirydate.setTime(new DateUtility()
							.makeDateFromLocalDateString(usedOn));

				} catch (Exception e) {
				}
				viewHolder.expiryDate.setVisibility(View.VISIBLE);
				TextView useNowText = (TextView) convertView
						.findViewById(R.id.useButton);
				if (rewardState.equals("true")
						|| rewardState.equals(RedeemReward.REDEEMING)) {
					String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May",
							"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

					if (rewardState.equals(RedeemReward.REDEEMING)) {
						if (((expirydate.getTimeInMillis()) + (30 * 60 * 1000))
								- (System.currentTimeMillis()) > 0) {
							viewHolder.expiryDate
									.setText(String.valueOf((((expirydate
											.getTimeInMillis()) + (30 * 60 * 1000)) - System
											.currentTimeMillis())
											/ (1000 * 60))
											+ " mins left");
						} else {
							ContentValues cv = new ContentValues();
							cv.put(DbHelper.REWARDSTATE, "true");
							db.update(
									DbHelper.REWARDS_TABLE,
									cv,
									DbHelper.REWARDID + " = ?",
									new String[] { rewardCursor.getString(rewardCursor
											.getColumnIndex(DbHelper.REWARDID)) });

							refresh();
						}

					} else {
						viewHolder.expiryDate
								.setText(" On "
										+ expirydate.get(Calendar.DAY_OF_MONTH)
										+ " "
										+ monthNames[(Integer.parseInt(String
												.valueOf(expirydate
														.get(Calendar.MONTH))))]
												.substring(0, 3));
						// useNowText.setText("USED");
					}
				} else if (rewardState.equals(RedeemReward.EXPIRED)) {
					viewHolder.expiryDate.setText(new DateUtility()
							.makeExpiryDate(new DateUtility()
									.makeDateFromLocalDateString(expiryDate)));
					useNowText.setText("EXPIRED");
				} else {
					if (expiryDate.equals("null")) {
						viewHolder.expiryDate.setText(" ");
					} else {
						Log.i("promo offer", expiryDate);
						Calendar expirayDateCalInstance = Calendar
								.getInstance();
						expirayDateCalInstance.setTime(new DateUtility()
								.makeDateFromLocalDateString(expiryDate));
						if (Calendar.getInstance().getTimeInMillis() > expirayDateCalInstance
								.getTimeInMillis()) {
							ContentValues cv = new ContentValues();
							cv.put(DbHelper.REWARDSTATE, RedeemReward.EXPIRED);
							db.update(
									DbHelper.REWARDS_TABLE,
									cv,
									DbHelper.REWARDID + " = ?",
									new String[] { rewardCursor.getString(rewardCursor
											.getColumnIndex(DbHelper.REWARDID)) });

							refresh();
						}

						viewHolder.expiryDate
								.setText(new DateUtility().makeExpiryDate(new DateUtility()
										.makeDateFromLocalDateString(expiryDate)));
					}
				}

			} catch (Exception e) {
				Log.e("Reward Cursor Error", e.toString());
				e.printStackTrace();

			}
			return convertView;
		}
	}

	public class RewardsUpdate extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
		}

	}

	void refresh() {
		mRewardAdapter.refresh();
		if (mrewardpageSwipeRefreshLayout.isRefreshing()) {
			mrewardpageSwipeRefreshLayout.setRefreshing(false);
		}
		drawRewardPage();
		if (i == 1) {
			adapterCursor = db.query(DbHelper.REWARDS_TABLE, args, null, null,
					null, null, DbHelper.REWARDSTATE + "," + DbHelper.EXPIRESON
							+ " Desc," + DbHelper.INSERT_TIME + " Desc");
			Log.i(tag, "Reward Update Intent Recieved");
			if (adapterCursor.getCount() == 0) {
				Snackbar.with(getActivity().getApplicationContext())
						// context
						.type(SnackbarType.MULTI_LINE)
						.text("Still no rewards!!!")
						.actionLabel("Okay")
						.actionColor(
								getActivity().getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(getActivity());
			} else {
				Snackbar.with(getActivity().getApplicationContext())
						// context
						.type(SnackbarType.MULTI_LINE)
						.text("Updated")
						.actionLabel("Okay")
						.actionColor(
								getActivity().getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(getActivity());
			}
			i++;
		} else {
			Log.i(tag, "i value : " + i);
		}

	}

}

class Reward {
	String storeCode, StoreName, rewardName, rewardMessage, expieryDate,
			rewardState, expieryTime, rewardId;

	Reward(String storeCode, String StoreName, String rewardId,
			String rewardName, String rewardMessage, String expieryDate,
			String rewardState, String expieryTime) {
		this.rewardName = rewardName;
		this.storeCode = storeCode;
		this.StoreName = StoreName;
		this.rewardMessage = rewardMessage;
		this.expieryDate = expieryDate;
		this.rewardState = rewardState;
		this.expieryTime = expieryTime;
		this.rewardId = rewardId;
	}

}