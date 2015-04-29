package com.example.android.slidingtabsbasic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import anipr.stampitgo.android.AppController;
import anipr.stampitgo.android.ComUtility;
import anipr.stampitgo.android.CustomParamRequest;
import anipr.stampitgo.android.R;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;

public class RegularOffers extends Fragment {

	String TAG = "regularOffers";
	Activity mActivity;
	String code;
	ProgressBar pb;
	private ExpandableListView regularOffersList;
	List<RegularOffer> regularOfferData;
	// List<Chailditem> chaildData;
	private RegularOffersAdapter regularOffersAdapter;
	private FrameLayout coverFrame;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.regular_offers, container,
				false);
		mActivity = getActivity();
		pb = (ProgressBar) rootView.findViewById(R.id.regular_offerss_pb);
		code = getArguments().getString("storeCode");
		regularOffersList = (ExpandableListView) rootView
				.findViewById(R.id.regular_offer_list);
		RestaurantDetails.shoutoutFloatingButton.attachToListView(regularOffersList);
		regularOfferData = new ArrayList<RegularOffers.RegularOffer>();
		
		if (new ComUtility(getActivity()).isConnectingToInternet()) {
			CustomParamRequest regularOffersRequest = new CustomParamRequest(
					Method.GET, ComUtility.ConnectionString + "/offers/" + code
							+ "/app-offers", AppController.cookie, null, null,
					new Response.Listener<String>() {

						@Override
						public void onResponse(String arg0) {
							try {
								JSONObject response = new JSONObject(arg0);
								if (response.getString("code").equals("1")) {
									pb.setVisibility(View.GONE);
									JSONArray dataArray = response
											.getJSONArray("data");
									if (dataArray.length() != 0) {
										for (int i = 0; i < dataArray.length(); i++) {
											JSONObject offerObject = dataArray
													.getJSONObject(i);
											String offerName = offerObject
													.getString("name");
											String desc = offerObject
													.getString("description");
											String expieryDate = offerObject
													.getString("end_date")
													.substring(0, 10);
											String toc = offerObject
													.getString("offer_terms");
											regularOfferData.add(new RegularOffer(
													offerName, desc, expieryDate,
													toc));
										}
										regularOffersAdapter = new RegularOffersAdapter();
										regularOffersList
												.setAdapter(regularOffersAdapter);
									} else {
										Log.d(TAG, "regular offer data array empty");
										pb.setVisibility(View.GONE);

										coverFrame = (FrameLayout) rootView
												.findViewById(R.id.regular_offers_coverframe);
										coverFrame.setVisibility(View.VISIBLE);
										TextView errorText = (TextView) rootView
												.findViewById(R.id.regular_offers_error_msg);
										errorText.setText("The store does not have any offers for now");
									}

								} else {
									Log.d(TAG, response.getString("message"));
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					}, new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError arg0) {
							Log.e("VollyError in regularOffers", arg0.toString());
						}

					}, Request.Priority.IMMEDIATE);
			AppController.getInstance().addToRequestQueue(regularOffersRequest);
		}else {
			Log.d(TAG, "No Internet");
			pb.setVisibility(View.GONE);

			coverFrame = (FrameLayout) rootView
					.findViewById(R.id.regular_offers_coverframe);
			coverFrame.setVisibility(View.VISIBLE);
			TextView errorText = (TextView) rootView
					.findViewById(R.id.regular_offers_error_msg);
			errorText.setText(getString(R.string.internet_error));
		}
		
		return rootView;
	}

	class RegularOffersAdapter extends BaseExpandableListAdapter {
		RegularOffer currentOffer;

		class ViewHolder {
			TextView offername, offerdetails, expierydate, toc;
		}

		@Override
		public int getGroupCount() {
			return regularOfferData.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return regularOfferData.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return regularOfferData.get(childPosition).desc;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int position, boolean isExpanded,
				View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mActivity).inflate(
					R.layout.regular_offer_list_item, null);
			TextView offername = (TextView) convertView
					.findViewById(R.id.regular_offer_name);
			TextView expierydate = (TextView) convertView
					.findViewById(R.id.regular_offer_expirery);
			

			if (position % 2 == 0) {
				convertView.findViewById(
						R.id.regular_offer_list_item_relative_layout)
						.setBackgroundColor(
								getResources().getColor(android.R.color.white));
			} else {
				convertView
						.findViewById(
								R.id.regular_offer_list_item_relative_layout)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.restaurant_details_list_bg_grey));
			}

			currentOffer = regularOfferData.get(position);
			offername.setText(currentOffer.offerName);
			expierydate.setText(currentOffer.expieryDate);
			// toc.setText(currentOffer.toc);
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mActivity).inflate(
					R.layout.regular_offer_child_item, parent, false);

			TextView toc = (TextView) convertView.findViewById(R.id.toc);
			TextView offerdetails = (TextView) convertView
					.findViewById(R.id.regular_offer_desc);
			if (groupPosition % 2 == 0) {
				convertView.findViewById(
						R.id.regular_offer_child_item_relative_layout)
						.setBackgroundColor(
								getResources().getColor(android.R.color.white));
			} else {
				convertView
						.findViewById(
								R.id.regular_offer_child_item_relative_layout)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.restaurant_details_list_bg_grey));
			}

			currentOffer = regularOfferData.get(groupPosition);
			offerdetails.setText(currentOffer.desc);
			toc.setText(currentOffer.toc);
			return convertView;

		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	class RegularOffer {
		String offerName, desc, expieryDate, toc;

		public RegularOffer(String offerName, String desc, String expieryDate,
				String toc) {
			this.offerName = offerName;
			this.desc = desc;
			this.expieryDate = expieryDate;
			this.toc = toc;
		}

	}

}