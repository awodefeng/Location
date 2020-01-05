package com.xxun.watch.location;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.provider.Settings;
import android.telephony.CellInfo;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
//import android.util.Log;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
//import com.xiaoxun.sdk.bean.ResponseData;
//import com.xiaoxun.sdk.interfaces.IResponseDataCallBack;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;


import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.sql.Array;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.SimpleFormatter;

import  android.util.XiaoXunUtil;


/**
 * Created by xiaoxun on 2017/9/28.
 */


public class XunLocRecord {
	private static final String TAG = "[XunLoc]XunLocRecord";
	private XunRecordWifi wifi = null;
	private XunRecordCell cell = null;
	private XunRecordGps gps = null;
	private XunLocBaiduInfo baiduInfo = null;
	private long timestamp = 0;
	private long steps = 0;
	private int sos = 0;
	private int loc_type = 0;
	private int locflag = 0;
	private int drop = 0;
	private int region = 0;
	private int cdma = 0;
	private int feedback = 0;
	private String efenceFeature = null;

	public XunLocRecord() {
		wifi = new XunRecordWifi();
		cell = new XunRecordCell();
		gps = new XunRecordGps();
		baiduInfo = new XunLocBaiduInfo();
	}

	public void setWifiRecord(List<ScanResult> scanResultList){
		wifi = new XunRecordWifi();
		wifi.setWifiInfo(scanResultList);
	}

	public void setWifiRecord(XunRecordWifi recordWifi){
		wifi = new XunRecordWifi();
		wifi.setWifiInfo(recordWifi);
	}

	public XunRecordWifi getWifiRecord(){
		return wifi;
	}

	public void setCellRecord(){
		cell = new XunRecordCell();
		cell.setCellInfo();
	}

	public void setCellRecord(XunRecordCell recordCell){
		cell = new XunRecordCell();
		cell.setCellInfo(recordCell);
	}

	public void setCellRecord(List<CellInfo> cellInfoList, int netWorkType){
		cell = new XunRecordCell();
		cell.setCellInfo(cellInfoList, netWorkType);
	}

	public void setCellRecordAgain(){
		if(cell.getCellType() == XunRecordCell.TYPE_LTE){
			if(cell.getLteCellInfo().get(0).signalStrength == 0){
				Log.d(TAG, "setCellRecordAgain : need cell signalStrength = 0");
				setCellRecord();
			}
		}
	}

	public XunRecordCell getCellRecord(){
		return cell;
	}

	public void setGpsRecord(Location location){
		gps = new XunRecordGps();
		gps.setGpsPos(location);
	}

	public XunRecordGps getGpsRecord(){
		return gps;
	}

	public void setBaiduInfo(){
		baiduInfo.CreateBaiduInfo(XunLocation.getmContext());
	}

	public String getBaiduID(){
		return baiduInfo.getBdId();
	}

	public String getBaiduLocInfo(){
		return baiduInfo.getLocString();
	}

	public XunLocBaiduInfo getBaiduInfo(){
		return baiduInfo;
	}
	
	public void setFeedback(int need){
		feedback = need;
	}	


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean convertToJson(JSONObject pl) {

		if((!gps.isAvaliable())&&(!cell.isAvaliable())&&(!wifi.isAvaliable())) {
			return false;
		}

		if(timestamp == 0) {
			timestamp = System.currentTimeMillis();
		}

		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		pl.put("timestamp", formater.format(new Date(timestamp)));

		//pl.put("GID", XiaoXunNetworkManager.getInstance().getWatchGid());
		pl.put("GID", XunLocation.getmNetworkManager().getWatchGid());


		pl.put("imei", XunLocation.getmTelephonyManager().getDeviceId());

		if(XunLocation.getmTelephonyManager().getSubscriberId() != null) {
			pl.put("imsi", XunLocation.getmTelephonyManager().getSubscriberId());
		}

		//add sos info
		if(sos != 0)
			pl.put("SOS", sos);

		//add loctype info
		if(loc_type != 0)
			pl.put("loctype", loc_type);

		//add steps
		pl.put("Steps", steps);

		//add drop info
		if(drop != 0)
			pl.put("drop", drop);

		//add region info
		if(region == 0) {
			if(cell.isAvaliable()) {
				cell.getRegion();
			}
		}
		if(region != 0) {
			pl.put("region", region);
		}

		if(feedback != 0){
			pl.put("feedback", 1);
		}

		if(gps.isAvaliable()){
			//add assesstype
			pl.put("accesstype", 2);

			gps.addInfoToJson(pl);

		}
		else if(wifi.isAvaliable()||cell.isAvaliable()) {
			//add assesstype
			pl.put("accesstype",0);

			//add serverip
			pl.put("serverip"," ");
//			if (XiaoXunUtil.XIAOXUN_CONFIG_PRODUCT_SEVEN_THREE_ZERO){

//			}else{
				if(baiduInfo.isAvaliable()) {
					baiduInfo.addInfoToJson(pl);
				}
//			}
		}

		if(cell.isAvaliable()){
			cell.addInfoToJson(pl);
		}

		if(wifi.isAvaliable()){
			wifi.addInfoToJson(pl);
		}

		if(efenceFeature != null){
			if(efenceFeature.length() > 0) {
				pl.put("feature", efenceFeature);
			}
		}



		return true;
	}

	public void saveToDb() {
		if(XunLocation.getmContext() != null) {
			XunLocRecordDAO recordDAO = XunLocRecordDAO.getInstance(XunLocation.getmContext());
			JSONObject fmt = new JSONObject();
			if(convertToJson(fmt)) {
				recordDAO.writeLocation(timestamp, fmt);
			}
		}
	}


	public void compeleteInfoData(){
		if(timestamp == 0){
			timestamp = System.currentTimeMillis();
		}
		steps = XunLocPolicyLastRecord.getInstance().getStepCountDiff();
		if(XunLocTrackingLocation.getInstance().isInTrackingTime()){
			loc_type = 1;
		}
		if(baiduInfo.isAvaliable() == false) {
			baiduInfo.CreateBaiduInfo(XunLocation.getmContext());
		}
		//private ArrayList<>efence;
		//private int drop;

		//get mcc
		String networkOperator = XunLocation.getmTelephonyManager().getNetworkOperator();
		if (!TextUtils.isEmpty(networkOperator)) {
			region = Integer.parseInt(networkOperator.substring(0, 3));
		}

		if(efenceFeature == null){
			efenceFeature = XunEfenceStatus.getInstance().getFeatureString(this);
			if(efenceFeature == null){
				efenceFeature = new String();
			}
		}
		setCellRecordAgain();
	}

	public void setSos(int status){
		sos = status;
	}

	public void setDrop(int status){
		drop = status;
	}

}
