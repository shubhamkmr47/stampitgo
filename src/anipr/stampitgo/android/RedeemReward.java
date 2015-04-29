package anipr.stampitgo.android;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.android.common.activities.SampleActivityBase;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.enums.SnackbarType;
import com.squareup.picasso.Picasso;

public class RedeemReward extends SampleActivityBase {

	public static final String REDEEMING = "active";
	public static final String EXPIRED = "true_expired";
	private TextView rewardMessage;
	private String rewardId, rewardStoreCode;
	private ImageView rewardStoreCover, rewardStoreLogo;
	private Button redeemButton;
	private LinearLayout redeemNowBtn;
	private SQLiteDatabase dbRead, dbWrite;
	private DbHelper dbHelper;
	private DateUtility dateUtility;
	String[] monthNames = { "January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November",
			"December" };
	private String tag = getClass().getSimpleName();
	private TextView rewardName;
	private FrameLayout timerFrame, activeFrame, redeemdFrame;
	private TextView mins;
	private TextView secs;
	private String rewardMessageText;
	private String rewardNameText;
	private String rewardStoreNameText;
	private SweetAlertDialog pDialog;
	private UiLifecycleHelper uiHelper;
	private RelativeLayout fbShare;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.redeem_reward);
		dbHelper = DbHelper.getInstance(getApplicationContext());
		dbRead = dbHelper.getReadableDatabase();
		dbWrite = dbHelper.getWritableDatabase();
		Intent i = getIntent();
		dateUtility = new DateUtility();
		rewardId = i.getStringExtra(DbHelper.REWARDID);
		uiHelper = new UiLifecycleHelper(this, null);
		fbShare = (RelativeLayout) findViewById(R.id.response_page_fbshare);
		fbShare.setOnClickListener(fbShareListener);
		Cursor rewardDetailCursor = dbRead.rawQuery("select * from "
				+ DbHelper.REWARDS_TABLE + " where " + DbHelper.REWARDID
				+ " = '" + rewardId + "' ;", null);
		if (rewardDetailCursor.moveToFirst()) {
			rewardStoreCode = rewardDetailCursor.getString(rewardDetailCursor
					.getColumnIndex(DbHelper.REWARDSTORECODE));
			rewardMessageText = rewardDetailCursor.getString(rewardDetailCursor
					.getColumnIndex(DbHelper.REWARDDETAILS));
			rewardNameText = rewardDetailCursor.getString(rewardDetailCursor
					.getColumnIndex(DbHelper.REWARDNAME));
			rewardStoreNameText = rewardDetailCursor
					.getString(rewardDetailCursor
							.getColumnIndex(DbHelper.REWARDSTORE));
		}
		rewardDetailCursor.close();
		rewardStoreCover = (ImageView) findViewById(R.id.reward_store_cover);
		rewardStoreLogo = (ImageView) findViewById(R.id.reward_store_logo);
		TextView rewardStore = (TextView) findViewById(R.id.redeem_page_store_name);
		// timer = (TextView) findViewById(R.id.timer);
		rewardName = (TextView) findViewById(R.id.redeem_page_reward_name);
		rewardMessage = (TextView) findViewById(R.id.redeem_page_reward_details);
		rewardStore.setText(rewardStoreNameText);
		rewardName.setText(rewardNameText);
		rewardMessage.setText(rewardMessageText);
		getImages();

		redeemButton = (Button) findViewById(R.id.redeem_button);
		redeemNowBtn = (LinearLayout) findViewById(R.id.stop_timer);
		timerFrame = (FrameLayout) findViewById(R.id.timerFrame);
		activeFrame = (FrameLayout) findViewById(R.id.active_frame);
		redeemdFrame = (FrameLayout) findViewById(R.id.redeemed_frame);

	}

	private void getImages() {
		File coverFile = new File(this.getApplicationInfo().dataDir + "/app_"
				+ AppController.COVER_DIR, rewardStoreCode);
		if (coverFile.exists()) {
			Picasso.with(this).load(coverFile).into(rewardStoreCover);
		} else {
			DbHelper dhelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase db = dhelper.getReadableDatabase();
			String query = "select * from " + DbHelper.RESTAURANT_TABLE
					+ " where " + DbHelper.STORECODE + " = ? ;";
			Cursor c = db.rawQuery(query, new String[] { rewardStoreCode });
			if (c.moveToFirst()) {
				Log.e("image downlaod", "Downloading image"
						+ rewardStoreNameText);
				new DownloadImagesVolley(this, c.getString(c
						.getColumnIndex(DbHelper.COVER_URL)), rewardStoreCode,
						AppController.COVER_DIR).makeRequest();
			}
			c.close();

		}
		File logoFile = new File(this.getApplicationInfo().dataDir + "/app_"
				+ AppController.LOGO_DIR, rewardStoreCode);
		if (logoFile.exists()) {
			Picasso.with(this).load(logoFile).into(rewardStoreLogo);
		} else {
			DbHelper dhelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase db = dhelper.getReadableDatabase();
			String query = "select " + DbHelper.LOGO_URL + " from "
					+ DbHelper.RESTAURANT_TABLE + " where "
					+ DbHelper.STORECODE + " = ? ;";
			Cursor c = db.rawQuery(query, new String[] { rewardStoreCode });
			if (c.moveToFirst()) {
				Log.e("image downlaod", "Downloading image" + rewardNameText);
				new DownloadImagesVolley(this, c.getString(c
						.getColumnIndex(DbHelper.LOGO_URL)), rewardStoreCode,
						AppController.LOGO_DIR).makeRequest();
			}
			c.close();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
		redeemReward();
	}

	void redeemReward() {
		dbWrite = dbHelper.getWritableDatabase();
		dbRead = dbHelper.getReadableDatabase();
		Cursor c = dbRead.rawQuery("select * from " + DbHelper.REWARDS_TABLE
				+ " where " + DbHelper.REWARDID + " = '" + rewardId + "' ;",
				null);
		if (c.moveToFirst()) {
			if (c.getString(c.getColumnIndex(DbHelper.REWARDSTATE)).equals(
					RedeemReward.REDEEMING)) {
				timerFrame = (FrameLayout) findViewById(R.id.timerFrame);
				activeFrame.setVisibility(View.GONE);
				redeemButton.setVisibility(View.GONE);
				TextView code = (TextView) findViewById(R.id.code);
				code.setText(c.getString(c.getColumnIndex(DbHelper.REWARDCODE)));
				redeemNowBtn.setVisibility(View.VISIBLE);
				timerFrame.setVisibility(View.VISIBLE);
				mins = (TextView) findViewById(R.id.mins);
				secs = (TextView) findViewById(R.id.secs);
				mins.setVisibility(View.VISIBLE);
				secs.setVisibility(View.VISIBLE);
				Calendar expiryTime = Calendar.getInstance();
				expiryTime.setTime(dateUtility.makeDateFromLocalDateString(c
						.getString(c.getColumnIndex(DbHelper.USED_ON))));
				if (((expiryTime.getTimeInMillis()) + (30 * 60 * 1000))
						- (System.currentTimeMillis()) > 0) {

					new CountDownTimer((expiryTime.getTimeInMillis()
							+ (30 * 60 * 1000) - System.currentTimeMillis()),
							1000) {

						public void onTick(long millisUntilFinished) {
							// mins.setText("" + ((millisUntilFinished) / 1000)
							// / 60);
							if ((millisUntilFinished / 1000) / 60 > 9) {
								mins.setText("" + (millisUntilFinished / 1000)
										/ 60);
							} else {
								mins.setText("0" + (millisUntilFinished / 1000)
										/ 60);
							}

							if ((millisUntilFinished / 1000) % 60 > 9) {
								secs.setText("" + (millisUntilFinished / 1000)
										% 60);
							} else {
								secs.setText("0" + (millisUntilFinished / 1000)
										% 60);
							}

						}

						public void onFinish() {
							timerFrame.setVisibility(View.GONE);
							FrameLayout redeemedFrame = (FrameLayout) findViewById(R.id.redeemed_frame);
							redeemedFrame.setVisibility(View.VISIBLE);
							TextView message = (TextView) findViewById(R.id.message);
							message.setText("Message");
							rewardMessage.setText("reward reedeemed");

						}
					}.start();

				} else {
					ContentValues cv = new ContentValues();
					cv.put(DbHelper.REWARDSTATE, "true");
					dbWrite.update(DbHelper.REWARDS_TABLE, cv,
							DbHelper.REWARDID + " = ?",
							new String[] { rewardId });
					redeemReward();
				}

				redeemNowBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						new SweetAlertDialog(RedeemReward.this,
								SweetAlertDialog.WARNING_TYPE)
								.setTitleText("Are you sure?")
								.setContentText("Your Reward will be Redeemed!")
								.setConfirmText("Yes")
								.setConfirmClickListener(
										new SweetAlertDialog.OnSweetClickListener() {
											@Override
											public void onClick(
													SweetAlertDialog sDialog) {
												dbWrite = dbHelper
														.getWritableDatabase();
												ContentValues cv = new ContentValues();
												cv.put(DbHelper.REWARDSTATE,
														"true");
												dbWrite.update(
														DbHelper.REWARDS_TABLE,
														cv,
														DbHelper.REWARDID
																+ " = ?",
														new String[] { rewardId });
												timerFrame
														.setVisibility(View.GONE);
												FrameLayout redeemedFrame = (FrameLayout) findViewById(R.id.redeemed_frame);
												redeemedFrame
														.setVisibility(View.VISIBLE);
												TextView message = (TextView) findViewById(R.id.message);
												message.setText("Reward Redeemed");
												redeemReward();
												sDialog.setTitleText(
														"Redeemed!")
														.setContentText(
																"Your reward has been redeemed!")
														.setConfirmText("OK")
														.setConfirmClickListener(
																null)
														.changeAlertType(
																SweetAlertDialog.SUCCESS_TYPE);
												sDialog.dismissWithAnimation();
												onResume();
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
				});
			} else if (c.getString(c.getColumnIndex(DbHelper.REWARDSTATE))
					.equals("false")) {
				timerFrame.setVisibility(View.GONE);
				redeemButton.setVisibility(View.VISIBLE);
				redeemButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						redeem(rewardId);
					}
				});
			} else if (c.getString(c.getColumnIndex(DbHelper.REWARDSTATE))
					.equals(RedeemReward.EXPIRED)) {

				activeFrame.setVisibility(View.GONE);
				redeemdFrame.setVisibility(View.VISIBLE);
				TextView message = (TextView) findViewById(R.id.message);
				Calendar expiryTime = Calendar.getInstance();
				Log.i("passing",
						c.getString(c.getColumnIndex(DbHelper.USED_ON)));
				expiryTime.setTime(dateUtility.makeDateFromLocalDateString(c
						.getString(c.getColumnIndex(DbHelper.EXPIRESON))));
				message.setText(new DateUtility().makeExpiryDate(expiryTime
						.getTime()));
				ImageView errorImage = (ImageView) findViewById(R.id.redeemed_frame_image);
				errorImage.setImageResource(R.drawable.ic_icon_reward_expired);
			} else {
				try {
					activeFrame.setVisibility(View.GONE);
					redeemdFrame.setVisibility(View.VISIBLE);
					TextView message = (TextView) findViewById(R.id.message);
					Calendar expiryTime = Calendar.getInstance();
					Log.i("passing",
							c.getString(c.getColumnIndex(DbHelper.USED_ON)));
					expiryTime.setTime(dateUtility
							.makeDateFromLocalDateString(c.getString(c
									.getColumnIndex(DbHelper.USED_ON))));
					message.setText("Used "
							+ dateUtility.getFriendlyDateString(expiryTime
									.getTime()));
					ImageView errorImage = (ImageView) findViewById(R.id.redeemed_frame_image);
					errorImage.setImageResource(R.drawable.ic_icon_reward_used);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		c.close();
	}

	private void redeem(final String rewardId) {
		final SweetAlertDialog d = new SweetAlertDialog(RedeemReward.this,
				SweetAlertDialog.WARNING_TYPE);
		d.setTitleText("Are you sure?")
				.setContentText(getString(R.string.redeemWarningMessage))
				.setConfirmText("Yes")
				.setConfirmClickListener(
						new SweetAlertDialog.OnSweetClickListener() {
							@Override
							public void onClick(final SweetAlertDialog sDialog) {
								try {

									pDialog = new SweetAlertDialog(
											RedeemReward.this,
											SweetAlertDialog.PROGRESS_TYPE);
									pDialog.getProgressHelper().setBarColor(
											getResources().getColor(
													R.color.primary_color));
									pDialog.setTitleText("Please Wait...");
									pDialog.setCancelable(false);
									pDialog.show();
									CustomParamRequest redeemRewardReq = new CustomParamRequest(
											Method.GET,
											ComUtility.ConnectionString
													+ "/redeem-reward/"
													+ rewardId,
											AppController.cookie, null, null,
											new Response.Listener<String>() {

												@Override
												public void onResponse(
														String arg0) {
													Log.d(tag, arg0);
													try {
														if (new JSONObject(arg0)
																.getString(
																		"code")
																.equals("1")) {
															DbHelper dHelper = DbHelper
																	.getInstance(getApplicationContext());
															SQLiteDatabase dbWrite = dHelper
																	.getReadableDatabase();
															ContentValues cv = new ContentValues();
															cv.put(DbHelper.REWARDSTATE,
																	REDEEMING);
															cv.put(DbHelper.USED_ON,
																	Calendar.getInstance()
																			.getTime()
																			.toString());
															Log.i("UsedOn",
																	Calendar.getInstance()
																			.getTime()
																			.toString());
															dbWrite.update(
																	DbHelper.REWARDS_TABLE,
																	cv,
																	DbHelper.REWARDID
																			+ " = ?",
																	new String[] { rewardId });
															redeemReward();
															sDialog.setTitleText(
																	"Timer Started!")
																	.setContentText(
																			"Use your reward with in 30 mins")
																	.setConfirmText(
																			"OK")
																	.setConfirmClickListener(
																			null)
																	.changeAlertType(
																			SweetAlertDialog.SUCCESS_TYPE);
														} else {
															redeemButton
																	.setVisibility(View.GONE);
															FrameLayout redeemedFrame = (FrameLayout) findViewById(R.id.redeemed_frame);
															redeemedFrame
																	.setVisibility(View.VISIBLE);
															TextView message = (TextView) findViewById(R.id.message);

															String responseMsg = new JSONObject(
																	arg0)
																	.getString("message");
															rewardMessage
																	.setText(responseMsg);
															message.setText(responseMsg);
															if (responseMsg
																	.equals("This reward is Expired")) {
																// Expierd
																DbHelper dHelper = DbHelper
																		.getInstance(getApplicationContext());
																SQLiteDatabase dbWrite = dHelper
																		.getReadableDatabase();
																ContentValues cv = new ContentValues();

																Calendar redeemtime = Calendar
																		.getInstance();
																redeemtime
																		.setTimeInMillis(Calendar
																				.getInstance()
																				.getTimeInMillis() - 40 * 60 * 1000);
																cv.put(DbHelper.REWARDSTATE,
																		RedeemReward.EXPIRED);
																cv.put(DbHelper.USED_ON,
																		redeemtime
																				.getTime()
																				.toString());
																dbWrite.update(
																		DbHelper.REWARDS_TABLE,
																		cv,
																		DbHelper.REWARDID
																				+ " = ?",
																		new String[] { rewardId });
															} else {
																// Used
																DbHelper dHelper = DbHelper
																		.getInstance(getApplicationContext());
																SQLiteDatabase dbWrite = dHelper
																		.getReadableDatabase();
																ContentValues cv = new ContentValues();

																Calendar redeemtime = Calendar
																		.getInstance();
																redeemtime
																		.setTimeInMillis(Calendar
																				.getInstance()
																				.getTimeInMillis() - 40 * 60 * 1000);
																cv.put(DbHelper.REWARDSTATE,
																		"true");
																cv.put(DbHelper.USED_ON,
																		redeemtime
																				.getTime()
																				.toString());
																dbWrite.update(
																		DbHelper.REWARDS_TABLE,
																		cv,
																		DbHelper.REWARDID
																				+ " = ?",
																		new String[] { rewardId });
															}

															redeemReward();
															sDialog.setTitleText(
																	"Failed")
																	.setContentText(
																			new JSONObject(
																					arg0)
																					.getString("message"))
																	.setConfirmText(
																			"OK")
																	.setConfirmClickListener(
																			null)
																	.changeAlertType(
																			SweetAlertDialog.ERROR_TYPE);
														}
													} catch (JSONException e) {
														e.printStackTrace();
														sDialog.setTitleText(
																"Sorry")
																.setContentText(
																		getString(R.string.server_error))
																.setConfirmText(
																		"OK")
																.setConfirmClickListener(
																		null)
																.changeAlertType(
																		SweetAlertDialog.ERROR_TYPE);
													} finally {
														if (pDialog.isShowing()) {
															pDialog.dismissWithAnimation();
														}
													}

												}
											}, new Response.ErrorListener() {

												@Override
												public void onErrorResponse(
														VolleyError arg0) {
													if (pDialog.isShowing()) {
														pDialog.dismissWithAnimation();
													}
													if (d.isShowing()) {
														d.dismiss();
													}
													arg0.printStackTrace();
													if (arg0 instanceof NoConnectionError) {
														Snackbar.with(
																RedeemReward.this
																		.getApplicationContext())
																.type(SnackbarType.MULTI_LINE)
																.text(getString(R.string.internet_error_short))
																.actionLabel(
																		"Okay")
																.actionColor(
																		RedeemReward.this
																				.getResources()
																				.getColor(
																						R.color.sb__button_text_color_yellow))
																.duration(
																		Snackbar.SnackbarDuration.LENGTH_SHORT)
																.show(RedeemReward.this);
													} else {
														Snackbar.with(
																RedeemReward.this
																		.getApplicationContext())
																.type(SnackbarType.MULTI_LINE)
																.text(getString(R.string.server_error))
																.actionLabel(
																		"Okay")
																.actionColor(
																		RedeemReward.this
																				.getResources()
																				.getColor(
																						R.color.sb__button_text_color_yellow))
																.duration(
																		Snackbar.SnackbarDuration.LENGTH_SHORT)
																.show(RedeemReward.this);
													}

												}
											}, Priority.IMMEDIATE);
									AppController.getInstance()
											.addToRequestQueue(redeemRewardReq);
									redeemReward();

								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						})
				.showCancelButton(true)
				.setCancelClickListener(
						new SweetAlertDialog.OnSweetClickListener() {
							@Override
							public void onClick(SweetAlertDialog sDialog) {
								sDialog.cancel();
							}
						}).show();
	}

	OnClickListener fbShareListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			if (checkPermissions()) {
				if (FacebookDialog.canPresentShareDialog(
						getApplicationContext(),
						FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
					FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
							RedeemReward.this)
							.setLink("https://www.facebook.com/stampitgo")
							.setPicture(
									"https://fbcdn-sphotos-f-a.akamaihd.net/hphotos-ak-xap1/v/t1.0-9/1520648_1509812645966405_3960241966416745335_n.png?oh=0e6bd2ae7465ca804c05cb9bab73646a&oe=552DB815&__gda__=1429238217_baa058274d43f163c60f3990fe6ee8a9")
							.setDescription(
									"Hey friends! I just got " + rewardNameText
											+ " as a reward from Stampitgo at "
											+ rewardStoreNameText).build();
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
					RedeemReward.this, Arrays.asList("publish_actions")));
		}

		private boolean checkPermissions() {
			Session s = Session.openActiveSession(RedeemReward.this, true,
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

	private void publishFeedDialog() {
		Bundle params = new Bundle();
		params.putString("name", "Stampitgo");
		params.putString("caption", "stamp • redeem • repeat");
		params.putString("description",

		"Hey folks! I just got " + rewardNameText
				+ " as a reward from Stampitgo at " + rewardStoreNameText);
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
										RedeemReward.this
												.getApplicationContext())
										// context
										.type(SnackbarType.MULTI_LINE)
										.text("Thanks for Spreading the Love.")
										.actionColor(
												RedeemReward.this
														.getResources()
														.getColor(
																R.color.sb__button_text_color_yellow))
										.duration(
												Snackbar.SnackbarDuration.LENGTH_SHORT)
										.show(RedeemReward.this);
							} else {

							}
						} else if (error instanceof FacebookOperationCanceledException) {

						} else {
							// Generic, ex: network error
							Snackbar.with(
									RedeemReward.this.getApplicationContext())
									// context
									.type(SnackbarType.MULTI_LINE)
									.text(getString(R.string.internet_error_short))
									.actionColor(
											RedeemReward.this
													.getResources()
													.getColor(
															R.color.sb__button_text_color_yellow))
									.duration(
											Snackbar.SnackbarDuration.LENGTH_SHORT)
									.show(RedeemReward.this);
						}
					}

				}).build();
		feedDialog.show();
	}
}
