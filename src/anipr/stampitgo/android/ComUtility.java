package anipr.stampitgo.android;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Request.Priority;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.facebook.Session;

public class ComUtility {
	// public static String ConnectionString = "http://192.168.1.21:3010/api";
	// public static String ConnectionString =
	// "http://dev-rtd-io-f7br5bh8x4b3.runscope.net/api";
	// public static String ConnectionString = "http://dev.rtd.io/api";
	// public static String ConnectionString =
	// public static String ConnectionString =
	// "https://test-stampitgo-com-p8piwyc8fq29.runscope.net/api";
	// public static String ConnectionString = "http://test.stampitgo.com/api";
	 public static String ConnectionString = "http://api.stampitgo.com/api";
//	public static String ConnectionString = "http://test-stampitgo-com-q413btusdyw9.runscope.net/api";
	private SQLiteDatabase dbWrite, dbRead;
	// static final String SENDER_ID = "511945442603";

	private DbHelper dhelper;
	private Context context;
	private String latlong = "";
	private String tag = getClass().getSimpleName();

	public ComUtility(Context context) {
		super();
		this.context = context;
		dhelper = DbHelper.getInstance(context.getApplicationContext());
	}

	String varifyOfflineScans() {
		String result = null;
		String[] columns = { DbHelper.SCAN_CODE, DbHelper.SCANTIME };
		if (dbRead.isOpen()) {

		} else {
			dbRead = dhelper.getReadableDatabase();
		}
		Cursor cursor = dbRead.query(DbHelper.OFFLINE_STAMPS_TABLE, columns,
				null, null, null, null, null, null);
		cursor.moveToFirst();
		if (cursor != null && cursor.getCount() > 0) {
			result = cursor.getColumnName(cursor
					.getColumnIndex(DbHelper.SCAN_CODE))
					+ cursor.getColumnName(cursor
							.getColumnIndex(DbHelper.SCANTIME));
		} else {

			result = "none";
		}

		cursor.close();
		return result;
	}

	public int insertOfflineStamp(String scanCode) {

		dbWrite = dhelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(DbHelper.SCAN_CODE, scanCode);
		cv.put(DbHelper.SCANTIME, System.currentTimeMillis());
		dbWrite.insert(DbHelper.OFFLINE_STAMPS_TABLE, null, cv);
		return 1;
	}

	public void varifyStamp() {

	}

