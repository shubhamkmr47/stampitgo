package anipr.stampitgo.android;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class Login extends Activity {
	private EditText username;
	private EditText password;
	private Button loginBtn;
	private RelativeLayout backBtn;
	private RelativeLayout fbLoginButton;

	private static final List<String> PERMISSIONS = Arrays.asList("email",
			"user_friends", "user_birthday");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		username = (EditText) findViewById(R.id.usernameField);
		password = (EditText) findViewById(R.id.passwordField);
		loginBtn = (Button) findViewById(R.id.signIn);
		loginBtn.setOnClickListener(onLoginClick);
		backBtn = (RelativeLayout) findViewById(R.id.back_button);
		RelativeLayout signUpText = (RelativeLayout) findViewById(R.id.sign_up_label);
		fbLoginButton = (RelativeLayout) findViewById(R.id.login_fb);
		fbLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ComUtility(getApplicationContext())
						.isConnectingToInternet()) {
					loginViaFB();
				} else {
					Snackbar.with(Login.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(Login.this
									.getString(R.string.internet_error_short))
							.actionLabel("Okay")
							.actionColor(
									Login.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(Login.this);
				}
			}
		});

		signUpText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Login.this, SignUp.class);
				startActivity(i);
				overridePendingTransition(R.anim.right_in, R.anim.left_out);
			}
		});
		backBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backPressed();
			}
		});

	}

	private class LoginTask extends AsyncTask<String, Integer, HttpResponse> {

		String sid;
		String result = "default";
		Header[] mCookies;
		SQLiteDatabase db;
		HttpResponse response;
		private SweetAlertDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// Showing progress dialog
			pDialog = new SweetAlertDialog(Login.this,
					SweetAlertDialog.PROGRESS_TYPE);
			pDialog.getProgressHelper().setBarColor(
					getResources().getColor(R.color.primary_color));
			pDialog.setTitleText("Please Wait...");
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... args) {

		}

		@Override
		protected HttpResponse doInBackground(String... params) {
			try {
				// Create a new HttpClient and Post Header

				HttpPost httppost = new HttpPost(params[0]);
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs
						.add(new BasicNameValuePair("username", params[1]));
				nameValuePairs
						.add(new BasicNameValuePair("password", params[2]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpClient httpclient = new DefaultHttpClient();
				response = httpclient.execute(httppost);
			} catch (ParseException e) {
				e.printStackTrace();
				if (pDialog.isShowing())
					pDialog.dismiss();
			} catch (IOException e) {
				if (pDialog.isShowing())
					pDialog.dismiss();

				Snackbar.with(Login.this.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(Login.this.getString(R.string.server_error))
						.actionLabel("Okay")
						.actionColor(
								Login.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(Login.this);
				e.printStackTrace();
			} catch (Exception e) {
				if (pDialog.isShowing())
					pDialog.dismiss();
				Snackbar.with(Login.this.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(Login.this.getString(R.string.server_error))
						.actionLabel("Okay")
						.actionColor(
								Login.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(Login.this);
				e.printStackTrace();
			}
			return response;
		}

		protected void onPostExecute(HttpResponse response) {
			super.onPostExecute(response);

			try {
				JSONObject json;

				if (response.getStatusLine().getStatusCode() == 200)
				// getting Cookie Headers
				{
					HttpEntity loginentity = response.getEntity();

					json = new JSONObject(EntityUtils.toString(loginentity));

					// Dismiss the progress dialog

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
											Log.d("beforeResponse", "Prof");
											if (prof.getString("code").equals(
													"1")) {

												Log.d("onResponse",
														"ProfileResponse"
																+ response);
												JSONObject p = prof
														.getJSONObject("data");
												SharedPreferences myPrefs;
												myPrefs = getSharedPreferences(
														"report", MODE_PRIVATE);
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

												ComUtility comUtility = new ComUtility(
														Login.this);
												if (p.getString("name")
														.length() == 0)
													comUtility
															.makeEntryNotifications(
																	1,
																	getString(R.string.update_name_notification_title),
																	getString(R.string.update_name_notification_message));
												Log.i("birthday",
														p.getString("birthday")
																.substring(0,
																		10));
												editor.putString(
														"date",
														p.getString("birthday")
																.substring(0,
																		10))
														.commit();
												if (p.getString("birthday")
														.length() == 0)
													comUtility
															.makeEntryNotifications(
																	1,

																	getString(R.string.update_bday_notification_title),
																	getString(R.string.update_bday_notification_message));
											}

										} catch (JSONException e) {
											ComUtility comUtility = new ComUtility(
													Login.this);
											comUtility
													.makeEntryNotifications(
															2,
															getString(R.string.update_bday_notification_title),
															getString(R.string.update_bday_notification_message));
											e.printStackTrace();
										}

										Log.e("Login time", "ProfileResponse"
												+ response);
										Intent i = new Intent(Login.this,
												GetStarted.class);
										i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										if (pDialog.isShowing())
											pDialog.dismiss();
										startActivity(i);
										overridePendingTransition(
												R.anim.right_in,
												R.anim.left_out);
										Login.this.finish();
									}
								}, new Response.ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										Log.e("Login time",
												"ProfileResposne Error");
										if (arg0 instanceof NoConnectionError) {
											Snackbar.with(
													Login.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(getString(R.string.internet_error_short))
													.actionLabel("Okay")
													.actionColor(
															Login.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(Login.this);
										} else {
											Snackbar.with(
													Login.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(getString(R.string.server_error))
													.actionLabel("Okay")
													.actionColor(
															Login.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(Login.this);
										}
									}
								}, Priority.NORMAL);
						AppController.getInstance().addToRequestQueue(
								profileReq);
						String userID = userData.getString("user_code");
						ContentValues cv = new ContentValues();
						cv.put(DbHelper.LOGGEDUSERID, userID);
						cv.put(DbHelper.COOKIE, sid);
						db = DbHelper.getInstance(getApplicationContext())
								.getWritableDatabase();
						db.delete(DbHelper.LOGIN_DETAILS_TABLE, null, null);
						db.delete(DbHelper.RESTAURANT_TABLE, null, null);
						db.delete(DbHelper.REWARDS_TABLE, null, null);
						db.insert(DbHelper.LOGIN_DETAILS_TABLE, null, cv);

					} else {
						if (pDialog.isShowing())
							pDialog.dismiss();
						Snackbar.with(Login.this.getApplicationContext())
								.type(SnackbarType.MULTI_LINE)
								.text(json.getString("message"))
								.actionLabel("Okay")
								.actionColor(
										Login.this
												.getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(Login.this);
					}

				} else {
					if (pDialog.isShowing())
						pDialog.dismiss();

					Snackbar.with(Login.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(Login.this.getString(R.string.server_error))
							.actionLabel("Okay")
							.actionColor(
									Login.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(Login.this);
				}
			} catch (JSONException e) {
				if (pDialog.isShowing())
					pDialog.dismiss();
				Log.e(getClass().getSimpleName(), "json exception");
				e.printStackTrace();

				Snackbar.with(Login.this.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(Login.this.getString(R.string.server_error))
						.actionLabel("Okay")
						.actionColor(
								Login.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(Login.this);

			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	OnClickListener onLoginClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(username.getWindowToken(), 0);
			String userid = username.getText().toString();
			String pwd = password.getText().toString();
			String stringUrl = ComUtility.ConnectionString + "/app-login";
			ConnectivityManager connMgr = (ConnectivityManager) Login.this
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				try {
					new LoginTask().execute(stringUrl, userid, pwd);

				} catch (Exception e) {
					e.printStackTrace();

					Snackbar.with(Login.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(Login.this.getString(R.string.server_error))
							.actionLabel("Okay")
							.actionColor(
									Login.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(Login.this);
				}
			} else {
				Snackbar.with(Login.this.getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(Login.this
								.getString(R.string.internet_error_short))
						.actionLabel("Okay")
						.actionColor(
								Login.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(Login.this);
			}
		}
	};

	public void loginViaFB() {

		try {
			PackageInfo info = Login.this.getPackageManager().getPackageInfo(
					"anipr.stampitgo.android", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Session.openActiveSession(Login.this, true, callback);

	}

	Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.d("token", session.getAccessToken());

				FbLoginRequest fbReq = new FbLoginRequest(Login.this);
				fbReq.execute(session.getAccessToken());

			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(Login.this, requestCode,
				resultCode, data);

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public void backPressed() {
		onBackPressed();
	}

	public void forgotPassword(View V) {
		Intent i = new Intent(this, ForgotPassword.class);
		startActivity(i);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
