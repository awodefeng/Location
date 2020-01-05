package com.xxun.watch.location;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
//import android.util.Log;
import android.provider.Settings;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import java.util.List;

import  android.util.XiaoXunUtil;

/**
 * Created by xiaoxun on 2017/9/28.
 */


public class XunProdicLocation {
	private static final String TAG = "[XunLoc]XunProdicLocation";
	private static final int WIFI_RELIABLE_COUNT = 8;

	public static final int WORKING_MODE_NORMAL = 0;
	public static final int WORKING_MODE_FAST = 1;
	public static final int WORKING_MODE_LAZY = 2;

	private static final int NORMAL_MODE_PRODIC_INTERVAL	= 300;
	private static final int POSITIVE_MODE_PRODIC_INTERVAL = 240;
	private static final int FAST_MODE_PRODIC_INTERVAL	= 180;

	private int workingMode = WORKING_MODE_NORMAL;

	private static XunProdicLocation instance = null;
	private XunLocRecord xunLocRecord = null;
	private boolean needGps = false;
	private boolean needUpload = false;
	private boolean keeyRefWifi = false;
	private int continue_still_count = 0;
	private boolean flightModeStill;
	private boolean scaning;
	private int[] old_efenceStatus = null;
	private boolean positive;	
	private long scantik = 0;


	private XunLocWifiScan.WifiScanFinished wifiScanFinished = new XunLocWifiScan.WifiScanFinished() {
		@Override
		public void onWifiScanFinished(List<ScanResult> scanResults) {
			Log.d(TAG, "onWifiScanFinished:");
			XunLocPolicyLastRecord lastPos = XunLocPolicyLastRecord.getInstance();
			xunLocRecord = new XunLocRecord();
			xunLocRecord.setWifiRecord(scanResults);
			xunLocRecord.setBaiduInfo();
			if(xunLocRecord.getWifiRecord().isAvaliable()) {
				Log.d(TAG, "onWifiScanFinished:Wifi " + xunLocRecord.getWifiRecord().formatListToString());
			}else{
				Log.d(TAG, "onWifiScanFinished: wifi null");
			}
			
			xunLocRecord.setCellRecord();
			if(xunLocRecord.getCellRecord().isAvaliable()){
				Log.d(TAG, "onWifiScanFinished:Cell" + xunLocRecord.getCellRecord().formatNetWorkString()+" "+xunLocRecord.getCellRecord().formatBtsString());
			}else{
				Log.d(TAG, "onWifiScanFinished:Cell null");
			}


			if(lastPos.isPosChanged(xunLocRecord)){
				needGps = true;
				needUpload = true;
			}else{
				if(lastPos.getLastGps().isAvaliable() == true){
					Log.d(TAG, "onWifiScanFinished: not report in last gps succ");
				} else if (lastPos.getLastWifi().isAvaliable() == false){
					needGps = true;
				} else if(lastPos.getLastWifi().getWifiInfo().size() <= WIFI_RELIABLE_COUNT){
					Log.d(TAG, "onWifiScanFinished: have new wifi insert here");
					if(lastPos.mergeWifiList(xunLocRecord.getWifiRecord()) > 0){
						Log.d(TAG, "onWifiScanFinished: have new wifi insert here");
						keeyRefWifi = true;
						if(xunLocRecord.getWifiRecord().getWifiInfo().size() <= 2){
							Log.d(TAG, "onWifiScanFinished: wifi count <= 2");
							needGps = true;
						}else{
							Log.d(TAG, "onWifiScanFinished: wifi count >2");
							needUpload = true;
						}
					} else{
						Log.d(TAG, "onWifiScanFinished: not upload");
					}
				}
				Log.d(TAG, "onWifiScanFinished: FlightMotion="+lastPos.getFlightMotionCounter());
				if(lastPos.getFlightMotionCounter() < 3){
					Log.d(TAG, "onWifiScanFinished flightModeStill: true");
					flightModeStill = true;
				}
			}


			if(needGps) {
				XunLocGetGpsByPolicy.getPos(singleLocation);
			}else{
				if(needUpload) {
					if (keeyRefWifi) {
						uploadMeragePos();
					} else {
						uploadNewPos();
					}
				}
				onScanFinished();
			}
		}
	};

