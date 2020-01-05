package com.xxun.watch.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
//import android.util.Log;

import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IMessageReceiveListener;
import com.xiaoxun.sdk.IResponseDataCallBack;


import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import  android.util.XiaoXunUtil;

/**
 * Created by xiaoxun on 2017/11/15.
 */

public class XunFlightModeModeSwitcher {
	private static final String TAG = "[XunLoc]XunFlightModeModeSwitcher";

	public static final String ENTRY_FLIGHT_MODE = "com.xxun.watch.location.entryFlightMode";
	public static final String EXIT_FLIGHT_MODE = "com.xxun.watch.location.entryExitMode";
	public static final String UPGRADE_ALARM = "com.xxun.watch.location.alarm.upgrade";

	public static final String ACTION_SCHPWR_ON = "ACTION_SCHPWR_ON";
	public static final String UPGRADE_NOW = "com.broadcast.xxun.upgrade_now";

	private static XunFlightModeModeSwitcher instance = null;
	private static final int DEFAULT_START_H = 21;
	private static final int DEFAULT_START_M = 0;
	private static final int DEFAULT_END_H = 7;
	private static final int DEFAULT_END_M = 0;
	private static final int DEFAULT_ON_OFF = 1;
	private static final int DEFAULT_TYPE = 0;

	private static final int DEFAULT_730_START_H = 21;
	private static final int DEFAULT_730_START_M = 0;
	private static final int DEFAULT_730_END_H = 7;
	private static final int DEFAULT_730_END_M = 0;
	private static final int DEFAULT_730_ON_OFF = 1;
	private static final int DEFAULT_730_TYPE = 0;

	private int startHour =  		DEFAULT_START_H;
	private int startMin =  		DEFAULT_START_M;
	private int endHour = 		DEFAULT_END_H;
	private int endMin = 			DEFAULT_END_M;
	private int onoff = 				DEFAULT_ON_OFF;
	private int type = 					DEFAULT_TYPE;
	private int timeOffset = 0;
	private long stepCount = 0;
	private long lastScreenOffTimeStamp = 0;
	private long lastScreenOnTimeStamp = 0;


	private Handler turnOn_Light_hander = new Handler();
	private Runnable turnOn_Light_Runable = new Runnable() {
		@Override
		public void run() {
			if (lastScreenOnTimeStamp == 0)
				return;
			if(SystemClock.elapsedRealtime() > lastScreenOnTimeStamp){
				long diff = SystemClock.elapsedRealtime() - lastScreenOnTimeStamp;
				Log.d(TAG, "run:  diff = "+ diff);
				if(diff>30*1000){
					if(XunLocation.isBinded()) {
						if (isAirPlaneModeOn()) {
							setAirplaneModeOn(false);
						}
					}
					lastScreenOnTimeStamp = 0;
				}else{
					turnOn_Light_hander.postDelayed(turnOn_Light_Runable, 1000);
				}
			}
		}
	};


	PendingIntent mEntryFlightModePendingIntent = null;
	PendingIntent mExitFlightModePendingIntent = null;
	PendingIntent mWaitUpgradePendingIntent = null;



	public static XunFlightModeModeSwitcher getInstance(){
		if(instance == null){
			instance = new XunFlightModeModeSwitcher();
		}
		return instance;
	}


	public XunFlightModeModeSwitcher(){
		//init
		Log.d(TAG, "XunFlightModeModeSwitcher: init");
		createNewOffset();
		regModeContentObserver();
		SyncFlightMode();
		initScreenStatus();

	}

	public void createNewOffset(){
		timeOffset = new Random().nextInt() % 30;
		timeOffset = Math.abs(timeOffset);
		Log.d(TAG, "createNewOffset: "+ timeOffset);
	}

	public boolean isNeedEntryTimeScope() {
		Calendar calendar = Calendar.getInstance();

		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "isNeedEntryTimeScope: curTime="+ curTime);

		int startTime = startHour*60+startMin;

		Log.d(TAG, "isNeedEntryTimeScope: startTime="+ startTime);
		int endTime = endHour*60+endMin;

		Log.d(TAG, "isNeedEntryTimeScope: endTime="+ endTime);

		Log.d(TAG, "isNeedEntryTimeScope: timeOffset"+timeOffset);

		if((endTime > curTime)&&((endTime - curTime)<30))
		{
			Log.d(TAG, "isNeedEntryTimeScope: in 30 min area");
			return false;
		}



