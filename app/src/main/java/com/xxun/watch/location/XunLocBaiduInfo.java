package com.xxun.watch.location;

import android.content.Context;
//import android.util.Log;
import net.minidev.json.JSONObject;
import com.baidu.location.watch.BDLocManager;

/**
 * Created by xiaoxun on 2018/4/5.
 */

public class XunLocBaiduInfo {
	private static final String TAG= "[XunLoc]XunLocBaiduInfo";
	boolean avaliable = false;
	private String bdId;
	private String locString;

	public XunLocBaiduInfo(){
		avaliable = false;
		bdId = null;
		locString = null;
	}


	public void CreateBaiduInfo(Context context){
		try{
			BDLocManager bdLocManager = null;
			bdLocManager = new BDLocManager(context);

			bdId = bdLocManager.getBaiduID();
			Log.d(TAG, "XunLocBaiduInfo: bdid="+bdId);

			locString = bdLocManager.getLocString();
			Log.d(TAG, "onScanResults: locString"+locString);

			avaliable = true;
		}catch (Exception e){
			avaliable = false;
			e.printStackTrace();
		}
	}

	public boolean isAvaliable(){
		return avaliable;
	}

	public void addInfoToJson(JSONObject pl){
		pl.put("bdid",bdId);
		pl.put("locString", locString);
	}

	public String getBdId(){
		return bdId;
	}

	public String getLocString(){
		return locString;
	}
}
