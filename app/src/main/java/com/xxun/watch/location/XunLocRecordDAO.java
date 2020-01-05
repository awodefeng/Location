package com.xxun.watch.location;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
//import android.util.Log;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by xiaoxun on 2017/10/9.
 */

public class XunLocRecordDAO extends XunLocationDAO {
	private static final String TAG = "[XunLoc]XunLocRecordDAO";
	private static XunLocRecordDAO instance = null;
	private static final int MAX_READ_COUNT = 10;

	public XunLocRecordDAO(Context mContext) {
		super(mContext, XunLocationDbHelper.TRACE_TABLE_NAME);
		// TODO Auto-generated constructor stub
	}

	public static XunLocRecordDAO getInstance(Context context) {
		if (instance == null)
			instance = new XunLocRecordDAO(context);
		return instance;
	}

	public ArrayList<Long> readLocation(JSONArray fmt_array) {
		ArrayList<Long> timestamps = new ArrayList<Long>();

		Cursor myCursor = null;
		SQLiteDatabase db = this.openReadableDb();
		if (db == null) {
			Log.d(TAG, "readLocation: db open fail");
			return timestamps;
		}

		try {
			StringBuilder szBuff = new StringBuilder();

			szBuff.append("SELECT * FROM ");
			szBuff.append(getTableName());
			szBuff.append(" ORDER BY ");
			szBuff.append(XunLocationDbHelper.FIELD_TIMESTAMP);

			Log.d(TAG, "readLocation: szBuff="+szBuff.toString());
			myCursor = db.rawQuery(szBuff.toString(), null);

			int i = 0;
			while (myCursor.moveToNext())//已经有了
			{
				if (i >= MAX_READ_COUNT) {
					Log.d(TAG, "readLocation: over max count");
					break;
				}
				Log.d(TAG, "readLocation: reading "+String.valueOf(i));
				//myCursor.getPosition();
				//myCursor.getLong(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_TIMESTAMP));
				Long timestamp = new Long(myCursor.getLong(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_TIMESTAMP)));
				JSONObject jsonObject = (JSONObject) JSONValue.parse(myCursor.getString(myCursor.getColumnIndex(XunLocationDbHelper.FIELD_JSON_DATA)));
				timestamps.add(new Long(timestamp));
				fmt_array.add(jsonObject);
				Log.d(TAG, "readLocation: " + timestamp.toString() +" ,"+ jsonObject.toJSONString());
				i++;
			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "LOcationDAO  AddCursor() Exp:" + e.getMessage());
		}
		db.close();
		closeCursor(myCursor);

		return timestamps;

	}


	//更新或添加Location
	public void writeLocation(long timestamp, JSONObject fmt) {
		int id = -1;
		SQLiteDatabase db = this.openWritableDb();
		Cursor myCursor = null;
		if (db == null){
			Log.d(TAG, "writeLocation: open db error");
		}

		Log.d(TAG, "writeLocation: open db succ");
		try {
			ContentValues rowData = new ContentValues();

			rowData.clear();
			rowData.put(XunLocationDbHelper.FIELD_TIMESTAMP, timestamp);
			rowData.put(XunLocationDbHelper.FIELD_JSON_DATA, fmt.toJSONString());
			Log.d(TAG, "writeLocation: rowData="+rowData.toString());

			StringBuilder qure = new StringBuilder();

			qure.append("SELECT * FROM ");
			qure.append(getTableName());
			qure.append(" WHERE ");
			qure.append(XunLocationDbHelper.FIELD_TIMESTAMP);
			qure.append(" = ");
			qure.append(timestamp);
			Log.d(TAG, "writeLocation: qure="+qure.toString());
			myCursor = db.rawQuery(qure.toString(), null);
			if (myCursor.moveToNext()) {
				Log.d(TAG, "writeLocation: have same timestamp record");
				StringBuilder szBuff = new StringBuilder();
				szBuff.append(XunLocationDbHelper.FIELD_TIMESTAMP);
				szBuff.append("= ");
				szBuff.append(timestamp);
				Log.d(TAG, "writeLocation: szBuff:"+szBuff.toString());
				id = db.update(XunLocationDbHelper.TRACE_TABLE_NAME, rowData, szBuff.toString(), null);
				if (-1 == id) {
					Log.d(TAG, "updateLocation: error");
				}
			} else {
				Log.d(TAG, "writeLocation: insert new record");
				db.insertOrThrow(getTableName(), null, rowData);
			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "updateLocation: " + e.getMessage());
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
