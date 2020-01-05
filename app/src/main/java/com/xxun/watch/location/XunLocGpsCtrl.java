package com.xxun.watch.location;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
//import android.util.Log;

import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by wuliang on 2017/9/28.
 */

class XunLocGpsCtrl {
	private static final String TAG = "[XunLoc]XunLocGpsCtrl";
	private static boolean powerOn;
	private static int mSuccCounter;
	private static ArrayList<GpsSatellite> mGpsSatelliteArrayList;
	private static Location mLocation;
	private static Location mlastLocation;
	private static Location mSuccLocation;
	private static long timeStamp;
	private static boolean mSucc = false;
	private static int mStatus;
	private static PowerManager.WakeLock wakeLock;
//	private static int ttff;

	private static LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "onLocationChanged: "+location);
			mLocation = new Location(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			switch (status) {
				// GPS状态为可见时
				case LocationProvider.AVAILABLE:
					//mLocation = new Location(provider);
					Log.d(TAG, "onStatusChanged: AVAILABLE" + provider);
					break;
				// GPS状态为服务区外时
				case LocationProvider.OUT_OF_SERVICE:
					Log.d(TAG, "onStatusChanged: OUT_OF_SERVICE" + provider);
					mSuccCounter = 0;
					mLocation = null;
					break;
				// GPS状态为暂停服务时
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					Log.d(TAG, "onStatusChanged: TEMPORARILY_UNAVAILABLE" + provider);
					mSuccCounter = 0;
					mLocation = null;
					break;
			}
			mStatus = status;
			Log.d(TAG, "onStatusChanged="+String.format("%d", mStatus));
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "onProviderEnabled");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled");
		}
	};

	private static GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			GpsStatus gpsStatus = (GpsStatus) XunLocation.getmLocationManager().getGpsStatus(null);
			switch(event) {
				case GpsStatus.GPS_EVENT_STARTED:
					Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_STARTED");
					//powerOn = true;
					break;
				case GpsStatus.GPS_EVENT_STOPPED:
					Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_STOPPED");
					//powerOn = false;
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					int ttff = gpsStatus.getTimeToFirstFix();
					Log.d(TAG, "onGpsStatusChanged: GPS_EVENT_FIRST_FIX" + ttff);
					break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					int maxsatellites =  gpsStatus.getMaxSatellites();
					StringBuilder stringBuilder = new StringBuilder();
					//stringBuilder.append("Sate: ");
					Iterable<GpsSatellite> gpsSatelliteIterable = gpsStatus.getSatellites();
					mGpsSatelliteArrayList = new ArrayList<GpsSatellite>();
					for (GpsSatellite gpsSatellite: gpsSatelliteIterable){
						mGpsSatelliteArrayList.add(gpsSatellite);
						//stringBuilder.append(gpsSatellite.getPrn());
						//stringBuilder.append(',');
						//stringBuilder.append(gpsSatellite.getSnr());
						//stringBuilder.append(',');
						//stringBuilder.append(gpsSatellite.usedInFix());
						//stringBuilder.append("| ");
					}
					if(mGpsSatelliteArrayList.size() == 0){
						Log.d(TAG, "Sate size = 0");
					}
					//Log.d(TAG, stringBuilder.toString());
					System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

					Collections.sort(mGpsSatelliteArrayList, new Comparator<GpsSatellite>() {
						@Override
						public int compare(GpsSatellite lhs, GpsSatellite rhs) {
							if ((lhs == null) || (rhs == null)) {
								Log.d(TAG, "sort error");
								return 0;
							}
							if (rhs.getSnr() > lhs.getSnr()) {
								return 1;
							} else if (rhs.getSnr() == lhs.getSnr()) {
								return 0;
							} else {
								return -1;
							}
						}
					});

					stringBuilder = new StringBuilder();
					stringBuilder.append("Sate sort: ");
					for (GpsSatellite gpsSatellite: mGpsSatelliteArrayList){
						stringBuilder.append(gpsSatellite.getPrn());
						stringBuilder.append(',');
						stringBuilder.append(gpsSatellite.getSnr());
						stringBuilder.append(',');
						stringBuilder.append(gpsSatellite.usedInFix());
						stringBuilder.append(',');
						stringBuilder.append(gpsSatellite.hasAlmanac());
						stringBuilder.append(',');
						stringBuilder.append(gpsSatellite.hasEphemeris());
						stringBuilder.append("| ");
					}
					Log.d(TAG, stringBuilder.toString());
					break;
			}
		}
	};

	private static GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
		@Override
		public void onNmeaReceived(long timestamp, String nmea) {
			Date date = new Date();
			Log.d(TAG, date.toString()+" "+nmea);
			if(nmea.contains("RMC")) {
				try{
					XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_GPS_START_TIME , 1);
				}catch (Exception e){
					e.printStackTrace();
				}
				try {
					parseRMC(nmea);
				} catch (Exception e) {
					e.printStackTrace();
				}
				checkGpsSuccPos();
			}
		}
	};

	private static String[] split(String str) {
		String[] result = null;
		String delims = "[,]";
		result = str.split(delims);
		return result;
	}

	private static void checkGpsSuccPos(){
		if(mLocation == null) {
			mSuccCounter = 0;
			mlastLocation = null;
			mSucc = false;
			return;
		}
		if(mlastLocation == null) {
			mSuccCounter = 0;
			mSucc = false;
			mlastLocation = new Location(mLocation);
			return;
		}
		if((mLocation.distanceTo(mlastLocation) < 100.0) &&(mLocation.getAccuracy() < 10.0)  &&(mLocation.getAltitude()<5000.0)&&(mLocation.getAltitude()>-1000.0)) {
			Log.d(TAG, "checkGpsSuccPos: mSuccCounter= " + mSuccCounter);
			if(mSuccCounter>=1) {
				mSuccLocation = new Location(mLocation);
				timeStamp = System.currentTimeMillis();
				mSucc = true;
			}else {
				mSuccCounter++;
				mSucc = false;
			}
		}else{
			mSuccCounter = 0;
			mSucc = false;
		}
		mlastLocation = new Location(mLocation);
	}

	public static Location getLastSuccPosByInterval(long interval){
		if(mSuccLocation == null) {
			return null;
		}
		long diff = System.currentTimeMillis() - timeStamp;
		if((diff >= 0)&&(diff < interval*1000)){
			return mSuccLocation;
		}
		else{
			return null;
		}
	}

	public static void startGps(){
		try {
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_GPS_START);
		}catch (Exception e){
			e.printStackTrace();
		}
		mSuccCounter = 0;
		mGpsSatelliteArrayList = null;
		mLocation = null;
		mlastLocation = null;
