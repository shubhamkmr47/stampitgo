package com.example.android.slidingtabsbasic;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import anipr.stampitgo.android.AppController;
import anipr.stampitgo.android.ComUtility;
import anipr.stampitgo.android.CustomParamRequest;
import anipr.stampitgo.android.DbHelper;
import anipr.stampitgo.android.DownloadImagesVolley;
import anipr.stampitgo.android.R;
import anipr.stampitgo.android.ShoutOut;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.example.android.common.activities.SampleActivityBase;
import com.melnykov.fab.FloatingActionButton;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Picasso;

public class RestaurantDetails extends SampleActivityBase {
	public String storecode;
	public final String TAG = getClass().getSimpleName();

	private ViewPager mViewPager;
	private SQLiteDatabase db;
	private ImageView logo, cover, membership;
	private TextView storeName, address, currentStamps, netxMembershipDetails;
	private Bundle bundle;
	private MixpanelAPI mMixpanel;
	public static FloatingActionButton shoutoutFloatingButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMixpanel = MixpanelAPI.getInstance(this, AppController.MIXPANEL_TOKEN);
		setContentView(R.layout.restaurant_page);
		Intent i = getIntent();
		storecode = i.getStringExtra(DbHelper.STORECODE);
		Log.d(TAG, "Deatils of " + storecode);
		bundle = new Bundle();
		bundle.putString("storeCode", storecode);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mViewPager.setAdapter(new SamplePagerAdapter(
				getSupportFragmentManager()));

		PagerSlidingTabStrip mSlidingTabLayout = (PagerSlidingTabStrip) findViewById(R.id.tabs_pager);

