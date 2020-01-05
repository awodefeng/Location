package com.xxun.watch.location;

import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.provider.CalendarContract;
//import android.util.Log;


import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IMessageReceiveListener;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by xiaoxun on 2017/9/28.
 */


public class XunLocTrackingLocation implements XunLocGetGpsByPolicy.IProdicLocation,XunLocWifiScan.WifiScanFinished {
	private static final String TAG = "[XunLoc]XunLocTrackingLocation";
	private static final int MAX_SKIP_COUNTE = 5;
	private static XunLocTrackingLocation instance = null;
	private int mProdic = 0;
	private long mEndtime = 0;
	private int uploadSkipCounter = 0;


	public static XunLocTrackingLocation getInstance(){
		if(instance == null){
			Log.d(TAG, "getInstance: start");
			instance = new XunLocTrackingLocation();
		}
		return instance;
	}

	@Override
	public void onProdicReport(Location location) {
		boolean need_upload = false;
		try{
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION);
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_LOCATION);
		}catch (Exception e){
			e.printStackTrace();
		}
		if (location == null) {
			Log.d(TAG, "onProdicReport: gps fail");
			XunLocWifiScan.start(this);
		} else {
			Log.d(TAG, "onProdicReport: gps succ");
			XunLocRecord xunLocRecord = new XunLocRecord();
			xunLocRecord.setGpsRecord(location);
			xunLocRecord.setCellRecord();

			XunLocPolicyLastRecord lastRecord = XunLocPolicyLastRecord.getInstance();
			if(uploadSkipCounter > MAX_SKIP_COUNTE) {
				Log.d(TAG, "onProdicReport: upload By Counter");
				need_upload = true;
			}else if(lastRecord.isAvaliable() == false) {
				Log.d(TAG, "onProdicReport: lastRecord unavaliable");
				need_upload = true;
			}else if(lastRecord.getLastGps().isAvaliable() == true){
				Log.d(TAG, "onProdicReport: lastRecord gps avaliable");
				float distance = lastRecord.getLastGps().getGpsPos().distanceTo(xunLocRecord.getGpsRecord().getGpsPos());
				Log.d(TAG, "onProdicReport: distance ="+distance);
				if(distance > 25.0) {
					need_upload = true;
				}
			}else{
				Log.d(TAG, "onProdicReport: gps upload");
				need_upload = true;
			}

			if(need_upload) {
				xunLocRecord.compeleteInfoData();
				XunLocPosUploadPolicy.getInstance().uploadOrSaveByNetworkStatus(xunLocRecord);
				lastRecord.setNewRecord(xunLocRecord);
				uploadSkipCounter = 0;
			}else{
				uploadSkipCounter++;
			}
		}
	}

	@Override
	public boolean isLocationWorking() {
		if(isInTrackingTime()) {
			return true;
		}else {
			mEndtime = 0;
			return false;
		}
	}

	@Override
	public int getReportProdic() {
		return mProdic;
	}

	@Override
	public void onWifiScanFinished(List<ScanResult> scanResults) {
		XunLocRecord xunLocRecord = new XunLocRecord();
		XunLocPolicyLastRecord lastRecord = XunLocPolicyLastRecord.getInstance();
		boolean need_upload = false;
		xunLocRecord.setWifiRecord(scanResults);
		xunLocRecord.setCellRecord();
		Log.d(TAG, "onWifiScanFinished: "+xunLocRecord.getWifiRecord().formatListToString());
		if(uploadSkipCounter > MAX_SKIP_COUNTE){
			Log.d(TAG, "onWifiScanFinished: upload by counter");
			need_upload = true;
		}else if(lastRecord.isAvaliable() == false){
			Log.d(TAG, "onWifiScanFinished: last record not avaliable");
			need_upload = true;
		}else if(xunLocRecord.getWifiRecord().isAvaliable()){
			if(lastRecord.loc_srv_ca_wifi_check_change(xunLocRecord.getWifiRecord()) == true){
				Log.d(TAG, "onWifiScanFinished: wifi change");
				need_upload = true;
			}
		}else if(lastRecord.getStepCountDiff() > 50){
			Log.d(TAG, "onWifiScanFinished: step over 50");
			need_upload = true;
		}

		if(need_upload) {
			xunLocRecord.compeleteInfoData();
			XunLocPosUploadPolicy.getInstance().uploadOrSaveByNetworkStatus(xunLocRecord);
			lastRecord.setNewRecord(xunLocRecord);
			uploadSkipCounter = 0;
		}else{
			uploadSkipCounter++;
		}
	}

	public void start(int prodic, long endtime) {
		Log.d(TAG, "start: prodic="+prodic);
		Log.d(TAG, "start: endtime="+endtime);

		if(isInTrackingTime() == false){
			uploadSkipCounter = MAX_SKIP_COUNTE+1;
		}

		mEndtime = endtime;
		mProdic = prodic;
		if(isInTrackingTime()) {
			XunLocGetGpsByPolicy.getPos(this);
		}else {
		
		}
	}

	public void stop() {
		mEndtime = 0;
		Log.d(TAG, "stop: ");
		uploadSkipCounter = MAX_SKIP_COUNTE +1;
	}

	public boolean isInTrackingTime() {
		if (mEndtime > System.currentTimeMillis()) {
			Log.d(TAG, "isInTrackingTime: true");
			return true;
		} else {
			Log.d(TAG, "isInTrackingTime: false");
			return false;
		}
	}
