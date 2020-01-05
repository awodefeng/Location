package com.xxun.watch.location;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
//import android.util.Log;

import net.minidev.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by xiaoxun on 2017/10/12.
 */

public class XunLocPolicyLastRecord {
	private static final String TAG = "[XunLoc]XunLocPolicyLastRecord";
	private static XunLocPolicyLastRecord instance = null;


	public static final int WIFI_STATUS_NO_CHANGED = 0;
	public static final int WIFI_STATUS_CHANGED = 1;
	public static final int WIFI_STATUS_NOT_AVALIABLE = 2;


	public static final int CELL_STATUS_NO_CHANGED = 0;
	public static final int CELL_STATUS_HAVE_CHANGED = 1;
	public static final int CELL_STATUS_ALL_CHANGED = 2;
	public static final int CELL_STATUS_NOT_AVALIABLE = 4;



	private long timeStamp;
	private boolean avaliable;
	private XunRecordWifi lastWifi;
	private XunRecordWifi lastRefWifi;
	private XunLocCellSensor lastCell;
	private XunRecordGps lastGps;
	private long motionCounter;//last pos record motion counter
	private long lastSteps;


	private long periodMotionCounter;//period location motion check counter
	private long flightMotionCounter = 5;//flight mode motion check counter

	public XunLocPolicyLastRecord(){
		Log.d(TAG, "XunLocPolicyLastRecord: ");
		clean();

//		XunLocation.getmTelephonyManager().listen(new PhoneStateListener(){
//			@Override
//			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//				super.onSignalStrengthsChanged(signalStrength);
//				lastCell.onCellChange();
//			}
//		}, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	public static XunLocPolicyLastRecord getInstance(){
		if(instance == null){
			instance = new XunLocPolicyLastRecord();
		}
		return instance;
	}

	public void clean(){
		avaliable = false;
		timeStamp = 0;
		lastSteps = 0;
		motionCounter = 255;
		lastWifi = new XunRecordWifi();
		lastRefWifi = new XunRecordWifi();
		lastGps = new XunRecordGps();
		lastCell = new XunLocCellSensor();
	}

	public void setNewRecord(XunLocRecord record){
		avaliable = true;
		lastWifi = new XunRecordWifi();
		lastRefWifi = new XunRecordWifi();
		lastGps = new XunRecordGps();
		lastCell = new XunLocCellSensor();
		clearMotion();
		clearPeriodMotionCounter();
		clearFlightMotionCounter();


		timeStamp = record.getTimestamp();

		lastSteps = XunSensorProc.getInstance().stepcounter;
		if(record.getWifiRecord().isAvaliable() == true){
			lastWifi.setWifiInfo(record.getWifiRecord());
			Log.d(TAG, "setNewRecord:lastWifi "+lastWifi.formatListToString());
			lastRefWifi.setWifiInfo(record.getWifiRecord());
			Log.d(TAG, "setNewRecord:lastRefWifi "+lastRefWifi.formatListToString());
		}

		if(record.getCellRecord().isAvaliable() == true){
			lastCell.setCellInfo(record.getCellRecord());
			Log.d(TAG, "setNewRecord:lastCell "+lastCell.formatNetWorkString()+" "+lastCell.formatBtsString());
		}

		if(record.getGpsRecord().isAvaliable() == true){
			lastGps.setGpsPos(record.getGpsRecord().getGpsPos());
			JSONObject pl = new JSONObject();
			lastGps.addInfoToJson(pl);
			Log.d(TAG, "setNewRecord:lastGps "+ pl.toJSONString());
		}
	}

