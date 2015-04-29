package anipr.stampitgo.android;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.Session;
import com.facebook.SessionState;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class GetStarted extends FragmentActivity {

	public static String cookie;
	ViewPager mViewPager;
	ImageView firstDot, secondDot, thirdDot;
	private FragmentPageAdapter mFragmentPageAdapter;
	RelativeLayout facebookButton;
	private static final List<String> PERMISSIONS = Arrays.asList("email",
			"user_friends", "user_birthday");
	LinearLayout getStartedBtn;
	private String tag = getClass().getSimpleName();

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(tag, "In GetStarted onStart()");
		getActionBar().hide();
		ComUtility comUtility = new ComUtility(this);
		if (comUtility.getLoginCredentials() == 1) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			Log.d("Login check", "true");

			if (networkInfo != null && networkInfo.isConnected()) {
				try {
					Log.d("MainActivity cookie", AppController.cookie);
					if (AppController.cookie != null
							&& !AppController.cookie
									.equals(AppController.GUEST_USER)) {
						comUtility.getMyStamps();
						comUtility.getRewards();
						comUtility.getNotifications();

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			Intent i = new Intent(this, MainActivity.class);
			// Checking for any extra action

			try {
				Log.i(tag, "Checking for any extra action");
				i.setAction(getIntent().getAction());
			} catch (Exception e) {
			}
			startActivity(i);
			overridePendingTransition(R.anim.right_in, R.anim.left_out);
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.get_started);
		getActionBar().hide();

	}

	protected void onResume() {
		super.onResume();
		firstDot = (ImageView) findViewById(R.id.first_dot);
		secondDot = (ImageView) findViewById(R.id.second_dot);
		thirdDot = (ImageView) findViewById(R.id.third_dot);
		firstDot.setImageResource(R.drawable.dot_active);
		secondDot.setImageResource(R.drawable.dot_inactive);
		thirdDot.setImageResource(R.drawable.dot_inactive);
		mFragmentPageAdapter = new FragmentPageAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mFragmentPageAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				switch (arg0) {
				case 0:
					firstDot.setImageResource(R.drawable.dot_active);
					secondDot.setImageResource(R.drawable.dot_inactive);
					thirdDot.setImageResource(R.drawable.dot_inactive);
					break;
				case 1:
					firstDot.setImageResource(R.drawable.dot_inactive);
					secondDot.setImageResource(R.drawable.dot_active);
					thirdDot.setImageResource(R.drawable.dot_inactive);
					break;

				case 2:
					firstDot.setImageResource(R.drawable.dot_inactive);
					secondDot.setImageResource(R.drawable.dot_inactive);
					thirdDot.setImageResource(R.drawable.dot_active);
					break;
				default:
					break;
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		facebookButton = (RelativeLayout) findViewById(R.id.getstarted_facebook_login_button);
		facebookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (new ComUtility(getApplicationContext())
						.isConnectingToInternet()) {
					Session.openActiveSession(GetStarted.this, true, callback);
				} else {
					Snackbar.with(GetStarted.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(GetStarted.this
									.getString(R.string.internet_error_short))
							.actionLabel("Okay")
							.actionColor(
									GetStarted.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(GetStarted.this);

				}
			}
		});
		getStartedBtn = (LinearLayout) findViewById(R.id.skip_text);
		getStartedBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppController.cookie = AppController.GUEST_USER;
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);

			}
		});
	}

	Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.d("token", session.getAccessToken());

				FbLoginRequest fbReq = new FbLoginRequest(GetStarted.this);
				fbReq.execute(session.getAccessToken());

			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(GetStarted.this,
				requestCode, resultCode, data);

	}

	protected void requestPermissions() {
		Session s = Session.getActiveSession();
		if (s != null)
			s.requestNewPublishPermissions(new Session.NewPermissionsRequest(
					this, PERMISSIONS));
	}

	protected boolean checkPermissions() {

		Session s = Session.getActiveSession();
		if (s != null) {
			return s.getPermissions().containsAll(PERMISSIONS);
		} else
			return false;

	}

	class FragmentPageAdapter extends FragmentPagerAdapter {

		public FragmentPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
			case 0:
				return new Fragment1();
			case 1:
				return new Fragment2();
			case 2:
				return new Fragment3();
			default:
				break;
			}
			return null;

		}

		@Override
		public int getCount() {
			return 3;
		}

	}

	public void openLogin(View v) {
		Intent i = new Intent(getApplicationContext(), Login.class);
		startActivity(i);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	public void openSignup(View v) {
		Intent i = new Intent(getApplicationContext(), SignUp.class);
		startActivity(i);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);

	}
}