	private XunLocGetGpsByPolicy.ISingleLocation singleLocation = new XunLocGetGpsByPolicy.ISingleLocation() {
		@Override
		public void onSingleReport(Location location) {
			Log.d(TAG, "onSingleReport:");
			XunLocPolicyLastRecord lastPos = XunLocPolicyLastRecord.getInstance();
			xunLocRecord.setGpsRecord(location);
			if(xunLocRecord.getGpsRecord().isAvaliable()){
				Log.d(TAG, "onSingleReport: gps=" + xunLocRecord.getGpsRecord().toString());
				if(lastPos.isAvaliable()){
					if(lastPos.getLastGps().isAvaliable())
					{
						Log.d(TAG, "onSingleReport: last gps succ");
						if(location.distanceTo(lastPos.getLastGps().getGpsPos()) > 15){
							Log.d(TAG, "onSingleReport: distance >15");
							uploadNewPos();
						}else{
							Log.d(TAG, "onSingleReport: near last pos not upload");
						}
					}else{
						Log.d(TAG, "onSingleReport: last gps fail upload new");
						uploadNewPos();
					}
				}else {
					Log.d(TAG, "onSingleReport: last pos empty");
					uploadNewPos();
				}
			}else{
				Log.d(TAG, "onSingleReport: gps fail");
				if(keeyRefWifi){
					Log.d(TAG, "onSingleReport: keep ref wifi upload");
					uploadMeragePos();
				}else{
					Log.d(TAG, "onSingleReport: not keep wifi");
					uploadNewPos();
				}
			}
			onScanFinished();
		}
	};


	private void uploadNewPos(){
		Log.d(TAG, "uploadNewPos");
		xunLocRecord.setTimestamp(System.currentTimeMillis());
		xunLocRecord.compeleteInfoData();
		boolean drop = false;//XunEfenceStatus.getInstance().getDropStatus(xunLocRecord);
		boolean padding_upload = !XunEfenceStatus.getInstance().isEfenceChange();


		if(drop){
			xunLocRecord.setDrop(1);
		}

		if(positive){
			padding_upload = false;
		}

		xunLocRecord.saveToDb();
		XunLocPolicyLastRecord.getInstance().setNewRecord(xunLocRecord);

		XunLocPosUploadPolicy.getInstance().uploadCacheLocData(padding_upload);

	}


	private void uploadMeragePos(){
		Log.d(TAG, "uploadMeragePos: ");
		xunLocRecord.setTimestamp(System.currentTimeMillis());

		boolean padding_upload = !XunEfenceStatus.getInstance().isEfenceChange();		

		if(positive){
			padding_upload = false;
		}

		xunLocRecord.saveToDb();
		XunLocPolicyLastRecord.getInstance().updateLastRecord(xunLocRecord);


		XunLocPosUploadPolicy.getInstance().uploadCacheLocData(padding_upload);

	}


	public XunProdicLocation(){
		Log.d(TAG, "XunProdicLocation: start");

		SyncWorkingMode();
		regModeContentObserver();
	}


	private void SyncWorkingMode(){

		int connect_mode = 3;

		try {
			connect_mode = android.provider.Settings.System.getInt(XunLocation.getmContext().getContentResolver(), "operation_mode_value");
			Log.d(TAG, "SyncWorkingMode: "+connect_mode);
		}catch (Exception e){
			Log.d(TAG, "initWorkingMode: "+e.toString());
			connect_mode = 3;
		}

		if(connect_mode == 3){
			workingMode = WORKING_MODE_NORMAL;
		}else if(connect_mode == 4){
			workingMode = WORKING_MODE_LAZY;
		}else if(connect_mode == 5){
			workingMode = WORKING_MODE_FAST;
		}else{
			workingMode = WORKING_MODE_NORMAL;
		}
	}

