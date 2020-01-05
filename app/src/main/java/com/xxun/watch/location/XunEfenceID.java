package com.xxun.watch.location;



//import android.util.Log;

import net.minidev.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by xiaoxun on 2018/1/17.
 */

public class XunEfenceID {
	private static final String TAG = "[XunLoc]XunEfenceID";


	private static final String KEY_WIFI = "wifi";
	private static final String KEY_WGS84= "wgs84";
	private static final String KEY_RADIUS = "radius";
	private static final String KEY_TIMESTAMP = "timestamp";


	public int index;
	public ArrayList<XunLocWifiInfo> wifiInfos = null;
	public double lng = 0.0;
	public double lat = 0.0;
	public double radius = 0.0;
	public String timestamp = null;


	public void parseEfenceData(int index, JSONObject itemObject){
		this.index = index;
		try{
			String posStr = (String) itemObject.get(KEY_WGS84);
			if(posStr == null) {
				return;
			}
			Log.d(TAG, "parseEfenceData:posStr= "+posStr);

			Object radiusObj = itemObject.get(KEY_RADIUS);
			if(radiusObj == null) {
				return;
			}
			Log.d(TAG, "parseEfenceData:radiusObj= "+radiusObj);

			String timestampStr = (String) itemObject.get(KEY_TIMESTAMP);
			if(timestampStr == null) {
				return;
			}
			Log.d(TAG, "parseEfenceData: timestampStr="+timestampStr);

			if(posStr.length() == 0) {
				return;
			}

			String[] strs = posStr.split(",");
			if(strs.length != 2) {
				return;
			}

			lng = Double.valueOf(strs[0]);
			lat = Double.valueOf(strs[1]);

			Log.d(TAG, "parseEfenceData: lng="+lng+" lat="+lat);

			if(radiusObj instanceof Double) {
				radius = ((Double)radiusObj).doubleValue();
			}else if(radiusObj instanceof  Float){
				radius = ((Float)radiusObj).doubleValue();
			}else if(radiusObj instanceof  Integer){
				radius = ((Integer)radiusObj).doubleValue();
			}else if(radiusObj instanceof  Long){
				radius = ((Long)radiusObj).doubleValue();
			}else{
				return;
			}

			Log.d(TAG, "parseEfenceData: radius="+radius);

			if(timestampStr.length() == 0){
				return;
			}

			timestamp = new String(timestampStr);

			String wifiListStr = (String)itemObject.get(KEY_WIFI);
			Log.d(TAG, "parseEfenceData: wifiListStr="+ wifiListStr);
			splitWifiStr(wifiListStr);

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public String formatWifiToString(){
		StringBuilder sb = new StringBuilder();
		if(wifiInfos == null){
			return null;
		}

		if(wifiInfos.size() == 0){
			return null;
		}

		for(XunLocWifiInfo xunLocWifiInfo:wifiInfos){
			if(sb.length() != 0){
				sb.append("|");
			}
			sb.append(xunLocWifiInfo.BSSID);
			sb.append(",");
			sb.append(xunLocWifiInfo.level);
		}
		return sb.toString();
	}

	public void splitWifiStr(String wifiListStr){
		if(wifiListStr == null){
			return;
		}
		if(wifiListStr.length() == 0){
			return;
		}
		wifiInfos = new ArrayList<XunLocWifiInfo>();
		if(wifiListStr.contains("|"))
		{
			String[] strs = wifiListStr.split("\\|");
			for (int i = 0; i < strs.length ; i++) {
				if(strs[i].length() > 0){
					//Log.d(TAG, "splitWifiStr: "+strs[i]);
					String[] wifiItemArray = strs[i].split(",");
					XunLocWifiInfo wifiInfo = new XunLocWifiInfo();
					wifiInfo.BSSID = new String(wifiItemArray[0]);
					wifiInfo.level = new Integer(wifiItemArray[1]).intValue();
					wifiInfos.add(wifiInfo);
				}
			}
		}
	}

	public boolean isAvaliable(){
		if((timestamp != null)&&(lng != 0.0)&&(lat != 0.0)&&(radius != 0.0)){
			Log.d(TAG, "isAvaliable: true");
			return true;
		}
		return false;
	}

}
