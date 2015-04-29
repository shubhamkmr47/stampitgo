package anipr.stampitgo.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class GetLocation implements LocationListener {

	Activity activity;
	double lat = 0;
	double lng = 0;
	int ENABLED = 1;
	int DISABLED = 2;
	int status;
	private LocationManager locationManager;
	private MixpanelAPI mMixpanel;

	public GetLocation(Activity mContext) {
		mMixpanel = MixpanelAPI.getInstance(mContext,
				AppController.MIXPANEL_TOKEN);
		this.activity = mContext;
		String context = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) activity.getApplicationContext()
				.getSystemService(context);

	}

	public Location getCurrentLocationd() {
		Location location = null;
		try {
//			String provider = LocationManager.GPS_PROVIDER;
			String networkProvider = LocationManager.NETWORK_PROVIDER;
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 2000, this);
			location = locationManager.getLastKnownLocation(networkProvider);
			JSONObject locationData = new JSONObject();
			if (!AppController.cookie.equals(AppController.GUEST_USER)) {
				locationData.put("userId", AppController.userCode);
			} else {
				locationData.put("userId", AppController.GUEST_USER);
			}
			locationData.put("latitude", location.getLatitude());
			locationData.put("longitude", location.getLongitude());
			mMixpanel.track("Location Data ", locationData);
			mMixpanel.flush();
		} catch (Exception e) {
			Log.e("GetLocaion Class", "Error while fetching location");
			e.printStackTrace();
		}
		return location;
	}

	@Override
	public void onLocationChanged(Location location) {
		lat = location.getLatitude();
		lng = location.getLongitude();
		try {
			Log.i(getClass().getSimpleName(), "Near BY reuest Fired");
//			new ComUtility(activity).getNearByStores(lat + "," + lng);

		} catch (Exception e) {
			Log.e("Fetch Error", e.toString());
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		status = 0;
	}

	@Override
	public void onProviderEnabled(String provider) {
		status = 1;
		try {
			new ComUtility(activity).getNearByStores(lat + "," + lng);
		} catch (JSONException e) {
			Log.e("Fetch Error", e.toString());
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

}
