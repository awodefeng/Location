package com.xxun.watch.location;

import android.location.GpsSatellite;
import android.location.Location;
import android.os.Handler;
//import android.util.Log;

import java.util.ArrayList;

/**
 * Created by wuliang on 2017/9/28.
 */

public class XunLocGetGpsByPolicy {
	public static final String TAG = "[XunLoc]XunLocGetGpsByPolicy";
	private static ArrayList<ISingleLocation> mSingleLocationArrayList;
	//	static SingleLocation mSingleLocation;
	private static IProdicLocation mProdicLocation;
	private static int singleLocation_counter = 0;
	private static int prodicLocation_counter = 0;
	private static Handler handler = new Handler();
	private static Runnable runnable = new Runnable() {
		@Override
		public void run() {
			onTimingCheck();
			handler.postDelayed(runnable, 1000L);
			gpsSwitch();
		}
	};

	private static XunLocGpsPerfLog perfLog;


	public XunLocGetGpsByPolicy(){
		//mSingleLocation = singleLocation;
		//gpsSwitch();
	}

	public static void getPos(ISingleLocation singleLocation){
		if(mSingleLocationArrayList == null){
			mSingleLocationArrayList = new ArrayList<ISingleLocation>();
			mSingleLocationArrayList.add(singleLocation);
			if(XunLocation.isGps_perf_test()) {
				perfLog = new XunLocGpsPerfLog();
			}else{
				perfLog = null;
			}
		}else {
			mSingleLocationArrayList.add(singleLocation);
		}
		gpsSwitch();
	}


	public static void getPos(IProdicLocation prodicLocation){
		mProdicLocation = prodicLocation;
		gpsSwitch();
	}

	static private void gpsSwitch(){
		Log.d(TAG, "gpsSwitch: ");
		if((mProdicLocation != null) || (mSingleLocationArrayList != null)){
			if(XunLocGpsCtrl.isPowerOn() == false) {
				XunLocGpsCtrl.startGps();
				Log.d(TAG, "gpsSwitch: startGps");
				startTimingCheck();
			}
		}
		else {
			if(XunLocGpsCtrl.isPowerOn() == true) {
				Log.d(TAG, "gpsSwitch: stopGps");
				XunLocGpsCtrl.stopGps();
				stopTimingCheck();
			}
		}
	}

	static private void startTimingCheck(){
		Log.d(TAG, "startTimingCheck: ");
		handler.postDelayed(runnable, 1000L);
	}

	static private void stopTimingCheck(){
		Log.d(TAG, "stopTimingCheck: ");
		handler.removeCallbacks(runnable);
	}

	static private void onTimingCheck(){
		prodicWorkingProc();
		singleWorkingProc();
	}

	static private void singleWorkingProc() {
		if(mSingleLocationArrayList == null){
			return;
		}
		if(mSingleLocationArrayList.size() == 0){
			mSingleLocationArrayList = null;
			return;
		}
		if((XunLocGpsCtrl.isSucc() == true)&&(XunLocGpsCtrl.getLocation() != null)){
			SingleLocationReport(XunLocGpsCtrl.getLocation());
			if(perfLog != null) {
				perfLog.onFinish(true);
			}
		}else{
			singleLocation_counter++;
			if(perfLog != null){
				perfLog.onTimingRecord();
				if(singleLocation_counter >= 90) {
					SingleLocationReport(null);
					perfLog.onFinish(false);
				}
			}else if(singleLocation_counter >= 56){
					SingleLocationReport(null);
			}else if(singleLocation_counter >= 25){
				if(getSnrBySateIndex(2) < 15){
					SingleLocationReport(null);
				}
			}else if(singleLocation_counter >= 6){
				if(getSnrBySateIndex(0)< 15){
					SingleLocationReport(null);
				}
			}
		}
	}

	public static float getSnrBySateIndex(int index)
	{
		ArrayList<GpsSatellite> satellites = XunLocGpsCtrl.getGpsSatelliteArrayList();
		if(satellites == null) {
			Log.d(TAG, "getSnrBySateIndex: null index="+String.valueOf(index)+" snr = 0");
			return 0;
		}
		Log.d(TAG, "getSnrBySateIndex: count ="+String.valueOf(satellites.size()));
		if(satellites.size()>index) {
			float snr = XunLocGpsCtrl.getGpsSatelliteArrayList().get(index).getSnr();
			Log.d(TAG, "getSnrBySateIndex: index="+String.valueOf(index)+" snr = "+String.valueOf(snr));
			return XunLocGpsCtrl.getGpsSatelliteArrayList().get(index).getSnr();
		} else {
			Log.d(TAG, "getSnrBySateIndex: index="+String.valueOf(index)+" snr = 0");
			return 0;
		}
	}

	static private void SingleLocationReport(Location location){
		for(ISingleLocation singleLocation : mSingleLocationArrayList) {
			singleLocation.onSingleReport(location);
		}
		mSingleLocationArrayList = null;
		singleLocation_counter = 0;
	}


	static private void prodicWorkingProc(){
		if(mProdicLocation == null){
			return;
		}
		if(prodicLocation_counter >= mProdicLocation.getReportProdic()){
			prodicLocation_counter = 0;
			if((XunLocGpsCtrl.isSucc() == true)&&(XunLocGpsCtrl.getLocation() != null)){
				mProdicLocation.onProdicReport(XunLocGpsCtrl.getLocation());
			}else{
				mProdicLocation.onProdicReport(null);
			}
		}else{
			prodicLocation_counter++;
		}
		if(!mProdicLocation.isLocationWorking()){
			mProdicLocation = null;
			prodicLocation_counter = 0;
		}
	}

	interface ISingleLocation{
		void onSingleReport(Location location);
	}

	interface IProdicLocation{
		void onProdicReport(Location location);
		boolean isLocationWorking();
		int getReportProdic();
	}


}

