package com.xxun.watch.location;

import android.net.wifi.ScanResult;
//import android.util.Log;

import net.minidev.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by xiaoxun on 2017/9/29.
 */

public class XunRecordWifi {
	private static final String TAG = "[XunLoc]XunRecordWifi";
	private boolean avaliable;
	private ArrayList<XunLocWifiInfo> wifiInfo;

	public XunRecordWifi(){
		avaliable = false;
		wifiInfo = null;
	}

	public void setWifiInfo(List<ScanResult> scanResults){
		if(scanResults == null){
			avaliable = false;
			wifiInfo = null;
			return;
		}
		if(scanResults.size() == 0){
			avaliable = false;
			wifiInfo = null;
			return;
		}

		avaliable = true;
		wifiInfo = new ArrayList<XunLocWifiInfo>();
		for(ScanResult scanResult: scanResults){
			if(scanResult.level != 0) {
				wifiInfo.add(new XunLocWifiInfo(scanResult));
			}
		}
	}

	public void setWifiInfo(XunRecordWifi recordWifi){
		if(recordWifi == null){
			avaliable = false;
			wifiInfo = null;
			return;
		}
		if(recordWifi.isAvaliable() == false){
			avaliable = false;
			wifiInfo = null;
			return;
		}

		avaliable = true;
		wifiInfo = new ArrayList<XunLocWifiInfo>();
		for(XunLocWifiInfo xunLocWifiInfo: recordWifi.getWifiInfo()){
			wifiInfo.add(new XunLocWifiInfo(xunLocWifiInfo));
		}
	}

	public boolean isAvaliable() {
		return avaliable;
	}

	public ArrayList<XunLocWifiInfo> getWifiInfo(){
		return wifiInfo;
	}


	public void addInfoToJson(JSONObject pl){
		if(avaliable == false){
			return;
		}
		pl.put("macs", formatListToString());
	}

	public String formatListToString(){
		StringBuilder sb = new StringBuilder();

		Log.d(TAG, "formatListToString: "+wifiInfo);
		// sort wifi by level
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		try{
			Collections.sort(wifiInfo, new Comparator<XunLocWifiInfo>() {
				@Override
				public int compare(XunLocWifiInfo lhs, XunLocWifiInfo rhs) {
					if((lhs == null)||(rhs == null)){
						return 0;
					}
					if(rhs.level > lhs.level){
						return 1;
					}else if(rhs.level == lhs.level){
						return 0;
					}else{
						return -1;
					}
				}
			});
		}catch (Exception e){
			Log.d(TAG, "formatListToString: sort fail");
		}
		Log.d(TAG, "formatListToString: "+wifiInfo);
		int i = 0;
		for(XunLocWifiInfo xunLocWifiInfo:wifiInfo){
			if(i > 60)
				break;
			if(sb.length() != 0){
				sb.append("|");
			}
			sb.append(xunLocWifiInfo.BSSID);
			sb.append(",");
			sb.append(xunLocWifiInfo.level);
			sb.append(",");
			//sb.append(xunLocWifiInfo.SSID);
			i++;
		}
		return sb.toString();
	}
}
