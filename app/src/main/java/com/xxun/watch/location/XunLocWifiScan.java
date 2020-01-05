package com.xxun.watch.location;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
//import android.util.Log;

//import com.baidu.location.watch.BDLocManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuliang on 2017/9/28.
 */


public class XunLocWifiScan {
	final public static String TAG = "[XunLoc]XunLocWifiScan";
	private static final int MAX_WIFI_SCAN_COUNT = 3;
	private static final int MAX_WIFI_AP_MAX_COUNT = 15;
	private static final int MAX_WIFI_AP_MIN_COUNT = 2;
	public static int scanCounter = 0;
	private static ArrayList<WifiScanFinished> wifiScanFinishedArrayList;
	private static ArrayList<ScanResult> wifis= null;
	private static Handler handler = new Handler();
	private static PowerManager.WakeLock wakeLock;




	public static Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "run: time out");
			if(wifiScanFinishedArrayList == null) {
				return;
			}
			addNewWifiToList(XunLocation.getmWifiManager().getScanResults());
			reportWifiScanResults();
		}
	};

	public static void start(WifiScanFinished cb) {
		Log.d(TAG, "start: ");
		if(wifiScanFinishedArrayList == null) {
			wifiScanFinishedArrayList = new ArrayList<WifiScanFinished>();
			wifiScanFinishedArrayList.add(cb);
			ScanStart();
		}else {
			wifiScanFinishedArrayList.add(cb);
		}
	}

	private static void ScanStart(){
		Log.d(TAG, "ScanStart: ");
		Log.d(TAG, "ScanStart: isWifiEnabled="+XunLocation.getmWifiManager().isWifiEnabled());
		Log.d(TAG, "ScanStart: isScanAlwaysAvailable = "+XunLocation.getmWifiManager().isScanAlwaysAvailable());

		int wifiState = XunLocation.getmWifiManager().getWifiState();
		Log.d(TAG, "ScanStart: getWifiState="+wifiState);
//		if(true)//wifiState == WifiManager.WIFI_STATE_ENABLED) {
			Log.d(TAG, "ScanStart:  wifi scan start");
			XunLocation.getmWifiManager().startScan();
			scanCounter++;
//		}
//	else{
//			Log.d(TAG, "ScanStart: open wifi_scan_always_enabled 1");
//			openWifiScan();
//		}

		handler.postDelayed(runnable, 10000L);
	}

	public interface WifiScanFinished {
		void onWifiScanFinished(List<ScanResult> scanResults);
	}

	public static void onWifiScanAvailable(){
		Log.d(TAG, "onWifiScanAvailable: ");

		if (wifiScanFinishedArrayList != null) {
			Log.d(TAG, "onWifiScanAvailable: start scan");
			Log.d(TAG, "[singleLocTest] wifi open succ start scan "+ System.currentTimeMillis());
			XunLocation.getmWifiManager().startScan();
			scanCounter++;
		} else {
			Log.d(TAG, "onWifiScanAvailable:  null not scan");
		}
	}


	public static void openWifiScan(){
		Log.d(TAG, "openWifiScan: ");
		Settings.Global.putInt(XunLocation.getmContext().getContentResolver(), "wifi_scan_always_enabled", 1);
	}

	public static void closeWifiScan(){
		//Log.d(TAG, "closeWifiScan: ");
		//Settings.Global.putInt(XunLocation.getmContext().getContentResolver(), "wifi_scan_always_enabled", 0);
	}

	public static void set24GBand(){
	//XunLocation.getmWifiManager().setFrequencyBand(WIFI_FREQUENCY_BAND_2GHZ, true);
	}

	public static void onScanResults(){
		Log.d(TAG, "onScanResults: ");
		if(wifiScanFinishedArrayList == null){
			Log.d(TAG, "onScanResults: wifiScanFinishedArrayList null");
			return;
		}

		Log.d(TAG, "[singleLocTest] wifi scan succ "+ System.currentTimeMillis());
		addNewWifiToList(XunLocation.getmWifiManager().getScanResults());
		Log.d(TAG, "onScanResults: size = "+wifis.size()+" counter="+scanCounter);
		if(needContinueScan() == false){
			reportWifiScanResults();
		}else{
			XunLocation.getmWifiManager().startScan();
			scanCounter++;
		}
	}

	public static boolean needContinueScan(){
		if(scanCounter >= MAX_WIFI_SCAN_COUNT) {
			Log.d(TAG, "needContinueScan: over max count");
			return false;
		}else if(scanCounter == 2){
			if(wifis.size() > MAX_WIFI_AP_MIN_COUNT) {
				Log.d(TAG, "needContinueScan: size over min count");
				return false;
			}
		}else if(scanCounter == 1){
			if(wifis.size() >= MAX_WIFI_AP_MAX_COUNT){
				Log.d(TAG, "needContinueScan: size over max count");
				return false;
			}
		}
		Log.d(TAG, "needContinueScan: true");
		return true;
	}

	public static void addNewWifiToList(List<ScanResult> scanResults){
		Log.d(TAG, "addNewWifiToList: ");
		if(wifis == null){
			Log.d(TAG, "addNewWifiToList: wifis null");
			wifis = new ArrayList<ScanResult>();
		}
		if(scanResults == null) {
			Log.d(TAG, "addNewWifiToList: result = null");
			return;
		}
		if(scanResults.size() == 0){
			Log.d(TAG, "addNewWifiToList: result = 0");
			return;
		}
		try {
			if (wifis.size() == 0) {
				Log.d(TAG, "addNewWifiToList: wifis = 0");
				for (ScanResult scanResult : scanResults) {
					if(scanResult.level == 0) {
						Log.d(TAG, "addNewWifiToList: error wifi here");
						continue;
					}
					wifis.add(scanResult);
					Log.d(TAG, "addNewWifiToList: add " + scanResult.BSSID + ", " + scanResult.level);
				}
				return;
			} else {
				int count = wifis.size();
				int count2 = scanResults.size();
				Log.d(TAG, "addNewWifiToList: wifis = " + count);
				ScanResult scanResult = null;
				ScanResult wifi = null;
				boolean same = false;
				for (int j = 0; j < count2; j++) {
					scanResult = scanResults.get(j);
					same = false;
					if(scanResult.level == 0){
						Log.d(TAG, "addNewWifiToList: error wifi here");
						continue;
					}
					for (int i = 0; i < count; i++) {
						wifi = wifis.get(i);
						if (wifi.BSSID.equalsIgnoreCase(scanResult.BSSID)) {
							wifis.set(i, scanResult);
							Log.d(TAG, "addNewWifiToList: set " + scanResult.BSSID + ", " + scanResult.level + " to" + i);
							same = true;
							break;
						}
					}
					if(same == false) {
						wifis.add(scanResult);
						Log.d(TAG, "addNewWifiToList: add " + scanResult.BSSID + ", " + scanResult.level);
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private static void reportWifiScanResults(){
		if(wifiScanFinishedArrayList == null){
			Log.d(TAG, "reportWifiScanResults: wifiScanFinishedArrayList null");
			return;
		}

		for (WifiScanFinished scanCb: wifiScanFinishedArrayList) {
			scanCb.onWifiScanFinished(wifis);
		}
		//closeWifiScan();
		handler.removeCallbacks(runnable);
		wifiScanFinishedArrayList = null;
		wifis = null;
		scanCounter = 0;
	}
}
