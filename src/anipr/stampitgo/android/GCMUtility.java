package anipr.stampitgo.android;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMUtility {

	Activity mActivity;
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	String SENDER_ID = "511945442603";
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;

	String regid;
	private String TAG = "StampItGo";

	public GCMUtility(Activity activity) {
		mActivity = activity;
		if (checkPlayServices()) {
			context = activity.getApplicationContext();
			gcm = GoogleCloudMessaging.getInstance(activity);
			regid = getRegistrationId(context);

			registerInBackground();
		} else {

			Log.e(TAG, "No valid Google Play Services APK found.");
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(mActivity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, mActivity,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("not supported", "This device is not supported.");
				mActivity.finish();
			}
			return false;
		}
		return true;
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}

		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return mActivity.getSharedPreferences(
				MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerInBackground() {
		new AsyncTask<Void , Void , Void >() {
	        @Override
	        protected Void doInBackground(Void... params) {
	        	try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                
	        	}
	        	catch (IOException e)
	        	{
	        		Log.e("CM regid Error", e.toString());
	        	}
	        	Log.i("registering device ", regid);
	    	   
	        	String gcmPayload = "{\"id\": \""+regid+"\",\"platform\": \"android\"}";
	    	    storeRegistrationId(context, regid);	
	    	    CustomParamRequest gcmRequest = new CustomParamRequest(Method.POST, ComUtility.ConnectionString+"/register-device", AppController.cookie, null,gcmPayload, new Response.Listener<String>() {

	    			@Override
	    			public void onResponse(String arg0) {
	    				Log.d("DEVICE registered with response", regid+arg0.toString());
	    			}
	    		}, new Response.ErrorListener() {

	    			@Override
	    			public void onErrorResponse(VolleyError arg0) {
	    				Log.e("DEVICE registration Error", arg0.toString());
	    			}
	    		},Priority.LOW);
	    	    AppController.getInstance().addToRequestQueue(gcmRequest);
	    	    return null;
	            
	        }

	        @Override
	        protected void onPostExecute( Void Result) {
	           
	        	Log.d("", regid);
	            
	        }
	    }.execute(null, null, null);
		 
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}
}
