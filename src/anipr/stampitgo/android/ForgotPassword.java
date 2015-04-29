package anipr.stampitgo.android;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class ForgotPassword extends Activity {
	private EditText emailField;
	private Button sendRequest;
	private RelativeLayout backBtn;
	private String tag = getClass().getSimpleName();
	private SweetAlertDialog pDialog;

	@Override
	protected void onStart() {
		getActionBar().hide();
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forgot_password);

		emailField = (EditText) findViewById(R.id.forgot_password_email);
		sendRequest = (Button) findViewById(R.id.forgot_pwd_send);
		backBtn = (RelativeLayout) findViewById(R.id.back_button);

		sendRequest.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(emailField.getWindowToken(), 0);

				String email = emailField.getText().toString();
				if (email.length() == 0) {
					SweetAlertDialog sDialog = new SweetAlertDialog(
							ForgotPassword.this, SweetAlertDialog.WARNING_TYPE)
							.setContentText(
									getString(R.string.forgot_pwd_alert))
							.setTitleText("");
					sDialog.setCanceledOnTouchOutside(false);
					sDialog.show();
				} else {
					pDialog = new SweetAlertDialog(ForgotPassword.this, SweetAlertDialog.PROGRESS_TYPE);
					pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.primary_color));
					pDialog.setTitleText("Please Wait...");
					pDialog.setCancelable(false);
					pDialog.show();
					String payload = "{\"email\": \"" + email + "\"}";
					Map<String, String> params = new HashMap<String, String>();
					params.put("email", email);

					CustomParamRequest forgotPwd = new CustomParamRequest(
							Method.POST, ComUtility.ConnectionString
									+ "/forgot-password", null, null, payload,
							new Response.Listener<String>() {

								@Override
								public void onResponse(String arg0) {
									try {
										Log.i("Forgot Pwd", arg0);
										if (pDialog.isShowing()) {
											pDialog.dismiss();
										}
										JSONObject responseObj = new JSONObject(
												arg0);
										if (responseObj.getString("code")
												.equals("1")) {
											new SweetAlertDialog(ForgotPassword.this,
													SweetAlertDialog.SUCCESS_TYPE).setTitleText(
													"Done! ")
													.setContentText("A password reset link has been forwarded to the email address provided.").show();
										

										} else {

											Snackbar.with(
													ForgotPassword.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(responseObj
															.getString("message"))
													.actionLabel("Okay")
													.actionColor(
															ForgotPassword.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(ForgotPassword.this);
										}
									} catch (JSONException e) {
										e.printStackTrace();

										Snackbar.with(
												ForgotPassword.this
														.getApplicationContext())
												.type(SnackbarType.MULTI_LINE)
												.text(getString(R.string.server_error))
												.actionLabel("Okay")
												.actionColor(
														ForgotPassword.this
																.getResources()
																.getColor(
																		R.color.sb__button_text_color_yellow))
												.duration(
														Snackbar.SnackbarDuration.LENGTH_SHORT)
												.show(ForgotPassword.this);

									} catch (Exception e) {
										e.printStackTrace();
										Snackbar.with(
												ForgotPassword.this
														.getApplicationContext())
												.type(SnackbarType.MULTI_LINE)
												.text(getString(R.string.server_error))
												.actionLabel("Okay")
												.actionColor(
														ForgotPassword.this
																.getResources()
																.getColor(
																		R.color.sb__button_text_color_yellow))
												.duration(
														Snackbar.SnackbarDuration.LENGTH_SHORT)
												.show(ForgotPassword.this);
									}

								}
							}, new Response.ErrorListener() {

								

								@Override
								public void onErrorResponse(VolleyError arg0) {
									if (pDialog.isShowing()) {
										pDialog.dismiss();
									}
									if (arg0 instanceof NoConnectionError) {
										Snackbar.with(
												ForgotPassword.this
														.getApplicationContext())
												.type(SnackbarType.MULTI_LINE)
												.text(getString(R.string.internet_error_short))
												.actionLabel("Okay")
												.actionColor(
														ForgotPassword.this
																.getResources()
																.getColor(
																		R.color.sb__button_text_color_yellow))
												.duration(
														Snackbar.SnackbarDuration.LENGTH_SHORT)
												.show(ForgotPassword.this);
									} else {
										NetworkResponse networkResponse = arg0.networkResponse;
										Log.d(tag, ""+networkResponse.statusCode);
										if (arg0 instanceof NoConnectionError) {
											Snackbar.with(
													ForgotPassword.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(getString(R.string.internet_error_short))
													.actionLabel("Okay")
													.actionColor(
															ForgotPassword.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(ForgotPassword.this);
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
															ForgotPassword.this
																	.getApplicationContext())
															.type(SnackbarType.MULTI_LINE)
															.text(jsonObject.getString("message"))
															.actionLabel("Okay")
															.actionColor(
																	ForgotPassword.this
																			.getResources()
																			.getColor(
																					R.color.sb__button_text_color_yellow))
															.duration(
																	Snackbar.SnackbarDuration.LENGTH_SHORT)
															.show(ForgotPassword.this);
												} catch (JSONException e) {
													e.printStackTrace();
												} catch (UnsupportedEncodingException error) {
													error.printStackTrace();
												}
											} else {
												Log.d(tag, arg0.toString());
												Snackbar.with(
														ForgotPassword.this
																.getApplicationContext())
														.type(SnackbarType.MULTI_LINE)
														.text(getString(R.string.server_error))
														.actionLabel("Okay")
														.actionColor(
																ForgotPassword.this
																		.getResources()
																		.getColor(
																				R.color.sb__button_text_color_yellow))
														.duration(
																Snackbar.SnackbarDuration.LENGTH_SHORT)
														.show(ForgotPassword.this);

											}

										}

									}
								}
							}, Priority.IMMEDIATE);
					AppController.getInstance().addToRequestQueue(forgotPwd);

				}
			}

		});
		backBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);

	}
}