/*
{
	"RC": 1,
	"Version": "00040000",
	"SN": -1512745849,
	"PL": {
		"mode": 1,
		"EID": "92732B2437A8885971774F96A7A03EDA",
		"sub_action": 106,
		"GID": "CE6797E7CAFB04687F5B4B15C87A2FC0",
		"freq": 20,
		"endTime": "20180306134936207",
		"value": 1,
		"timestamp": "20180306134436208"
	},
	"CID": 53022
}
*/
	public void getTrackStatus(String data){
		JSONObject pl = null;
		if(data == null) {
			return;
		}

		try{
			pl = (JSONObject) JSONValue.parse(data);

			if(pl == null) {
				return;
			}
			Integer val = (Integer)pl.get("value");
			if(val != null) {
				if (val.intValue() == 0) {
					stop();
				} else {
					String strEndTime = (String) pl.get("endTime");
					int freq = (Integer) pl.get("freq");
					Log.d(TAG, "getTrackStatus: endTime = "+strEndTime);
					Log.d(TAG, "getTrackStatus: freq = "+freq);
					SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
					Date endTime = format.parse(strEndTime);
					start(20, endTime.getTime());
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void SyncTrackingModeFromServer(){

		Log.d(TAG, "SyncTrackingModeFromServer: ");
		if(XunLocation.isBinded() == false) {
			Log.d(TAG, "SyncTrackingModeFromServer: bind false");
			return;
		}

		String eid = XunLocation.getmNetworkManager().getWatchEid();
		String gid = XunLocation.getmNetworkManager().getWatchGid();

		Log.d(TAG, "SyncTrackingModeFromServer: eid gid="+eid+"  "+gid);
		try {
			if ((eid != null)&&(gid != null)) {
				XunLocation.getmNetworkManager().getTrackStatus(eid, gid, new CallBack() {
					@Override
					public void onSuccess(ResponseData responseData) {
						if(responseData.getResponseCode() == 100){
							Intent intent = new Intent("action.location.track");
							intent.putExtra("data", responseData.getResponseData());
							XunLocation.getmContext().sendBroadcast(intent);
//							try{
//								Log.d(TAG,"responseData:"+responseData.getResponseData());
//								getTrackStatus(responseData.getResponseData());

//							}catch(Exception e){
//								e.printStackTrace();
//							}
						}
					}

					@Override
					public void onError(int i, String s) {

					}
				});
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
    public class CallBack extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {}
           @Override
           public void onError(int i, String s) {}    
    }

}
