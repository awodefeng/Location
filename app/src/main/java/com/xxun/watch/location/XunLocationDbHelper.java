package com.xxun.watch.location;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;


public class XunLocationDbHelper extends SQLiteOpenHelper {
	private static final String TAG = "[XunLoc]XunLocationDbHelper";
	private static final String DB_NAME = "xxun.location.db";
	private static final int DB_VERSION = 2;
	//数据库表名
	public static final String TRACE_TABLE_NAME = "trace_records";
	public static final String EFENCE_TABLE_NAME = "efence_records";


	//数据库field名
	public static final String FIELD_REC_ID = "rec_id"; //rec_id
	public static final String FIELD_TIMESTAMP = "time"; //时间戳
	public static final String FIELD_JSON_DATA = "json_data";

	public static final String FIELD_EF_ID = "ef_id";
	public static final String FIELD_EF_TIMESTAMP = "ef_timestamp";
	public static final String FIELD_EF_POS_LAT = "ef_lat";
	public static final String FIELD_EF_POS_LNG = "ef_lng";
	public static final String FIELD_EF_RADIUS = "ef_radius";
	public static final String FIELD_EF_WIFI_LIST = "ef_wifi_list";





	public XunLocationDbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "create db");
		db.execSQL("CREATE TABLE "+TRACE_TABLE_NAME+
				" (" +
				FIELD_REC_ID+
				" INTEGER PRIMARY KEY AUTOINCREMENT," +
				FIELD_TIMESTAMP+" INTEGER," +//cfgid
				FIELD_JSON_DATA+" TEXT" + //cfg的值
				");");

		db.execSQL("CREATE TABLE "+EFENCE_TABLE_NAME+
				" (" +
				FIELD_EF_ID+ " INTEGER PRIMARY KEY," + //efid
				FIELD_EF_TIMESTAMP+" TEXT," + //timestamp
				FIELD_EF_POS_LAT+" DOUBLE," + //lat
				FIELD_EF_POS_LNG+" DOUBLE," + //lng
				FIELD_EF_RADIUS +" DOUBLE," +//radius
				FIELD_EF_WIFI_LIST+" TEXT" + //wifi
				");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.d(TAG, "onUpgrade db");

		if (oldVersion<DB_VERSION){
			db.execSQL("DROP TABLE IF EXISTS "+TRACE_TABLE_NAME+";");
			db.execSQL("DROP TABLE IF EXISTS "+EFENCE_TABLE_NAME+";");
			onCreate(db);
		}
	}
}
