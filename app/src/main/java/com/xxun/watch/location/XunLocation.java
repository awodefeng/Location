package com.xxun.watch.location;

import android.app.AlarmManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.os.PowerManager;

import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.DateFormat;
//import android.util.Log;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import android.os.SystemProperties;
import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;
import com.xxun.watch.stepcountservices.StepCounterRecverAndSender;
import com.xxun.watch.stepcountservices.StepsRanksApp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class XunLocation extends Service {
	private static final String  TAG = "[XunLoc]XunLocation";
	private static Context mContext = null;

//	private static TelephonyManager mTelephonyManager = null;
//	private static WifiManager mWifiManager = null;
//	private static LocationManager mLocationManager = null;
//	private static SensorManager mSensorManager = null;
//	private static PowerManager mPowerManager = null;
//	private static XiaoXunStatisticsManager statisticsManager = null;
//	public static XiaoXunNetworkManager mNetworkManager = null;
//	private static AlarmManager mAlarmManager = null;
	private XunProdicLocation prodicLocation = null;
//	private PowerManager.WakeLock wakeLock = null;
	private static boolean cmcc = false;
	private static boolean gps_perf_test = false;
//	private PowerManager.WakeLock wakeLock;

	public static StepsRanksApp mApp = null;

	BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive: "+intent.toString());
			if(isBinded()){
				XunLocTiming.getInstance().checkAlarmIsWorking();
			}
			if(intent.getAction().compareTo("action.location.single") == 0){
				try {
					Log.d(TAG, "[singleLocTest] receive start "+ System.currentTimeMillis());
					String data_string = intent.getExtras().getString("data");
					Log.d(TAG, "onReceive:data= "+data_string);

					JSONObject root = (JSONObject) JSONValue.parse(data_string);
					Object objectSn= root.get("SN");

					//find instance type
					if(objectSn instanceof  Integer) {

						Integer sn = (Integer)objectSn;
						new XunActiveLocation(sn.longValue());

					} else if(root.get("SN") instanceof Long){

						Long sn = (Long)objectSn;
						new XunActiveLocation(sn.longValue());

					}
				}catch (Exception e){
					Log.d(TAG, "onReceive: "+e.toString());
				}
			}else if(intent.getAction().compareTo("action.location.track") == 0){
				try {
						/* simple
						{
							"CID": 30012,
							"SN": 1515739396,
							"Version": "00040000",
							"PL": {
								"mode": 1,
								"GID": "9958963476656C5DB3A251B1C1336755",
								"EID": "ABFB39D62BDA0667EFA628ED71DD5CC1",
								"timestamp": "20180112144316687",
								"endTime": "20180112151316686",
								"value": 1,
								"freq": 20,
								"sub_action": 106
							},
							"SEID": "F35F3B667D04E35ED0560C2F8BAC43B2"
						}*/
					String data_string = intent.getExtras().getString("data");
					Log.d(TAG, "onReceive:data= "+data_string);

					JSONObject root = (JSONObject) JSONValue.parse(data_string);


					JSONObject pl = (JSONObject) root.get("PL");
					if(pl == null){
						Log.d(TAG, "onReceive: pl == null try to use root to parse data");
						pl = root;
					}

					Integer val = (Integer)pl.get("value");
					if(val != null) {
						if (val.intValue() == 0) {
							//wakeLock.release();
							//wakeLock = null;
							//XunSensorProc.getInstance().logStart = false;
							XunLocTrackingLocation.getInstance().stop();
						} else {
							//wakeLock = XunLocation.getmPowerManager().newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorLogWakeLock");
							//wakeLock.acquire();
							//XunSensorProc.getInstance().logStart = true;
							String strEndTime = (String) pl.get("endTime");
							int freq = (Integer) pl.get("freq");
							SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
							Date endTime = format.parse(strEndTime);
							XunLocTrackingLocation.getInstance().start(20, endTime.getTime());
						}
					}
				}catch (Exception e){
					Log.d(TAG, "onReceive: "+e.toString());
					e.printStackTrace();
				}
			}else if(intent.getAction().equals("action.location.safeArea")) {
				String data_string = intent.getExtras().getString("data");
				Log.d(TAG, "onReceive:data= "+data_string);
				XunEfenceStatus.getInstance().ParseSafeAreaStateInd(data_string);
			}else if(intent.getAction().equals("action.location.efenceData")) {
				String data_string = intent.getExtras().getString("data");
				Log.d(TAG, "onReceive:data= "+data_string);
				XunEfenceStatus.getInstance().ParseEfidDataInd(data_string);
			} else if(intent.getAction().compareTo("action.location.realTimeLocation") == 0) {
				Log.d(TAG, "onReceive: action.location.realTimeLocation not support");

			}else if(intent.getAction().equals("wifi_scan_available")){
				Log.d(TAG, "onReceive: wifi_scan_available");
				Log.d(TAG, "onReceive: scan_enabled="+intent.getExtras().getInt("scan_enabled") );
				if(intent.getExtras().getInt("scan_enabled") == WifiManager.WIFI_STATE_ENABLED) {
					XunLocWifiScan.onWifiScanAvailable();
				}
			}else if(intent.getAction().equals("android.net.wifi.SCAN_RESULTS")){
				Log.d(TAG, "onReceive: android.net.wifi.SCAN_RESULTS");
				XunLocWifiScan.onScanResults();
			}
			else if(intent.getAction().equals("com.xunlauncher.sos")){
				Log.d(TAG, "onReceive: com.xunlauncher.sos");
				new XunLocSos();
			}else if(intent.getAction().equals(XunLocTiming.XUN_PRODIC_LOCATION_TIMEOUT)){
				Log.d(TAG, "onReceive: "+XunLocTiming.XUN_PRODIC_LOCATION_TIMEOUT);
				getServiceManager();
				//XunProdicLocation.getInstance().onProdicTimingProc();
				XunLocTiming.getInstance().onTimingProc();
			}else if(intent.getAction().equals("com.xunlauncher.bindsuccess")){
				Log.d(TAG, "onReceive:com.xunlauncher.bindsuccess");
				if(prodicLocation == null){
					Log.d(TAG, "onReceive:start prodicLocation");
					XunLocTiming.getInstance();
					XunProdicLocation.getInstance();
					XunFlightModeModeSwitcher.getInstance();				
				}
				XunEfenceStatus.getInstance().SyncEfenceDataFromServer();
				XunLocTrackingLocation.getInstance().SyncTrackingModeFromServer();
			}else if(intent.getAction().equals("com.xiaoxun.sdk.action.LOGIN_OK")){
				Log.d(TAG, "onReceive:com.xiaoxun.sdk.action.LOGIN_OK");
				XunFlightModeModeSwitcher.getInstance().createNewOffset();
				if(prodicLocation == null){
					if(isBinded() == true){
						Log.d(TAG, "onReceive: LOGIN_OK succ start prodicLocation");
						XunLocTiming.getInstance();
						XunProdicLocation.getInstance();
						XunFlightModeModeSwitcher.getInstance();
					}
				}else{
					if(isBinded()){
						XunLocTiming.getInstance().checkAlarmIsWorking();
					}
				}
				XunEfenceStatus.getInstance().SyncEfenceDataFromServer();
				XunLocTrackingLocation.getInstance().SyncTrackingModeFromServer();
				XunLocPosUploadPolicy.getInstance().uploadCacheLocData(false);

			} else if(intent.getAction().equals("com.xiaoxun.sdk.action.SESSION_OK")) {
				Log.d(TAG, "onReceive:com.xiaoxun.sdk.action.SESSION_OK");
				XunFlightModeModeSwitcher.getInstance().createNewOffset();
				if(prodicLocation == null){
					if(isBinded() == true){
						Log.d(TAG, "onReceive: SESSION_OK start prodicLocation");
						XunLocTiming.getInstance();
						XunProdicLocation.getInstance();
						XunFlightModeModeSwitcher.getInstance();
					}
				}else{
					if(isBinded()){
						XunLocTiming.getInstance().checkAlarmIsWorking();
					}
				}
				XunEfenceStatus.getInstance().SyncEfenceDataFromServer();
				XunLocTrackingLocation.getInstance().SyncTrackingModeFromServer();
				XunLocPosUploadPolicy.getInstance().uploadCacheLocData(false);

			} else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
				Log.d(TAG, "onReceive: WIFI_STATE_CHANGED_ACTION");
			}
			else if(intent.getAction().equals(XunFlightModeModeSwitcher.ENTRY_FLIGHT_MODE)){
				Log.d(TAG, "onReceive: ENTRY_FLIGHT_MODE");
				XunFlightModeModeSwitcher.getInstance().onEntryFlightModeAlarm();
			}else if(intent.getAction().equals(XunFlightModeModeSwitcher.EXIT_FLIGHT_MODE)){
				Log.d(TAG, "onReceive: EXIT_FLIGHT_MODE");
				XunFlightModeModeSwitcher.getInstance().onExitFlightModeAlarm();
			}else if(intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
				XunFlightModeModeSwitcher.getInstance().onBatteryConnected();
			}
			else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				XunFlightModeModeSwitcher.getInstance().onScreenOn();
			}else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				XunFlightModeModeSwitcher.getInstance().onScreenOff();
			} else if(intent.getAction().equals("com.xxun.watch.location.stepInterrupt")){
				XunSensorProc.getInstance().onStepIntent();
			}else if(intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)){
				//XunProdicLocation.getInstance().onFlightModeSwitch();
				XunLocTiming.getInstance().onFlightModeSwitch();
			}else if(intent.getAction().equals(XunFlightModeModeSwitcher.UPGRADE_ALARM)){
				XunFlightModeModeSwitcher.getInstance().onStartWaitUpgradeAlarm();
			}else if(intent.getAction().equals("com.xxun.watch.xunfriends.action.resend.location")){
				new XunLocGetPos();
			}			
			//add for stepCounter receiver
			StepCounterRecverAndSender.getInstance().onReceive(context, intent);


		}
	};


	public XunLocation() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d(TAG, "onStartCommand");

		getServiceManager();

		cmcc = SystemProperties.getBoolean("persist.sys.xxun.cmcc", false);
		Log.d(TAG, "onStartCommand: persist.sys.xxun.cmcc :"+cmcc);


		if(cmcc == false){
			cmcc = SystemProperties.getBoolean("persist.sys.xxun.cmcc.location", false);
			Log.d(TAG, "onStartCommand: persist.sys.xxun.cmcc.location :"+cmcc);		
		}

		gps_perf_test = SystemProperties.getBoolean("persist.sys.xxun.gps.perf.log", false);
		Log.d(TAG, "onStartCommand: persist.sys.xxun.gps.perf.log :"+gps_perf_test);


		IntentFilter filter = new IntentFilter();
		filter.addAction("action.location.single");
		filter.addAction("action.location.track");
		filter.addAction("action.location.realTimeLocation");
		filter.addAction("wifi_scan_available");
		filter.addAction("android.net.wifi.SCAN_RESULTS");
		filter.addAction("com.xunlauncher.sos");
		filter.addAction(XunLocTiming.XUN_PRODIC_LOCATION_TIMEOUT);
		filter.addAction("com.xunlauncher.bindsuccess");
		filter.addAction("com.xiaoxun.sdk.action.LOGIN_OK");
		filter.addAction("com.xiaoxun.sdk.action.SESSION_OK");
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(XunFlightModeModeSwitcher.ENTRY_FLIGHT_MODE);
		filter.addAction(XunFlightModeModeSwitcher.EXIT_FLIGHT_MODE);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction("action.location.safeArea");
		filter.addAction("action.location.efenceData");
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction("com.xxun.watch.location.stepInterrupt");
		filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(XunFlightModeModeSwitcher.UPGRADE_ALARM);
		filter.addAction("com.xxun.watch.xunfriends.action.resend.location");

		//add for step counter recever
		//filter.addAction("com.xxun.watch.stepcountservices.steps.count.alarm.flags");
		filter.addAction("com.xxun.watch.stepcountservices.action.broast.sensor.steps");
		filter.addAction("brocast.action.step.current.noti");
		filter.addAction("xxun.steps.action.broast.sensor.steps");

		registerReceiver(broadcastReceiver, filter);
		
		if(isBinded() == true){
			Log.d(TAG, "onStartCommand: bind succ start prodicLocation");
			XunLocTiming.getInstance();
			XunProdicLocation.getInstance();
			XunFlightModeModeSwitcher.getInstance();
		}


		try {
			int isWifiScanAvaliable = Settings.Global.getInt(mContext.getContentResolver(), "wifi_scan_always_enabled");
			Log.d(TAG, "onStartCommand: isWifiScanAvaliable="+String.valueOf(isWifiScanAvaliable));
		}catch (Exception e){
			Log.d(TAG, "onStartCommand: isWifiScanAvaliable :"+e.toString());
		}
		XunSensorProc.getInstance();
		StepCounterRecverAndSender.getInstance();

		if(getmNetworkManager().isLoginOK()) {
			XunEfenceStatus.getInstance().SyncEfenceDataFromServer();
			XunLocTrackingLocation.getInstance().SyncTrackingModeFromServer();
		}

		XunLocWifiScan.openWifiScan();
		//XunLocWifiScan.set24GBand();

		return START_STICKY;
	}

	private void getServiceManager(){
		//get server managers
		mContext = getApplicationContext();
		mApp = (StepsRanksApp)getApplication();

//		mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
//		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
//		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
//		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
//		mNetworkManager = (XiaoXunNetworkManager)mContext.getSystemService("xun.network.Service");
//		mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}	

	public static TelephonyManager getmTelephonyManager(){
		return (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}

	public static SensorManager getmSensorManager(){
		return (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
	}

	public static PowerManager getmPowerManager(){
		return  (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
	}

	public static WifiManager getmWifiManager(){
		return (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	public static LocationManager getmLocationManager(){
		return (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public static AlarmManager getmAlarmManager(){
		return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
	}

	public static XiaoXunNetworkManager getmNetworkManager() {
		return (XiaoXunNetworkManager) mContext.getSystemService("xun.network.Service");
	}

	public static XiaoXunStatisticsManager getStatisticsManager(){
		return (XiaoXunStatisticsManager) mContext.getSystemService("xun.statistics.service");
	}

	public static Context getmContext(){
		return mContext;
	}

	public static boolean isCmcc(){
		return cmcc;
	}

	public static boolean isGps_perf_test(){
		return gps_perf_test;
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");		
//		wakeLock.release();
	}


	public static boolean isBinded(){
		return SystemProperties.getBoolean("persist.sys.isbinded", false);
	}
	
}
