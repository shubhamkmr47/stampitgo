package anipr.stampitgo.android;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class SignUp extends Activity {

	private EditText emailField, passwordField, confirmPasswordField;
	private String email, password;
	private Button signUpButton;
	private RelativeLayout backButton;
	private RelativeLayout facebookButton;

	private String tag = getClass().getSimpleName();
	private static final List<String> PERMISSIONS = Arrays.asList("email",
			"user_friends", "user_birthday");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().hide();

		setContentView(R.layout.sign_up_page);
		signUpButton = (Button) findViewById(R.id.sign_up_Button);
		emailField = (EditText) findViewById(R.id.signup_page_email);
		passwordField = (EditText) findViewById(R.id.signup_page_password);
		confirmPasswordField = (EditText) findViewById(R.id.confirmPassword);
		backButton = (RelativeLayout) findViewById(R.id.back_but);
		signUpButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						confirmPasswordField.getWindowToken(), 0);
				email = emailField.getText().toString();
				password = passwordField.getText().toString();
				String confirnPwd = confirmPasswordField.getText().toString();
				if (email.length() != 0 && password.length() != 0) {
					if (password.equals(confirnPwd)) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("email", email);
						map.put("password", password);
						if (new ComUtility(SignUp.this)
								.isConnectingToInternet()) {
							new SignUptask().execute(email, password);
						} else {

							Log.e(tag, "no internet");
							Snackbar.with(getApplicationContext())
									// context
									.type(SnackbarType.MULTI_LINE)
									.text(getString(R.string.internet_error_short))
									.actionLabel("Okay")
									.actionColor(
											getResources()
													.getColor(
															R.color.sb__button_text_color_yellow))
									.duration(
											Snackbar.SnackbarDuration.LENGTH_SHORT)
									.show(SignUp.this);
						}

					} else {
						Snackbar.with(getApplicationContext())
								// context
								.type(SnackbarType.MULTI_LINE)
								.text("Passwords don't match")
								.actionLabel("Try Again")
								.actionColor(
										getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(SignUp.this);
						Log.e(tag, "Passwords don't match");
					}

				} else {
					Snackbar.with(getApplicationContext())
							// context
							.type(SnackbarType.MULTI_LINE)
							.text("Please fill all the fields")
							.actionLabel("Okay")
							.actionColor(
									getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(SignUp.this);
				}

			}
		});
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		facebookButton = (RelativeLayout) findViewById(R.id.signup_facebook_button);
		facebookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (new ComUtility(getApplicationContext())
						.isConnectingToInternet()) {
					Session.openActiveSession(SignUp.this, true, callback);
				} else {
					Snackbar.with(SignUp.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(SignUp.this
									.getString(R.string.internet_error_short))
							.actionLabel("Okay")
							.actionColor(
									SignUp.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(SignUp.this);

				}
			}
		});

	}

	Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.d("token", session.getAccessToken());

				FbLoginRequest fbReq = new FbLoginRequest(SignUp.this);
				fbReq.execute(session.getAccessToken());

			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(SignUp.this, requestCode,
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

	private class SignUptask extends AsyncTask<String, Integer, Void> {
		org.apache.http.Header[] mCookies;
		JSONObject json;
		private HttpResponse response;
		private SweetAlertDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new SweetAlertDialog(SignUp.this,
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
		protected Void doInBackground(String... params) {
			HttpPost httppost = new HttpPost(ComUtility.ConnectionString
					+ "/register");
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("email", params[0]));
			nameValuePairs.add(new BasicNameValuePair("password", params[1]));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpClient httpclient = new DefaultHttpClient();
				response = httpclient.execute(httppost);
				HttpEntity loginentity = response.getEntity();

				if (response.getStatusLine().getStatusCode() == 200)
				// getting Cookie Headers
				{
					json = new JSONObject(EntityUtils.toString(loginentity));
					Log.d("response recieved", json.toString());

				}
			} catch (InterruptedIOException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void res) {
			super.onPostExecute(res);
			try {
				if (json.getString("code").equals("1")) {
					mCookies = response.getHeaders("Set-Cookie");
					Log.d("cookie", mCookies[0].toString());
					String sid = mCookies[0].toString().split(";")[0]
							.split(":")[1];
					Log.d("sid", sid);
					CustomParamRequest profileReq = new CustomParamRequest(
							Method.GET, ComUtility.ConnectionString
									+ "/profile", sid, null, null,
							new Response.Listener<String>() {
								@Override
								public void onResponse(String response) {
									try {
										JSONObject profileResponse = new JSONObject(
												response);
										Log.d("beforeResponse", "Prof");
										if (profileResponse.getString("code")
												.equals("1")) {

											Log.d("onResponse",
													"ProfileResponse"
															+ response);
											JSONObject userData = profileResponse
													.getJSONObject("data");
											SharedPreferences myPrefs;
											myPrefs = getSharedPreferences(
													"report", MODE_PRIVATE);
											SharedPreferences.Editor editor = myPrefs
													.edit();
											editor.putString(
													"emailid",
													userData.getString("email")
															.toString())
													.commit();
											editor.putString(
													"name",
													userData.getString("name")
															.toString())
													.commit();
											try {
												editor.putString(
														"phoneno",
														userData.getString(
																"mobile")
																.toString())
														.commit();

											} catch (JSONException e) {
												Log.e("SignUp",
														"Phone no not found");
											}
											ComUtility comUtility = new ComUtility(
													SignUp.this);
											if (userData.getString("name")
													.length() == 0)
												comUtility
														.makeEntryNotifications(
																1,
																getString(R.string.update_name_notification_title),
																getString(R.string.update_name_notification_message));

											editor.putString(
													"date",
													userData.getString(
															"birthday")
															.substring(0, 10))
													.commit();

											if (userData.getString("birthday")
													.length() == 0)
												comUtility
														.makeEntryNotifications(
																2,
																getString(R.string.update_bday_notification_title),
																getString(R.string.update_bday_notification_message));
										}

									} catch (JSONException e) {
										ComUtility comUtility = new ComUtility(
												SignUp.this);

										comUtility.makeEntryNotifications(2,
												getString(R.string.update_bday_notification_title),
												getString(R.string.update_bday_notification_message));
										e.printStackTrace();
									}

									Intent i = new Intent(SignUp.this,
											GetStarted.class);
									i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									if (pDialog.isShowing())
										pDialog.dismiss();
									startActivity(i);
									overridePendingTransition(R.anim.right_in,
											R.anim.left_out);
									finish();
								}
							}, new Response.ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError arg0) {
									arg0.printStackTrace();
									if (arg0 instanceof NoConnectionError) {
										Snackbar.with(
												SignUp.this
														.getApplicationContext())
												.type(SnackbarType.MULTI_LINE)
												.text(getString(R.string.internet_error_short))
												.actionLabel("Okay")
												.actionColor(
														SignUp.this
																.getResources()
																.getColor(
																		R.color.sb__button_text_color_yellow))
												.duration(
														Snackbar.SnackbarDuration.LENGTH_SHORT)
												.show(SignUp.this);
									} else {
										Snackbar.with(
												SignUp.this
														.getApplicationContext())
												.type(SnackbarType.MULTI_LINE)
												.text(getString(R.string.server_error))
												.actionLabel("Okay")
												.actionColor(
														SignUp.this
																.getResources()
																.getColor(
																		R.color.sb__button_text_color_yellow))
												.duration(
														Snackbar.SnackbarDuration.LENGTH_SHORT)
												.show(SignUp.this);
									}
								}
							}, Priority.NORMAL);
					AppController.getInstance().addToRequestQueue(profileReq);
					ContentValues cv = new ContentValues();
					cv.put(DbHelper.COOKIE, sid);
					cv.put(DbHelper.LOGGEDUSERID, json.getJSONObject("data")
							.getJSONObject("user").getString("user_code"));
					SQLiteDatabase db = DbHelper.getInstance(
							getApplicationContext()).getWritableDatabase();
					db.delete(DbHelper.LOGIN_DETAILS_TABLE, null, null);
					db.delete(DbHelper.RESTAURANT_TABLE, null, null);
					db.delete(DbHelper.REWARDS_TABLE, null, null);
					db.delete(DbHelper.LOGIN_DETAILS_TABLE, null, null);

					db.insert(DbHelper.LOGIN_DETAILS_TABLE, null, cv);

				} else {
					if (pDialog.isShowing())
						pDialog.dismiss();
					Snackbar.with(getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(json.getInt("message"))
							.actionLabel("Okay")
							.actionColor(
									getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(SignUp.this);
					Log.e(tag,
							"Server response with code 0 "
									+ json.getString("message"));
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("SIGN UP", e.toString());
			} catch (Exception e) {
				e.printStackTrace();
				Snackbar.with(getApplicationContext())
						.type(SnackbarType.MULTI_LINE)
						.text(getString(R.string.server_error))
						.actionLabel("Okay")
						.actionColor(
								getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(SignUp.this);
				if (pDialog.isShowing()) {
					pDialog.dismiss();
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
