package anipr.stampitgo.android;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

	Context context;
	public static DbHelper sInstance;
	
	
	public static final String INSERT_TIME = "timestamp";
	// Restaurant table Columns
	public static final String C_ID = "_id";
	public static final String STORECODE = "storecode";
	public static final String STORENAME = "storename";
	public static final String STORE_CATEGORY = "category";
	public static final String ADDRESS = "address";
	public static final String GEOCODE = "geocode";
	public static final String LATLONG = "latlong";
	public static final String LOCALITYVALUE = "localityvalue";
	public static final String LOCALITYCODE = "localitycode";
	public static final String USERSTAMPS = "userStamps";
	public static final String NEXTREWARDAT = "nextRewardAt";
	public static final String REWARDMESSAGE = "rewardMesage";
	public static final String OFFERS = "offers";
	public static final String LOGO_URL = "logo";
	public static final String COVER_URL = "coverUrl";
	// login details table
	public static final String LOGGEDUSERID = "userid";
	public static final String COOKIE = "cookie";
	// OffLineDetails table
	public static final String SCAN_CODE = "ScanCode";
	// reward details table
	public static final String REWARDSTORECODE = "RewardStorecode";
	public static final String SCANTIME = "scanTime";
	public static final String REWARDSTORE = "RewardStore";
	public static final String REWARDID = "_id";
	public static final String REWARDNAME = "rewardName";
	public static final String REWARDDETAILS = "rewardDetails";
	public static final String EXPIRESON = "Validity";
	public static final String REWARDSTATE = "state";
	public static final String USED_ON = "ExpieryTime";
	public static final String REWARDCODE = "RewardCode";

	// user details table columns
	public static final String USERCODE = "UserCode";
	public static final String USERNAME = "UserName";
	public static final String USEREMAIL = "UserEmail";
	public static final String USERDOB = "UserDOB";
	public static final String USERPHONENO = "UserPhNo";
	public static final String USERFB_ACCESSTOKEN = "FbAccessToken";
	public static final String USERPROFILE_PIC_URL = "ProfilePicURL";

	// nearby table columns
	public static final String NEARBYSTORECODE = "storecode";
	public static final String NEARBYSTORENAME = "storename";
	public static final String DISTANCE = "Distance";
	// Database Version
	private static final int DATABASE_VERSION = 3;

	// Database Name
	private static final String DATABASE_NAME = "RestaurantData";

	// TableNames
	public static final String RESTAURANT_TABLE = "RestaurantTable";
	public static final String USERSTAMPS_TABLE = "UserStamps";
	public static final String LOGIN_DETAILS_TABLE = "LoginDetailsTable";
	public static final String REWARDS_TABLE = "RewardsTable";
	public static final String OFFLINE_STAMPS_TABLE = "OffLineStamps";
	public static final String NEAR_BY_TABLE = "NearByTable";
	public static final String USER_TABLE = "UserDetails";
	public static final String NOTIFICATION_TABLE = "Notifications";
	// Notifications table columns
	public static final String NOTIFICATION_ID = "_id";
	public static final String NOTIFICATION_ORIGIN = "Origin";
	public static final String NOTIFICATION_STORE = "StoreCode";
	public static final String NOTIFICATION_STORE_NAME = "StoreName";
	public static final String NOTIFICATION_LOGO_URL = "LogoUrl";
	public static final String NOTIFICATION_TITLE = "title";
	public static final String NOTIFICATION_MESSSAGE = "message";
	public static final String NOTIFICATION_STATUS = "status";
	public static final String NOTIFICATION_RECIEVE_TIME = "NotificationRecieveTime";
	public static final String NOTIFICATION_STATUS_READ = "1";
	public static final String NOTIFICATION_STATUS_UNREAD = "0";
	// DB Create
	private static String createNotificationDB = "create  table if not exists "
			+ NOTIFICATION_TABLE + "(" + NOTIFICATION_ORIGIN + " text ,"
			+ NOTIFICATION_ID + " text ," + NOTIFICATION_STORE + " text ,"
			+ NOTIFICATION_STORE_NAME + " text ," + NOTIFICATION_LOGO_URL
			+ " text ," + NOTIFICATION_TITLE + " text ,"
			+ NOTIFICATION_MESSSAGE + " text ," + NOTIFICATION_RECIEVE_TIME
			+ " text ," + NOTIFICATION_STATUS + " text );";

	private static String createLoginDetailsDB = "create   table if not exists "
			+ LOGIN_DETAILS_TABLE
			+ "("
			+ LOGGEDUSERID
			+ " text ,"
			+ COOKIE
			+ " text );";
	private static String createRestaurantDB = "create  table  if not exists "
			+ RESTAURANT_TABLE + "(" + STORECODE + " integer primary key, "
			+ STORENAME + " text, " + STORE_CATEGORY + " text, " + ADDRESS
			+ " text, " + LOCALITYVALUE + " text, " + LOCALITYCODE + " text, "
			+ USERSTAMPS + " text, " + NEXTREWARDAT + " text, " + REWARDMESSAGE
			+ " text, " + OFFERS + " text, " + LOGO_URL + " text, " + INSERT_TIME + " integer, " + COVER_URL
			+ " text);";

	private static String createNearByDB = "create  table if not exists "
			+ NEAR_BY_TABLE + "(" + NEARBYSTORECODE + " integer primary key , "
			+ NEARBYSTORENAME + " text, " + STORE_CATEGORY + " text, "
			+ DISTANCE + " float, " + LATLONG + " text " + LOCALITYVALUE
			+ " text, " + LOCALITYCODE + " text, " + REWARDMESSAGE + " text);";

	private static String createRewardsTable = "create table  if not exists "
			+ REWARDS_TABLE + " ( " + REWARDSTORECODE + " text, " + REWARDSTORE
			+ " text, " + REWARDID + " text, " + REWARDNAME + " text, "
			+ REWARDCODE + " text, " + REWARDDETAILS + " text, " + EXPIRESON
			+ " text, " + INSERT_TIME + " integer,  " + REWARDSTATE + " text, " + USED_ON + " text );";

	public static DbHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DbHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 * Constructor should be private to prevent direct instantiation. make call
	 * to static method "getInstance()" instead.
	 */
	private DbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i("database version", String.valueOf(db.getVersion()));
		db.execSQL(createLoginDetailsDB);
		db.execSQL(createRestaurantDB);
		db.execSQL(createRewardsTable);
		db.execSQL(createNearByDB);
		db.execSQL(createNotificationDB);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("OnUpgrade", "called");
		db.execSQL("drop table if exists " + RESTAURANT_TABLE);
		db.execSQL("drop table if exists " + REWARDS_TABLE);
		db.execSQL("drop table if exists " + NOTIFICATION_TABLE);
		db.execSQL("drop table if exists " + NEAR_BY_TABLE);
		ContextWrapper cw = new ContextWrapper(context);
		// path to /data/data/yourapp/app_data/imageDir
		
		File directory = cw.getDir(AppController.COVER_DIR, Context.MODE_PRIVATE);
		deleteRecursive(directory);
		File logo = cw.getDir(AppController.LOGO_DIR, Context.MODE_PRIVATE);
		deleteRecursive(logo);
		onCreate(db);
	}

	void deleteRecursive(File dir) {
		if (dir.isDirectory()) {
		        String[] children = dir.list();
		        for (int i = 0; i < children.length; i++) {
		            new File(dir, children[i]).delete();
		            Log.d("deleting file", children[i]);
		        } 
		    } 
		dir.delete();
		Log.d( "On Upgrade" ,"Files deleted");
		
	}

}
