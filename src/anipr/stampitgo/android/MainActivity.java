package anipr.stampitgo.android;

import java.util.HashMap;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import anipr.stampitgo.android.AppController.TrackerName;

import com.astuetz.PagerSlidingTabStrip;
import com.astuetz.PagerSlidingTabStrip.IconTabProvider;
import com.example.android.common.activities.SampleActivityBase;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends SampleActivityBase implements OnClickListener {
	private String[] pageTitle = { "My Stamps", "Rewards", "Near By" };
	private ViewPager viewPager;
	private FragmentPageAdapter mFragmentPageAdapter;
	private ComUtility comUtility;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	public static String user_id = "user_id";
	private int noOfTimesCalled;
	private ShowcaseView showcaseView;
	private int counter = 0;
	private int showCount = 0;
	private LayoutDrawBroadcastReciever layoutDrawBroadcastReciever;
	private LocationUpdateReciever locationUpdateReciever;
	int tabPosition;
	private String tag = getClass().getSimpleName();
	public static Location currentLocation;

	@Override
	protected void onStart() {

		getOverflowMenu();
		Log.d("Oncreate called", "MainActivity");
		invalidateOptionsMenu();
		super.onStart();
		getActionBar().setHomeButtonEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("Oncreate called", "MainActivity");
		((AppController) getApplication())
				.getTracker(AppController.TrackerName.APP_TRACKER);
		setContentView(R.layout.activity_main);
		comUtility = new ComUtility(this);

		if (tabPosition != 0) {
			getActionBar().setTitle(pageTitle[0]);
		} else {
			getActionBar().setTitle(pageTitle[tabPosition]);
		}
		// check for logged user
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {

			try {
				new Locator(this).getCurrentLocation();
				// GetLocation locate = new GetLocation(this);
				// Location location = locate.getCurrentLocation();
				// if (location != null) {
				// Log.d(getClass().getSimpleName(), "Nearby Request Fired");
				// Log.d(getClass().getSimpleName(),
				// "Locator calss location "+new
				// Locator(this).getCurrentLocation());
				// comUtility.getNearByStores(location.getLatitude() + ","
				// + location.getLongitude());
				//
				// } else {
				// Log.d(getClass().getSimpleName(),
				// "Near BY request didnt fire " + " NO location");
				// }
				// } catch (JSONException e) {
				// e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			// TODO Uncomment the check
			// comUtility.varifyOfflineScans();

		}
		mFragmentPageAdapter = new FragmentPageAdapter(
				getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		viewPager.setAdapter(mFragmentPageAdapter);
		mPagerSlidingTabStrip.setViewPager(viewPager);
		mPagerSlidingTabStrip.setIndicatorHeight(10);
		mPagerSlidingTabStrip.setIndicatorColorResource(R.color.primary_color);
		mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(
				android.R.color.white));
		mPagerSlidingTabStrip.setUnderlineHeight(3);
		mPagerSlidingTabStrip.setUnderlineColor(getResources().getColor(
				R.color.underline_color));
		mPagerSlidingTabStrip
				.setOnPageChangeListener(new OnPageChangeListener() {

					@Override
					public void onPageSelected(int arg0) {
						getActionBar().setTitle(pageTitle[arg0]);
						tabPosition = arg0;
						Tracker fragmentTracker = AppController.getInstance().getTracker(TrackerName.APP_TRACKER);
						fragmentTracker.setScreenName(pageTitle[arg0]);
						HashMap<String, String> hitParameters = new HashMap<String, String>();
						hitParameters.put("hit_type", "appview");
						hitParameters.put("screen_name", pageTitle[arg0]);

						fragmentTracker.send(hitParameters);
						
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {

					}

					@Override
					public void onPageScrollStateChanged(int arg0) {

					}
				});
		try {
			if (getIntent().getAction().equals(AppController.REWARDINTENT)) {
				Log.i(tag, getIntent().getAction());
				viewPager.setCurrentItem(1, true);
			} else {
				Log.i(tag, "Else Loop" + getIntent().getAction());
			}
		} catch (Exception e) {
			Log.e(tag, "Action Null");
		}
		new GCMUtility(this);
	}

	private void showDemo() {
		ActionItemTarget scanTarget = new ActionItemTarget(this,
				R.id.action_scan);
		showcaseView = new ShowcaseView.Builder(this, true)
				.setTarget(scanTarget).setContentText(R.string.scan_tool_tip)
				.singleShot(42).setOnClickListener(this).build();
		showcaseView.setButtonText("next");

	}

	class FragmentPageAdapter extends FragmentPagerAdapter implements
			IconTabProvider {

		public FragmentPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
			case 0:
				return new StampsFragment();
			case 1:
				return new Rewards();
			case 2:
				return new NearMe();
			default:
				break;
			}
			return null;

		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public int getPageIconResId(int position) {
			switch (position) {
			case 0:
				return R.drawable.ic_icon_mystores_bg;
			case 1:
				return R.drawable.ic_icon_giftbox;
			case 2:
				return R.drawable.ic_icon_nearby;
			}
			return 0;
		}

	}

	@Override
	protected void onResume() {
		layoutDrawBroadcastReciever = new LayoutDrawBroadcastReciever();
		locationUpdateReciever = new LocationUpdateReciever();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.stampitgo.rewards_update");
		filter.addAction("com.stampitgo.near_by_update");
		filter.addAction("com.stampitgo.stampsupdate");
		IntentFilter locaionFilter = new IntentFilter();
		locaionFilter.addAction("com.stampitgo.location_update");
		LocalBroadcastManager.getInstance(this).registerReceiver(
				layoutDrawBroadcastReciever, filter);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				locationUpdateReciever, locaionFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				layoutDrawBroadcastReciever);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				locationUpdateReciever);
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (AppController.cookie != AppController.GUEST_USER) {
			if (noOfTimesCalled % 2 == 0) {
				noOfTimesCalled++;
				Toast.makeText(this, "Press back once more to exit the app",
						1000).show();
			} else {
				finish();
			}
		} else {
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tabPosition", tabPosition);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		tabPosition = savedInstanceState.getInt("tabPosition");
		if (tabPosition != 0) {
			getActionBar().setTitle(pageTitle[0]);
		} else {
			getActionBar().setTitle(pageTitle[tabPosition]);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch (counter) {
		case 0:
			showcaseView.setShowcase(new ViewTarget(
					findViewById(R.id.my_stamps_tab)), true);
			showcaseView.setContentText(getString(R.string.mystamps_tool_tip));
			break;

		case 1:
			showcaseView.setShowcase(new ViewTarget(
					findViewById(R.id.rewards_tab)), true);
			showcaseView.setContentText(getString(R.string.rewards_tool_tip));
			break;
		case 2:
			showcaseView.setShowcase(new ViewTarget(
					findViewById(R.id.near_by_tab)), true);
			showcaseView.setContentText(getString(R.string.nearby_tool_tip));
			break;
		case 3:
			ActionViewTarget overFlowTarget = new ActionViewTarget(this,
					ActionViewTarget.Type.OVERFLOW);
			showcaseView.setShowcase(overFlowTarget, true);
			showcaseView.setContentText(getString(R.string.overflow_tool_tip));
			showcaseView.setButtonText("Finish");
			break;
		case 4:
			showcaseView.hide();

		}
		counter++;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	class LayoutDrawBroadcastReciever extends BroadcastReceiver {
		private String tag = getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(tag, "Broadcast recieved " + showCount);
			if (showCount == 2) {
				showDemo();
			}
			showCount++;
		}
	}

	class LocationUpdateReciever extends BroadcastReceiver {
		private String tag = getClass().getSimpleName() + " MainActivity";

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(tag, "Location Update Recieved " + intent.getExtras());
			try {
				if (AppController.LocationNotificationRequestCount == 0) {
					comUtility.getNearByStores(intent.getExtras().get("lat")
							+ "," + intent.getExtras().get("lng"));
					Log.d(tag, "notificationRequestFired count = "
							+ AppController.LocationNotificationRequestCount);
					AppController.LocationNotificationRequestCount++;

				} else {
					AppController.LocationNotificationRequestCount++;
					Log.d(tag, "notificationRequestCount = "
							+ AppController.LocationNotificationRequestCount);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	protected void onStop() {
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		super.onStop();
	}
	
}
