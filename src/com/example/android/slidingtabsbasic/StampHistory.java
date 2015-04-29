package com.example.android.slidingtabsbasic;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import anipr.stampitgo.android.AppController;
import anipr.stampitgo.android.ComUtility;
import anipr.stampitgo.android.CustomParamRequest;
import anipr.stampitgo.android.DateUtility;
import anipr.stampitgo.android.R;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class StampHistory extends Fragment {

	private HistoryAdapter historyAdapter;
	String TAG = "STAMP HISTORY";
	ListView historyList;
	private String code;
	ProgressBar stampHistoryPb;
	List<History> historyData;
	private FrameLayout coverFrame;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.stamp_history,
				container, false);
		stampHistoryPb = (ProgressBar) rootView
				.findViewById(R.id.stamp_history_pb);
		code = getArguments().getString("storeCode");
		historyList = (ListView) rootView
				.findViewById(R.id.stamp_history_list);
		RestaurantDetails.shoutoutFloatingButton.attachToListView(historyList);
		historyData = new ArrayList<History>();
		if (new ComUtility(getActivity()).isConnectingToInternet()) {
			if (!((AppController.cookie).equals(AppController.GUEST_USER))) {
				CustomParamRequest historyRequest = new CustomParamRequest(
						Method.GET, ComUtility.ConnectionString
								+ "/user-history/" + code,
						AppController.cookie, null, null,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String arg0) {
								try {
									JSONObject response = new JSONObject(arg0);
									if (response.getString("code").equals("1")) {
										stampHistoryPb.setVisibility(View.GONE);
										JSONArray dataArray = response
												.getJSONArray("data");
										if (dataArray.length() != 0) {
											for (int i = dataArray.length() - 1; i >= 0; i--) {
												String count = dataArray
														.getJSONObject(i)
														.getString(
																"stamp_count");
												DateUtility dateUtils = new DateUtility();
												Date stampDate = Calendar.getInstance().getTime();
												try {
													stampDate = dateUtils
															.convertSerevrDatetoLocalDate(dataArray
																	.getJSONObject(
																			i)
																	.getString(
																			"createdAt"));
												} catch (ParseException e) {
													e.printStackTrace();
												}
												String date = dataArray
														.getJSONObject(i)
														.getString("createdAt")
														.substring(0, 10);
												Calendar localtime = Calendar
														.getInstance();
												localtime.setTime(stampDate);
												String am_pm;
												if (localtime
														.get(Calendar.AM_PM) == 0)
													am_pm = "AM";
												else
													am_pm = "PM";
												String min;
												if (localtime
														.get(Calendar.MINUTE) < 10) {
													min = "0"
															+ localtime
																	.get(Calendar.MINUTE);
												} else {
													min = ""
															+ localtime
																	.get(Calendar.MINUTE);
												}
												String time = String.valueOf(localtime
														.get(Calendar.HOUR))
														+ ":"
														+ min
														+ " "
														+ am_pm;
												historyData.add(new History(
														count, date, time));
											}
											historyAdapter = new HistoryAdapter(
													getActivity(), historyData);
											historyList
													.setAdapter(historyAdapter);
										} else {
											Log.d(TAG, "No history");
											stampHistoryPb
													.setVisibility(View.GONE);

											coverFrame = (FrameLayout) rootView
													.findViewById(R.id.stamps_history_coverframe);
											coverFrame
													.setVisibility(View.VISIBLE);
											TextView errorText = (TextView) rootView
													.findViewById(R.id.stamps_history_error_msg);
											errorText
													.setText(getString(R.string.stamp_history_null));
										}
									} else {
										Log.e(TAG,
												response.getString("message"));
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError arg0) {
								Log.e(TAG, arg0.toString());
							}
						}, Priority.IMMEDIATE);
				AppController.getInstance().addToRequestQueue(historyRequest);
			} else {
				Log.d(TAG, "No logged user");
				stampHistoryPb.setVisibility(View.GONE);

				coverFrame = (FrameLayout) rootView
						.findViewById(R.id.stamps_history_coverframe);
				coverFrame.setVisibility(View.VISIBLE);
				TextView errorText = (TextView) rootView
						.findViewById(R.id.stamps_history_error_msg);
				errorText.setText(getString(R.string.guest_user_no_stamps));
			}

		} else {
			Log.d(TAG, "No Internet");
			stampHistoryPb.setVisibility(View.GONE);

			coverFrame = (FrameLayout) rootView
					.findViewById(R.id.stamps_history_coverframe);
			coverFrame.setVisibility(View.VISIBLE);
			TextView errorText = (TextView) rootView
					.findViewById(R.id.stamps_history_error_msg);
			errorText.setText(getString(R.string.internet_error));
			// internet error
		}

		return rootView;

	}
class HistoryAdapter extends BaseAdapter {
		Context context;
		List<History> historyData;
		History currentHistory;

		public HistoryAdapter(Context c, List<History> historyData) {
			this.context = c;
			this.historyData = historyData;
		}

		@Override
		public int getCount() {
			return historyData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.stamp_history_list_item, null);

				holder = new ViewHolder();
				holder.stampCount = (TextView) convertView
						.findViewById(R.id.stamp_count);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (position % 2 == 0) {
				convertView.findViewById(R.id.history_relative_layout)
						.setBackgroundColor(
								getResources().getColor(android.R.color.white));
			} else {
				convertView
						.findViewById(R.id.history_relative_layout)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.restaurant_details_list_bg_grey));
			}
			currentHistory = historyData.get(position);
			holder.stampCount.setText(currentHistory.count);
			holder.date.setText(currentHistory.date);
			holder.time.setText(currentHistory.time);
			return convertView;
		}

		class ViewHolder {
			TextView stampCount, date, time;
		}
	}

	class History {
		private String time;

		public History(String count, String date, String time) {
			this.count = count;
			this.date = date;
			this.time = time;
		}

		String count, date;
	}

}
