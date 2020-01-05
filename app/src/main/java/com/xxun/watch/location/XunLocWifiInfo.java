package com.xxun.watch.location;

import android.net.wifi.ScanResult;

/**
 * Created by xiaoxun on 2017/10/13.
 */

public class XunLocWifiInfo {
	private static final String TAG = "[XunLoc]XunLocWifiInfo";
	public String BSSID;
	public String SSID;
	public int level;

	public XunLocWifiInfo(ScanResult result){
		BSSID = new String(result.BSSID);
		SSID = new String(result.SSID);
		level = result.level;
	}

	public XunLocWifiInfo(XunLocWifiInfo result){
		BSSID = new String(result.BSSID);
		SSID = new String(result.SSID);
		level = result.level;
	}

	public XunLocWifiInfo(){

	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(BSSID);
		sb.append(",");
		sb.append(SSID);
		sb.append(",");
		sb.append(level);
		return sb.toString();
	}
}
