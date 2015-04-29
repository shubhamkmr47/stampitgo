package com.example.android.slidingtabsbasic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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

public class LoyalOffers extends Fragment {

	ExpandableListView lv;
	String code;
	List<LoyalOffer> loyalOffers;
	ProgressBar loyalOffersPb;
	private FrameLayout coverFrame;
	View footerView;
	TextView tocText, tocLabel;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.loyal_offers, container,
				false);

		loyalOffersPb = (ProgressBar) rootView
				.findViewById(R.id.loyal_offers_pb);
		code = getArguments().getString("storeCode");
		loyalOffers = new ArrayList<LoyalOffers.LoyalOffer>();
		lv = (ExpandableListView) rootView.findViewById(R.id.loyal_offers_list);
		footerView = inflater.inflate(R.layout.toc_footer_view, lv, false);
		tocLabel = (TextView) footerView.findViewById(R.id.toc_label);
		return rootView;

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RestaurantDetails.shoutoutFloatingButton.attachToListView(lv);
		if (new ComUtility(getActivity()).isConnectingToInternet()) {
			CustomParamRequest cr = new CustomParamRequest(Method.GET,
					ComUtility.ConnectionString + "/stores/" + code,
					AppController.cookie, null, null,
					new Response.Listener<String>() {

						@Override
						public void onResponse(String arg0) {
							try {
								JSONObject jObject = new JSONObject(arg0);
								if (jObject.getString("code").equals("1")) {
									loyalOffersPb.setVisibility(View.GONE);
									JSONObject dataObj = jObject
											.getJSONObject("data");
									JSONObject stampObject = dataObj
											.getJSONObject("stamp");
									JSONArray rewardArray = stampObject
											.getJSONArray("rewards");
									for (int i = 0; i < rewardArray.length(); i++) {
										loyalOffers
												.add(new LoyalOffer(
														rewardArray
																.getJSONObject(
																		i)
																.getString(
																		"stampcount"),
														rewardArray
																.getJSONObject(
																		i)
																.getString(
																		"name"),
														rewardArray
																.getJSONObject(
																		i)
																.getString(
																		"description")));

									}
									lv.setAdapter(new LoyalOfferAdapter());
									if(rewardArray.length()%2==0){
										footerView.setBackgroundColor(
												getResources().getColor(android.R.color.white));
									}
									tocText = (TextView) footerView
											.findViewById(R.id.toc_text);
									tocText.setText(stampObject
											.getString("message"));
									tocText.setVisibility(View.GONE);
									lv.addFooterView(footerView);
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

		} else {
			loyalOffersPb.setVisibility(View.GONE);

			coverFrame = (FrameLayout) view
					.findViewById(R.id.loyal_offers_coverframe);
			coverFrame.setVisibility(View.VISIBLE);
			TextView errorText = (TextView) view
					.findViewById(R.id.loyal_offers_error_msg);
			errorText.setText(getString(R.string.internet_error));
		}
		footerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (tocText.isShown()) {
					tocText.setVisibility(View.GONE);
				} else {
					
					tocText.setVisibility(View.VISIBLE);
				}
			}
		});

	}

	private class LoyalOfferAdapter extends BaseExpandableListAdapter {

		class ViewHolder {
			TextView stamp_count, rewardMsg;
		}

		@Override
		public int getGroupCount() {
			return loyalOffers.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return loyalOffers.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return loyalOffers.get(childPosition).rewardDesc;
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
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			ViewHolder holder;

			convertView = LayoutInflater.from(getActivity()).inflate(
					R.layout.loyal_offer_list_item, null);

			holder = new ViewHolder();
			holder.stamp_count = (TextView) convertView
					.findViewById(R.id.stamp_count);
			holder.rewardMsg = (TextView) convertView
					.findViewById(R.id.reward_msg);
			convertView.setTag(holder);

			LoyalOffer currentOffer = loyalOffers.get(groupPosition);
			if (groupPosition % 2 == 0) {
				convertView.findViewById(
						R.id.loyal_offer_list_item_relative_layout)
						.setBackgroundColor(
								getResources().getColor(android.R.color.white));
			} else {
				convertView
						.findViewById(
								R.id.loyal_offer_list_item_relative_layout)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.restaurant_details_list_bg_grey));
			}
			holder.stamp_count.setText(currentOffer.stampCount + " Stamps");
			holder.rewardMsg.setText(currentOffer.rewardMessage);
			return convertView;

		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(getActivity()).inflate(
					R.layout.loyal_offer_list_item_child, null);

			TextView rewardDesc = (TextView) convertView
					.findViewById(R.id.loyal_offer_details);
			LoyalOffer currentOffer = loyalOffers.get(groupPosition);
			if (groupPosition % 2 == 0) {
				convertView.findViewById(
						R.id.loyal_offer_child_item_relative_layout)
						.setBackgroundColor(
								getResources().getColor(android.R.color.white));
			} else {
				convertView
						.findViewById(
								R.id.loyal_offer_child_item_relative_layout)
						.setBackgroundColor(
								getResources()
										.getColor(
												R.color.restaurant_details_list_bg_grey));
			}
			rewardDesc.setText(currentOffer.rewardDesc);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	protected class LoyalOffer {
		String stampCount, rewardMessage, rewardDesc;

		public LoyalOffer(String stampCount, String rewardMessage,
				String rewardDesc) {
			this.stampCount = stampCount;
			this.rewardMessage = rewardMessage;
			this.rewardDesc = rewardDesc;
		}
	}
}
