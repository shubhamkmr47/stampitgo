package anipr.stampitgo.android;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class Profile extends SampleActivityBase {

	TextView email;
	EditText user_name, phone_no, dob;
	Button update, changePassword;
	TextView birth_date, birth_month, birth_year;
	String uname, uemail, uphone, udate;
	SharedPreferences myPrefs;
	private String prefName = "report";
	static final int DATE_PICKER_ID = 1111;
	Calendar mycal = Calendar.getInstance();
	RelativeLayout birth_layout;
	Map<String, String> params;
	private int noOfTimesCalled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		email = (TextView) findViewById(R.id.profileEmail);
		phone_no = (EditText) findViewById(R.id.profilePhone);

		user_name = (EditText) findViewById(R.id.profileName);
		update = (Button) findViewById(R.id.update);
		changePassword = (Button) findViewById(R.id.change_password);
		birth_date = (TextView) findViewById(R.id.profile_date);
		birth_month = (TextView) findViewById(R.id.profile_month);
		birth_year = (TextView) findViewById(R.id.profile_year);
		birth_layout = (RelativeLayout) findViewById(R.id.birthday_layout);

		

	}

	@Override
	protected void onResume() {
		super.onResume();
		myPrefs = getSharedPreferences(prefName, MODE_PRIVATE);
		uname = myPrefs.getString("name", "");
		uphone = myPrefs.getString("phoneno", "");
		uemail = myPrefs.getString("emailid", "");
		udate = myPrefs.getString("date", "");
		params = new HashMap<String, String>();
		if (uname.length() == 0) {
			user_name.setHint("Enter your name");

		} else {
			user_name.setText(uname);
		}

		if (uphone.length() == 0) {
			phone_no.setHint("Enter your phone no");
		} else {
			phone_no.setText(uphone);
		}

		if (uemail.length() == 0) {
			email.setText("Enter your email");
		} else {
			email.setText(uemail);
		}
		TextView birth = (TextView) findViewById(R.id.birth);

		if (udate.length() == 0) {

			birth.setVisibility(View.VISIBLE);
			birth_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new DatePickerDialog(Profile.this, picListner, mycal
							.get(Calendar.YEAR), mycal.get(Calendar.MONTH),
							mycal.get(Calendar.DAY_OF_MONTH)).show();

				}
			});
		} else {
			birth.setText("Birthday");
			birth_date.setText(udate.split("-")[2]);
			birth_month.setText(udate.split("-")[1]);
			birth_year.setText(udate.split("-")[0].substring(2));

		}

		update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (user_name.getText().toString().length() == 0) {
				} else {
					params.put("name", user_name.getText().toString());
				}
				if (phone_no.getText().toString().length() == 0) {

				} else {
					params.put("mobile", phone_no.getText().toString());
				}
				fireRequest();
			}
		});
		changePassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Profile.this, ChangePassword.class);
				startActivity(i);
			}
		});
	}

	private void updateLabel() {

		String myFormat = "yyyy/MM/dd"; // In which you need put here
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		birth_date.setText(sdf.format(mycal.getTime()).split("/")[2]);
		birth_month.setText(sdf.format(mycal.getTime()).split("/")[1]);
		birth_year.setText(sdf.format(mycal.getTime()).split("/")[0].substring(
				2, 4));
	}

	DatePickerDialog.OnDateSetListener picListner = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, final int year,
				final int monthOfYear, final int dayOfMonth) {

			if (noOfTimesCalled % 2 == 0) {

				Log.d("Working", "datepicker");
				mycal.set(Calendar.YEAR, year);
				mycal.set(Calendar.MONTH, monthOfYear);
				mycal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				if (mycal
						.getTime()
						.toString()
						.substring(0, 11)
						.equals(Calendar.getInstance().getTime().toString()
								.substring(0, 11))) {
					Log.d("date ", "today's date can't be set");
				} else {
					if ((mycal.getTimeInMillis() > Calendar.getInstance()
							.getTimeInMillis())) {

						Snackbar.with(Profile.this.getApplicationContext())
								.type(SnackbarType.MULTI_LINE)
								.text("Dont be crazy! U cant be born in future!")
								.actionLabel("Okay")
								.actionColor(
										Profile.this
												.getResources()
												.getColor(
														R.color.sb__button_text_color_yellow))
								.duration(
										Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(Profile.this);
					} else {
						new SweetAlertDialog(Profile.this,
								SweetAlertDialog.WARNING_TYPE)
								.setTitleText("Are you Sure?")
								.setContentText(
										"It seems you are trying to update your b'day. You will get only one shot at this. Are you sure it is your right birthdate?")
								.setConfirmText("Yes,I'm sure.")
								.setConfirmClickListener(
										new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(
													SweetAlertDialog sDialog) {

												updateLabel();
												params.put(
														"birthday",
														String.valueOf(mycal
																.getTimeInMillis()));
												sDialog.dismissWithAnimation();

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

					}

				}
			}
			noOfTimesCalled++;

		}
	};

	void fireRequest() {
		Log.d("params", params.toString()+params.get("birthday")+params.get("phone")+params.get("name"));
		String payload = new JSONObject(params).toString();
		CustomParamRequest updateprofileRequest = new CustomParamRequest(
				Method.POST, ComUtility.ConnectionString + "/profile",
				AppController.cookie, null, payload,
				
				
				new Response.Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						
						try {
							JSONObject response = new JSONObject(arg0);
							if (response.getString("code").equals("1")) {
								SharedPreferences.Editor editor = myPrefs
										.edit();
								editor.putString("name",
										user_name.getText().toString())
										.commit();
								editor.putString("phoneno",
										phone_no.getText().toString()).commit();
								editor.putString("emailid",
										email.getText().toString()).commit();
								if (udate.length() == 0) {
									if ((mycal.getTimeInMillis() < Calendar
											.getInstance().getTimeInMillis())) {
										if (!(mycal.getTime().toString()
												.substring(0, 11)
												.equals(Calendar.getInstance()
														.getTime().toString()
														.substring(0, 11)))) {
											editor.putString("date",
													mycal.get(Calendar.YEAR)+"-"+mycal.get(Calendar.MONTH)+"-"+mycal.get(Calendar.DAY_OF_MONTH)).commit();
										}
									}
								}
								Snackbar.with(Profile.this.getApplicationContext())
								.type(SnackbarType.MULTI_LINE)
								.text("Profile Updated")
								.actionLabel("Okay")
								.actionColor(
										Profile.this.getResources().getColor(
												R.color.sb__button_text_color_yellow))
								.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(Profile.this);
							}else{
								Snackbar.with(Profile.this.getApplicationContext())
								.type(SnackbarType.MULTI_LINE)
								.text(response.getString("message"))
								.actionLabel("Okay")
								.actionColor(
										Profile.this.getResources().getColor(
												R.color.sb__button_text_color_yellow))
								.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
								.show(Profile.this);
								
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						onResume();
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e( "Profile update error",arg0.toString()+" : "+arg0.getCause()+" : "+arg0.getMessage());
						
						
						
						if(arg0 instanceof NoConnectionError) {
							Snackbar.with(Profile.this.getApplicationContext())
							.type(SnackbarType.MULTI_LINE)
							.text(getString(R.string.internet_error_short))
							.actionLabel("Okay")
							.actionColor(
									Profile.this.getResources().getColor(
											R.color.sb__button_text_color_yellow))
							.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
							.show(Profile.this);
							    } else{
							    	Snackbar.with(Profile.this.getApplicationContext())
									.type(SnackbarType.MULTI_LINE)
									.text(getString(R.string.server_error))
									.actionLabel("Okay")
									.actionColor(
											Profile.this.getResources().getColor(
													R.color.sb__button_text_color_yellow))
									.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
									.show(Profile.this);
							    }
						}
				});
		if (params.size() != 0) {
			AppController.getInstance().addToRequestQueue(updateprofileRequest);
		} else {
				Snackbar.with(Profile.this.getApplicationContext())
			.type(SnackbarType.MULTI_LINE)
			.text("Add some fields first!"
					)
			.actionLabel("Okay")
			.actionColor(
					Profile.this.getResources().getColor(
							R.color.sb__button_text_color_yellow))
			.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
			.show(Profile.this);
		}
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
		MenuItem profile_item = (MenuItem) menu.findItem(R.id.action_profile);
		profile_item.setVisible(false);
		return true;
	}
}