	private void regModeContentObserver(){
		Uri operationModeValueUri = Settings.System.getUriFor("operation_mode_value");
		XunLocation.getmContext().getContentResolver().registerContentObserver(operationModeValueUri, false, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				Log.d(TAG, "regModeContentObserver onChange: operation_mode_value change");
				SyncWorkingMode();
			}
		});
	}




	public static XunProdicLocation getInstance(){
		if(instance == null){
			instance = new XunProdicLocation();
		}
		return instance;
	}


	public void startPositionCheck(){
		Log.d(TAG, "startPositionCheck: ");		
		if(false == XunLocation.isBinded()){
			Log.d(TAG, "startPositionCheck: not bind return");
			XunLocTiming.getInstance().setNextScanInterval();
			return;
		}


		if(scaning == true){
			Log.d(TAG, "startPositionCheck: scaning...");
			if((SystemClock.elapsedRealtime() - scantik)>20*60*1000){
				Log.d(TAG, "startPositionCheck: over 20 min not working");
				scaning = false;
				scantik = 0;
			}
			return;
		}


		scaning = true;
		scantik = SystemClock.elapsedRealtime();
		flightModeStill = false;


		positive = XunLocPositiveMode.getPositiveMode(XunLocation.getmContext());
		Log.d(TAG, "startPositionCheck: getPositiveMode= "+positive);


		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			onScanFinished();
			return;
		}

		int offlineMode = getOfflineMode();

		if((workingMode == WORKING_MODE_LAZY)||(offlineMode != 0)){
			Log.d(TAG, "startPositionCheck: Lazy Mode not working FlightMotion="+XunLocPolicyLastRecord.getInstance().getFlightMotionCounter());
			if( XunLocPolicyLastRecord.getInstance().getFlightMotionCounter() < 3) {
				Log.d(TAG, "startPositionCheck: flightModeStill true");
				flightModeStill = true;
			}
			onScanFinished();
			return;
		}


		Log.d(TAG, "startPositionCheck: PeriodMotion="+XunLocPolicyLastRecord.getInstance().getPeriodMotionCounter());
		if( XunLocPolicyLastRecord.getInstance().getPeriodMotionCounter()<3)
		{
			Log.d(TAG, "startPositionCheck: flightModeStill true");
			flightModeStill = true;
			onScanFinished();
			return;
		}
		needGps = false;
		needUpload = false;
		keeyRefWifi = false;
		xunLocRecord = new XunLocRecord();
		try{
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION);
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_LOCATION);
		}catch (Exception e){
			e.printStackTrace();
		}
		XunLocWifiScan.start(wifiScanFinished);
	}




	public int getCurWorkingMode(){
		Log.d(TAG, "getCurWorkingMode: "+workingMode);
		return workingMode;
	}

	public int getStillCount(){
		return continue_still_count;
	}

	public void clearStillCount(){
		continue_still_count = 0;
	}


	public long getTimerProdic(){
		Log.d(TAG, "getTimerProdic: ");
		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			return NORMAL_MODE_PRODIC_INTERVAL * 1000;
		} else if(workingMode == WORKING_MODE_NORMAL){
			if(positive == true){
				return POSITIVE_MODE_PRODIC_INTERVAL * 1000;
			}else{
				return NORMAL_MODE_PRODIC_INTERVAL * 1000;
			}
		}else if(workingMode == WORKING_MODE_FAST) {
			return FAST_MODE_PRODIC_INTERVAL* 1000;
		}else if(workingMode == WORKING_MODE_LAZY) {
			return NORMAL_MODE_PRODIC_INTERVAL * 1000;
		}

		return NORMAL_MODE_PRODIC_INTERVAL*1000;
	}

	public long getAlarmProdic(){
		Log.d(TAG, "getAlarmProdic: ");
		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			return 0;
		} else if(workingMode == WORKING_MODE_NORMAL){
			if(positive == true){
				return POSITIVE_MODE_PRODIC_INTERVAL * 1000;
			}else{
				return NORMAL_MODE_PRODIC_INTERVAL * 1000;
			}
		}else if(workingMode == WORKING_MODE_FAST) {
			return FAST_MODE_PRODIC_INTERVAL* 1000;
		}else if(workingMode == WORKING_MODE_LAZY) {
			return NORMAL_MODE_PRODIC_INTERVAL * 1000;
		}
		return NORMAL_MODE_PRODIC_INTERVAL * 1000;
	}

	private void onScanFinished(){
		Log.d(TAG, "onScanFinished: ");

		XunFlightModeModeSwitcher.getInstance().checkFlightMode();
		XunLocTiming.getInstance().setNextScanInterval();


		if(flightModeStill == true){
			Log.d(TAG, "onScanFinished: continue_still_count = "+continue_still_count);
			continue_still_count ++;
		}else{
			Log.d(TAG, "onScanFinished: continue_still_count = "+continue_still_count);
			continue_still_count = 0;
		}
		XunLocPolicyLastRecord.getInstance().clearFlightMotionCounter();
		XunLocPolicyLastRecord.getInstance().clearPeriodMotionCounter();
		scaning = false;
		scantik = 0;		


	}

	private int getOfflineMode(){
		int offlinevalue = 0;
		//if("userdebug".equals(SystemProperties.get("ro.build.type"))){
		if (XiaoXunUtil.XIAOXUN_CONFIG_PRODUCT_SEVEN_THREE_ZERO){

		}else{
			try{
				offlinevalue = Settings.System.getInt(XunLocation.getmContext().getContentResolver(),"offlinevalue");
				Log.d(TAG, "getOfflineMode: offlinevalue = "+offlinevalue);
			}catch(Exception e){
				//e.printStackTrace();
				Log.d(TAG, "getOfflineMode: offlinevalue read fail");
				offlinevalue = 0;

			}
		}
		return offlinevalue;
	}


}


