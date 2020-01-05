package com.xxun.watch.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
//import android.util.Log;

/**
 * Created by xiaoxun on 2018/4/25.
 */

public class XunLocTiming {
	private static final String TAG = "[XunLoc]XunLocTiming";
	private static XunLocTiming instance = null;
	public static final String XUN_PRODIC_LOCATION_TIMEOUT = "xunprodic.location.timeout";
	private long timingtik = 0;
	private long scanProdic = 0;
	private long alarmStartTik = 0;
	private PendingIntent mTimeoutIntent;
	private Handler handler;
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			//onTimingProc();
			//check prodic in 1 second
			//handler.postDelayed(runnable, 1000L);
		}
	};

	public static XunLocTiming getInstance(){
		if(instance == null){
			instance = new XunLocTiming();
		}
		return instance;
	}

	public XunLocTiming(){
		Log.d(TAG, "XunLocTiming: ");
		scanProdic = XunProdicLocation.getInstance().getTimerProdic();
		//handler = new Handler();
		//handler.postDelayed(runnable, 10000L);
		setNextScanInterval();
	}

	public void onTimingProc(){
		//for prodic location
		long time_diff = SystemClock.elapsedRealtime() - timingtik;
		Log.d(TAG, "onProdicTimingProc: ");
		//if((time_diff<0)||(time_diff > scanProdic - 30000)) {
			XunProdicLocation.getInstance().startPositionCheck();
			timingtik = SystemClock.elapsedRealtime();
		//}
	}


	private void StartAlarm(long timeout){
		Log.d(TAG, "StartAlarm: timeout = "+timeout );
		Intent intent = new Intent(XUN_PRODIC_LOCATION_TIMEOUT);
		mTimeoutIntent = PendingIntent.getBroadcast(XunLocation.getmContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		XunLocation.getmAlarmManager().setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+timeout, mTimeoutIntent);
		alarmStartTik = SystemClock.elapsedRealtime();
	}

	private void StopAlarm(){
		Log.d(TAG, "StopAlarm: ");
		try {
			XunLocation.getmAlarmManager().cancel(mTimeoutIntent);
		}catch (Exception e){
			Log.d(TAG, "StopAlarm: "+e.toString());
		}
	}

	private long getAlarmTikDiff(){
		return SystemClock.elapsedRealtime() - alarmStartTik;
	}

	public void checkAlarmIsWorking(){

		Log.d(TAG, "checkAlarmIsWorking: "+getAlarmTikDiff());
		if(XunLocation.isBinded() == false)
			return;
		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn())
			return;
		if(getAlarmTikDiff() > (XunProdicLocation.getInstance().getAlarmProdic()*2)){
			Log.d(TAG, "checkAlarmIsWorking Alarm not working: ");
			XunProdicLocation.getInstance().startPositionCheck();
		}
	}

	public void setNextScanInterval() {
		Log.d(TAG, "setNextScanInterval: ");

		//set time next working prodic
		timingtik = SystemClock.elapsedRealtime();
		scanProdic = XunProdicLocation.getInstance().getTimerProdic();


		//set alarm next working prodic
		StopAlarm();

		if(XunProdicLocation.getInstance().getAlarmProdic() != 0){
			StartAlarm(XunProdicLocation.getInstance().getAlarmProdic());
		}
	}


	public void onFlightModeSwitch(){

		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			Log.d(TAG, "onFlightModeSwitch: On stop Alarm");
			StopAlarm();
		}else{
			Log.d(TAG, "onFlightModeSwitch: Off start Alarm");
			setNextScanInterval();
			XunSensorProc.getInstance().closeStepInterrupt();
		}
	}

}
