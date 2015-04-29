package anipr.stampitgo.android;

import java.io.File;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.android.slidingtabsbasic.RestaurantDetails;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	final String TAG = "Notification";
	private String tag = getClass().getSimpleName();

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {

			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				Log.e("Notification Error", "Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification("Deleted messages on server: "
						+ extras.toString());

			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {

				// Post notification of received message.
				handlePush(extras);
				Log.i(TAG, "Received: " + extras);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void handlePush(Bundle extras) {
		int actionCode = Integer.parseInt(extras.getString("code"));
		switch (actionCode) {
		case 1:
			sendNotification(extras);
			break;
		case 2:
			handleRewardRedeemPush(extras);
			break;
		case 3:
			updateStamp(extras);
			break;
		case 4:
			updateImage(extras);
			break;
		case 5:
			appUpdate(extras);
			break;
		case 6:
			deleteStore(extras);
			break;
		default:
			break;
		}
	}

	private void appUpdate(Bundle extras) {

	}

	private void deleteStore(Bundle extras) {
		Log.d(tag, extras.toString());
		try {
			int deletedRowCount = 0;
			String storeCode = extras.getString("store_code");
			SQLiteDatabase dbWrite = DbHelper.getInstance(
					getApplicationContext()).getWritableDatabase();
			deletedRowCount = deletedRowCount
					+ dbWrite.delete(DbHelper.RESTAURANT_TABLE,
							DbHelper.STORECODE + "= '" + storeCode + "'", null);
			deletedRowCount = deletedRowCount
					+ dbWrite.delete(DbHelper.REWARDS_TABLE,
							DbHelper.REWARDSTORECODE + "= '" + storeCode + "'",
							null);
			deletedRowCount = deletedRowCount
					+ dbWrite.delete(DbHelper.RESTAURANT_TABLE,
							DbHelper.NEARBYSTORECODE + "= '" + storeCode + "'",
							null);
			Log.d(tag, "deletedRowCount = " + deletedRowCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateStamp(Bundle extras) {
		String cookie = "0";
		SQLiteDatabase dbRead = DbHelper.getInstance(getApplicationContext())
				.getReadableDatabase();
		String[] columns = { DbHelper.LOGGEDUSERID, DbHelper.COOKIE };
		Cursor cursor = dbRead.query(DbHelper.LOGIN_DETAILS_TABLE, columns,
				null, null, null, null, null, null);

		try {
			cursor.moveToFirst();
			if (cursor != null && cursor.getCount() > 0) {
				cookie = cursor.getString(cursor
						.getColumnIndex(DbHelper.COOKIE));
				AppController.cookie = cursor.getString(cursor
						.getColumnIndex(DbHelper.COOKIE));
				AppController.userCode = cursor.getString(cursor
						.getColumnIndex(DbHelper.LOGGEDUSERID));

			} else {
				cookie = "0";
			}

			cursor.close();
		} catch (Exception e) {
			Log.e(tag, "Login Failed");
			cursor.close();
		}
		if (!cookie.equals("0")) {
			Cursor storeCursor = dbRead.query(DbHelper.RESTAURANT_TABLE, null,
					DbHelper.STORECODE + " = '" + extras.getString("store")
							+ "'", null, null, null, null);
			if (storeCursor.moveToFirst()) {
				new ComUtility(getApplicationContext()).getRewards();
				new ComUtility(getApplicationContext()).getStampCount(cookie);

			} else {
				new ComUtility(getApplicationContext()).getMyStamps();
			}
			try {

				mNotificationManager = (NotificationManager) this
						.getSystemService(Context.NOTIFICATION_SERVICE);
				PendingIntent contentIntent = null;
				new ComUtility(this).getNotifications();

				try {
					Intent i = new Intent(this, GetStarted.class);
					i.setAction(AppController.REWARDINTENT);
					contentIntent = PendingIntent.getActivity(this, 0, i, 0);

				} catch (Exception e) {
					e.printStackTrace();
				}

				Notification notification = new Notification.Builder(
						getApplicationContext())
						.setSmallIcon(R.drawable.stampitgo_white_icon)
						.setContentTitle(extras.getString("title"))
						.setContentText(extras.getString("message"))
						.setAutoCancel(true).setContentIntent(contentIntent)
						.build();
				notification.defaults |= Notification.DEFAULT_ALL;
				mNotificationManager.notify(NOTIFICATION_ID, notification);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private void updateImage(Bundle extras) {
		try {

			String storeCode = extras.getString("store_code");
			File coverFile = new File(getApplicationContext()
					.getApplicationInfo().dataDir
					+ "/app_"
					+ AppController.COVER_DIR, storeCode);
			if (coverFile.exists()) {
				coverFile.delete();
				// Downloading new CoverImage
				DbHelper dhelper = DbHelper
						.getInstance(getApplicationContext());
				SQLiteDatabase db = dhelper.getReadableDatabase();
				String query = "select * from " + DbHelper.RESTAURANT_TABLE
						+ " where " + DbHelper.STORECODE + " = ? ;";
				Cursor c = db.rawQuery(query, new String[] { storeCode });
				if (c.moveToFirst()) {
					Log.e("image downlaod", "Downloading image " + storeCode);
					new DownloadImagesVolley(getApplicationContext(),
							c.getString(c.getColumnIndex(DbHelper.COVER_URL)),
							storeCode, AppController.COVER_DIR).makeRequest();
				}
				c.close();
			}
			File logoFile = new File(getApplicationContext()
					.getApplicationInfo().dataDir
					+ "/app_"
					+ AppController.LOGO_DIR, storeCode);
			if (logoFile.exists()) {
				logoFile.delete();
				// Downloading new LogoImage
				DbHelper dhelper = DbHelper
						.getInstance(getApplicationContext());
				SQLiteDatabase db = dhelper.getReadableDatabase();
				String query = "select " + DbHelper.LOGO_URL + " from "
						+ DbHelper.RESTAURANT_TABLE + " where "
						+ DbHelper.STORECODE + " = ? ;";
				Cursor c = db.rawQuery(query, new String[] { storeCode });
				if (c.moveToFirst()) {
					Log.e("image downlaod", "Downloading image " + storeCode);
					new DownloadImagesVolley(getApplicationContext(),
							c.getString(c.getColumnIndex(DbHelper.LOGO_URL)),
							storeCode, AppController.LOGO_DIR).makeRequest();

				}
				c.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleRewardRedeemPush(Bundle extras) {
		try {
			String rewardId = extras.getString("reward_id");
			DbHelper dHelper = DbHelper.getInstance(getApplicationContext());
			SQLiteDatabase dbWrite = dHelper.getReadableDatabase();
			ContentValues cv = new ContentValues();
			cv.put(DbHelper.REWARDSTATE, "true");
			cv.put(DbHelper.USED_ON, Calendar.getInstance().getTime()
					.toString());
			Log.i("UsedOn", Calendar.getInstance().getTime().toString());
			int i = dbWrite.update(DbHelper.REWARDS_TABLE, cv,
					DbHelper.REWARDID + " = '" + rewardId + "'", null);
			Log.d("Reward Redeem Push", i + " rows updated");
			if (i > 0) {
				mNotificationManager = (NotificationManager) this
						.getSystemService(Context.NOTIFICATION_SERVICE);
				Intent intent = new Intent(this, RedeemReward.class);
				intent.putExtra(DbHelper.REWARDID, rewardId);
				PendingIntent contentIntent = PendingIntent.getActivity(this,
						0, intent, 0);

				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
						this).setSmallIcon(R.drawable.stampitgo_white_icon)
						.setContentTitle(" Reward Redeemed")
						.setAutoCancel(true)
						.setContentText(extras.getString("message"));

				mBuilder.setContentIntent(contentIntent);
				Notification notifiaction = mBuilder.build();
				notifiaction.defaults |= Notification.DEFAULT_ALL;
				mNotificationManager.notify(NOTIFICATION_ID, notifiaction);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, Notifications.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.stampitgo_white_icon)
				.setContentTitle("GCM ")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private void sendNotification(Bundle extras) {
		try {

			mNotificationManager = (NotificationManager) this
					.getSystemService(Context.NOTIFICATION_SERVICE);
			PendingIntent contentIntent = null;
			new ComUtility(this).getNotifications();
			if (extras.getString("source").equals("Stampitgo")) {
				contentIntent = PendingIntent.getActivity(this, 0, new Intent(
						this, Notifications.class), 0);
			} else {

				try {
					Intent i = new Intent(this, RestaurantDetails.class);
					String storeCode = new JSONObject(extras.getString("store"))
							.getString("code");
					Log.d(TAG, "PutExtra " + storeCode);
					i.putExtra(DbHelper.STORECODE, storeCode);
					i.setAction(AppController.REGULAR_OFFER_INTENT);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					contentIntent = PendingIntent.getActivity(this, 0, i, 0);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			Notification notification = new Notification.Builder(
					getApplicationContext())
					.setSmallIcon(R.drawable.stampitgo_white_icon)
					.setContentTitle(extras.getString("title"))
					.setContentText(extras.getString("message"))
					.setAutoCancel(true).setContentIntent(contentIntent)
					.build();
			notification.defaults |= Notification.DEFAULT_ALL;
			mNotificationManager.notify(NOTIFICATION_ID, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int getNotificationID() {
		final String LOCAL_NOTIFICATOIN_ID = "LOCAL_NOTIFICATION_ID";
		int lastNoficationID = AppController.myPrefs.getInt(
				LOCAL_NOTIFICATOIN_ID, 0);
		Editor editor = AppController.myPrefs.edit();
		editor.putInt(LOCAL_NOTIFICATOIN_ID, lastNoficationID + 1);
		editor.commit();
		return lastNoficationID + 1;
	}
}
