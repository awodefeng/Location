package com.xxun.watch.location;


import android.content.Context;
import android.provider.Settings;
//import android.util.Log;


import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.List;

/**
 * Created by xiaoxun on 2018/9/21.
 */

public class XunLocPositiveMode {
	private static final String TAG = "[XunLoc]XunLocPositiveMode";
	public static final int NEAR_SILENT_THUR=30; //
	/*
	simple
	[
		{
			"timeid":"201509101229266151","
			silence_call_in_onoff":0,"
			days":"0111100","
			onoff":"1",
			"startmin":"20","
			advanceop":1,"
			starthour":"08","
			endmin":"50","
			endhour":"11"
		},{"
			timeid":"201509101229266152","
			silence_call_in_onoff":0,"
			days":"0111100",
			"onoff":"1","
			startmin":"20",
			"advanceop":1,
			"starthour":"02",
			"endmin":"00",
			"endhour":"03"
		}
	]
	*/
	public static boolean getPositiveMode(Context context) {
		return getPositiveModeBySilenceList(context);
	}

	private static boolean getPositiveModeBySilenceList(Context context){
		String silenceListSetting = null;
		try {
			silenceListSetting = Settings.System.getString(context.getContentResolver(), "SilenceList");
			Log.d(TAG, "getPositiveModeBySilenceList: "+silenceListSetting);
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}

		if(silenceListSetting == null){
			return false;
		}

		try {
			List<Object> objectList = (List<Object>) JSONValue.parse(silenceListSetting);
			for (int i = 0; i < objectList.size(); i++) {
				JSONObject item = (JSONObject)objectList.get(i);
				Log.d(TAG, "getPositiveModeBySilenceList: "+item);
				if(new SilenceInfo(item).isCurTImeInPositiveMode() == true){
					Log.d(TAG, "getPositiveModeBySilenceList: true");
					return true;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
		Log.d(TAG, "getPositiveModeBySilenceList: false");
		return false;
	}

}
