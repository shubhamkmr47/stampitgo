package anipr.stampitgo.android;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;

public class ScanResponse extends SampleActivityBase {
	private TextView storeName, stampCount, scanFailed;
	private FrameLayout successFrame, failureFrame;
	private Button feedback;
	private String storeCode;
	private RelativeLayout fbShare;
	private String store;
	private ImageView badSmiley, flatfaceSmiley, happySmiley, delightedSmiley;
	private String rating;
	private String scanCode;
	final String DEFAULT = "DEFAULT";
	private SweetAlertDialog sDialog;
	private String tag = getClass().getSimpleName();

	private UiLifecycleHelper uiHelper;
	private SweetAlertDialog pDialog;
	private ComUtility comUtility;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.scan_response_activity);
		uiHelper = new UiLifecycleHelper(this, null);
		pDialog = new SweetAlertDialog(ScanResponse.this,
				SweetAlertDialog.PROGRESS_TYPE);
		pDialog.getProgressHelper().setBarColor(
				getResources().getColor(R.color.primary_color));
		pDialog.setTitleText("Please Wait...");
		pDialog.setCancelable(false);
		pDialog.show();
		scanCode = getIntent().getStringExtra("SCAN_RESULT");
		try {
			varifyScan(scanCode);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.toString());
			e.printStackTrace();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

	}

	void varifyScan(String scanCode) {
		if (AppController.cookie.equals(AppController.GUEST_USER)) {
			failureFrame = (FrameLayout) findViewById(R.id.failure_frame);
			failureFrame.setVisibility(View.VISIBLE);
			scanFailed = (TextView) findViewById(R.id.scan_failed);
			scanFailed.setVisibility(View.VISIBLE);
			scanFailed.setText("Please login to Scan the card");
			pDialog.dismissWithAnimation();
		} else {
			successFrame = (FrameLayout) findViewById(R.id.success_frame);
			storeName = (TextView) findViewById(R.id.response_page_store_name);
			stampCount = (TextView) findViewById(R.id.response_page_stampcount);
			feedback = (Button) findViewById(R.id.response_page_feedback_btn);

			rating = DEFAULT;
			delightedSmiley = (ImageView) findViewById(R.id.delightedsmiley);
			delightedSmiley.setOnClickListener(smileyClickListener);
			happySmiley = (ImageView) findViewById(R.id.happysmiley);
			happySmiley.setOnClickListener(smileyClickListener);
			flatfaceSmiley = (ImageView) findViewById(R.id.flatfacesmiley);
			flatfaceSmiley.setOnClickListener(smileyClickListener);
			badSmiley = (ImageView) findViewById(R.id.badsmiley);
			badSmiley.setOnClickListener(smileyClickListener);
			fbShare = (RelativeLayout) findViewById(R.id.response_page_fbshare);
			// pb = (ProgressBar) findViewById(R.id.scan_response_pb);
			comUtility = new ComUtility(getApplicationContext());
			if (comUtility.isConnectingToInternet()) {
				try {
					Map<String, String> params = new HashMap<String, String>();
					params.put("code", scanCode);
					params.put("timestamp", String.valueOf(Calendar
							.getInstance().getTimeInMillis()));
					params.put("latlong",
							"[17.44496717556572,78.38570385718778]");
					JSONObject paramsObj = new JSONObject(params);
					String payload = paramsObj.toString();
					CustomParamRequest scanCodeCheckRequest = new CustomParamRequest(
							Method.POST, ComUtility.ConnectionString
									+ "/scan-code", AppController.cookie, null,
							payload, new Response.Listener<String>() {

								@Override
								public void onResponse(String arg0) {

									try {
										JSONObject scanObject = new JSONObject(
												arg0);
										if (scanObject.getString("code")
												.equals("1")) {
											successFrame
													.setVisibility(View.VISIBLE);
											storeName.setText(scanObject
													.getJSONObject("data")
													.getString("storeName"));
											store = scanObject.getJSONObject(
													"data").getString(
													"storeName");
											storeCode = scanObject
													.getJSONObject("data")
													.getString("storeCode");
											stampCount.setText(scanObject
													.getJSONObject("data")
													.getString("stamps"));
											SQLiteDatabase dbWrite = DbHelper
													.getInstance(
															getApplicationContext())
													.getWritableDatabase();
											SQLiteDatabase dbRead = DbHelper
													.getInstance(
															getApplicationContext())
													.getReadableDatabase();
											String query = "SELECT * FROM "
													+ DbHelper.RESTAURANT_TABLE
													+ " where "
													+ DbHelper.STORECODE
													+ " = "
													+ scanObject.getJSONObject(
															"data").getString(
															"storeCode") + " ;";
											Cursor cursor = dbRead.rawQuery(
													query, null);
											if (cursor.moveToFirst()) {
												ContentValues cv = new ContentValues();
												cv.put(DbHelper.USERSTAMPS,
														scanObject
																.getJSONObject(
																		"data")
																.getString(
																		"stamps"));
												int i = dbWrite
														.update(DbHelper.RESTAURANT_TABLE,
																cv,
																DbHelper.STORECODE
																		+ " = '"
																		+ storeCode
																		+ "'",
																null);
												Log.d("Response", i
														+ " Rows updated");
												if (i != 0) {
													comUtility
															.getStampCount(AppController.cookie);
												} else {
													Log.d("Response",
															"else called");
													comUtility.getMyStamps();
												}
											} else {
												Log.d("Response", "else called");
												comUtility.getMyStamps();
											}
											try {
												Log.d("reward", scanObject
														.getJSONObject("data")
														.getString("rewardMsg"));
												if (scanObject.getJSONObject(
														"data").getString(
														"rewardMsg") != null) {
													comUtility.getRewards();
												}
											} catch (JSONException e) {
												Log.d("ScanCodeResponse",
														e.toString());
											}
											feedback.setOnClickListener(shoutOutListener);
											fbShare.setOnClickListener(fbShareListener);
										} else {
											Log.d(tag, arg0);
											failureFrame = (FrameLayout) findViewById(R.id.failure_frame);
											failureFrame
													.setVisibility(View.VISIBLE);
											scanFailed = (TextView) findViewById(R.id.scan_failed);
											scanFailed
													.setVisibility(View.VISIBLE);
											scanFailed.setText(scanObject
													.getString("message"));
										}
									} catch (JSONException e) {
										Log.e("response", e.toString());
									} finally {
										if (pDialog.isShowing()) {
											pDialog.hide();
										}
									}
								}
							}, new Response.ErrorListener() {

								@Override
								public void onErrorResponse(VolleyError arg0) {

									failureFrame = (FrameLayout) findViewById(R.id.failure_frame);
									failureFrame.setVisibility(View.VISIBLE);
									scanFailed = (TextView) findViewById(R.id.scan_failed);
									scanFailed.setVisibility(View.VISIBLE);
									scanFailed
											.setText("There was a internet problem. Please try Again");
									if (pDialog.isShowing()) {
										pDialog.hide();
									}
								}
							}, Priority.IMMEDIATE);
					AppController.getInstance().addToRequestQueue(
							scanCodeCheckRequest);
				} catch (Exception e) {
					Log.e("repons", e.toString());
				}

			} else {

				failureFrame = (FrameLayout) findViewById(R.id.failure_frame);
				failureFrame.setVisibility(View.VISIBLE);
				scanFailed = (TextView) findViewById(R.id.scan_failed);
				scanFailed.setVisibility(View.VISIBLE);
				scanFailed.setText(getString(R.string.internet_error));
				if (pDialog.isShowing()) {
					pDialog.hide();
				}
			}

		}
	}

	OnClickListener fbShareListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (checkPermissions()) {
				if (FacebookDialog.canPresentShareDialog(
						getApplicationContext(),
						FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
					// Publish the post using the Share Dialog
					FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
							ScanResponse.this)
							.setLink("https://www.facebook.com/stampitgo")
							.setPicture(
									"https://fbcdn-sphotos-f-a.akamaihd.net/hphotos-ak-xap1/v/t1.0-9/1520648_1509812645966405_3960241966416745335_n.png?oh=0e6bd2ae7465ca804c05cb9bab73646a&oe=552DB815&__gda__=1429238217_baa058274d43f163c60f3990fe6ee8a9")
							.setDescription(
									"Hey friends! I just got rewarded with a stamp from Stampitgo at "
											+ store).build();
					uiHelper.trackPendingDialogCall(shareDialog.present());

				} else {
					// Fallback. For example, publish the post using the Feed
					// Dialog
					publishFeedDialog();
				}

			} else {
				requestPermissions();
			}
		}

		private void requestPermissions() {
			Session s = Session.getActiveSession();
			s.requestNewPublishPermissions(new NewPermissionsRequest(
					ScanResponse.this, Arrays.asList("publish_actions")));
		}

		private boolean checkPermissions() {
			Session s = Session.openActiveSession(ScanResponse.this, true,
					callback);
			if (s != null) {
				return s.getPermissions().contains("publish_actions");
			} else
				return false;
		}
	};
	Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (session.isOpened()) {
				Log.d("session opened", session.getAccessToken());
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		uiHelper.onActivityResult(requestCode, resultCode, intent,
				new FacebookDialog.Callback() {
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						Log.e("Activity",
								String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						Log.i("Activity", "Success!");

					}
				});

	}

	OnClickListener smileyClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.delightedsmiley:
				if (rating == "4") {
					rating = DEFAULT;
					delightedSmiley.setImageResource(R.drawable.delighted);
				} else {
					delightedSmiley
							.setImageResource(R.drawable.delighted_pressed);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "4";
				}
				break;

			case R.id.happysmiley:
				if (rating == "3") {
					happySmiley.setImageResource(R.drawable.happy);
					rating = DEFAULT;
				}

				else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy_pressed);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "3";
				}
				break;

			case R.id.flatfacesmiley:
				if (rating == "2") {
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					rating = DEFAULT;
				} else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley
							.setImageResource(R.drawable.flatface_pressed);
					badSmiley.setImageResource(R.drawable.bad);
					rating = "2";
				}
				break;

			case R.id.badsmiley:
				if (rating == "1") {
					badSmiley.setImageResource(R.drawable.bad);
					rating = DEFAULT;
				} else {
					delightedSmiley.setImageResource(R.drawable.delighted);
					happySmiley.setImageResource(R.drawable.happy);
					flatfaceSmiley.setImageResource(R.drawable.flatface);
					badSmiley.setImageResource(R.drawable.bad_pressed);
					rating = "1";
				}
				break;

			default:
				delightedSmiley.setImageResource(R.drawable.delighted);
				happySmiley.setImageResource(R.drawable.happy);
				flatfaceSmiley.setImageResource(R.drawable.flatface);
				badSmiley.setImageResource(R.drawable.bad);
				rating = "DEFAULT";
			}

		}
	};
	OnClickListener shoutOutListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!rating.equals("DEFAULT")) {
				;
				sDialog = new SweetAlertDialog(ScanResponse.this,
						SweetAlertDialog.PROGRESS_TYPE);
				sDialog.getProgressHelper().setBarColor(
						getResources().getColor(R.color.primary_color));
				sDialog.setTitleText("Please wait ");
				sDialog.setCancelable(false);
				sDialog.show();
				String FeedbackJson = "{\"storeCode\": \"" + storeCode
						+ "\",\"feedback\": {\"rating\": \"" + rating
						+ "\",\"comment\": \" \"}}";
				CustomParamRequest feedbackReq = new CustomParamRequest(
						Method.POST, ComUtility.ConnectionString + "/feedback",
						AppController.cookie, null, FeedbackJson,
						new Response.Listener<String>() {

							@Override
							public void onResponse(String arg0) {
								Log.d("FeedBack", arg0);
								sDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
								sDialog.setTitleText("Done!");
								sDialog.setContentText(getString(R.string.shout_out_response));
								sDialog.setConfirmText("Done");
								sDialog.setCancelable(true);
								sDialog.setConfirmClickListener(new OnSweetClickListener() {

									@Override
									public void onClick(
											SweetAlertDialog sweetAlertDialog) {
										finish();
									}
								});

							}
						}, new Response.ErrorListener() {

							@Override
							public void onErrorResponse(VolleyError arg0) {
								if (sDialog.isShowing()) {
									sDialog.dismiss();
								}
								if (arg0 instanceof NoConnectionError) {
									Snackbar.with(
											ScanResponse.this
													.getApplicationContext())
											.type(SnackbarType.MULTI_LINE)
											.text(getString(R.string.internet_error_short))
											.actionLabel("Okay")
											.actionColor(
													ScanResponse.this
															.getResources()
															.getColor(
																	R.color.sb__button_text_color_yellow))
											.duration(
													Snackbar.SnackbarDuration.LENGTH_SHORT)
											.show(ScanResponse.this);
								} else {
									Snackbar.with(
											ScanResponse.this
													.getApplicationContext())
											.type(SnackbarType.MULTI_LINE)
											.text(getString(R.string.server_error))
											.actionLabel("Okay")
											.actionColor(
													ScanResponse.this
															.getResources()
															.getColor(
																	R.color.sb__button_text_color_yellow))
											.duration(
													Snackbar.SnackbarDuration.LENGTH_SHORT)
											.show(ScanResponse.this);
								}
							}
						});

				AppController.getInstance().addToRequestQueue(feedbackReq);
			} else {
				Snackbar.with(ScanResponse.this.getApplicationContext())
						// context
						.type(SnackbarType.MULTI_LINE)
						.text("Please select atleast one Smiley")
						.actionLabel("Okay")
						.actionColor(
								ScanResponse.this.getResources().getColor(
										R.color.sb__button_text_color_yellow))
						.duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
						.show(ScanResponse.this);

			}

		}
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public void onBackPressed() {
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem scan_item = (MenuItem) menu.findItem(R.id.action_scan);
		scan_item.setVisible(false);
		return true;
	}

	private void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", "Stampitgo");
		params.putString("caption", "stamp • redeem • repeat");
		params.putString("description",
				"Hey friends! I just got rewarded with a stamp from Stampitgo at "
						+ store);
		params.putString("link", "https://www.facebook.com/stampitgo");
		params.putString("picture",
				"https://s3-ap-southeast-1.amazonaws.com/stampitgo-assets/app/stampitgo.png");
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(this,
				Session.getActiveSession(), params)).setOnCompleteListener(
				new OnCompleteListener() {

					@Override
					public void onComplete(Bundle values,
							FacebookException error) {
						if (error == null) {
							// When the story is posted, echo the success
							// and the post Id.
							final String postId = values.getString("post_id");
							if (postId != null) {
								Snackbar.with(
										ScanResponse.this
												.getApplicationContext())
										// context
										.type(SnackbarType.MULTI_LINE)
										.text("Thanks for Spreading the Love.")
										.actionColor(
												ScanResponse.this
														.getResources()
														.getColor(
																R.color.sb__button_text_color_yellow))
										.duration(
												Snackbar.SnackbarDuration.LENGTH_SHORT)
										.show(ScanResponse.this);
							} else {

							}
						} else if (error instanceof FacebookOperationCanceledException) {

						} else {
							// Generic, ex: network error
							Snackbar.with(
									ScanResponse.this.getApplicationContext())
									// context
									.type(SnackbarType.MULTI_LINE)
									.text(getString(R.string.internet_error_short))
									.actionColor(
											ScanResponse.this
													.getResources()
													.getColor(
															R.color.sb__button_text_color_yellow))
									.duration(
											Snackbar.SnackbarDuration.LENGTH_SHORT)
									.show(ScanResponse.this);
						}
					}

				}).build();
		feedDialog.show();
	}
}
