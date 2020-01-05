package com.xxun.watch.location;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.provider.Settings;
//import android.util.Log;


import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by xiaoxun on 2017/9/28.
 */

public class XunActiveLocation implements XunLocGetGpsByPolicy.ISingleLocation, XunLocWifiScan.WifiScanFinished {
	private static final String TAG = "[XunLoc]XunActiveLocation";

	private boolean wifiScanFinished = false;
	private boolean gpsScanFinished = false;

	private XunLocRecord xunLocRecord;
	private long mSn;

	private Handler delayReportHandler = new Handler();
	private Runnable delayReportRunable = null;
	boolean delayReport = false;

	XunLocPolicyLastRecord lastPos = null;
	XunLocPosUploadPolicy uploadPolicy = null;
	XunRecordGps lastGps = null;


	@Override
	public void onWifiScanFinished(List<ScanResult> scanResults) {
		Log.d(TAG, "onWifiScanFinished: ");
		xunLocRecord.setWifiRecord(scanResults);
		xunLocRecord.setBaiduInfo();

		if(gpsScanFinished&&xunLocRecord.getGpsRecord().isAvaliable()){
				uploadNewPos();
		}else {
			if (XunLocation.isCmcc() == true) {
				uploadNewPos();
			} else if (lastGps.isAvaliable()) {
				Log.d(TAG, "onWifiScanFinished: last gps succ in 60s");
				xunLocRecord.setGpsRecord(lastGps.getGpsPos());
				uploadNewPos();
			} else if (isLastPosGpsAvaliable()) {
				Log.d(TAG, "onWifiScanFinished: last upload gps data can use now");
				uploadLastPos();
			} else if (lastPos.isPosChanged(xunLocRecord)) {
				uploadNewPos();
			} else if (lastPos.mergeWifiList(xunLocRecord.getWifiRecord()) == 0)
				uploadLastPos();
			else {
				uploadMeragePos();
			}
		}
		wifiScanFinished = true;
	}

	@Override
	public void onSingleReport(Location location) {
		Log.d(TAG, "onSingleReport: ");
		if (location != null) {
			Log.d(TAG, "onSingleReport: gps not null");
			xunLocRecord.setGpsRecord(location);
			xunLocRecord.setTimestamp(System.currentTimeMillis());
		}

		if(wifiScanFinished == false){
			Log.d(TAG, "onSingleReport: wifi scan not finished not upload");
		}else{

			if (xunLocRecord.getGpsRecord().isAvaliable()) {
				Log.d(TAG, "onSingleReport: gps is succ");
				Log.d(TAG, "[singleLocTest] gps upload start "+ System.currentTimeMillis());
				uploadNewPos();
			}

			if (delayReport == true) {
				Log.d(TAG, "onSingleReport: delay report running");
				if(delayReportRunable != null) {
					delayReportHandler.removeCallbacks(delayReportRunable);
					delayReportRunable.run();
				}
			}
		}

		gpsScanFinished = true;
	}


	private boolean isLastPosGpsAvaliable(){
		if(lastPos.isAvaliable() == false) {
			Log.d(TAG, "isLastPosGpsAvaliable: lastPos = false");
			return false;
		}
		if(lastPos.getLastGps().isAvaliable() == false) {
			Log.d(TAG, "isLastPosGpsAvaliable: last gps =false");
			return false;
		}

		if(lastPos.getMotionCounter() < 3){
			Log.d(TAG, "isLastPosGpsAvaliable: get gps record in motion <3");
			return true;
		}

		if((lastPos.getSameWifiCount(xunLocRecord.getWifiRecord())>7)&&(lastPos.getStepCountDiff()<200)){
			Log.d(TAG, "isLastPosGpsAvaliable: get gps record in wifi and step");
			return true;
		}
		return false;
	}