	public void Logout() {
		try {
			dbWrite = dhelper.getWritableDatabase();
			dbWrite.delete(DbHelper.RESTAURANT_TABLE, null, null);
			dbWrite.delete(DbHelper.LOGIN_DETAILS_TABLE, null, null);
			dbWrite.delete(DbHelper.REWARDS_TABLE, null, null);
			dbWrite.delete(DbHelper.NOTIFICATION_TABLE, null, null);
			SharedPreferences preferences = context.getSharedPreferences(
					"report", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.clear();
			editor.commit();
			if (Session.getActiveSession() != null) {
				Session.getActiveSession().closeAndClearTokenInformation();
			}
			Intent i = new Intent(context, GetStarted.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			((Activity) context).finish();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int getLoginCredentials() {

		int result = 0;
		String[] columns = { DbHelper.LOGGEDUSERID, DbHelper.COOKIE };
		dbRead = dhelper.getReadableDatabase();
		Cursor cursor = dbRead.query(DbHelper.LOGIN_DETAILS_TABLE, columns,
				null, null, null, null, null, null);

		try {
			cursor.moveToFirst();
			if (cursor != null && cursor.getCount() > 0) {
				AppController.cookie = cursor.getString(cursor
						.getColumnIndex(DbHelper.COOKIE));

				AppController.userCode = cursor.getString(cursor
						.getColumnIndex(DbHelper.LOGGEDUSERID));
				result = 1;
			} else {
				result = 0;
			}

			cursor.close();
		} catch (Exception e) {
			Toast.makeText(context, "Login Failed" + e, Toast.LENGTH_LONG)
					.show();
			cursor.close();
		}
		return result;

	}

	public void getStampCount(String cookie) {
		Log.i(tag, "Updating Stamp Count");
		CustomParamRequest stampRequest = new CustomParamRequest(Method.GET,
				ComUtility.ConnectionString + "/stamp-count", cookie, null,
				null, new Response.Listener<String>() {

					@Override
					public void onResponse(String stringResponse) {
						try {
							JSONObject response = new JSONObject(stringResponse);

							if (response.getString("code").equals("1")) {
								dbWrite = dhelper.getWritableDatabase();
								JSONArray dataArray = response
										.getJSONArray("data");
								for (int i = 0; i < dataArray.length(); i++) {
									JSONObject storeObject = dataArray
											.getJSONObject(i);
									String storecode = storeObject
											.getString("store_code");
									String stampCount = storeObject
											.getString("stamps");
									ContentValues cv = new ContentValues();
									cv.put(DbHelper.STORECODE, storecode);
									cv.put(DbHelper.USERSTAMPS, stampCount);
									int rowsUpdated = dbWrite.update(
											DbHelper.RESTAURANT_TABLE, cv,
											DbHelper.STORECODE + "= '"
													+ storecode + "'", null);
									Log.d("Stamp Cpunt Update", rowsUpdated
											+ " rows updated");
								}
								Intent i = new Intent();
								i.setAction("com.stampitgo.stampsupdate");
								LocalBroadcastManager.getInstance(context)
										.sendBroadcast(i);

								Log.e("Intend Sent", "mystamps");
							} else {
								Logout();

							}

						} catch (JSONException e) {
							Log.e("Parsing Error ", e.toString());
							e.printStackTrace();

						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						Log.e("volley error", error.toString());
					}
				}, Priority.LOW);
		AppController.getInstance().addToRequestQueue(stampRequest);
	}

	public String getMyStamps() {
		Log.i(tag, "Geting MyStamps");
		CustomParamRequest stampRequest = new CustomParamRequest(Method.GET,
				ComUtility.ConnectionString + "/stamp-cards",
				AppController.cookie, null, null,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String stringResponse) {
						try {
							Log.i(tag, "My Stamps Response Recieved");
							final JSONObject response = new JSONObject(
									stringResponse);

							if (response.getString("code").equals("1")) {

								new Thread(new Runnable() {
									public void run() {
										try {
											dbWrite = dhelper
													.getWritableDatabase();

											JSONArray dataArray = response
													.getJSONArray("data");
											for (int i = 0; i < dataArray
													.length(); i++) {
												JSONObject restObject = dataArray
														.getJSONObject(i);
												String restaurant_name = restObject
														.getJSONObject("store")
														.getString("name");
												String code = restObject
														.getJSONObject("store")
														.getString("code");

												dbWrite.delete(
														DbHelper.RESTAURANT_TABLE,
														DbHelper.STORECODE
																+ "=?",
														new String[] { code });
												String localityValue = restObject
														.getJSONObject("store")
														.getJSONObject(
																"address")
														.getJSONObject(
																"locality")
														.getString("name");
												String localityCode = restObject
														.getJSONObject("store")
														.getJSONObject(
																"address")
														.getJSONObject(
																"locality")
														.getString("code");
												String address = restObject
														.getJSONObject("store")
														.getJSONObject(
																"address")
														.getString(
																"address_line1")
														+ "," + localityValue;
												String storeCategory = restObject
														.getJSONObject("store")
														.getString("category");
												String user_stamps = restObject
														.getString("stamps");
												int reward_stamps = (0 + Integer.parseInt(restObject
														.getString("total")));
												String offer = ""
														+ restObject
																.getString("best_reward");
												String logoUrl = restObject
														.getJSONObject("store")
														.getString("logo");
												String coverUrl = restObject
														.getJSONObject("store")
														.getString(
																"background_image");

												ContentValues cv = new ContentValues();
												cv.put(DbHelper.STORECODE, code);
												cv.put(DbHelper.STORENAME,
														restaurant_name);
												cv.put(DbHelper.ADDRESS,
														address);
												cv.put(DbHelper.LOCALITYVALUE,
														localityValue);
												cv.put(DbHelper.LOCALITYCODE,
														localityCode);
												cv.put(DbHelper.USERSTAMPS,
														user_stamps);
												cv.put(DbHelper.NEXTREWARDAT,
														reward_stamps);
												cv.put(DbHelper.REWARDMESSAGE,
														offer);
												cv.put(DbHelper.OFFERS, offer);
												cv.put(DbHelper.LOGO_URL,
														logoUrl);
												cv.put(DbHelper.STORE_CATEGORY,
														storeCategory);
												cv.put(DbHelper.COVER_URL,
														coverUrl);
												cv.put(DbHelper.INSERT_TIME,
														Calendar.getInstance()
																.getTimeInMillis());
												dbWrite.insert(
														DbHelper.RESTAURANT_TABLE,
														null, cv);

												ContextWrapper cw = new ContextWrapper(
														context);
												File coverDirectory = cw
														.getDir(AppController.COVER_DIR,
																Context.MODE_PRIVATE);
												File coverImage = new File(
														coverDirectory, code);
												if (coverImage.exists()) {

												} else {
													new DownloadImagesVolley(
															context,
															coverUrl,
															code,
															AppController.COVER_DIR)
															.makeRequest();

												}
												File logoDirectory = cw.getDir(
														AppController.LOGO_DIR,
														Context.MODE_PRIVATE);
												File logoImage = new File(
														logoDirectory, code);
												if (logoImage.exists()) {

												} else {
													new DownloadImagesVolley(
															context,
															logoUrl,
															code,
															AppController.LOGO_DIR)
															.makeRequest();
												}

											}

											Intent i = new Intent();
											i.setAction("com.stampitgo.stampsupdate");
											LocalBroadcastManager.getInstance(
													context).sendBroadcast(i);

											Log.e("Intend Sent", "mystamps");
										} catch (JSONException e) {

										}

									}
								}).start();

							} else {
								Logout();

							}

						} catch (JSONException e) {
							e.printStackTrace();
							Log.e("volley error", e.toString());
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {

						Log.e("volley error", error.toString());
					}
				}, Priority.LOW);
		AppController.getInstance().addToRequestQueue(stampRequest);

		return "success";
	}

	public void getRewards() {
		Log.i(tag, "Geting Rewards");
		CustomParamRequest jsObjRequest = new CustomParamRequest(Method.GET,
				ComUtility.ConnectionString + "/rewards", AppController.cookie,
				null, null, new Response.Listener<String>() {
					@Override
					public void onResponse(final String stringResponse) {
						Log.i(tag, "Rewards Response Recieved");

						new Thread(new Runnable() {
							public void run() {
								try {
									JSONObject response = new JSONObject(
											stringResponse);
									if (response.getString("code").equals("1")) {

										dbWrite = dhelper.getWritableDatabase();

										JSONArray rewardsArray = response
												.getJSONArray("data");
										for (int i = 0; i < rewardsArray
												.length(); i++) {
											JSONObject reward = rewardsArray
													.getJSONObject(i);
											dbRead = dhelper
													.getReadableDatabase();
											if (!dbRead
													.rawQuery(
															"select * from "
																	+ DbHelper.REWARDS_TABLE
																	+ " where "
																	+ DbHelper.REWARDID
																	+ " = '"
																	+ reward.getString("_id")
																	+ "' ;",
															null).moveToFirst()) {

												ContentValues cv = new ContentValues();
												cv.put(DbHelper.REWARDSTORECODE,
														reward.getJSONObject(
																"store")
																.getString(
																		"code"));
												cv.put(DbHelper.REWARDSTORE,
														reward.getJSONObject(
																"store")
																.getString(
																		"name"));
												cv.put(DbHelper.REWARDID,
														reward.getString("_id"));
												cv.put(DbHelper.REWARDNAME,
														reward.getString("title"));
												cv.put(DbHelper.REWARDDETAILS,
														reward.getString("description"));
												cv.put(DbHelper.INSERT_TIME,
														Calendar.getInstance()
																.getTimeInMillis());
												cv.put(DbHelper.REWARDCODE,
														reward.getString("code"));
												try {
													Date parsed;
													try {
														parsed = new DateUtility()
																.convertSerevrDatetoLocalDate(reward
																		.getString("expiry"));
														Calendar localtime = Calendar
																.getInstance();
														localtime
																.setTime(parsed);
														cv.put(DbHelper.EXPIRESON,
																parsed.toString());
													} catch (ParseException e) {
														// TODO Auto-generated
														// catch block
														e.printStackTrace();
													}

												} catch (JSONException e) {
													Log.e("Reward data No value for expiery",
															e.toString());
												}
												try {
													if (reward.getString(
															"redeem_status")
															.equals("true")) {
														;
														Date parsed;
														try {
															parsed = new DateUtility()
																	.convertSerevrDatetoLocalDate(reward
																			.getString("redeem_time"));
															Calendar localtime = Calendar
																	.getInstance();
															localtime
																	.setTime(parsed);
															cv.put(DbHelper.USED_ON,
																	parsed.toString());
														} catch (ParseException e) {
															// TODO
															// Auto-generated
															// catch block
															e.printStackTrace();
														}

													} else {
														cv.put(DbHelper.USED_ON,
																"");
													}
												} catch (JSONException e) {
													Log.e(e + "", "error");
												}

												cv.put(DbHelper.REWARDSTATE,
														reward.getString("redeem_status"));
												dbWrite.insert(
														DbHelper.REWARDS_TABLE,
														null, cv);

											} else {
												Log.d("reward fetching",
														"reward Exists ");
											}

										}

										Intent i = new Intent();
										i.setAction("com.stampitgo.rewards_update");
										LocalBroadcastManager.getInstance(
												context).sendBroadcast(i);
										Log.e("Intend Sent", "Rewards");
									} else {
										Logout();
									}

								} catch (JSONException e) {

									e.printStackTrace();

								}
							}
						}).start();

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e("Voller Error fething NearMe", arg0.toString());
					}
				}, Priority.LOW);

		AppController.getInstance().addToRequestQueue(jsObjRequest);

	}

	public void getNearByStores(String latlong1) throws JSONException {
		this.latlong = latlong1;
		JSONObject jsonBody = new JSONObject(
				"{\"type\":\"nearby\",\"params\":{\"latlong\":[" + latlong
						+ "],\"radius\":15}}");
		Log.i("Near By request Fired from", context.getClass().getSimpleName());
		UpdateNearByStores storeUpdateRequest = new UpdateNearByStores(
				Method.POST, ComUtility.ConnectionString + "/stores/search",
				AppController.cookie, jsonBody,
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(final JSONObject response) {

						new Thread(new Runnable() {
							public void run() {
								try {
									if (response.getString("code").equals("1")) {
										dbWrite = dhelper.getWritableDatabase();
										dbWrite.delete(DbHelper.NEAR_BY_TABLE,
												null, null);
										JSONArray dataArray = response
												.getJSONArray("data");
										if (dataArray.length() != 0) {
											for (int i = 0; i < dataArray
													.length(); i++) {
												JSONObject store = dataArray
														.getJSONObject(i);
												JSONArray location = store
														.getJSONArray("geo_location");
												double lat = location
														.getDouble(0);
												double lon = location
														.getDouble(1);

												String[] getlatlong = latlong
														.split(",");
												Location storeLoc = new Location(
														"Store Location");
												storeLoc.setLatitude(lat);
												storeLoc.setLongitude(lon);
												Location currentLoc = new Location(
														"Current Location");
												currentLoc.setLatitude(Double
														.parseDouble(getlatlong[0]));
												currentLoc.setLongitude(Double
														.parseDouble(getlatlong[1]));
												double distance = Math.round((currentLoc
														.distanceTo(storeLoc) / 1000) * 100.0) / 100.0;
												ContentValues cv = new ContentValues();
												cv.put(DbHelper.NEARBYSTORECODE,
														store.getString("code"));
												cv.put(DbHelper.NEARBYSTORENAME,
														store.getString("name"));
												cv.put(DbHelper.STORE_CATEGORY,
														store.getJSONObject(
																"category")
																.getString(
																		"name"));
												cv.put(DbHelper.DISTANCE,
														distance);
												cv.put(DbHelper.LATLONG,
														latlong);
												cv.put(DbHelper.LOCALITYCODE,
														store.getJSONObject(
																"locality")
																.getString(
																		"name"));

												try {
													cv.put(DbHelper.REWARDMESSAGE,
															store.getJSONObject(
																	"active_card")
																	.getString(
																			"best_reward"));
												} catch (JSONException e) {
													Log.e("Near By",
															"No active card");
												}

												Log.e(getClass()
														.getSimpleName(),
														"Near By Store Added "
																+ " : "
																+ store.getString("code"));
												dbWrite.insert(
														DbHelper.NEAR_BY_TABLE,
														null, cv);

											}
										}
										Intent i = new Intent();
										i.setAction("com.stampitgo.near_by_update");
										LocalBroadcastManager.getInstance(
												context).sendBroadcast(i);
										Log.d("NearByupdateIntent", "Sent");
									}
								} catch (JSONException e) {
									Log.e("Unable parse JSON ", e.toString());
								}
							}
						}).start();

					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("Unable to update near by", error.toString());
					}
				});
		AppController.getInstance().addToRequestQueue(storeUpdateRequest);
	}

	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	public void getNotifications() {
		Log.i(tag, "Geting Notifications");
		CustomParamRequest notificationReq = new CustomParamRequest(Method.GET,
				ComUtility.ConnectionString + "/user-notifications",
				AppController.cookie, null, null,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String arg0) {
						try {
							Log.i(tag, "Notificationss Response Recieved");
							JSONObject response = new JSONObject(arg0);
							if (response.getString("code").equals("1")) {
								SQLiteDatabase dbWrite = dhelper
										.getWritableDatabase();
								JSONArray data = response.getJSONArray("data");
								for (int i = data.length() - 1; i >= 0; i--) {
									JSONObject notificationObj = data
											.getJSONObject(i);
									String notificationId = notificationObj
											.getString("_id");
									dbRead = dhelper.getReadableDatabase();
									if (!dbRead
											.rawQuery(
													"select * from "
															+ DbHelper.NOTIFICATION_TABLE
															+ " where "
															+ DbHelper.NOTIFICATION_ID
															+ " = '"
															+ notificationId
															+ "' ;", null)
											.moveToFirst()) {
										ContentValues cv = new ContentValues();

										cv.put("" + DbHelper.NOTIFICATION_ID,
												notificationObj
														.getString("_id"));
										cv.put(""
												+ DbHelper.NOTIFICATION_ORIGIN,
												notificationObj
														.getString("origin"));
										try {
											cv.put(DbHelper.NOTIFICATION_STORE,
													""
															+ notificationObj
																	.getJSONObject(
																			"store")
																	.getString(
																			"code"));
											cv.put(DbHelper.NOTIFICATION_STORE_NAME,
													""
															+ notificationObj
																	.getJSONObject(
																			"store")
																	.getString(
																			"name"));
											cv.put(DbHelper.NOTIFICATION_LOGO_URL,
													""
															+ notificationObj
																	.getJSONObject(
																			"store")
																	.getString(
																			"logo"));
										} catch (JSONException e) {
											Log.e("Notificaion",
													"No store found");
											cv.put(DbHelper.NOTIFICATION_STORE,
													"stampitgo");
											cv.put(DbHelper.NOTIFICATION_STORE_NAME,
													"stampitgo");
											cv.put(DbHelper.NOTIFICATION_LOGO_URL,
													"stampitgo");
										}
										cv.put(DbHelper.NOTIFICATION_TITLE,
												notificationObj
														.getString("title"));
										cv.put(DbHelper.NOTIFICATION_MESSSAGE,
												notificationObj
														.getString("message"));
										Date notificationCreated = Calendar
												.getInstance().getTime();
										try {
											notificationCreated = new DateUtility()
													.convertSerevrDatetoLocalDate(notificationObj
															.getString("created"));
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										if (((notificationCreated.getTime() - Calendar
												.getInstance()
												.getTimeInMillis()) / 86400000) == 0) {
											Log.i("Reading notification",
													"Unread"
															+ (notificationCreated
																	.getTime() - Calendar
																	.getInstance()
																	.getTimeInMillis())
															/ 86400000);
											cv.put(DbHelper.NOTIFICATION_STATUS,
													DbHelper.NOTIFICATION_STATUS_UNREAD);
										} else {
											Log.i("Reading notification",
													"Read"
															+ (notificationCreated
																	.getTime() - Calendar
																	.getInstance()
																	.getTimeInMillis())
															/ 86400000);
											cv.put(DbHelper.NOTIFICATION_STATUS,
													DbHelper.NOTIFICATION_STATUS_READ);
										}
										cv.put(DbHelper.NOTIFICATION_RECIEVE_TIME,
												notificationObj
														.getString("created"));
										dbWrite.insert(
												DbHelper.NOTIFICATION_TABLE,
												null, cv);
									} else {
									}

								}
							}
						} catch (JSONException e) {
							Log.e("NOtifications", e.toString());
						}

					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError arg0) {
						Log.e("NOtifications", arg0.toString());
					}
				}, Priority.NORMAL);
		AppController.getInstance().addToRequestQueue(notificationReq);
	}

	void makeEntryNotifications(int notificationId, String title, String msg) {
		ContentValues cv = new ContentValues();

		cv.put(DbHelper.NOTIFICATION_ID, notificationId);
		cv.put(DbHelper.NOTIFICATION_ORIGIN, "profile");
		cv.put(DbHelper.NOTIFICATION_STORE, "stampitgo");
		cv.put(DbHelper.NOTIFICATION_STORE_NAME, "stampitgo");
		cv.put(DbHelper.NOTIFICATION_LOGO_URL, "stampitgo");

		cv.put(DbHelper.NOTIFICATION_TITLE, title);
		cv.put(DbHelper.NOTIFICATION_MESSSAGE, msg);
		cv.put(DbHelper.NOTIFICATION_STATUS,
				DbHelper.NOTIFICATION_STATUS_UNREAD);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		TimeZone timeZone = TimeZone.getTimeZone("UTC"); 
		formatter.setTimeZone(timeZone);
		String[] newFormat = formatter.format(
				Calendar.getInstance(Locale.getDefault()).getTime()).split(" ");
		cv.put(DbHelper.NOTIFICATION_RECIEVE_TIME, newFormat[0] + "T"
				+ newFormat[1]);

		DbHelper.getInstance(context).getWritableDatabase()
				.insert(DbHelper.NOTIFICATION_TABLE, null, cv);
	}
}
