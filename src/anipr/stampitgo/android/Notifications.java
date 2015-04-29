package anipr.stampitgo.android;

import java.io.File;
import java.text.ParseException;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.slidingtabsbasic.RestaurantDetails;
import com.squareup.picasso.Picasso;

public class Notifications extends SampleActivityBase {
	private final String STAMPITGO = "stampitgo";
	private Context context;
	private ListView notificationsList;
	private NotificationAdapter notifyAdapter;
	private String[] args = { DbHelper.NOTIFICATION_ID,
			DbHelper.NOTIFICATION_ORIGIN, DbHelper.NOTIFICATION_STORE,
			DbHelper.NOTIFICATION_STORE_NAME, DbHelper.NOTIFICATION_LOGO_URL,
			DbHelper.NOTIFICATION_TITLE, DbHelper.NOTIFICATION_MESSSAGE,
			DbHelper.NOTIFICATION_STATUS, DbHelper.NOTIFICATION_RECIEVE_TIME };
	private Cursor notificationCursor;
	private View coverFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_activity);
		context = this;
		coverFrame = (FrameLayout) findViewById(R.id.notifications_page_coverframe);
		coverFrame.setVisibility(View.INVISIBLE);
		notificationsList = (ListView) findViewById(R.id.notifications_list);
		notificationCursor = DbHelper
				.getInstance(getApplicationContext())
				.getReadableDatabase()
				.query(DbHelper.NOTIFICATION_TABLE, args, null, null, null,
						null, DbHelper.NOTIFICATION_RECIEVE_TIME + " DESC");
		if (AppController.cookie != AppController.GUEST_USER) {
			Log.i("Notification count", "Notification count "
					+ notificationCursor.getCount());
			if (notificationCursor.getCount() != 0) {
				notifyAdapter = new NotificationAdapter(context,
						notificationCursor, 0);

				notificationsList.setAdapter(notifyAdapter);
				notificationsList
						.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {
								Cursor cursor = notifyAdapter.getCursor();
								if (cursor.moveToPosition(position)) {
									Log.d(getClass().getName(), "position");
									Log.d(getClass().getName(),
											cursor.getString(cursor
													.getColumnIndex(DbHelper.NOTIFICATION_STORE)));
									if (!cursor
											.getString(
													cursor.getColumnIndex(DbHelper.NOTIFICATION_STORE))
											.equals(STAMPITGO)) {
										Intent i = new Intent(
												Notifications.this,
												RestaurantDetails.class);
										i.putExtra(
												DbHelper.STORECODE,
												cursor.getString(cursor
														.getColumnIndex(DbHelper.NOTIFICATION_STORE)));
										i.setAction(AppController.REGULAR_OFFER_INTENT);
										startActivity(i);
									} else{
										if (cursor
												.getString(
														cursor.getColumnIndex(DbHelper.NOTIFICATION_ORIGIN))
												.equals("profile")) {
											Intent i = new Intent(
													Notifications.this,
													Profile.class);
											startActivity(i);
										}else{
											Log.d("Notification click", "Cursor val "+cursor
												.getString(
														cursor.getColumnIndex(DbHelper.NOTIFICATION_ORIGIN)));
										}
									} 
								}
							}
						});
			} else {
				ImageView error_frame_image = (ImageView) findViewById(R.id.notifications_page_error_frame_image);
				error_frame_image
						.setImageResource(R.drawable.ic_default_character);
				coverFrame.setVisibility(View.VISIBLE);
				TextView errorText = (TextView) findViewById(R.id.notifications_page_error_msg);
				errorText.setText("See all your notifications here");
			}

		} else {
			ImageView error_frame_image = (ImageView) findViewById(R.id.notifications_page_error_frame_image);
			error_frame_image.setImageResource(R.drawable.ic_default_character);
			coverFrame.setVisibility(View.VISIBLE);
			TextView errorText = (TextView) findViewById(R.id.notifications_page_error_msg);
			errorText.setText("Login and check your notifications here");
		}

	}

	class NotificationAdapter extends CursorAdapter {

		public NotificationAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);

		}

		@Override
		public void bindView(View convertView, Context mContext, Cursor cursor) {

			ViewHolder holder = (ViewHolder) convertView.getTag();

			String notificationTitle = ""
					+ cursor.getString(cursor
							.getColumnIndex(DbHelper.NOTIFICATION_TITLE));
			String notificationMessage = ""
					+ cursor.getString(cursor
							.getColumnIndex(DbHelper.NOTIFICATION_MESSSAGE));
			String storeName = ""
					+ cursor.getString(cursor
							.getColumnIndex(DbHelper.NOTIFICATION_STORE_NAME));
			String storeCode = cursor.getString(cursor
					.getColumnIndex(DbHelper.NOTIFICATION_STORE));
			String logoUrl = cursor.getString(cursor
					.getColumnIndex(DbHelper.NOTIFICATION_LOGO_URL));
			String status = cursor.getString(cursor
					.getColumnIndex(DbHelper.NOTIFICATION_STATUS));
			if (status.equals(DbHelper.NOTIFICATION_STATUS_UNREAD)) {
				// convertView
				// .setBackgroundColor(R.color.primary_color);
				SQLiteDatabase dbWrite = DbHelper.getInstance(
						getApplicationContext()).getWritableDatabase();
				ContentValues cv = new ContentValues();
				cv.put(DbHelper.NOTIFICATION_STATUS,
						DbHelper.NOTIFICATION_STATUS_READ);
				dbWrite.update(
						DbHelper.NOTIFICATION_TABLE,
						cv,
						DbHelper.NOTIFICATION_ID
								+ " = '"
								+ cursor.getString(cursor
										.getColumnIndex(DbHelper.NOTIFICATION_ID))
								+ "'", null);
			} else {
				// convertView
				// .setBackgroundColor(R.color.notification_read);
			}
			holder.notificationTitle.setText(notificationTitle);
			holder.notificationMessage.setText(notificationMessage);
			if (storeName.equals(STAMPITGO)) {
				holder.storeName.setText("Stampitgo Team");
				holder.logo.setImageResource(R.drawable.ic_launcher);
			} else {

				holder.storeName.setText(storeName);
				Log.i("Logo file name", logoUrl.split("/")[4].split("\\.")[0]);
				File logoFile = new File(
						Notifications.this.getApplicationInfo().dataDir
								+ "/app_" + AppController.LOGO_DIR, storeCode);
				if (logoFile.exists()) {
					Picasso.with(Notifications.this).load(logoFile)
							.into(holder.logo);
				} else {
					Log.e("image downlaod", "Downloading image");
					new DownloadImagesVolley(Notifications.this, logoUrl,
							storeCode, AppController.LOGO_DIR).makeRequest();
				}

			}
			String time = ""
					+ cursor.getString(cursor
							.getColumnIndex(DbHelper.NOTIFICATION_RECIEVE_TIME));
			Log.i(notificationTitle, time);

			try {
				holder.notificationRecieveTime.setText(""
						+ new DateUtility().notificationDate(new DateUtility()
								.convertSerevrDatetoLocalDate(time)));
			} catch (ParseException e) {
				e.printStackTrace();
				holder.notificationRecieveTime.setText(""
						+ new DateUtility().notificationDate(Calendar
								.getInstance().getTime()));
			} catch (Exception e) {
				holder.notificationRecieveTime.setText(""
						+ new DateUtility().notificationDate(Calendar
								.getInstance().getTime()));
			}

		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {

			View view = LayoutInflater.from(mContext).inflate(
					R.layout.notifications_list_item, arg2, false);

			ViewHolder viewHolder = new ViewHolder(view);
			view.setTag(viewHolder);
			return view;
		}

		private class ViewHolder {

			protected ImageView logo;
			protected TextView notificationTitle, notificationMessage,
					storeName, notificationRecieveTime;

			public ViewHolder(View convertView) {

				logo = (ImageView) convertView
						.findViewById(R.id.notification_logo);
				notificationTitle = (TextView) convertView
						.findViewById(R.id.title);
				notificationMessage = (TextView) convertView

				.findViewById(R.id.msg);
				storeName = (TextView) convertView
						.findViewById(R.id.store_name);
				notificationRecieveTime = (TextView) convertView
						.findViewById(R.id.notification_time);

			}
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
		MenuItem notification_item = (MenuItem) menu
				.findItem(R.id.action_notification);
		notification_item.setVisible(false);
		return true;
	}
}