	private void uploadNewPos(){
		Log.d(TAG, "uploadNewPos");
		xunLocRecord.setTimestamp(System.currentTimeMillis());
		xunLocRecord.compeleteInfoData();
		if(needDelayUpload()){
			Log.d(TAG, "[singleLocTest] delay upload start timer "+ System.currentTimeMillis());
			Log.d(TAG, "uploadNewPos: need delay upload");
			delayReport = true;
			delayReportRunable = new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "uploadNewPos: delay upload run");
					Log.d(TAG, "[singleLocTest] delay upload "+ System.currentTimeMillis());
					uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
					lastPos.setNewRecord(xunLocRecord);
					delayReport = false;
				}
			};
			delayReportHandler.postDelayed(delayReportRunable, 30000);//delay pos in 30s
			return;
		}
		uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
		Log.d(TAG, "[singleLocTest] upload start "+ System.currentTimeMillis());
		lastPos.setNewRecord(xunLocRecord);
	}

	private void uploadLastPos(){
		Log.d(TAG, "uploadLastPos");
		xunLocRecord = lastPos.getLastUploadRecord();
		xunLocRecord.setTimestamp(System.currentTimeMillis());
		xunLocRecord.compeleteInfoData();
		if(needDelayUpload()){
			Log.d(TAG, "[singleLocTest] delay upload start timer "+ System.currentTimeMillis());
			Log.d(TAG, "uploadLastPos: need delay upload");
			delayReport = true;
			delayReportRunable = new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "uploadLastPos: delay upload run");
					Log.d(TAG, "[singleLocTest] delay upload "+ System.currentTimeMillis());
					uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
					delayReport = false;
				}
			};
			delayReportHandler.postDelayed(delayReportRunable, 30000);//delay pos in 30s
			return;
		}
		Log.d(TAG, "[singleLocTest] upload start "+ System.currentTimeMillis());
		uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
	}

	private void uploadMeragePos(){
		Log.d(TAG, "uploadMeragePos: ");
		xunLocRecord.setTimestamp(System.currentTimeMillis());
		xunLocRecord.compeleteInfoData();
		if(needDelayUpload()){
			Log.d(TAG, "[singleLocTest] delay upload start timer "+ System.currentTimeMillis());
			Log.d(TAG, "uploadMeragePos: need delay upload");
			delayReport = true;
			delayReportRunable = new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "uploadMeragePos: delay upload run");
					Log.d(TAG, "[singleLocTest] delay upload "+ System.currentTimeMillis());
					uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
					lastPos.updateLastRecord(xunLocRecord);
					delayReport = false;
				}
			};
			delayReportHandler.postDelayed(delayReportRunable , 30000);//delay pos in 30s
			return;
		}
		uploadPolicy.ActiveLocUpload(mSn,xunLocRecord);
		Log.d(TAG, "[singleLocTest] upload start "+ System.currentTimeMillis());
		lastPos.updateLastRecord(xunLocRecord);
	}

	private boolean needDelayUpload(){
/*		
		if(xunLocRecord.getGpsRecord().isAvaliable()){
			Log.d(TAG, "needDelayUpload: gps succ ");
			return false;
		}
		if(gpsScanFinished){
			Log.d(TAG, "needDelayUpload: gps scan finished ");
			return false;
		}
		if(xunLocRecord.getWifiRecord().isAvaliable() == false){
			Log.d(TAG, "needDelayUpload: wifi not Avaliable delay report");
			return true;
		}

		if(xunLocRecord.getWifiRecord().getWifiInfo().size() < 3){
			Log.d(TAG, "needDelayUpload: wifi count < 3 delay report");
			return true;
		}
*/		
		return false;
	}




	public XunActiveLocation(long sn){
		mSn = sn;
		xunLocRecord = new XunLocRecord();
		lastPos = XunLocPolicyLastRecord.getInstance();
		uploadPolicy = XunLocPosUploadPolicy.getInstance();
		lastGps = new XunRecordGps(XunLocGpsCtrl.getLastSuccPosByInterval(60));
		uploadPolicy.uploadCacheLocData(false);
		//start wifi scan and gps
		wifiScanFinished = false;
		gpsScanFinished = false;
		xunLocRecord.setCellRecord();
		XunLocWifiScan.start(this);
		XunLocGetGpsByPolicy.getPos(this);
		try {
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_ACTIVE_LOCATION);
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_LOCATION);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