		mSlidingTabLayout.setViewPager(mViewPager);
		mSlidingTabLayout.setIndicatorHeight(10);
		mSlidingTabLayout.setIndicatorColorResource(R.color.primary_color);
		mSlidingTabLayout.setBackgroundColor(getResources().getColor(
				android.R.color.white));
		mSlidingTabLayout.setUnderlineHeight(3);
		mSlidingTabLayout.setUnderlineColor(getResources().getColor(
				R.color.underline_color));
		mSlidingTabLayout.setShouldExpand(false);
		cover = (ImageView) findViewById(R.id.store_details_cover);
		logo = (ImageView) findViewById(R.id.store_details_logo);
		membership = (ImageView) findViewById(R.id.membership);
		storeName = (TextView) findViewById(R.id.storeName);
		address = (TextView) findViewById(R.id.address);
		currentStamps = (TextView) findViewById(R.id.current_stamps);
		netxMembershipDetails = (TextView) findViewById(R.id.next_membership_details);
		shoutoutFloatingButton = (FloatingActionButton) findViewById(R.id.shout_out);
		shoutoutFloatingButton.setOnClickListener(shoutOutListener);
		populateStoreDetails(storecode);
		try {
			if (i.getAction().equals(AppController.REGULAR_OFFER_INTENT)) {
				mViewPager.setCurrentItem(1, true);

			}
		} catch (Exception e) {
			Log.d("No aciton", "");
		}
		mSlidingTabLayout.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				shoutoutFloatingButton.show();
			}
		});
		JSONObject storeViewData = new JSONObject();
		try {
			storeViewData.put("storeCode", storecode);
			if (AppController.cookie == AppController.GUEST_USER) {

				storeViewData.put("userID", AppController.GUEST_USER);

			} else {
				storeViewData.put("userID", AppController.userCode);

			}
			Log.i("Mixpanel Logged User", storeViewData.getString("userID"));
			mMixpanel.track("Store View Logged and Guest User ", storeViewData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void populateStoreDetails(String storeCode) {
		db = DbHelper.getInstance(getApplicationContext())
				.getWritableDatabase();
		String query = "select * from " + DbHelper.RESTAURANT_TABLE + " where "
				+ DbHelper.STORECODE + " = " + storeCode + " ;";
		Cursor c = db.rawQuery(query, null);

		if (c.moveToFirst()) {
			File coverFile = new File(this.getApplicationInfo().dataDir
					+ "/app_" + AppController.COVER_DIR, c.getString(c
					.getColumnIndex(DbHelper.STORECODE)));
			if (coverFile.exists()) {
				Picasso.with(this).load(coverFile).into(cover);
			} else {
				Log.e("image downlaod",
						"Downloading image"
								+ c.getString(c
										.getColumnIndex(DbHelper.STORECODE)));
				new DownloadImagesVolley(this, c.getString(c
						.getColumnIndex(DbHelper.COVER_URL)), storeCode,
						AppController.COVER_DIR).makeRequest();

			}
			File logoFile = new File(this.getApplicationInfo().dataDir
					+ "/app_" + AppController.LOGO_DIR, c.getString(c
					.getColumnIndex(DbHelper.STORECODE)));
			if (logoFile.exists()) {
				Picasso.with(this).load(logoFile).into(logo);
			} else {
				Log.e("image downlaod",
						"Downloading image"
								+ c.getString(c
										.getColumnIndex(DbHelper.STORECODE)));
				new DownloadImagesVolley(this, c.getString(c
						.getColumnIndex(DbHelper.LOGO_URL)), storeCode,
						AppController.LOGO_DIR).makeRequest();

			}

			storeName
					.setText(c.getString(c.getColumnIndex(DbHelper.STORENAME)));
			address.setText(c.getString(c.getColumnIndex(DbHelper.ADDRESS)));
			currentStamps.setText(c.getString(c
					.getColumnIndex(DbHelper.USERSTAMPS)));

			setMembership();
		} else {

			membership.setImageResource(R.drawable.add);
			membership.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((ImageView) v).setImageResource(R.drawable.added);
					CustomParamRequest storeAddRequest = new CustomParamRequest(
							Method.GET, ComUtility.ConnectionString
									+ "/new-stamp-card/" + storecode,
							AppController.cookie, null, null,
							new Response.Listener<String>() {

								@Override
								public void onResponse(String arg0) {
									try {
										JSONObject response = new JSONObject(
												arg0);
										if (response.getString("code").equals(
												"1")) {
											membership
													.setImageResource(R.drawable.membership);
											Toast.makeText(
													getApplicationContext(),
													"Store Added",
													Toast.LENGTH_SHORT).show();
											membership.setClickable(false);
											new ComUtility(
													RestaurantDetails.this)
													.getMyStamps();
										} else {
											membership
													.setImageResource(R.drawable.add);
											Toast.makeText(
													getApplicationContext(),
													response.getString("message"),
													500).show();
										}
									} catch (JSONException e) {
										e.printStackTrace();
									}
									;
								}
							}, new Response.ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError arg0) {
									Log.e("Volley Error", arg0.toString());
									membership.setImageResource(R.drawable.add);
								}
							}, Priority.IMMEDIATE);
					AppController.getInstance().addToRequestQueue(
							storeAddRequest);

				}
			});
			currentStamps.setText("0");
			CustomParamRequest cr = new CustomParamRequest(Method.GET,
					ComUtility.ConnectionString + "/stores/" + storeCode,
					AppController.cookie, null, null,
					new Response.Listener<String>() {

						@Override
						public void onResponse(String arg0) {
							try {
								JSONObject jObject = new JSONObject(arg0);
								if (jObject.getString("code").equals("1")) {
									JSONObject dataObj = jObject
											.getJSONObject("data");
									JSONObject storeObj = dataObj
											.getJSONObject("info");
									storeName.setText(storeObj
											.getString("name"));
									address.setText(storeObj.getJSONObject(
											"address").getString("door_no")
											+ storeObj.getJSONObject("address")
													.getString("address_line1")
											+ storeObj.getJSONObject("address")
													.getString("address_line2")
											+ ","
											+ storeObj.getJSONObject("address")
													.getJSONObject("locality")
													.getString("name"));

									File coverFile = new File(
											RestaurantDetails.this
													.getApplicationInfo().dataDir
													+ "/app_"
													+ AppController.COVER_DIR,
											storecode);
									if (coverFile.exists()) {
										Picasso.with(RestaurantDetails.this)
												.load(coverFile).into(cover);
									} else {
										Picasso.with(RestaurantDetails.this)
												.load(storeObj
														.getString("cover_image"))
												.into(cover);
									}
									File logoFile = new File(
											RestaurantDetails.this
													.getApplicationInfo().dataDir
													+ "/app_"
													+ AppController.LOGO_DIR,
											storecode);
									if (logoFile.exists()) {
										Picasso.with(RestaurantDetails.this)
												.load(logoFile).into(logo);
									} else {
										Picasso.with(RestaurantDetails.this)
												.load(storeObj
														.getString("logo"))
												.into(logo);
									}
								} else {

								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError arg0) {

						}
					}, Request.Priority.IMMEDIATE);
			AppController.getInstance().addToRequestQueue(cr);
		}

		c.close();
	}

	private void setMembership() {
		try {
			if (!AppController.cookie.equals(AppController.GUEST_USER)) {
				CustomParamRequest membershipReq = new CustomParamRequest(
						Method.GET, ComUtility.ConnectionString
								+ "/membership/" + storecode,
						AppController.cookie, null, null,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String arg0) {
								JSONObject response;
								try {
									response = new JSONObject(arg0);

									if (response.getString("code").equals("1")) {
										JSONObject data = response
												.getJSONObject("data");
										int membershipInt = Integer.parseInt(data
												.getString("membership"));
										switch (membershipInt) {
										case 1:
											membership
													.setImageResource(R.drawable.ic_icon_membership_silver);
											netxMembershipDetails.setText((Integer
													.parseInt(data
															.getJSONObject(
																	"store")
															.getJSONObject(
																	"membership")
															.getString("gold")) - (Integer.parseInt(data
													.getString("stamp_count"))))
													+ " more to Gold");
											break;
										case 2:
											membership
													.setImageResource(R.drawable.ic_icon_membership_gold);
											netxMembershipDetails.setText((Integer
													.parseInt(data
															.getJSONObject(
																	"store")
															.getJSONObject(
																	"membership")
															.getString(
																	"platinum")) - (Integer.parseInt(data
													.getString("stamp_count"))))
													+ " more to Platinum");
											break;
										case 3:
											membership
													.setImageResource(R.drawable.ic_icon_membership_platinum);
											netxMembershipDetails
													.setText("Platinum Membership");
											break;
										default:
											membership
													.setImageResource(R.drawable.store_add_ui);
											break;
										}
									} else {

									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError arg0) {

							}
						}, Priority.IMMEDIATE);
				AppController.getInstance().addToRequestQueue(membershipReq);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class SamplePagerAdapter extends FragmentPagerAdapter {

		public SamplePagerAdapter(FragmentManager fm) {
			super(fm);

		}

		@Override
		public Fragment getItem(int arg0) {
			switch (arg0) {
			case 0:
				LoyalOffers s = new LoyalOffers();
				s.setArguments(bundle);
				return s;
			case 1:
				RegularOffers f = new RegularOffers();
				f.setArguments(bundle);
				return f;
			case 2:
				StampHistory h = new StampHistory();
				h.setArguments(bundle);
				return h;
			default:
				return new LoyalOffers();
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Rewards";
			case 1:
				return "Offers";
			case 2:
				return "History";
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

	android.view.View.OnClickListener shoutOutListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent(getApplicationContext(), ShoutOut.class);
			i.putExtra(DbHelper.STORECODE, storecode);
			startActivity(i);
		}
	};

	@Override
	protected void onDestroy() {
		mMixpanel.flush();
		super.onDestroy();
	}

}
