package anipr.stampitgo.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GetData {
	Cursor cursor;
	Context context;
	String args[] = { DbHelper.NEARBYSTORECODE, DbHelper.NEARBYSTORENAME,
			DbHelper.STORE_CATEGORY, DbHelper.DISTANCE, DbHelper.LATLONG,
			DbHelper.LOCALITYCODE, DbHelper.REWARDMESSAGE };

	public GetData(Context context) {
		this.context = context;
	}

	List<NearByCard> getNearByStoresFromDb(String filterCategory) {
		List<NearByCard> nearBylist = new ArrayList<NearByCard>();
		SQLiteDatabase dbRead = DbHelper.getInstance(context.getApplicationContext()).getReadableDatabase();

		if (filterCategory.equals(AppController.CATEGORIES[0])) {
			cursor = dbRead.query(DbHelper.NEAR_BY_TABLE, args, null, null,
					null, null, DbHelper.DISTANCE);
		} else {
			cursor = dbRead.query(DbHelper.NEAR_BY_TABLE, args,
					DbHelper.STORE_CATEGORY + "=?",
					new String[] { filterCategory }, null, null,
					DbHelper.DISTANCE);
		}
		if (cursor.moveToFirst()) {
			do {
				String restaurant_name = cursor.getString(cursor
						.getColumnIndex(DbHelper.NEARBYSTORENAME));
				String code = cursor.getString(cursor
						.getColumnIndex(DbHelper.NEARBYSTORECODE));
				String category = cursor.getString(cursor
						.getColumnIndex(DbHelper.STORE_CATEGORY));
				String rewardDetails = cursor.getString(cursor
						.getColumnIndex(DbHelper.REWARDMESSAGE));
				String distance = cursor.getString(cursor
						.getColumnIndex(DbHelper.DISTANCE));
				String locality = cursor.getString(cursor
						.getColumnIndex(DbHelper.LOCALITYCODE));
				nearBylist.add(new NearByCard(restaurant_name, code, category,
						distance, rewardDetails, locality));

			} while (cursor.moveToNext());
		}
		cursor.close();
		
		return nearBylist;

	}

	class NearByCard {
		String storeName, storeCode, category, rewardDetails, distance,
				locality;

		NearByCard(String storeName, String code, String category,
				String distance, String rewardDetails, String locality) {
			this.storeName = storeName;
			this.storeCode = code;
			this.category = category;
			this.distance = distance;
			this.locality = locality;
			this.rewardDetails = rewardDetails;
		}
	}
}
