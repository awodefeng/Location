package com.xxun.watch.location;


//import android.util.Log;

import net.minidev.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by xiaoxun on 2018/9/25.
 */


/*
	{

			"timeid":"201509101229266151",
			"silence_call_in_onoff":0,
			"days":"0111100",
			"onoff":"1",
			"startmin":"20",
			"advanceop":1,
			"starthour":"08",
			"endmin":"50",
			"endhour":"11"
		}
 */

public class SilenceInfo {
	private static final String TAG = "[XunLoc]SilenceInfo";

	public boolean onoff = false;
	public int starthour = -1;
	public int startmin = -1;
	public int endhour = -1;
	public int endmin = -1;
	public String days = null;

	public SilenceInfo(JSONObject item){
		clear();
		try {
			String onoffStr = (String) item.get("onoff");
			Log.d(TAG, "SilenceInfo: onoff="+onoffStr);
			if(onoffStr.equals("1")) {
				onoff = true;
				Log.d(TAG, "SilenceInfo: onoff="+onoff);
				starthour = Integer.parseInt((String) item.get("starthour"));
				Log.d(TAG, "SilenceInfo: starthour="+starthour);
				startmin = Integer.parseInt((String) item.get("startmin"));
				Log.d(TAG, "SilenceInfo: startmin="+startmin);
				endhour = Integer.parseInt((String) item.get("endhour"));
				Log.d(TAG, "SilenceInfo: endhour="+endhour);
				endmin = Integer.parseInt((String) item.get("endmin"));
				Log.d(TAG, "SilenceInfo: endmin="+endmin);

				// over one day area can not work by weekly
				if (endhour < starthour) {
					Log.d(TAG, "SilenceInfo: error hour");
					clear();
				} else if (endhour == starthour) {
					if (endmin < startmin) {
						Log.d(TAG, "SilenceInfo: error min");
						clear();
					}
				}


				days = (String)item.get("days");
				Log.d(TAG, "SilenceInfo: days="+days);


				//String[] day_list=  days.split(",");
				boolean daysAvaliable = false;
				for(int i = 0; i < 7; i++){
					if(days.charAt(i) == '1') {
						daysAvaliable = true;
						break;
					}
				}


				if(daysAvaliable == false) {
					clear();
				}

			}

		}catch (Exception e){
			e.printStackTrace();
			clear();
		}
	}

	private void clear(){
		Log.d(TAG, "clear: ");
		onoff = false;
		starthour = -1;
		startmin = -1;
		endhour = -1;
		endmin = -1;
		days = null;
	}

	boolean isCurTImeInPositiveMode(){
		int[] week_convert_Tab = {-1,6,0,1,2,3,4,5};

		//
		if(!onoff) {
			return false;
		}

		Calendar calendar = Calendar.getInstance();
		int week = calendar.get(Calendar.DAY_OF_WEEK);


		Log.d(TAG, "isCurTImeInPositiveMode: week="+week);
		if((week <1) || (week>7)) {
			Log.d(TAG, "isCurTImeInPositiveMode: can not read day of week");
			return false;
		}
		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "isCurTImeInPositiveMode: curTime="+curTime);


		if(days.charAt(week_convert_Tab[week])== '0'){
			Log.d(TAG, "isCurTImeInPositiveMode: today not work");
			return false;
		}

		int startTime = (starthour*60)+startmin;
		Log.d(TAG, "isCurTImeInPositiveMode: startTime="+startTime);
		if(Math.abs(startTime-curTime)<=XunLocPositiveMode.NEAR_SILENT_THUR){
			Log.d(TAG, "isCurTImeInPositiveMode: in star time area");
			return true;
		}

		int endTime = (endhour*60)+endmin;
		Log.d(TAG, "isCurTImeInPositiveMode: endTime="+endTime);
		if(Math.abs(endTime-curTime) <= XunLocPositiveMode.NEAR_SILENT_THUR){
			Log.d(TAG, "isCurTImeInPositiveMode: in star time area");
			return true;
		}

		return false;
	}
}
