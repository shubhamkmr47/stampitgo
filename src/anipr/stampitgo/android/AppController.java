package anipr.stampitgo.android;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crittercism.app.Crittercism;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AppController extends Application {
	public static final String TAG = AppController.class.getSimpleName();
	private RequestQueue mRequestQueue;
	public static String userCode, cookie;
	public static final String LOGO_DIR = "coverDir";
	public static final String COVER_DIR = "logoDIr";
	public static final String FIRST_TIME_LOGIN = "first_time_login";
	private static AppController mInstance;
	public static final String REWARDINTENT = "reward_intent";
	public static String REGULAR_OFFER_INTENT = "open_regular_offer_tab";
	public static String[] CATEGORIES = { "All", "Restaurant", "Club", "Café",
			"Spa & Salon", "Bakery", "Ice Cream" };
	public static int[] CATEGORY_ICONS = { R.drawable.ic_icon_filter,
			R.drawable.ic_restaurant, R.drawable.ic_icon_cocktail,
			R.drawable.ic_coffee, R.drawable.ic_icon_haircut,
			R.drawable.ic_bakery, R.drawable.ic_icecream };
	public static String GUEST_USER = "guestUser";
	public static SharedPreferences myPrefs;
	public static String applicationStart = "applicationStart";
	public static final String MIXPANEL_TOKEN = "dd71b2ee6c47671ce730c1abcc51660c";
	public static int LocationNotificationRequestCount = 0;

	static void playSound(Context context, int resId) {
		MediaPlayer mp = MediaPlayer.create(context, resId);
		mp.start();
		mp.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
			}
		});
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		TypefaceUtils.overrideFont(getApplicationContext(), "SERIF",
				"fonts/RobotoCondensed-Regular.ttf");
		TypefaceUtils.overrideFont(getApplicationContext(), "MONOSPACE",
				"fonts/Roboto-Light.ttf");
		myPrefs = getSharedPreferences("report", MODE_PRIVATE);
		SQLiteDatabase db = DbHelper.getInstance(getApplicationContext())
				.getWritableDatabase();
		db.delete(DbHelper.NEAR_BY_TABLE, null, null);
		LocationNotificationRequestCount = 0;
		db.close();
		Crittercism.initialize(getApplicationContext(),
				"548abf023cf56b9e0457ca60");

	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}

	public RequestQueue getRequestQueue() {

		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}

		return mRequestQueue;
	}

	public <T> void addToRequestQueue(Request<T> req, String tag) {
		// set the default tag if tag is empty
		getRequestQueue().getCache().invalidate(req.getUrl(), true);
		req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
		getRequestQueue().add(req);
	}

	public <T> void addToRequestQueue(Request<T> req) {
		getRequestQueue().getCache().invalidate(req.getUrl(), true);
		req.setTag(TAG);
		getRequestQueue().add(req);
	}

	public void cancelPendingRequests(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}

	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
	}
	
	public HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
					.newTracker(R.xml.app_tracker)
					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
							.newTracker(R.xml.global_tracker) : analytics
							.newTracker(R.xml.ecommerce_tracker);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}
}
