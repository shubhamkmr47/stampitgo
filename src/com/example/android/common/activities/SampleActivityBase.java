/*
\ * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.common.activities;

import java.lang.reflect.Field;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.TextView;
import anipr.stampitgo.android.AboutUs;
import anipr.stampitgo.android.AppController;
import anipr.stampitgo.android.ComUtility;
import anipr.stampitgo.android.DbHelper;
import anipr.stampitgo.android.GetStarted;
import anipr.stampitgo.android.Notifications;
import anipr.stampitgo.android.Profile;
import anipr.stampitgo.android.PromoCode;
import anipr.stampitgo.android.R;
import anipr.stampitgo.android.ScanResponse;
import anipr.stampitgo.android.ShoutOut;

import com.google.zxing.client.android.CaptureActivity;

public class SampleActivityBase extends FragmentActivity {

	public static final String TAG = "SampleActivityBase";

	@Override
	protected void onStart() {
		super.onStart();
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getOverflowMenu();
		invalidateOptionsMenu();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		Log.d(TAG, "SampleActivityBase OnCreate() called");
		if (AppController.cookie == null) {

			Log.d("Appcontroler null", "MianActivity Onresume");
			if (new ComUtility(this).getLoginCredentials() != 1) {
				AppController.cookie = AppController.GUEST_USER;
			}
			;
		} else {
			Log.d("Appcontroler Cookie", AppController.cookie);
		}

		super.onCreate(arg0);
	}

	@Override
	public void startActivity(Intent intent) {

		super.startActivity(intent);
		overridePendingTransition(R.anim.right_in, R.anim.left_out);
	}

	protected void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_notification) {
			openNotifications();
			return true;
		} else if (itemId == R.id.action_scan) {
			openScan();
			return true;
		} else if (itemId == R.id.action_logout) {
			if (AppController.cookie.equals(AppController.GUEST_USER)) {
				Intent i = new Intent(this, GetStarted.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
			} else {
				ComUtility comUtility = new ComUtility(this);
				comUtility.Logout();
			}
			return true;
		} else if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		} else if (itemId == R.id.action_profile) {
			openProfile();
			return true;

		} else if (itemId == R.id.action_aboutUs) {
			openAboutUS();
			return true;
		} else if (itemId == R.id.action_feedback) {
			Intent i = new Intent(this, ShoutOut.class);
			i.putExtra(DbHelper.STORECODE, getString(R.string.app_name));
			startActivity(i);
			return true;

		} else if (itemId == R.id.action_promocode) {
			Intent i = new Intent(this, PromoCode.class);
			startActivity(i);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}

	}

	private void openAboutUS() {
		Intent i = new Intent(this, AboutUs.class);
		startActivity(i);
	}

	protected void openProfile() {
		Intent i = new Intent(this, Profile.class);
		startActivity(i);
	}

	protected void openNotifications() {
		Intent i = new Intent(this, Notifications.class);
		startActivity(i);

	}

	public void openScan() {

		Intent intent = new Intent(this, CaptureActivity.class);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
		startActivityForResult(intent, 0);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_itemlist, menu);
		MenuItem userid_item = (MenuItem) menu.findItem(R.id.action_profile);
		MenuItem logout_item = (MenuItem) menu.findItem(R.id.action_logout);
		try {
			if (!AppController.cookie.equals(AppController.GUEST_USER)) {
				userid_item.setTitle(AppController.userCode + " Profile");
				logout_item.setTitle("Logout");
			} else {

				userid_item.setVisible(false);
				logout_item.setTitle("Login");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		final View notification_view = menu.findItem(R.id.action_notification)
				.getActionView();
		TextView badgeNo = (TextView) notification_view
				.findViewById(R.id.notification_count);

		try {
			if (DbHelper
					.getInstance(getApplicationContext())
					.getReadableDatabase()
					.query(DbHelper.NOTIFICATION_TABLE,
							null,
							DbHelper.NOTIFICATION_STATUS + " = '"
									+ DbHelper.NOTIFICATION_STATUS_UNREAD + "'",
							null, null, null, null).getCount() != 0) {
				badgeNo.setVisibility(View.VISIBLE);
				badgeNo.setText(""
						+ DbHelper
								.getInstance(getApplicationContext())
								.getReadableDatabase()
								.query(DbHelper.NOTIFICATION_TABLE,
										null,
										DbHelper.NOTIFICATION_STATUS
												+ " = '"
												+ DbHelper.NOTIFICATION_STATUS_UNREAD
												+ "'", null, null, null, null)
								.getCount());
				Log.i("Unread notifications",
						" : "
								+ DbHelper
										.getInstance(getApplicationContext())
										.getReadableDatabase()
										.query(DbHelper.NOTIFICATION_TABLE,
												null,
												DbHelper.NOTIFICATION_STATUS
														+ " = '"
														+ DbHelper.NOTIFICATION_STATUS_UNREAD
														+ "'", null, null,
												null, null).getCount());
			} else {
				badgeNo.setVisibility(View.INVISIBLE);
			}
		} catch (Exception e) {
			Log.e("Notification read in superActivity", "Error");
			e.printStackTrace();
		}

		notification_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openNotifications();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			Intent i = new Intent(this, ScanResponse.class);
			i.putExtra("SCAN_RESULT", intent.getStringExtra("SCAN_RESULT"));
			startActivity(i);

		} else if (resultCode == RESULT_CANCELED) {
		}

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_in, R.anim.right_out);
	}
}