	public void updateLastRecord(XunLocRecord record){
		avaliable = true;
		lastWifi = new XunRecordWifi();
		lastGps = new XunRecordGps();
		lastCell = new XunLocCellSensor();
		clearMotion();
		clearPeriodMotionCounter();
		clearFlightMotionCounter();
		timeStamp = record.getTimestamp();
		Log.d(TAG, "updateLastRecord: timeStamp="+ new Date(timeStamp).toString()+new Time(timeStamp).toString());
		lastSteps = XunSensorProc.getInstance().stepcounter;
		if(record.getWifiRecord().isAvaliable() == true){
			lastWifi.setWifiInfo(record.getWifiRecord());
			Log.d(TAG, "updateLastRecord:lastWifi "+lastWifi.formatListToString());
			Log.d(TAG, "updateLastRecord:lastRefWifi "+lastRefWifi.formatListToString());
		}

		if(record.getCellRecord().isAvaliable() == true){
			lastCell.setCellInfo(record.getCellRecord());
			Log.d(TAG, "updateLastRecord:lastCell "+lastCell.formatNetWorkString()+" "+lastCell.formatBtsString());
		}

		if(record.getGpsRecord().isAvaliable() == true){
			lastGps.setGpsPos(record.getGpsRecord().getGpsPos());
			JSONObject pl = new JSONObject();
			lastGps.addInfoToJson(pl);
			Log.d(TAG, "updateLastRecord:lastGps "+ pl.toJSONString());
		}
	}

	public void setMotion(){
		motionCounter++;
		Log.d(TAG, "setMotion: ="+motionCounter);
	}

	public void clearMotion(){
		motionCounter = 0;
		Log.d(TAG, "clearMotion: ");
	}

	public void setPeriodMotionCounter(){
		periodMotionCounter++;
		Log.d(TAG, "setPeriodMotionCounter: ="+periodMotionCounter);
	}

	public long getPeriodMotionCounter() {
		if(isAvaliable()) {
			return periodMotionCounter;
		}else{
			return Long.MAX_VALUE;
		}
	}

	public void clearPeriodMotionCounter(){
		periodMotionCounter = 0;
		Log.d(TAG, "clearPeriodMotionCounter: ");
	}


	public void setFlightMotionCounter(){
		flightMotionCounter++;
		Log.d(TAG, "setFlightMotionCounter: ="+flightMotionCounter);
	}

	public void clearFlightMotionCounter(){
		flightMotionCounter = 0;
		Log.d(TAG, "clearFlightMotionCounter: ");
	}

	public long getFlightMotionCounter() {
//		if(isAvaliable()) {
			return flightMotionCounter;
//		}else{
//			return Long.MAX_VALUE;
//		}
	}

	public long getStepCountDiff(){
		long cur_steps = XunSensorProc.getInstance().stepcounter;
		if(!isAvaliable()) {
			return 0;
		}


		if(cur_steps > lastSteps){
			return cur_steps - lastSteps;
		}
		else{
			return 0;
		}
	}

	public boolean isAvaliable(){
		return avaliable;
	}

	public long getTimeStamp(){
		return timeStamp;
	}

	public XunRecordWifi getLastWifi(){
		return lastWifi;
	}

	public XunRecordWifi getLastRefWifi(){
		return lastRefWifi;
	}

	public XunLocCellSensor getLastCell(){
		return lastCell;
	}

	public XunRecordGps getLastGps(){
		return lastGps;
	}

	public long getMotionCounter() {
		if(isAvaliable()) {
			return motionCounter;
		}else{
			return Long.MAX_VALUE;
		}
	}


	public XunLocRecord getLastUploadRecord(){
		XunLocRecord xunLocRecord = new XunLocRecord();
		if(lastCell.isAvaliable()) {
			xunLocRecord.setCellRecord(lastCell);
		}
		if(lastWifi.isAvaliable()) {
			xunLocRecord.setWifiRecord(lastWifi);
		}
		if(lastGps.isAvaliable()) {
			xunLocRecord.setGpsRecord(lastGps.getGpsPos());
		}
		xunLocRecord.compeleteInfoData();

		return xunLocRecord;
	}

	public boolean loc_srv_ca_wifi_check_change(XunRecordWifi curr_wifi) {
		return loc_srv_ca_wifi_check_change(lastRefWifi, curr_wifi);
	}

