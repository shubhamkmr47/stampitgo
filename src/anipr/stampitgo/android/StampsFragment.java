package anipr.stampitgo.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.slidingtabsbasic.RestaurantDetails;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class StampsFragment extends Fragment {
	private int i = 0;
	private ListView lv;
	private List<StampCard> stampslist;
	private StampsAdapter adapter;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private Activity thisActivity;
	private FrameLayout coverFrame;
	private Spinner spnr;
	private String tag = getClass().getName();
	private MyStampsUpdate myStampsUpdate;
	private ProgressBar pb;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.mystmaps_layout, container,
				false);
		spnr = (Spinner) rootView.findViewById(R.id.stampsCategory);
		lv = (ListView) rootView.findViewById(R.id.storeList);
		coverFrame = (FrameLayout) rootView
				.findViewById(R.id.mystamps_coverframe);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(thisActivity, RestaurantDetails.class);
				i.putExtra(DbHelper.STORECODE,
						stampslist.get(position - 1).code);
				startActivity(i);
				getActivity().overridePendingTransition(R.anim.right_in,
						R.anim.left_out);
			}
		});
		myStampsUpdate = new MyStampsUpdate();
		mSwipeRefreshLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.SwipeRefreshLayout);
		mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.dark_red,
				R.color.light_red1, R.color.light_red2, R.color.light_red3);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		spnr.setClickable(false);
		try {
			
			thisActivity = getActivity();
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.stampitgo.stampsupdate");
			LocalBroadcastManager.getInstance(thisActivity).registerReceiver(
					myStampsUpdate, filter);
			pb = (ProgressBar) thisActivity.findViewById(R.id.my_stamps_pb);
			SpinerApapter spinerAdapter = new SpinerApapter(getActivity(),
					R.layout.filter_spinner_item, AppController.CATEGORIES);
			spnr.setAdapter(spinerAdapter);

			if (!((AppController.cookie).equals(AppController.GUEST_USER))) {

				stampslist = filterStamps(AppController.CATEGORIES[0]);
				if (!stampslist.isEmpty()) {
					coverFrame.setVisibility(View.GONE);
					spnr.setClickable(true);
					spnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							int position = spnr.getSelectedItemPosition();

							stampslist = filterStamps(AppController.CATEGORIES[position]);

							if (lv.getAdapter() == null) {
								adapter = new StampsAdapter(thisActivity,
										stampslist);
								lv.addHeaderView(new View(thisActivity));
								lv.addFooterView(new View(thisActivity));
								lv.setAdapter(adapter);

							} else {
								((StampsAdapter) ((HeaderViewListAdapter) lv
										.getAdapter()).getWrappedAdapter())
										.refresh(stampslist);
							}
							if (stampslist.size() == 0) {
								Log.i(tag,
										"StampsList Empty after filtering category");
								Snackbar.with(
										getActivity().getApplicationContext())
										.type(SnackbarType.MULTI_LINE)
										.text("No stores of this category in your collection ")
										.actionLabel("Okay")
										.actionColor(
												getActivity()
														.getResources()
														.getColor(
																R.color.sb__button_text_color_yellow))
										.duration(
												Snackbar.SnackbarDuration.LENGTH_SHORT)
										.show(getActivity());
							}

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

							stampslist = filterStamps(AppController.CATEGORIES[0]);
							if (lv.getAdapter() == null) {
								adapter = new StampsAdapter(thisActivity,
										stampslist);
								lv.addHeaderView(new View(thisActivity));
								lv.addFooterView(new View(thisActivity));
								lv.setAdapter(adapter);
							} else {
								((StampsAdapter) lv.getAdapter())
										.refresh(stampslist);

							}
						}
					});
					pb.setVisibility(View.GONE);
				} else {
					Log.i(tag, "StampsList Empty");
					pb.setVisibility(View.INVISIBLE);
					ImageView error_frame_image = (ImageView) thisActivity
							.findViewById(R.id.my_stamps_error_frame_image);
					error_frame_image
							.setImageResource(R.drawable.ic_default_character);
					coverFrame.setVisibility(View.VISIBLE);
					TextView errorText = (TextView) thisActivity
							.findViewById(R.id.my_stamps_error_msg);
					errorText.setText(getString(R.string.logged_in_no_stamps));
				}
			} else {
				Log.i(tag, "No logged user");
				pb.setVisibility(View.INVISIBLE);
				coverFrame.setVisibility(View.VISIBLE);
				coverFrame = (FrameLayout) thisActivity
						.findViewById(R.id.mystamps_coverframe);
				ImageView error_frame_image = (ImageView) thisActivity
						.findViewById(R.id.my_stamps_error_frame_image);
				error_frame_image
						.setImageResource(R.drawable.ic_default_character);
				TextView errorText = (TextView) thisActivity
						.findViewById(R.id.my_stamps_error_msg);
				errorText.setText(getString(R.string.guest_user_no_stamps));

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onPause() {
		LocalBroadcastManager.getInstance(thisActivity).unregisterReceiver(
				myStampsUpdate);
		super.onPause();

	};

	List<StampCard> filterStamps(String category) {
		DbHelper dbHelper;
		SQLiteDatabase dbRead;
		List<StampCard> stamplist = new ArrayList<StampCard>();
		dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
		dbRead = dbHelper.getReadableDatabase();
		String query = "";
		if (category.equals(AppController.CATEGORIES[0])) {
			query = "SELECT * FROM " + DbHelper.RESTAURANT_TABLE+" ORDER BY "+DbHelper.INSERT_TIME+" DESC;";
		} else {
			query = "SELECT * FROM " + DbHelper.RESTAURANT_TABLE + " WHERE "
					+ DbHelper.STORE_CATEGORY + " = '" + category + "' ORDER BY "+DbHelper.INSERT_TIME+" DESC;";
		}

		Cursor cursor = dbRead.rawQuery(query, null);
		if (cursor.moveToFirst()) {
			do {

				String restaurant_name = cursor.getString(cursor
						.getColumnIndex(DbHelper.STORENAME));
				String code = cursor.getString(cursor
						.getColumnIndex(DbHelper.STORECODE));
				String address = cursor.getString(cursor
						.getColumnIndex(DbHelper.LOCALITYVALUE));
				String userStamps = cursor.getString(cursor
						.getColumnIndex(DbHelper.USERSTAMPS));
				String storeCategory = cursor.getString(cursor
						.getColumnIndex(DbHelper.STORE_CATEGORY));
				String rewardDetails = cursor.getString(cursor
						.getColumnIndex(DbHelper.REWARDMESSAGE));
				byte[] logo = cursor.getBlob(cursor
						.getColumnIndex(DbHelper.LOGO_URL));
				stamplist.add(new StampCard(restaurant_name, code, address,
						userStamps, storeCategory, rewardDetails, logo));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return stamplist;
	}

	protected OnRefreshListener mOnRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {

			ComUtility c = new ComUtility(thisActivity);
			if (c.isConnectingToInternet()) {
				c.getMyStamps();
				i = 1;
			} else {
				Snackbar.with(getActivity().getApplicationContext())
						// context
						.type(SnackbarType.MULTI_LINE)
						.text(getString(R.string.internet_error_short))
						.actionLabel("Okay")
						.actionColor(
								getActivity().getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(getActivity());

				if (mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(false);
				}
			}
		}
	};

	public class MyStampsUpdate extends BroadcastReceiver {
		String tag = getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mSwipeRefreshLayout.isRefreshing()) {
				mSwipeRefreshLayout.setRefreshing(false);
			}
			onResume();
			if (i == 1) {
				Log.i(tag, "Stamps Update Intent Recieved");
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
				i++;
			} else {
				Log.i(tag, "i value : " + i);
			}

		}

	}

}
