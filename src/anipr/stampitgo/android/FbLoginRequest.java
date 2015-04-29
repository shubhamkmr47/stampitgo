package anipr.stampitgo.android;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class FbLoginRequest extends AsyncTask<String, Integer, HttpResponse> {

	String sid;
	String result = "default";
	Header[] mCookies;
	SQLiteDatabase db;
	HttpResponse response;
	HttpEntity loginentity;
	private Activity activity;
	SweetAlertDialog pDialog;

	public FbLoginRequest(Activity activity) {
		this.activity = activity;

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		pDialog.getProgressHelper().setInstantProgress((float) values[0]);
		pDialog.getProgressHelper().setProgress((float) values[0]);
		super.onProgressUpdate(values);

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Showing progress dialog
		pDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
		pDialog.getProgressHelper().setBarColor(
				activity.getResources().getColor(R.color.primary_color));
		pDialog.setTitleText("Please wait while we get your info...");
		pDialog.setCancelable(false);
		pDialog.show();

	}

	@Override
	protected HttpResponse doInBackground(String... params) {
		try {
			// Create a new HttpClient and Post Header

			HttpGet httppost = new HttpGet(ComUtility.ConnectionString
					+ "/facebook-login?access_token=" + params[0]);
			HttpClient httpclient = new DefaultHttpClient();
			response = httpclient.execute(httppost);
			loginentity = response.getEntity();

		}

		catch (Exception e) {
			Log.e("asynck task bolck ", e.toString());
			return response;
		}
		return response;
	}

	protected void onPostExecute(HttpResponse response) {
		super.onPostExecute(response);

		try {
			Log.d(getClass().getSimpleName(), "post execute");
			if (new ComUtility(activity.getApplicationContext())
					.isConnectingToInternet()) {
				// Dismiss the progress dialog
				JSONObject json;

				if (response.getStatusLine().getStatusCode() == 200)
				// getting Cookie Headers
				{
					json = new JSONObject(EntityUtils.toString(loginentity));

					// getting data from WhoAmI JSON
					Log.d("result value", result);
					if (json.getString("code").equals("1")) {
						mCookies = response.getHeaders("Set-Cookie");
						Log.d("cookie", mCookies[0].toString());
						sid = mCookies[0].toString().split(";")[0].split(":")[1];
						Log.d("sid", sid);
						result = json.getJSONObject("data")
								.getJSONObject("user").toString();
						final JSONObject userData = new JSONObject(result);

						Log.e("cookie", sid);
						CustomParamRequest profileReq = new CustomParamRequest(
								Method.GET, ComUtility.ConnectionString
										+ "/profile", sid, null, null,
								new Response.Listener<String>() {
									@Override
									public void onResponse(String response) {
										try {
											JSONObject prof = new JSONObject(
													response);
											if (prof.getString("code").equals(
													"1")) {

												JSONObject p = prof
														.getJSONObject("data");
												SharedPreferences myPrefs;
												myPrefs = activity
														.getSharedPreferences(
																"report",
																Context.MODE_PRIVATE);

												SharedPreferences.Editor editor = myPrefs
														.edit();
												editor.putString(
														"emailid",
														p.getString("email")
																.toString())
														.commit();
												editor.putString(
														"name",
														p.getString("name")
																.toString())
														.commit();
												try {
													editor.putString(
															"phoneno",
															p.getString(
																	"mobile")
																	.toString())
															.commit();

												} catch (JSONException e) {
													Log.e("FACEBOOK LOGIN",
															"phone no not found");
												}

												editor.putString(
														"date",
														p.getString("birthday")
																.substring(0,
																		10))
														.commit();
												Log.d("name",
														p.getString("name")
																.toString());
												Log.d("emailid",
														p.getString("email")
																.toString());
												Log.d("date",
														p.getString("birthday")
																.substring(0,
																		10));
												Log.d("phoneno",
														p.getString("phone")
																.toString());
												ComUtility comUtility = new ComUtility(
														activity);
												if (p.getString("name")
														.length() == 0)
													comUtility
															.makeEntryNotifications(
																	1,
																	activity.getString(R.string.update_name_notification_title),
																	activity.getString(R.string.update_name_notification_message));

												if (p.getString("birthday")
														.length() == 0)
													comUtility
															.makeEntryNotifications(
																	2,
																	activity.getString(R.string.update_bday_notification_title),
																	activity.getString(R.string.update_bday_notification_message));
																	
											}
										} catch (JSONException e) {
											e.printStackTrace();
										}
										Intent i = new Intent(activity,
												GetStarted.class);
										i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										i.setAction(AppController.FIRST_TIME_LOGIN);
										if (pDialog.isShowing())
											pDialog.dismiss();
										activity.startActivity(i);
										activity.overridePendingTransition(
												R.anim.right_in,
												R.anim.left_out);
										activity.finish();
									}
								}, new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError arg0) {
										arg0.printStackTrace();
										if (arg0 instanceof NoConnectionError) {
											Snackbar.with(
													activity.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text((R.string.internet_error_short))
													.actionLabel("Okay")
													.actionColor(
															activity.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(activity);
										} else {
											Snackbar.with(
													activity.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text((R.string.server_error))
													.actionLabel("Okay")
													.actionColor(
															activity.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(activity);
										}
									}
								}, Priority.NORMAL);
						AppController.getInstance().addToRequestQueue(
								profileReq);
						String userID = userData.getString("user_code");
						ContentValues cv = new ContentValues();
						cv.put(DbHelper.LOGGEDUSERID, userID);
						cv.put(DbHelper.COOKIE, sid);
						db = DbHelper.getInstance(
								activity.getApplicationContext())
								.getWritableDatabase();
						db.delete(DbHelper.LOGIN_DETAILS_TABLE, null, null);
						db.delete(DbHelper.RESTAURANT_TABLE, null, null);
						db.delete(DbHelper.REWARDS_TABLE, null, null);
						db.insert(DbHelper.LOGIN_DETAILS_TABLE, null, cv);

					} else {
						Snackbar.with(activity.getApplicationContext())
								.type(SnackbarType.MULTI_LINE)
								.text(json.getString("message"))
								.actionLabel("Okay")
								.actionColor(
										activity.getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(activity);
					}

				} else {
					Snackbar.with(activity.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(activity.getString(R.string.server_error))
							.actionLabel("Okay")
							.actionColor(
									activity.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(activity);
				}

			} else {
				Log.d(getClass().getSimpleName(), "no Intentnet");
				if (pDialog.isShowing())
					pDialog.dismiss();
				Snackbar.with(activity.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(activity.getString(R.string.internet_error_short))
						.actionLabel("Okay")
						.actionColor(
								activity.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(activity);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			if (pDialog.isShowing())
				pDialog.dismiss();
			Snackbar.with(activity.getApplicationContext())
					.type(SnackbarType.MULTI_LINE)
					.text(activity.getString(R.string.server_error))
					.actionLabel("Okay")
					.actionColor(
							activity.getResources().getColor(
									R.color.sb__button_text_color_yellow))
					.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
					.show(activity);
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
			if (pDialog.isShowing())
				pDialog.dismiss();
			Snackbar.with(activity.getApplicationContext())
					.type(SnackbarType.MULTI_LINE)
					.text(activity.getString(R.string.server_error))
					.actionLabel("Okay")
					.actionColor(
							activity.getResources().getColor(
									R.color.sb__button_text_color_yellow))
					.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
					.show(activity);
			e.printStackTrace();
		} catch (IOException e) {
			if (pDialog.isShowing())
				pDialog.dismiss();
			Snackbar.with(activity.getApplicationContext())
					.type(SnackbarType.MULTI_LINE)
					.text(activity.getString(R.string.server_error))
					.actionLabel("Okay")
					.actionColor(
							activity.getResources().getColor(
									R.color.sb__button_text_color_yellow))
					.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
					.show(activity);
			e.printStackTrace();
		} catch (Exception e) {
			if (pDialog.isShowing())
				pDialog.dismiss();
			Snackbar.with(activity.getApplicationContext())
					.type(SnackbarType.MULTI_LINE)
					.text(activity.getString(R.string.server_error))
					.actionLabel("Okay")
					.actionColor(
							activity.getResources().getColor(
									R.color.sb__button_text_color_yellow))
					.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
					.show(activity);
			e.printStackTrace();
		}

	}
}