	public static boolean loc_srv_ca_wifi_check_change(XunRecordWifi last_data, XunRecordWifi curr_data) {
		int total_wifi_count = 0;
		int same_wifi_count = 0;
		int i,j;
		//
		Log.d(TAG ,"[Loc_policy] loc_srv_ca_wifi_check_change" );
		if((last_data == null)||(curr_data == null)){
			return true;
		}

		if((last_data.isAvaliable() == false)||(curr_data.isAvaliable() == false)) {
			return true;
		}

		if(last_data.getWifiInfo().size() == 0){
			return true;
		}
		//get max wifi count
		total_wifi_count = last_data.getWifiInfo().size();
		//get same wifi count
		for(XunLocWifiInfo lastResult : last_data.getWifiInfo()) {
			for(XunLocWifiInfo currResult : curr_data.getWifiInfo()) {
				if(0 == lastResult.BSSID.compareTo(currResult.BSSID)){
					same_wifi_count++;
				}
			}
		}
		Log.d(TAG , String.format("[Loc_policy]same_wifi_count=%d, total_wifi_count=%d",same_wifi_count, total_wifi_count));
		if((same_wifi_count*100)/total_wifi_count > 20) {
			return false;
		} else {
			return true;
		}
	}

	public int getSameWifiCount(XunRecordWifi curWifi) {

		if ((curWifi == null)||(lastRefWifi == null)){
			Log.d(TAG, "getSameWifiCount: null");
			return 0;
		}

		if((curWifi.isAvaliable() == false)||(lastRefWifi.isAvaliable() == false)){
			Log.d(TAG, "getSameWifiCount: not avaliable");
			return 0;
		}

		if((curWifi.getWifiInfo().size() == 0)||(lastRefWifi.getWifiInfo().size() == 0)){
			Log.d(TAG, "getSameWifiCount: size = 0");
			return 0;
		}
		int same_wifi_count = 0;
		try{
			for(XunLocWifiInfo lastResult : lastRefWifi.getWifiInfo()) {
				for(XunLocWifiInfo currResult : curWifi.getWifiInfo()) {
					if(0 == lastResult.BSSID.compareTo(currResult.BSSID)){
						same_wifi_count++;
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			same_wifi_count = 0;
		}

		return same_wifi_count;
	}

	public int getWifiChangeStatus(XunRecordWifi curWifi){
		return getWifiChangeStatus(lastRefWifi, curWifi);
	}

	public static int getWifiChangeStatus(XunRecordWifi lastWifi, XunRecordWifi curWifi){
		if(curWifi.isAvaliable()){
			Log.d(TAG, "getWifiChangeStatus: cur isAvaliable");
			if(lastWifi.isAvaliable()){
				Log.d(TAG, "getWifiChangeStatus: last isAvaliable");
				if(loc_srv_ca_wifi_check_change(lastWifi, curWifi)){
					Log.d(TAG, "getWifiChangeStatus: WIFI_STATUS_CHANGED");
					return WIFI_STATUS_CHANGED;
				}else{
					Log.d(TAG, "getWifiChangeStatus: WIFI_STATUS_NO_CHANGED");
					return WIFI_STATUS_NO_CHANGED;
				}
			}else{
				Log.d(TAG, "getWifiChangeStatus: last null WIFI_STATUS_CHANGED");
				return WIFI_STATUS_CHANGED;
			}
		}else{
			Log.d(TAG, "getWifiChangeStatus: cur null");
			if(lastWifi.isAvaliable()){
				Log.d(TAG, "getWifiChangeStatus: last not null WIFI_STATUS_CHANGED");
				return WIFI_STATUS_CHANGED;
			}else{
				Log.d(TAG, "getWifiChangeStatus: last null WIFI_STATUS_NOT_AVALIABLE");
				return WIFI_STATUS_NOT_AVALIABLE;
			}
		}
	}


	public int getCellChangeStatus(XunRecordCell curCell) {
		if((lastCell == null)||(curCell == null)){
			Log.d(TAG, "getCellChangeStatus: null CELL_STATUS_NOT_AVALIABLE");
			return CELL_STATUS_NOT_AVALIABLE;
		}

		if(lastCell.isAvaliable() != curCell.isAvaliable()){
			Log.d(TAG, "getCellChangeStatus: isAvaliable CELL_STATUS_HAVE_CHANGED");
			return CELL_STATUS_HAVE_CHANGED;
		}


		int sameCount = 0;
		int totalCount = 0;
		Log.d(TAG, "getCellChangeStatus: curType:"+ String.valueOf(curCell.getCellType()));
		if(lastCell.gsmCellList != null)
			Log.d(TAG, "getCellChangeStatus: lastgsm"+ String.valueOf(lastCell.gsmCellList.toString()));
		if(lastCell.cdmaCellList != null)
			Log.d(TAG, "getCellChangeStatus: lastcdma"+ String.valueOf(lastCell.cdmaCellList.toString()));
		if(lastCell.wcdmaCellList != null)
			Log.d(TAG, "getCellChangeStatus: lastwcdma"+ String.valueOf(lastCell.wcdmaCellList.toString()));
		if(lastCell.lteCellList != null)
			Log.d(TAG, "getCellChangeStatus: lastlte"+ String.valueOf(lastCell.lteCellList.toString()));
		switch (curCell.getCellType()) {
			case XunRecordCell.TYPE_GSM:
				if(lastCell.getGsmCellInfo() == null)
					break;
				totalCount = lastCell.getGsmCellInfo().size();
				for(XunRecordCellGsm lastCellInfo :lastCell.getGsmCellInfo()){
					for(XunRecordCellGsm curCellInfo :curCell.getGsmCellInfo()){
						if(lastCellInfo.cid == curCellInfo.cid){
							sameCount++;
						}
					}
				}
				break;
			case XunRecordCell.TYPE_CDMA:
				if(lastCell.getCdmaCellInfo() == null)
					break;
				totalCount = lastCell.getCdmaCellInfo().size();
				for(XunRecordCellCdma lastCellInfo :lastCell.getCdmaCellInfo()){
					for(XunRecordCellCdma curCellInfo :curCell.getCdmaCellInfo()){
						if(lastCellInfo.basestationId == curCellInfo.basestationId){
							sameCount++;
						}
					}
				}
				break;
			case XunRecordCell.TYPE_WCDMA:
				if(lastCell.getWcdmaCellInfo() == null){
					break;
				}
				totalCount = lastCell.getWcdmaCellInfo().size();
				for(XunRecordCellWcdma lastCellInfo :lastCell.getWcdmaCellInfo()){
					for(XunRecordCellWcdma curCellInfo :curCell.getWcdmaCellInfo()){
						if(lastCellInfo.cid == curCellInfo.cid){
							sameCount++;
						}
					}
				}
				break;
			case XunRecordCell.TYPE_LTE:
				if(lastCell.getLteCellInfo() == null){
					break;
				}
				totalCount = lastCell.getLteCellInfo().size();
				for(XunRecordCellLte lastCellInfo :lastCell.getLteCellInfo()){
					for(XunRecordCellLte curCellInfo :curCell.getLteCellInfo()){
						if(lastCellInfo.pci == curCellInfo.pci){
							sameCount++;
						}
					}
				}
				break;
			default:
				break;
		}

		Log.d(TAG, "getCellChangeStatus: total="+String.valueOf(totalCount)+"same="+String.valueOf(sameCount));
		if(totalCount == 0){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_HAVE_CHANGED");
		} else if(sameCount == 0){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_ALL_CHANGED");
			return CELL_STATUS_ALL_CHANGED;
		}else if(totalCount == sameCount){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_NO_CHANGED");
			return CELL_STATUS_NO_CHANGED;
		}

		Log.d(TAG, "getCellChangeStatus: CELL_STATUS_HAVE_CHANGED");
		return CELL_STATUS_HAVE_CHANGED;
	}

/*
	public static int getCellChangeStatus(XunRecordCell lastCell, XunRecordCell curCell) {
		if((lastCell == null)||(curCell == null)){
			Log.d(TAG, "getCellChangeStatus: null CELL_STATUS_NOT_AVALIABLE");
			return CELL_STATUS_NOT_AVALIABLE;
		}

		if(lastCell.isAvaliable() != curCell.isAvaliable()){
			return CELL_STATUS_HAVE_CHANGED;
		}


		if((lastCell.isAvaliable() == false)||(curCell.isAvaliable() == false)){
			Log.d(TAG, "getCellChangeStatus: false CELL_STATUS_NOT_AVALIABLE");
			return CELL_STATUS_NOT_AVALIABLE;
		}


		if(lastCell.getNetWork() != curCell.getNetWork()){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_NET_CHANGED");
			return CELL_STATUS_NET_CHANGED;
		}

		int sameCount = 0;
		int totalCount = lastCell.getCellInfos().size();
		for(XunLocCellInfo lastCellInfo :lastCell.getCellInfos()){
			for(XunLocCellInfo curCellInfo :curCell.getCellInfos()){
				if(lastCellInfo.mnc != curCellInfo.mnc){
					Log.d(TAG, "getCellChangeStatus: mnc Switch CELL_STATUS_HAVE_CHANGED");
					return CELL_STATUS_HAVE_CHANGED;
				}
				if(lastCellInfo.cid == curCellInfo.cid){
					sameCount++;
				}
			}
		}

		Log.d(TAG, "getCellChangeStatus: total="+String.valueOf(totalCount)+"same="+String.valueOf(sameCount));
		if(sameCount == 0){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_ALL_CHANGED");
			return CELL_STATUS_ALL_CHANGED;
		}else if(totalCount == sameCount){
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_NO_CHANGED");
			return CELL_STATUS_NO_CHANGED;
		}else{
			Log.d(TAG, "getCellChangeStatus: CELL_STATUS_HAVE_CHANGED");
			return CELL_STATUS_HAVE_CHANGED;
		}
	}
*/

	public boolean isPosChanged(XunLocRecord curRecord){

		if(avaliable == false){
			return true;
		}

		int wifiStatus = getWifiChangeStatus(lastWifi, curRecord.getWifiRecord());
		int cellStatus = getCellChangeStatus(curRecord.getCellRecord());
		long stepDiff = getStepCountDiff();

		if(wifiStatus == WIFI_STATUS_CHANGED) {
			Log.d(TAG, "isPosChanged: WIFI_STATUS_CHANGED true");
			return true;
		}

		if(cellStatus == CELL_STATUS_ALL_CHANGED){
			Log.d(TAG, "isPosChanged: CELL_STATUS_ALL_CHANGED false");
			return true;
		}

		if(cellStatus == CELL_STATUS_HAVE_CHANGED) {
			Log.d(TAG, "isPosChanged: CELL_STATUS_HAVE_CHANGED");
			if((stepDiff >= 100)&&(wifiStatus == WIFI_STATUS_NOT_AVALIABLE)){
				return true;
			}
			if(stepDiff >= 300){
				return true;
			}
		}

		return false;
	}


	public int mergeWifiList(XunRecordWifi curWifi){
		int count = 0;

		if((curWifi == null)||(lastWifi == null)){
			Log.d(TAG, "mergeWifiList: null");
			return 0;
		}

		if(curWifi.isAvaliable() == false || lastWifi.isAvaliable() == false){
			Log.d(TAG, "mergeWifiList: empty");
			return 0;
		}

		if(lastWifi.getWifiInfo().size() > 15){
			Log.d(TAG, "mergeWifiList: size >15");
			return 0;
		}
		try {
			int countCur = curWifi.getWifiInfo().size();
			int countLast = lastWifi.getWifiInfo().size();
			XunLocWifiInfo cur = null;
			XunLocWifiInfo last = null;
			boolean same = false;
			for(int j = 0; j < countLast; j++){
				last = lastWifi.getWifiInfo().get(j);
				same = false;
				for(int i = 0; i < countCur; i++) {
					cur = curWifi.getWifiInfo().get(i);
					if (last.BSSID.equalsIgnoreCase(cur.BSSID)) {
						same = true;
						Log.d(TAG, "mergeWifiList: same wifi"+last.BSSID);
						break;
					}
				}
				if(same == false){
					curWifi.getWifiInfo().add(last);
					Log.d(TAG, "mergeWifiList: add wifi"+last.BSSID);
					count++;
				}
			}
			lastWifi.setWifiInfo(curWifi);
		}catch (Exception e){
			e.printStackTrace();
			count = 0;
		}


		Log.d(TAG, "mergeWifiList: count "+ String.valueOf(count));
		return count;
	}
}
