package anipr.stampitgo.android;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;

public class ChangePassword extends SampleActivityBase {
	private EditText oldpassword, newpassword, conformpassword;
	private Button changepassword;
	private String newpass, conformpass;
	private String tag = getClass().getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);

		oldpassword = (EditText) findViewById(R.id.oldpasswordField);
		newpassword = (EditText) findViewById(R.id.newpasswordField);
		conformpassword = (EditText) findViewById(R.id.conformpasswordField);
		changepassword = (Button) findViewById(R.id.reset_password);
		try {
			Typeface robotoCondensed = Typeface.createFromAsset(
					getApplicationContext().getAssets(), "Face Your Fears.ttf");
			oldpassword.setTypeface(robotoCondensed);
		} catch (Exception e) {
			Log.e("Font Load Exception", e.toString());
		}

		changepassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(newpassword.getWindowToken(), 0);

				newpass = newpassword.getText().toString();
				conformpass = conformpassword.getText().toString();
				if (oldpassword.getText().toString().length() != 0) {
					if (newpass.length() != 0) {
						if (newpass.equals(conformpass)) {
							Map<String, String> params = new HashMap<String, String>();
							params.put("password", newpass);
							params.put("old_password", oldpassword.getText()
									.toString());
							String payload = new JSONObject(params).toString();
							CustomParamRequest changePwdReq = new CustomParamRequest(
									Method.POST, ComUtility.ConnectionString
											+ "/change-password",
									AppController.cookie, null, payload,
									new Response.Listener<String>() {

										@Override
										public void onResponse(String arg0) {
											try {
												JSONObject response = new JSONObject(
														arg0);
												if (response.getString("code")
														.equals("1")) {
													Snackbar.with(
															ChangePassword.this
																	.getApplicationContext())
															// context
															.type(SnackbarType.MULTI_LINE)
															.text("Password Changed")
															.actionLabel("Okay")
															.actionColor(
																	ChangePassword.this
																			.getResources()
																			.getColor(
																					R.color.sb__button_text_color_yellow))
															.duration(
																	Snackbar.SnackbarDuration.LENGTH_SHORT)
															.actionListener(
																	new ActionClickListener() {

																		@Override
																		public void onActionClicked() {
																			onBackPressed();
																		}
																	})
															.show(ChangePassword.this);
												} else {
													Log.d("not changed",
															response.getString("message"));
													Snackbar.with(
															ChangePassword.this
																	.getApplicationContext())
															// context
															.type(SnackbarType.MULTI_LINE)
															.text(response
																	.getString("message"))
															.actionLabel("Okay")
															.actionColor(
																	ChangePassword.this
																			.getResources()
																			.getColor(
																					R.color.sb__button_text_color_yellow))
															.duration(
																	Snackbar.SnackbarDuration.LENGTH_SHORT)
															.show(ChangePassword.this);
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}

										}
									}, new Response.ErrorListener() {

										@Override
										public void onErrorResponse(
												VolleyError arg0) {
											arg0.printStackTrace();

											NetworkResponse networkResponse = arg0.networkResponse;
											Log.d(tag, ""+networkResponse.statusCode);
											if (arg0 instanceof NoConnectionError) {
												Snackbar.with(
														ChangePassword.this
																.getApplicationContext())
														.type(SnackbarType.MULTI_LINE)
														.text(getString(R.string.internet_error_short))
														.actionLabel("Okay")
														.actionColor(
																ChangePassword.this
																		.getResources()
																		.getColor(
																				R.color.sb__button_text_color_yellow))
														.duration(
																Snackbar.SnackbarDuration.LENGTH_SHORT)
														.show(ChangePassword.this);
											} else {
												if (String.valueOf(networkResponse.statusCode).startsWith("4")) {
													try {
														String responseBody = new String(
																arg0.networkResponse.data,
																"utf-8");
														JSONObject jsonObject = new JSONObject(
																responseBody);
														Log.d(tag, jsonObject
																.toString());
														Snackbar.with(
																ChangePassword.this
																		.getApplicationContext())
																.type(SnackbarType.MULTI_LINE)
																.text(jsonObject.getString("message"))
																.actionLabel("Okay")
																.actionColor(
																		ChangePassword.this
																				.getResources()
																				.getColor(
																						R.color.sb__button_text_color_yellow))
																.duration(
																		Snackbar.SnackbarDuration.LENGTH_SHORT)
																.show(ChangePassword.this);
													} catch (JSONException e) {
														e.printStackTrace();
													} catch (UnsupportedEncodingException error) {
														error.printStackTrace();
													}
												} else {
													Log.d(tag, arg0.toString());
													Snackbar.with(
															ChangePassword.this
																	.getApplicationContext())
															.type(SnackbarType.MULTI_LINE)
															.text(getString(R.string.server_error))
															.actionLabel("Okay")
															.actionColor(
																	ChangePassword.this
																			.getResources()
																			.getColor(
																					R.color.sb__button_text_color_yellow))
															.duration(
																	Snackbar.SnackbarDuration.LENGTH_SHORT)
															.show(ChangePassword.this);

												}

											}
										}
									});
							AppController.getInstance().addToRequestQueue(
									changePwdReq);
						} else {
							Snackbar.with(
									ChangePassword.this.getApplicationContext())
									// context
									.type(SnackbarType.MULTI_LINE)
									.text("Passwords don't match")
									.actionLabel("Okay")
									.actionColor(
											ChangePassword.this
													.getResources()
													.getColor(
															R.color.sb__button_text_color_yellow))
									.duration(
											Snackbar.SnackbarDuration.LENGTH_SHORT)
									.show(ChangePassword.this);
						}
					} else {
						Snackbar.with(
								ChangePassword.this.getApplicationContext())
								// context
								.type(SnackbarType.MULTI_LINE)
								.text("New password can't be blank")
								.actionLabel("Okay")
								.actionColor(
										ChangePassword.this
												.getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(ChangePassword.this);

					}
				} else {
					Snackbar.with(ChangePassword.this.getApplicationContext())
							// context
							.type(SnackbarType.MULTI_LINE)
							.text("Current password can't be blank")
							.actionLabel("Okay")
							.actionColor(
									ChangePassword.this
											.getResources()
											.getColor(
													R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(ChangePassword.this);

				}

			}
		});

	}

}