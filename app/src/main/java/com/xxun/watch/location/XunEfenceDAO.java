package com.xxun.watch.location;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;

/**
 * Created by xiaoxun on 2018/01/08.
 */

public class XunEfenceDAO extends XunLocationDAO {
	private static final String TAG = "[XunLoc]XunEfenceDAO";
	private static XunEfenceDAO instance = null;
	private static final int MAX_READ_COUNT = 10;

	public XunEfenceDAO(Context mContext) {
		super(mContext, XunLocationDbHelper.EFENCE_TABLE_NAME);
		// TODO Auto-generated constructor stub
	}

	public static XunEfenceDAO getInstance(Context context) {
		if (instance == null)
			instance = new XunEfenceDAO(context);
		return instance;
	}

	public void readEfence(ArrayList<XunEfenceID> efenceIDs) {
		ArrayList<Long> timestamps = new ArrayList<Long>();

		Cursor myCursor = null;
		SQLiteDatabase db = this.openReadableDb();
		if (db == null) {
			Log.d(TAG, "readLocation: db open fail");
			return;
		}

		try {
			StringBuilder szBuff = new StringBuilder();

			szBuff.append("SELECT * FROM ");
			szBuff.append(getTableName());
			szBuff.append(" ORDER BY ");
			szBuff.append(XunLocationDbHelper.FIELD_EF_ID);

			Log.d(TAG, "readLocation: szBuff="+szBuff.toString());
			myCursor = db.rawQuery(szBuff.toString(), null);

			int i = 0;
			while (myCursor.moveToNext())//已经有了
			{
				if (i >= MAX_READ_COUNT) {
					Log.d(TAG, "readLocation: over max count");
					break;
				}
				XunEfenceID tempEfence = new XunEfenceID();
				Log.d(TAG, "readLocation: reading "+i);
				tempEfence.index = myCursor.getInt(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_ID));
				tempEfence.timestamp = myCursor.getString(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_TIMESTAMP));
				tempEfence.lat = myCursor.getDouble(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_POS_LAT));
				tempEfence.lng = myCursor.getDouble(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_POS_LNG));
				tempEfence.radius = myCursor.getDouble(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_RADIUS));
				tempEfence.splitWifiStr(myCursor.getString(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_EF_WIFI_LIST)));
				if(tempEfence.isAvaliable()) {
					efenceIDs.add(tempEfence);
					i++;
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		db.close();
		closeCursor(myCursor);

	}


	//更新或添加Location
	public void writeEfence(XunEfenceID efence) {
		int id = -1;
		SQLiteDatabase db = this.openWritableDb();
		Cursor myCursor = null;
		if (db == null){
			Log.d(TAG, "writeEfence: open db error");
		}

		Log.d(TAG, "writeEfence: open db succ");
		try {
			ContentValues rowData = new ContentValues();

			rowData.clear();
			rowData.put(XunLocationDbHelper.FIELD_EF_ID, efence.index);
			rowData.put(XunLocationDbHelper.FIELD_EF_TIMESTAMP, efence.timestamp);
			rowData.put(XunLocationDbHelper.FIELD_EF_POS_LAT, efence.lat);
			rowData.put(XunLocationDbHelper.FIELD_EF_POS_LNG, efence.lng);
			rowData.put(XunLocationDbHelper.FIELD_EF_RADIUS, efence.radius);
			rowData.put(XunLocationDbHelper.FIELD_EF_WIFI_LIST, efence.formatWifiToString());

			Log.d(TAG, "writeEfence: rowData="+rowData.toString());

			StringBuilder qure = new StringBuilder();

			qure.append("SELECT * FROM ");
			qure.append(getTableName());
			qure.append(" WHERE ");
			qure.append(XunLocationDbHelper.FIELD_EF_ID);
			qure.append(" = ");
			qure.append(efence.index);
			Log.d(TAG, "writeEfence: qure="+qure.toString());
			myCursor = db.rawQuery(qure.toString(), null);
			if (myCursor.moveToNext()) {
				Log.d(TAG, "writeEfence: have same timestamp record");
				StringBuilder szBuff = new StringBuilder();
				szBuff.append(XunLocationDbHelper.FIELD_EF_ID);
				szBuff.append("= ");
				szBuff.append(efence.index);
				Log.d(TAG, "writeEfence: szBuff:"+szBuff.toString());
				id = db.update(getTableName(), rowData, szBuff.toString(), null);
				if (-1 == id) {
					Log.d(TAG, "writeEfence: error");
				}
			} else {
				Log.d(TAG, "writeEfence: insert new record");
				db.insertOrThrow(getTableName(), null, rowData);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "writeEfence: " + e.getMessage());
		}
		db.close();
		closeCursor(myCursor);

	}

	public void removeLocation(ArrayList<Long> timeStamps) {
		SQLiteDatabase db = this.openWritableDb();
		if (db == null) {
			Log.d(TAG, "removeLocation: db=null");
			return;
		}

		if(timeStamps == null){
			Log.d(TAG, "removeLocation: timestamp null");
			return;
		}
		if(timeStamps.size() == 0){
			Log.d(TAG, "removeLocation: timeStamp = 0");
			return;
		}

		try {
			StringBuilder szBuff = new StringBuilder();
			szBuff.append("DELETE FROM ");
			szBuff.append(XunLocationDbHelper.TRACE_TABLE_NAME);
			szBuff.append(" WHERE ");
			szBuff.append(XunLocationDbHelper.FIELD_TIMESTAMP);
			szBuff.append(" in (");
			StringBuilder timelist = new StringBuilder();
			for(Long timestamp : timeStamps){
				if(timelist.length() != 0){
					timelist.append(",");
				}
				timelist.append(timestamp);
			}
			szBuff.append(timelist.toString());
			szBuff.append(")");
			Log.d(TAG, "removeLocation: szBuffer="+szBuff.toString());
			db.execSQL(szBuff.toString());
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "removeLocation: "+e.getMessage());
		}
		db.close();
		return;
	}


	@Override
	protected void closeCursor(Cursor cursor) {
		// TODO Auto-generated method stub
		super.closeCursor(cursor);
	}

	@Override
	protected String getTableName() {
		// TODO Auto-generated method stub
		return super.getTableName();
	}

	@Override
	protected SQLiteDatabase openWritableDb() {
		// TODO Auto-generated method stub
		return super.openWritableDb();
	}

	@Override
	protected SQLiteDatabase openReadableDb() {
		// TODO Auto-generated method stub
		return super.openReadableDb();
	}

	@Override
	public int delete(long id) {
		// TODO Auto-generated method stub
		return super.delete(id);
	}

	@Override
	public int deleteAll() {
		// TODO Auto-generated method stub
		return super.deleteAll();
	}


}
