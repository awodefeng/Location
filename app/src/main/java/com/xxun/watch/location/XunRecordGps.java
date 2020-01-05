package com.xxun.watch.location;

import android.location.Location;
import android.provider.Settings;

import net.minidev.json.JSONObject;

/**
 * Created by xiaoxun on 2017/9/29.
 */

public class XunRecordGps {
	private static final String TAG = "[XunLoc]XunRecordGps";
	private boolean avaliable;
	private Location gps;
	private long timeStamp;

	public XunRecordGps(){
		avaliable = false;
		gps = null;
	}

	public XunRecordGps(Location location){
		if(location != null) {
			gps = new Location(location);
			avaliable = true;
			timeStamp = System.currentTimeMillis();
		}else{
			gps = null;
			avaliable = false;
			timeStamp = 0;
		}
	}


	public void setGpsPos(Location location){
		if(location != null) {
			gps = new Location(location);
			avaliable = true;
			timeStamp = System.currentTimeMillis();
		}else{
			gps = null;
			avaliable = false;
			timeStamp = 0;
		}
	}

	public Location getGpsPos(){
		return gps;
	}

	public boolean isAvaliable(){
		return avaliable;
	}

	public void addInfoToJson(JSONObject pl){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(gps.getLongitude());
		stringBuilder.append(",");
		stringBuilder.append(gps.getLatitude());
		stringBuilder.append("|");
		stringBuilder.append((long)gps.getAccuracy());

		pl.put("gps", stringBuilder.toString());

		JSONObject gps_data = new JSONObject();
		gps_data.put("ground_speed", String.valueOf(gps.getSpeed()*3.6));
		gps_data.put("trace_degree", String.valueOf(gps.getBearing()));
		gps_data.put("altitude", String.valueOf(gps.getAltitude()));

		pl.put("gps_data", gps_data);
	}

}
