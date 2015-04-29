package anipr.stampitgo.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class PromoCode extends SampleActivityBase {

	EditText promoCode;
	Button promocodeButton;
	Map<String, String> params;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_promo_code);
		promoCode = (EditText) findViewById(R.id.promocode);
		promocodeButton = (Button) findViewById(R.id.redeem_promocode);
		params = new HashMap<String, String>();

		promocodeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(promoCode.getWindowToken(), 0);

				if (AppController.cookie.equals(AppController.GUEST_USER)) {
					AlertDialog.Builder bulider = new AlertDialog.Builder(
							PromoCode.this);
					bulider.setMessage("Please Login to Enter a Promocode")
							.setTitle("Promocode Response")
							.setPositiveButton("ok", null);
					AlertDialog dialog1 = bulider.create();
					dialog1.show();
				} else {
					if (promoCode.getText().toString().length() != 0) {
						params.put("code", promoCode.getText().toString());
						JSONObject payload = new JSONObject(params);
						CustomParamRequest promoCodeReq = new CustomParamRequest(
								Method.POST, ComUtility.ConnectionString
										+ "/promo-code", AppController.cookie,
								null, payload.toString(),
								new Response.Listener<String>() {

									@Override
									public void onResponse(String arg0) {
										try {
											JSONObject responseObj = new JSONObject(
													arg0);
											if (responseObj.getString("code")
													.equals("1")) {
												new ComUtility(
														getApplicationContext())
														.getRewards();
												Log.d(getClass()
														.getSimpleName(),
														responseObj.toString());
												new SweetAlertDialog(
														PromoCode.this,
														SweetAlertDialog.SUCCESS_TYPE)
														.setTitleText(
																"Congrats !!! \n Promocode Successfuly Applied!")
														.setContentText(
																"Lets open Rewards tab ad check out your new reward.")
														.setConfirmText(
																"Yes,take me there!")
														.setConfirmClickListener(
																new SweetAlertDialog.OnSweetClickListener() {
																	@Override
																	public void onClick(
																			SweetAlertDialog sDialog) {
																		sDialog.dismissWithAnimation();
																		Intent i = new Intent(
																				PromoCode.this,
																				MainActivity.class);
																		i.setAction(AppController.REWARDINTENT);
																		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
																		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
																		startActivity(i);
																		finish();
																	}
																})
														.showCancelButton(true)
														.setCancelClickListener(
																new SweetAlertDialog.OnSweetClickListener() {
																	@Override
																	public void onClick(
																			SweetAlertDialog sDialog) {
																		sDialog.cancel();
																	}
																}).show();
											} else {
												new SweetAlertDialog(
														PromoCode.this,
														SweetAlertDialog.ERROR_TYPE)
														.setTitleText("Sorry")
														.setContentText(
																responseObj
																		.getString("message"))
														.show();
												Log.d(getClass()
														.getSimpleName(),
														responseObj
																.getString("message"));

											}
										} catch (Exception e) {

										}

									}
								}, new Response.ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError arg0) {
										Log.d(getClass().getSimpleName(),
												"Promocode response Error");
										arg0.printStackTrace();

										if (arg0 instanceof NoConnectionError) {
											Snackbar.with(
													PromoCode.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(getString(R.string.internet_error_short))
													.actionLabel("Okay")
													.actionColor(
															PromoCode.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(PromoCode.this);
										} else {
											Snackbar.with(
													PromoCode.this
															.getApplicationContext())
													.type(SnackbarType.MULTI_LINE)
													.text(getString(R.string.server_error))
													.actionLabel("Okay")
													.actionColor(
															PromoCode.this
																	.getResources()
																	.getColor(
																			R.color.sb__button_text_color_yellow))
													.duration(
															Snackbar.SnackbarDuration.LENGTH_SHORT)
													.show(PromoCode.this);
										}
									}
								});
						AppController.getInstance().addToRequestQueue(
								promoCodeReq);
					} else {
						new SweetAlertDialog(PromoCode.this,
								SweetAlertDialog.WARNING_TYPE).setTitleText(
								"Please Enter a promocode").show();

					}
				}

			}
		});

	}

	public void onBackPressed() {
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem promocode_item = (MenuItem) menu
				.findItem(R.id.action_promocode);
		promocode_item.setVisible(false);
		return true;
	}
}
