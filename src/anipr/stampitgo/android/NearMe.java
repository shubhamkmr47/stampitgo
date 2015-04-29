package anipr.stampitgo.android;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import anipr.stampitgo.android.GetData.NearByCard;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.slidingtabsbasic.RestaurantDetails;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class NearMe extends Fragment {

	private static final String tag = "NearMe";
	private ListView nearByList;
	private List<NearByCard> nearByStoreList;
	private NearByListAdapter nearByListAdapter;
	private Activity mContext;
	private ComUtility comUtility;
	private GetData getData;
	private SwipeRefreshLayout mNearBySwipeRefreshLayout;
	private ProgressBar mProgressBar;
	private Spinner nearBYfilter;
	private NearByUpdate mnearMeUpdate;
	private FrameLayout coverFrame;
	private SQLiteDatabase db;
	private int i = 0;
	private LocationManager locationManager;
	private LocationUpdateReciever locationUpdateReciever;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {

			return null;
		}

		View rootView = inflater.inflate(R.layout.nearme_layout, container,
				false);

		mNearBySwipeRefreshLayout = (SwipeRefreshLayout) rootView
				.findViewById(R.id.nearbypageSwipeRefreshLayout);
		mNearBySwipeRefreshLayout.setColorSchemeResources(R.color.dark_red,
				R.color.light_red1, R.color.light_red2, R.color.light_red3);
		nearByList = (ListView) rootView.findViewById(R.id.nearMeList);
		nearBYfilter = (Spinner) rootView.findViewById(R.id.nearby_filter);
		mProgressBar = (ProgressBar) rootView
				.findViewById(R.id.near_me_progressbar);
		coverFrame = (FrameLayout) rootView
				.findViewById(R.id.near_by_coverframe);
		mnearMeUpdate = new NearByUpdate();
		mContext = getActivity();

		nearByList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent i = new Intent(mContext, RestaurantDetails.class);
						i.putExtra(DbHelper.STORECODE,
								nearByStoreList.get(position - 1).storeCode);
						startActivity(i);
						getActivity().overridePendingTransition(
								R.anim.right_in, R.anim.left_out);
					}
				});

		return rootView;

	}

	@Override
	public void onPause() {
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				mnearMeUpdate);
		LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
				locationUpdateReciever);
		super.onPause();
	}

	@Override
	public void onResume() {
		locationManager = (LocationManager) getActivity()
				.getApplicationContext().getSystemService(
						Context.LOCATION_SERVICE);
		db = DbHelper.getInstance(mContext.getApplicationContext())
				.getWritableDatabase();
		coverFrame.setVisibility(View.GONE);
		mContext = getActivity();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.stampitgo.near_by_update");
		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				mnearMeUpdate, filter);
		locationUpdateReciever = new LocationUpdateReciever();
		getData = new GetData(getActivity());
		comUtility = new ComUtility(mContext);
		super.onResume();
		nearBYfilter.setClickable(false);
		if (comUtility.isConnectingToInternet()) {
			if (locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				nearByStoreList = getData
						.getNearByStoresFromDb(AppController.CATEGORIES[0]);
				if (nearByStoreList.size() != 0) {
					nearBYfilter.setClickable(true);
					SpinerApapter spinerAdapter = new SpinerApapter(
							getActivity(), R.layout.filter_spinner_item,
							AppController.CATEGORIES);
					nearBYfilter.setAdapter(spinerAdapter);
					nearBYfilter
							.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
								@Override
								public void onItemSelected(AdapterView<?> arg0,
										View arg1, int arg2, long arg3) {
									int position = nearBYfilter
											.getSelectedItemPosition();
									nearByStoreList = getData
											.getNearByStoresFromDb(AppController.CATEGORIES[position]);
									nearByListAdapter = new NearByListAdapter(
											mContext, nearByStoreList);
									if (nearByList.getAdapter() == null) {
										Log.d("NearBy", "getting data from DB ");
										nearByList.addHeaderView(new View(
												getActivity()));
										nearByList.addFooterView(new View(
												getActivity()));
										nearByList
												.setAdapter(nearByListAdapter);
									} else {
										((NearByListAdapter) ((HeaderViewListAdapter) nearByList
												.getAdapter())
												.getWrappedAdapter()).reload(getData
												.getNearByStoresFromDb(AppController.CATEGORIES[position]));

									}
								}

								@Override
								public void onNothingSelected(
										AdapterView<?> arg0) {
									nearByStoreList = getData
											.getNearByStoresFromDb(AppController.CATEGORIES[0]);
									nearByListAdapter = new NearByListAdapter(
											mContext, nearByStoreList);
									if (nearByList.getAdapter() == null) {
										Log.d("NearBy", "onResume");
										nearByList.addHeaderView(new View(
												getActivity()));
										nearByList.addFooterView(new View(
												getActivity()));
										nearByList
												.setAdapter(nearByListAdapter);
									} else {
										((NearByListAdapter) ((HeaderViewListAdapter) nearByList
												.getAdapter())
												.getWrappedAdapter()).reload(getData
												.getNearByStoresFromDb(AppController.CATEGORIES[0]));
									}
								}
							});
					mProgressBar.setVisibility(View.GONE);

				} else {

					coverFrame.setVisibility(View.VISIBLE);
					TextView errorText = (TextView) mContext
							.findViewById(R.id.near_by_error_msg);
					errorText.setText(getString(R.string.no_nearby_stores));
					ImageButton button = (ImageButton) mContext
							.findViewById(R.id.nearby_frame_button);
					ImageView error_frame_image = (ImageView) getActivity()
							.findViewById(R.id.nearme_error_frame_image);
					error_frame_image
							.setImageResource(R.drawable.ic_default_character);

					button.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							i = 1;
							refreshnearByStores();
							mProgressBar.setVisibility(View.VISIBLE);
						}
					});
					Log.d("list null", "nearbylist");

				}

			} else {
				coverFrame.setVisibility(View.VISIBLE);
				TextView errorText = (TextView) mContext
						.findViewById(R.id.near_by_error_msg);
				errorText.setText("Please turn on your location services");
				ImageButton button = (ImageButton) mContext
						.findViewById(R.id.nearby_frame_button);
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new SweetAlertDialog(getActivity(),
								SweetAlertDialog.WARNING_TYPE)
								.setTitleText("GPS Disabled!")
								.setContentText(
										"Do you want us to open settings to turn on Location Services")
								.setConfirmText("Yes,take me there!")
								.setConfirmClickListener(
										new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(
													SweetAlertDialog sDialog) {
												sDialog.dismissWithAnimation();
												Intent i = new Intent(
														new Intent(
																Settings.ACTION_LOCATION_SOURCE_SETTINGS));
												mContext.startActivity(i);
											}
										})
								.showCancelButton(true)
								.setCancelClickListener(
										new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(
													SweetAlertDialog sDialog) {
												sDialog.cancel();
											}
										}).show();

					}
				});
			}
		} else {
			coverFrame.setVisibility(View.VISIBLE);
			TextView errorText = (TextView) mContext
					.findViewById(R.id.near_by_error_msg);
			errorText.setText(getString(R.string.internet_error));
			ImageButton button = (ImageButton) mContext
					.findViewById(R.id.nearby_frame_button);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (comUtility.isConnectingToInternet()) {
						refresh();
					} else {
						Snackbar.with(NearMe.this.mContext)
								.type(SnackbarType.MULTI_LINE)
								.text(NearMe.this
										.getString(R.string.internet_error_short))
								.actionLabel("Okay")
								.actionColor(
										NearMe.this
												.getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(mContext);

					}
				}
			});
		}

		mNearBySwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);

	}

	protected OnRefreshListener mOnRefreshListener = new OnRefreshListener() {

		@Override
		public void onRefresh() {
			i = 1;
			refreshnearByStores();
		}

	};

	class NearByListAdapter extends BaseAdapter {

		private Context context;
		int position;
		ViewHolder holder;
		private List<NearByCard> listItems;

		public NearByListAdapter(Context context,
				List<NearByCard> nearByStoreList) {

			this.context = context;
			listItems = nearByStoreList;
		}

		public void reload(List<NearByCard> nearByStoresFromDb) {
			listItems.clear();
			listItems.addAll(nearByStoresFromDb);
			notifyDataSetChanged();

		}

		@Override
		public int getCount() {

			return listItems.size();
		}

		@Override
		public Object getItem(int arg0) {

			return listItems.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {

			position = arg0;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.nearme_list_item, null);
				holder = new ViewHolder();
				holder.nearByStoreOffer = (TextView) convertView
						.findViewById(R.id.nearbyStoreName);
				holder.nearByStoreName = (TextView) convertView
						.findViewById(R.id.nearbyStoreOffer);
				holder.locality = (TextView) convertView
						.findViewById(R.id.nearbyStoreLocality);
				holder.nearByStoreDistance = (TextView) convertView
						.findViewById(R.id.nearbyStoreDistance);
				holder.nearByAdd = (ImageView) convertView
						.findViewById(R.id.store_add_button);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			NearByCard currentCard = listItems.get(arg0);

			String query = "select * from " + DbHelper.RESTAURANT_TABLE
					+ " where " + DbHelper.STORECODE + " = "
					+ currentCard.storeCode + " ;";
			Cursor c = db.rawQuery(query, null);
			if (c.moveToFirst()) {
				holder.nearByAdd.setImageResource(R.drawable.added);
			} else {
				holder.nearByAdd.setImageResource(R.drawable.add);
				holder.nearByAdd.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(final View v) {
						// AppController.playSound(getActivity().getApplicationContext(),
						// R.raw.pling);
						int position = nearByList.getPositionForView((View) v
								.getParent());
						((ImageView) v).setImageResource(R.drawable.added);
						CustomParamRequest storeAddRequest = new CustomParamRequest(
								Method.GET,
								ComUtility.ConnectionString
										+ "/new-stamp-card/"
										+ listItems.get(position - 1).storeCode,
								AppController.cookie, null, null,
								new Response.Listener<String>() {

									@Override
									public void onResponse(String arg0) {
										try {
											JSONObject response = new JSONObject(
													arg0);
											if (response.getString("code")
													.equals("1")) {
												Toast.makeText(context,
														"Store Added",
														Toast.LENGTH_SHORT)
														.show();
												new ComUtility(mContext)
														.getMyStamps();
											} else {
												((ImageView) v)
														.setImageResource(R.drawable.add);
												Toast.makeText(
														context,
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
										((ImageView) v)
												.setImageResource(R.drawable.add);
									}
								}, Priority.IMMEDIATE);
						AppController.getInstance().addToRequestQueue(
								storeAddRequest);
					}
				});

			}
			c.close();
			holder.nearByStoreName.setText(currentCard.storeName);
			if (currentCard.locality.length() > 25) {
				holder.locality.setText(""
						+ currentCard.locality.substring(0, 23) + "..");
			} else {
				holder.locality.setText("" + currentCard.locality);
			}
			holder.nearByStoreOffer.setText(currentCard.rewardDetails);
			holder.nearByStoreDistance.setText("" + currentCard.distance
					+ " km");

			return convertView;
		}

		private class ViewHolder {
			TextView nearByStoreOffer, nearByStoreName, nearByStoreDistance,
					locality;
			ImageView nearByAdd;
		}
	}

	private void refresh() {
		if (mNearBySwipeRefreshLayout.isRefreshing()) {
			mNearBySwipeRefreshLayout.setRefreshing(false);
		}
		onResume();
		if (i == 1) {
			Log.i(tag, "NearBy Update Intent Recieved");
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

	protected void refreshnearByStores() {
		if (comUtility.isConnectingToInternet()) {
			// GetLocation locate = new GetLocation(mContext);
			IntentFilter locaionFilter = new IntentFilter();
			locaionFilter.addAction("com.stampitgo.location_update");
			LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
					locationUpdateReciever, locaionFilter);
			new Locator(getActivity());

		} else {
			Toast.makeText(mContext, getString(R.string.internet_error),
					Toast.LENGTH_SHORT).show();
		}
	}

	class NearByUpdate extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			refresh();
			Log.d("NearMe", "Nearby Layout Refresh Request");
		}

	}

	class LocationUpdateReciever extends BroadcastReceiver {
		private String tag = "NearBy " + getClass().getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(tag, "Location Update Recieved " + intent.getExtras());
			try {
				LocalBroadcastManager.getInstance(getActivity())
						.unregisterReceiver(locationUpdateReciever);
				comUtility.getNearByStores(intent.getExtras().get("lat") + ","
						+ intent.getExtras().get("lng"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
}