//		mSuccLocation = null;
		mSucc = false;
		powerOn = true;
		Criteria criteria = new Criteria();
		//设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//设置是否要求速度
		criteria.setSpeedRequired(false);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(true);
		//设置是否需要方位信息
		criteria.setBearingRequired(true);
		//设置是否需要海拔信息
		criteria.setAltitudeRequired(true);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_HIGH);

		String bestProvider = XunLocation.getmLocationManager().getBestProvider(criteria, true);

		XunLocation.getmLocationManager().addGpsStatusListener(gpsStatusListener);
		XunLocation.getmLocationManager().addNmeaListener(nmeaListener);
		XunLocation.getmLocationManager().getLastKnownLocation(bestProvider);
		XunLocation.getmLocationManager().requestLocationUpdates(bestProvider, 0, 0, locationListener);



		wakeLock = XunLocation.getmPowerManager().newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CPUKeepRunning");
		wakeLock.acquire();
	}

	public static void stopGps() {
		if(mSucc){
			try{
				XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_GPS_LOCATION_SUCCESS);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		XunLocation.getmLocationManager().removeGpsStatusListener(gpsStatusListener);
		XunLocation.getmLocationManager().removeNmeaListener(nmeaListener);
		XunLocation.getmLocationManager().removeUpdates(locationListener);
		mSuccCounter = 0;
		mGpsSatelliteArrayList = null;
		mLocation = null;
		mlastLocation = null;
//		mSuccLocation = null;
		mSucc = false;
		powerOn = false;
		wakeLock.release();
	}

	public static boolean isPowerOn() {
		return powerOn;
	}

	public static ArrayList<GpsSatellite> getGpsSatelliteArrayList(){
		return mGpsSatelliteArrayList;
	}

	public static boolean isSucc(){
		return mSucc;
	}

	public static Location getLocation(){
		return mSuccLocation;
	}

	/**
	 * <$GPRMC> Recommended minimum specific GPS/Transit data.
	 *
	 *      eg. $GPRMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,ddmmyy,x.x,a*hh
	 *      1    = UTC of position fix
	 *      2    = Data status (V=navigation receiver warning)
	 *      3    = Latitude of fix
	 *      4    = N or S
	 *      5    = Longitude of fix
	 *      6    = E or W
	 *      7    = Speed over ground in knots
	 *      8    = Track made good in degrees True
	 *      9    = UT date
	 *      10   = Magnetic variation degrees (Easterly var. subtracts from true course)
	 *      11   = E or W
	 *      12   = Checksum
	 *
	 */
	private static void parseRMC(String record) {
		Log.d(TAG, "parseRMC: "+ record);

		String[] values = split(record);

		// First value = $GPRMC
		// Date time of fix (eg. 041107.000)
		// String dateTimeOfFix = values[1];

		// Warning (eg. A:valid, V:warning)
		final String warning = values[2];


		if (warning.equals("A")) {
			Log.d(TAG, "parseRMC: A");
		}else {
			Log.d(TAG, "parseRMC: V");
			mSuccCounter = 0;
			mLocation = null;
		}

	}


}