		if(startTime > endTime)//time dur over 00:00 clock
		{
			Log.d(TAG, "isNeedEntryTimeScope: over 00:00");
			if((endTime+(60*24)-startTime)<60) // flight mode dur less than 1 hour not entry flight mode
			{
				Log.d(TAG, "isNeedEntryTimeScope: dur less than 1hour");
				return false;
			}
		}
		else if(startTime <  endTime)//time dur in one day
		{
			Log.d(TAG, "isNeedEntryTimeScope: in one day");
			if((endTime - startTime)<60) // flight mode dur less than 1 hour not entry flight mode
			{
				Log.d(TAG, "isNeedEntryTimeScope: dur less than 1hour");
				return false;
			}
		}
		else
		{
			return false;
		}

		startTime = (startTime+timeOffset)%(3600*24);

		Log.d(TAG, "isNeedEntryTimeScope: startTime"+startTime);

		if(startTime >  endTime)
		{
			Log.d(TAG, "isNeedEntryTimeScope: over 00:00");
			if((curTime >= startTime)|| (curTime < endTime))
			{
				Log.d(TAG, "isNeedEntryTimeScope: true");
				return true;
			}
		}
		else  if(startTime <  endTime)
		{
			Log.d(TAG, "isNeedEntryTimeScope: in one day");
			if((curTime >= startTime)&& (curTime < endTime))
			{
				Log.d(TAG, "isNeedEntryTimeScope: true");
				return true;
			}
		}
		else
		{
			Log.d(TAG, "isNeedEntryTimeScope: false");
			return false;
		}
		Log.d(TAG, "isNeedEntryTimeScope: false");
		return false;
	}


	public boolean isNeedExitTimeScope() {
		boolean needEntryFlightMode = false;
		Calendar calendar = Calendar.getInstance();

		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "isNeedExitTimeScope: curTime="+ curTime);

		int startTime = startHour*60+startMin;

		Log.d(TAG, "isNeedExitTimeScope: startTime="+ startTime);
		int endTime = endHour*60+endMin;

		Log.d(TAG, "isNeedExitTimeScope: endTime="+ endTime);

		Log.d(TAG, "isNeedExitTimeScope: offset = "+timeOffset);

		if((startTime > curTime)&&((startTime - curTime)<30))
		{
			Log.d(TAG, "isNeedExitTimeScope: in 30min area");
			return true;
		}


		if(startTime > endTime)//time dur over 00:00 clock
		{
			Log.d(TAG, "isNeedExitTimeScope: over 00:00");
			if((endTime+(60*24)-startTime)<60) // flight mode dur less than 1 hour not entry flight mode
			{
				Log.d(TAG, "isNeedExitTimeScope: dur less than 1hour");
				return false;
			}
		}
		else if(startTime <  endTime)//time dur in one day
		{
			Log.d(TAG, "isNeedExitTimeScope: in one day");
			if((endTime - startTime)<60) // flight mode dur less than 1 hour not entry flight mode
			{
				Log.d(TAG, "isNeedExitTimeScope: dur less than 1hour");
				return false;
			}
		}
		else
		{
			Log.d(TAG, "isNeedExitTimeScope: false ");
			return false;
		}

		//startTime = (startTime+timeOffset)%(3600*24);
		endTime = (endTime+(60*24) - timeOffset)%(60*24);

		Log.d(TAG, "isNeedExitTimeScope: endTime="+endTime);

		if(startTime >  endTime)
		{
			Log.d(TAG, "isNeedExitTimeScope: over 00:00");
			if((curTime >= startTime)|| (curTime < endTime))
			{
				Log.d(TAG, "isNeedExitTimeScope: true");
				return true;
			}
		}
		else  if(startTime <  endTime)
		{
			Log.d(TAG, "isNeedExitTimeScope: over 00:00");
			if((curTime >= startTime)&& (curTime < endTime))
			{
				Log.d(TAG, "isNeedExitTimeScope: true");
				return true;
			}
		}
		else
		{
			Log.d(TAG, "isNeedExitTimeScope: false");
			return false;
		}
		Log.d(TAG, "isNeedExitTimeScope: false");
		return false;
	}


	public static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin) {
		Calendar calendar = Calendar.getInstance();

		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "isCurrentInTimeScope: curTime="+ curTime);

		int startTime = beginHour*60+beginMin;

		Log.d(TAG, "isCurrentInTimeScope: startTime="+ startTime);
		int endTime = endHour*60+endMin;

		Log.d(TAG, "isCurrentInTimeScope: endTime="+ endTime);
		if(startTime < endTime) {
			if((curTime>startTime)&&(curTime<endTime)){
				return true;
			}
		}else if(startTime > endTime){
			if((curTime<endTime)||(curTime>startTime)){
				return true;
			}
		}else{
			return false;
		}
		return false;
	}

	private boolean getChargeStatus(){
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = XunLocation.getmContext().registerReceiver(null, ifilter);

		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

		Log.d(TAG, "getChargeStatus: "+status);
		
		if((status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL))
			return true;
		else
			return false;
	}

	private boolean isFlightModeStill(){
		int mode = XunProdicLocation.getInstance().getCurWorkingMode();
		int stillCount = XunProdicLocation.getInstance().getStillCount();
		Log.d(TAG, "isFlightModeStill: mode="+mode+ "  stillCount=" +stillCount);
		boolean still = false;
		switch (mode){
			case XunProdicLocation.WORKING_MODE_NORMAL:
				if(stillCount >= 3) {
					still = true;
				}
				break;
			case XunProdicLocation.WORKING_MODE_FAST:
				if(stillCount >=5){
					still = true;
				}
				break;
			case XunProdicLocation.WORKING_MODE_LAZY:
				if(stillCount >= 3) {
					still = true;
				}
				break;
			default:
				still = false;
				break;
		}
		return still;
	}


	private boolean onFlightModeEntryCheck(){
		if(false == isNeedEntryTimeScope()) {
			Log.d(TAG, "onFlightModeEntryCheck: isNeedEntryTimeScope false");
			return false;
		}

		if(isAnyCallExist()){
			Log.d(TAG, "onFlightModeEntryCheck: isAnyCallExist");
			return false;
		}

		if(getChargeStatus() == true) {
			Log.d(TAG, "onFlightModeEntryCheck: charging");
			return false;
		}

		if(isUserOperating() == true){
			Log.d(TAG, "onFlightModeEntryCheck: isUserOperating");
			return false;
		}

		if(isFlightModeStill() == false){
			Log.d(TAG, "onFlightModeEntryCheck: still false");
			return false;
		}

		if(XunLocTrackingLocation.getInstance().isInTrackingTime()){
			Log.d(TAG, "onFlightModeEntryCheck: tracking mode working");
			return false;
		}

		if((XunSensorProc.getInstance().stepcounter - stepCount)>50){
			Log.d(TAG, "onFlightModeEntryCheck: steps over 50 false");
			return false;
		}

		return true;
	}

	private boolean onFlightModeExitCheck(){
		if(false == isNeedExitTimeScope()){
			Log.d(TAG, "onFlightModeExitCheck: isNeedExitTimeScope false");
			return false;
		}
		if(getChargeStatus() == true) {
			Log.d(TAG, "onFlightModeExitCheck: charging");
			return false;
		}
		
		if((XunSensorProc.getInstance().stepcounter - stepCount) > 50){
			Log.d(TAG, "onFlightModeExitCheck: step over 50 in");
			return false;
		}
		return true;
	}

	private void parseSleepModeSettings() {
		if (XiaoXunUtil.XIAOXUN_CONFIG_PRODUCT_SEVEN_THREE_ZERO){
			startHour = DEFAULT_730_START_H;
			startMin = DEFAULT_730_START_M;
			endHour = DEFAULT_730_END_H;
			endMin = DEFAULT_730_END_M;
			onoff = DEFAULT_730_ON_OFF;
			type = DEFAULT_730_TYPE;
		}else{
			startHour = DEFAULT_START_H;
			startMin = DEFAULT_START_M;
			endHour = DEFAULT_END_H;
			endMin = DEFAULT_END_M;
			onoff = DEFAULT_ON_OFF;
			type = DEFAULT_TYPE;
		}

		Log.d(TAG, "parseSleepModeSettings: ");

		String sleepSetting = Settings.System.getString(XunLocation.getmContext().getContentResolver(),"SleepList");
		Log.d(TAG, "parseSleepModeSettings: "+sleepSetting);
		if(sleepSetting != null){
			try {
				/*  {"endmin":"00","timeid":"20171205172937758","onoff":"1","startmin":"00","endhour":"07","starthour":"21"}*/
				JSONObject root = (JSONObject)JSONValue.parse(sleepSetting);
				onoff = Integer.parseInt((String)root.get("onoff"));
				if(onoff == 1){
					startHour = Integer.parseInt((String)root.get("starthour"));
					startMin = Integer.parseInt((String)root.get("startmin"));
					endHour = Integer.parseInt((String)root.get("endhour"));
					endMin = Integer.parseInt((String)root.get("endmin"));


					Object type_obj = root.get("type");
					if(type_obj != null) {
						type = Integer.parseInt((String)type_obj);
					}
				}
			}catch (Exception e){
				Log.d(TAG, "onFlightModeCheck: error"+e.toString());
			}
		}
		Log.d(TAG, "parseSleepModeSettings: start="+startHour+":"+startMin+"endtime="+endHour+":"+endMin+"onoff="+onoff+"type="+type);
	}


	public void checkFlightMode(){
		boolean needEntryFlight = false;

		if(XunLocation.isBinded() == false){
			return;
		}

		parseSleepModeSettings();

		if(onoff == 0) {
			Log.d(TAG, "onFlightModeCheck: disabled");
			needEntryFlight = false;
		}else{
			if(isAirPlaneModeOn()){
				needEntryFlight = onFlightModeExitCheck();
			}else{
				needEntryFlight = onFlightModeEntryCheck();
			}
		}
		Log.d(TAG, "onFlightModeCheck: needEntryFlight = "+String.valueOf(needEntryFlight));

		if(needEntryFlight != isAirPlaneModeOn()) {

			if(needEntryFlight == true){
				uploadEntryFlightStatus();
			}else{
				Log.d(TAG, "checkFlightMode: Exit flight mode");
				setAirplaneModeOn(false);
			}

		}
		stepCount = XunSensorProc.getInstance().stepcounter;
		Log.d(TAG, "checkFlightMode: cur steps = "+stepCount);
	}


	public boolean isAirPlaneModeOn(){
		int mode = 0;
		try {
			mode = Settings.Global.getInt(XunLocation.getmContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON);
		}catch (Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "isAirPlaneModeOn: " +String.valueOf(mode));
		return mode == 1;//为1的时候是飞行模式
	}


	private void setAirplaneModeOn(boolean enabling) {
		Log.d(TAG, "setAirplaneModeOn: "+String.valueOf(enabling));
		if(enabling == false){
			releaseAllAlarm();
		}

		// Change the system setting
		Settings.Global.putInt(XunLocation.getmContext().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
				enabling ? 1 : 0);

		// Post the intent
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", enabling);
		XunLocation.getmContext().sendBroadcast(intent);

	}

	private void uploadEntryFlightStatus(){
		String gid = XunLocation.getmNetworkManager().getWatchGid();
		String keys[] = {"watch_status"};
		String values[] = new String[1];
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		if (type == 0){
			//Entry Flight mode
			values[0] = formater.format(new Date(System.currentTimeMillis())) + "_3";
		}else if(type == 1){
			//power off mode
			values[0] = formater.format(new Date(System.currentTimeMillis())) + "_2";
		}

		//broadcast need entry flight mode
		Intent intent = new Intent("ACTION_MOVE_TO_SLEEP_MODE");
		XunLocation.getmContext().sendBroadcast(intent);


		try{
			XunLocation.getmNetworkManager().setMapMSetValue(gid, keys, values, new CallBack() {
				@Override
				public void onSuccess(ResponseData responseData) {
					Log.d(TAG, "onSuccess: ");
					EntrySleepMode();
				}

				@Override
				public void onError(int i, String s) {
					Log.d(TAG, "onError: ");
					EntrySleepMode();
				}
			});
		}catch (Exception e){
			e.printStackTrace();
			EntrySleepMode();
		}

	}


	private void StartEntryAlarm(long timeout){
		Log.d(TAG, "StartEntryAlarm: "+timeout);
		Intent intent = new Intent(ENTRY_FLIGHT_MODE);
		mEntryFlightModePendingIntent = PendingIntent.getBroadcast(XunLocation.getmContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		XunLocation.getmAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+timeout, mEntryFlightModePendingIntent);
	}

	private void StartExitAlarm(long timeout){
		Log.d(TAG, "StartExitAlarm: "+timeout);
		Intent intent = new Intent(EXIT_FLIGHT_MODE);
		mExitFlightModePendingIntent = PendingIntent.getBroadcast(XunLocation.getmContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		XunLocation.getmAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+timeout, mExitFlightModePendingIntent);
	}

	private void regModeContentObserver(){
		Uri operationModeValueUri = Settings.System.getUriFor("SleepList");
		XunLocation.getmContext().getContentResolver().registerContentObserver(operationModeValueUri, false, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				Log.d(TAG, "regModeContentObserver onChange: operation_mode_value change");
				SyncFlightMode();
			}
		});
	}

	private void releaseAllAlarm(){
		Log.d(TAG, "releaseAllAlarm: ");
		try {
			XunLocation.getmAlarmManager().cancel(mEntryFlightModePendingIntent);
		}catch (Exception e){
			Log.d(TAG, "StopAlarm: "+e.toString());
		}
		try {
			XunLocation.getmAlarmManager().cancel(mExitFlightModePendingIntent);
		}catch (Exception e){
			Log.d(TAG, "StopAlarm: "+e.toString());
		}

	}

	public void SyncFlightMode() {
		Log.d(TAG, "SyncFlightMode: ");
		parseSleepModeSettings();
		if(XunLocation.isCmcc()) {
			releaseAllAlarm();
			setSwitchAlarm();
		}
	}

	private void setSwitchAlarm(){
		Calendar calendar = Calendar.getInstance();

		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "setSwitchAlarm: curTime="+ curTime);

		int startTime = startHour*60+startMin;

		Log.d(TAG, "setSwitchAlarm: startTime="+ startTime);
		int endTime = endHour*60+endMin;

		Log.d(TAG, "setSwitchAlarm: endTime="+ endTime);


		if(curTime < startTime){
			long delay = (startTime - curTime)*60*1000;
			StartEntryAlarm(delay);
		}else if(curTime > startTime){
			long delay = (startTime + (24*60) - curTime)*60*1000;
			StartEntryAlarm(delay);
		}else{
			setAirplaneModeOn(true);
		}

		if(curTime < endTime){
			long delay = (endTime - curTime)*60*1000;
			StartExitAlarm(delay);
		}else if(curTime > endTime){
			long delay = (endTime+(24*60) - curTime)*60*1000;
			StartExitAlarm(delay);
		}else{
			setAirplaneModeOn(false);
		}
	}

	public void onEntryFlightModeAlarm(){
		if(XunLocation.isCmcc()) {
			Log.d(TAG, "onEntryFlightModeAlarm: cmcc");
			uploadEntryFlightStatus();
		}
	}

	public void onExitFlightModeAlarm(){
		if(XunLocation.isCmcc()) {
			Log.d(TAG, "onExitFlightModeAlarm: Exit flight by Alarm time out");
			setAirplaneModeOn(false);
		}else{
			Log.d(TAG, "onExitFlightModeAlarm: Exit flight by Alarm time out");
			releaseAllAlarm();
			if(isAirPlaneModeOn() == true) {
				Log.d(TAG, "onExitFlightModeAlarm: Exit");
				setAirplaneModeOn(false);
			}
		}
	}

	private void setExitFlightAlarm(){
		Calendar calendar = Calendar.getInstance();

		int curTime = calendar.get(Calendar.HOUR_OF_DAY) * 60;
		curTime += calendar.get(Calendar.MINUTE);
		Log.d(TAG, "setExitFlightAlarm: curTime="+ curTime);

		int endTime = (endHour*60+endMin + (24*60) - timeOffset)%(24*60);

		Log.d(TAG, "setExitFlightAlarm: endTime="+ endTime);

		if(curTime < endTime){
			long delay = (endTime - curTime)*60*1000;
			StartExitAlarm(delay);
		}else if(curTime > endTime){
			long delay = (endTime+(24*60) - curTime)*60*1000;
			StartExitAlarm(delay);
		}else{
			setAirplaneModeOn(false);
		}
	}


	public void onBatteryConnected() {
		if(XunLocation.isBinded()){
			if(isAirPlaneModeOn()) {
				setAirplaneModeOn(false);
			}
		}
	}

	public void onScreenOn(){
		Log.d(TAG, "onScreenOn: screen on");
		lastScreenOffTimeStamp = 0;
		lastScreenOnTimeStamp = SystemClock.elapsedRealtime();
		Log.d(TAG, "onScreenOn: screen on "+lastScreenOnTimeStamp);
		turnOn_Light_hander.postDelayed(turnOn_Light_Runable, 1000);
	}

	public void onScreenOff(){
		lastScreenOnTimeStamp = 0;
		lastScreenOffTimeStamp = SystemClock.elapsedRealtime();
		Log.d(TAG, "onScreenOff: screen off "+lastScreenOffTimeStamp);
		turnOn_Light_hander.removeCallbacks(turnOn_Light_Runable);
	}

	public void initScreenStatus(){
		boolean isScreenon = XunLocation.getmPowerManager().isScreenOn();
		Log.d(TAG, "initScreenStatus: isScreenon = "+isScreenon);
		if(isScreenon){
			onScreenOn();
		}else{
			onScreenOff();
		}
	}


	private boolean isUserOperating(){
		if(lastScreenOffTimeStamp == 0){
			Log.d(TAG, "isUserOperating: screen is on");
			return true;
		}
		if(SystemClock.elapsedRealtime() > lastScreenOffTimeStamp){
			long diff = SystemClock.elapsedRealtime() - lastScreenOffTimeStamp;
			Log.d(TAG, "isUserOperating: diff ="+diff);
			if(diff > 300*1000){
				return false;
			}else{
				return true;
			}
		}else{
			Log.d(TAG, "isUserOperating: error reset timestamp");
			lastScreenOffTimeStamp = SystemClock.elapsedRealtime();
			return true;
		}
	}

	private boolean isAnyCallExist(){
		if(XunLocation.getmTelephonyManager().getCallState() == TelephonyManager.CALL_STATE_IDLE) {
			Log.d(TAG, "isAnyCallExist: false");
			return false;
		}else{
			Log.d(TAG, "isAnyCallExist: true");
			return true;
		}
	}


	private void EntrySleepMode(){
		Log.d(TAG, "EntrySleepMode: ");
		XunProdicLocation.getInstance().clearStillCount();
		if(type == 0){
			Log.d(TAG, "EntrySleepMode: now");
			setAirplaneModeOn(true);
			setExitFlightAlarm();
			XunSensorProc.getInstance().openStepInterrupt(new XunSensorProc.onStepInterrupt() {
				@Override
				public void run() {
					setAirplaneModeOn(false);
				}
			});
		}else if(readAutoUpgradeFlag() == 1){
			setAirplaneModeOn(true);
			setExitFlightAlarm();
			Log.d(TAG, "EntrySleepMode: now upgrade");
			SendAutoUpgradeIntent();
			//StartWaitUpgradeAlarm(180000);

		}else {
			Log.d(TAG, "EntrySleepMode: power off now");
			SendPowerOnStatus();
		}
	}

	private void SendPowerOnStatus(){
		Intent intent = new Intent(ACTION_SCHPWR_ON);
		//add by liaoyi 18/8/14
		intent.setPackage("com.xxun.xunlauncher");
		//end
		int endTime = (endHour*60+endMin + (24*60) - timeOffset)%(24*60);
		int powerOnHour = endTime/60;
		int powerOnMin = endTime%60;

		intent.putExtra("poweronhour", powerOnHour);
		intent.putExtra("poweronmin", powerOnMin);
		XunLocation.getmContext().sendBroadcast(intent);
	}

	
    private class CallBack extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {}
           @Override
           public void onError(int i, String s) {}    
    }



    public int readAutoUpgradeFlag(){
    	Log.d(TAG, "readAutoUpgradeFlag: ");
		try{
			int flag = android.provider.Settings.System.getInt(XunLocation.getmContext().getContentResolver(),"auto_upgrade_flag",0);
			Log.d(TAG,"readAutoUpgradeFlag,flag="+flag);
			return flag;
		}catch(Exception e){
			Log.d(TAG,"readAutoUpgradeFlag fail");
			return 0;
		}
    }

	private void SendAutoUpgradeIntent(){
		Log.d(TAG, "SendAutoUpgradeIntent: ");
		Intent intent = new Intent(UPGRADE_NOW);
		intent.setPackage("com.xxun.watch.xunsettings");
		XunLocation.getmContext().sendBroadcast(intent);
	}

	private void StartWaitUpgradeAlarm(long timeout){
		Log.d(TAG, "StartWaitUpgradeAlarm: "+timeout);
		Intent intent = new Intent(UPGRADE_ALARM);
		mWaitUpgradePendingIntent = PendingIntent.getBroadcast(XunLocation.getmContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		XunLocation.getmAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+timeout, mWaitUpgradePendingIntent);
	}

	public void onStartWaitUpgradeAlarm(){
		Log.d(TAG, "onStartWaitUpgradeAlarm: ");
		SendPowerOnStatus();
	}

}
