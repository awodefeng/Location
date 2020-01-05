package com.xxun.watch.location;


import android.location.Location;
import android.os.SystemClock;
import android.provider.Settings;
//import android.util.Log;


import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IMessageReceiveListener;
import com.xiaoxun.sdk.IResponseDataCallBack;
import com.xiaoxun.sdk.XiaoXunNetworkManager;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xiaoxun on 2018/1/15.
 */



public class XunEfenceStatus {
	private static final int MAX_EFNECE_COUNT = 10;
	private static final String TAG = "[XunLoc]XunEfenceStatus";
	private static XunEfenceStatus instance = null;
	private int[] efenceStatus;
	private int[] dropStatus;
	private long dropStartTik;
	private boolean isEfenceChange;

	public XunEfenceStatus(){
		efenceStatus = new int[MAX_EFNECE_COUNT];
		dropStatus = new int[MAX_EFNECE_COUNT];
		for (int i=0; i< MAX_EFNECE_COUNT; i++){
			efenceStatus[i] = 0;
			dropStatus[i] = 0;
		}
		dropStartTik = 0;

	}

	public static XunEfenceStatus getInstance(){
		if(instance == null){
			instance = new XunEfenceStatus();
		}
		return instance;
	}

/*
{
	"SN": 95651856,
	"SEID": null,
	"PL": {
		"EID": "F92B6B0413B807B585DBBB51A8B2E00F",
		"Type": 1,
		"sub_action": 163,
		"GID": "0ABB6EDE29AFDFA0C585E7BDDF621238",
		"EFence": {
			"bdLng": 113.60601243356,
			"Radius": 50,
			"Desc": "广东省韶关市浈江区风采街道园前东路17号",
			"Type": 1,
			"Lng": 113.599488,
			"EFID": "EFID1",
			"Lat": 24.795931,
			"bdLat": 24.801749591449,
			"Name": "家"
		},
		"EFID": "EFID1",
		"Timestamp": "20180116215641000",
		"Key": "79819883784348143",
		"Location": {
			"country": "中国",
			"city": "韶关市",
			"adcode": "440204",
			"mapType": "0",
			"poi": "鸿兴大厦",
			"type": "3",
			"province": "广东省",
			"citycode": "0751",
			"road": "园前东路",
			"street": "园前东路",
			"location": "113.5991686,24.7959456",
			"radius": "25",
			"region": 460,
			"desc": "广东省 韶关市 浈江区 园前东路 靠近鸿兴大厦",
			"timestamp": "20180116215641000"
		}
	},
	"CID": 30012
}
*/

	public void ParseSafeAreaStateInd(String data){
		JSONObject root = null;
		JSONObject pl = null;
		if(data == null) {
			Log.d(TAG, "ParseSafeAreaStateInd: null");
			return;
		}
		try{
			root = (JSONObject) JSONValue.parse(data);
			pl = (JSONObject)root.get("PL");
			if(pl == null) {
				return;
			}

			Log.d(TAG, "ParseSafeAreaStateInd: pl="+pl.toJSONString());

			JSONObject EFence = (JSONObject)pl.get("EFence");
			if(EFence == null){
				return;
			}
			Log.d(TAG, "ParseSafeAreaStateInd: efence="+ EFence.toJSONString());

			Object typeObj = EFence.get("Type");
			int type = 255;
			if(typeObj == null){
				return;
			}

			if(typeObj instanceof Integer){
				type = ((Integer)typeObj).intValue();
			}else if(typeObj instanceof Long){
				type = ((Long)typeObj).intValue();
			}else if(typeObj instanceof Float){
				type = ((Float)typeObj).intValue();
			}else if(typeObj instanceof Double){
				type = ((Double)typeObj).intValue();
			}
			Log.d(TAG, "ParseSafeAreaStateInd: type="+type);

			if(type != 1){
				return;
			}

			String EFID = (String)EFence.get("EFID");
			if(EFID == null){
				return;
			}

			Log.d(TAG, "ParseSafeAreaStateInd: EFID="+EFID);

			String[] strs = EFID.split("EFID");
			for(int i = 0; i< strs.length; i++){
				if(strs[i].length() > 0) {
					Log.d(TAG, "ParseSafeAreaStateInd: strs ="+strs[i]);
					int index = Integer.valueOf(strs[i]);
					if((index > 0)&&(index<=MAX_EFNECE_COUNT)) {
						Log.d(TAG, "ParseSafeAreaStateInd: Entry"+index+" Efence");
						//efenceStatus[index-1] = 1;
						setEntryEfence(index - 1);
					}
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
/*
	{
		"SEID": "CFD7D0401BE9A83D54577DEDBADC2D39",
			"SN": 1516176656,
			"PL": {
		"sub_action": 600,
				"EFID1": {
			"wifi": "10:1B:54:21:BC:C1,998|62:1B:54:21:2D:B9,998|92:1B:54:21:2D:B9,998|62:1B:54:21:BC:C1,998|92:1B:54:21:BC:C1,998|92:1B:54:21:9A:07,998|10:1B:54:21:2D:B9,998|62:1B:54:21:7E:B3,993|10:1B:54:21:9A:07,993|62:1B:54:21:30:A5,993|10:1B:54:21:98:83,993|62:1B:54:21:9A:07,993|62:1B:54:21:9A:05,993|92:1B:54:21:97:67,993|62:1B:54:21:98:83,993|62:1B:54:21:C5:63,986|62:1B:54:21:BC:A1,986|F0:B4:29:D3:39:4B,986|10:1B:54:21:97:67,986|14:75:90:50:4D:AF,986|E4:D3:32:73:B4:80,986|62:1B:54:21:7B:1F,986|62:1B:54:21:97:A7,986|62:1B:54:1F:71:E1,986|74:C3:30:9B:81:3A,986|10:1B:54:21:9B:81,986|62:1B:54:21:97:67,986|92:1B:54:21:98:83,986|10:1B:54:1F:71:E1,986|10:1B:54:21:97:A7,986|10:1B:54:21:9A:05,12|62:1B:54:21:7E:A7,12|10:1B:54:21:98:0B,12|92:1B:54:21:9A:05,12|10:1B:54:21:7E:A7,12|10:1B:54:21:30:A5,12|62:1B:54:21:C5:61,7|D8:15:0D:70:69:6E,7|62:1B:54:21:97:4B,7|CC:81:DA:C9:F8:18,7",
					"wgs84": "121.23282986,37.56480713",
					"radius": 200,
					"timestamp": "20180117161056937"
		}
	},
		"CID": 30012
	}
*/
	public void ParseEfidDataInd(String data){

		JSONObject root = null;
		JSONObject pl = null;
		if(data == null) {
			return;
		}

		try{
			root = (JSONObject) JSONValue.parse(data);
			pl = (JSONObject)root.get("PL");
			if(pl == null) {
				return;
			}

			Log.d(TAG, "ParseEfidDataInd: pl="+pl.toJSONString());


			XunEfenceDAO.getInstance(XunLocation.getmContext()).deleteAll();
			for (int i = 0; i < 10; i++) {
				JSONObject efenceObj = null;
				efenceObj = (JSONObject)pl.get("EFID"+(i+1));
				if(efenceObj == null) {
					continue;
				}
				Log.d(TAG, "ParseEfidDataInd: EFID"+(i+1)+"="+efenceObj.toJSONString());

				XunEfenceID efenceID = new XunEfenceID();
				efenceID.parseEfenceData(i, efenceObj);
				if(efenceID.isAvaliable()) {
					XunEfenceDAO.getInstance(XunLocation.getmContext()).writeEfence(efenceID);
				}
			}
			dumpEfidData();


		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void dumpEfidData(){
		ArrayList<XunEfenceID> Efids = new ArrayList<XunEfenceID>();
		XunEfenceDAO.getInstance(XunLocation.getmContext()).readEfence(Efids);
		if (Efids.size()>0) {
			for (XunEfenceID efid : Efids) {
				StringBuilder sb = new StringBuilder();
				sb.append(efid.index+1);
				sb.append(" ");
				sb.append(efid.lat);
				sb.append(" ");
				sb.append(efid.lng);
				sb.append(" ");
				sb.append(efid.radius);
				sb.append(" ");
				sb.append(efid.timestamp);
				sb.append(" ");
				sb.append(efid.formatWifiToString());
				Log.d(TAG, "dumpEfidData: "+sb.toString());
			}
		}
	}
/*
	{
		"SID": "027761985A8F4DBCA8E75B2B18D1D96E",
			"Version": "00030000",
			"SN": 1516293319,
			"PL": {
				"EFID1": "20180117172241218",
				"EFID2": "20180118080839459"
			},
		"CID": 51041
	}
	*/
	public void SyncEfenceDataFromServer(){
		if(XunLocation.isBinded() == false) {
			Log.d(TAG, "SyncEfenceDataFromServer: bind false");
			return;
		}
		dumpEfidData();
		ArrayList<XunEfenceID> efenceIDs = new ArrayList<XunEfenceID>();
		JSONObject root = new JSONObject();
		JSONObject pl = new JSONObject();
		ArrayList<String> efids = new ArrayList<String>();
		ArrayList<String> timestamps = new ArrayList<String>();
		int count = 0;


		root.put("SN", new Integer(XunLocation.getmNetworkManager().getMsgSN()));
		root.put("Version", new String("00030000"));
		root.put("PL", pl);
		root.put("CID", new Integer(51041));

		XunEfenceDAO.getInstance(XunLocation.getmContext()).readEfence(efenceIDs);
		try {
			for (XunEfenceID efenceID : efenceIDs) {
				int index = efenceID.index;
				String timestamp = efenceID.timestamp;
				if ((timestamp != null) && (timestamp.length() > 0)) {
					if (index >= 0 && index < MAX_EFNECE_COUNT) {
						pl.put("EFID" + (index + 1), timestamp);
						efids.add("EFID" + (index + 1));
						timestamps.add(timestamp);
						count++;
					}
				}
			}
			Log.d(TAG, "SyncEfenceDataFromServer: " + root.toJSONString());

			efids.toArray(new String[efids.size()]);

		}catch (Exception e){
			e.printStackTrace();
			efids = new ArrayList<String>();
			timestamps = new ArrayList<String>();
		}

		try{
			XunLocation.getmNetworkManager().getEFenceStatus(efids.toArray(new String[efids.size()]), timestamps.toArray(new String[timestamps.size()]), new CallBack() {
				@Override
				public void onSuccess(ResponseData responseData) {
					if(responseData.getResponseCode() == 100){
						try{
							Log.d("MyReceiver","responseData:"+responseData.getResponseData());
							getEFenceStatusRsp(responseData.getResponseData());

						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onError(int i, String s) {

				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public void getEFenceStatusRsp(String data){
		JSONObject pl = null;
		if(data == null) {
			return;
		}

		try{
			pl = (JSONObject) JSONValue.parse(data);

			if(pl == null) {
				return;
			}

			Log.d(TAG, "ParseEfidDataInd: pl="+pl.toJSONString());


			XunEfenceDAO.getInstance(XunLocation.getmContext()).deleteAll();
			for (int i = 0; i < 10; i++) {
				JSONObject efenceObj = null;
				efenceObj = (JSONObject)pl.get("EFID"+(i+1));
				if(efenceObj == null) {
					continue;
				}
				Log.d(TAG, "ParseEfidDataInd: EFID"+(i+1)+"="+efenceObj.toJSONString());

				XunEfenceID efenceID = new XunEfenceID();
				efenceID.parseEfenceData(i, efenceObj);
				if(efenceID.isAvaliable()) {
					XunEfenceDAO.getInstance(XunLocation.getmContext()).writeEfence(efenceID);
				}
			}
			dumpEfidData();

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void clearAllDropStatus(){
		Log.d(TAG, "clearAllDropStatus: ");
		for (int i = 0; i < MAX_EFNECE_COUNT; i++) {
			dropStatus[i] = 0;
		}
		dropStartTik = 0;
	}

	public void setEntryEfence(int index){
		dropStatus[index] = 1;
		dropStartTik = SystemClock.elapsedRealtime();
		Log.d(TAG, "setEntryEfence:  dropStatus ="+Arrays.toString(dropStatus));
		Log.d(TAG, "setEntryEfence:  dropStartTik ="+dropStartTik);
	}

	public boolean getDropStatus(XunLocRecord record){
		ArrayList<XunEfenceID> efids = new ArrayList<XunEfenceID>();

		XunEfenceDAO.getInstance(XunLocation.getmContext()).readEfence(efids);

		if(efids.size()==0){
			Log.d(TAG, "getFeatureString: efence empty");
			clearAllDropStatus();
			return false;
		}

		Log.d(TAG, "getDropStatus: old dropStatus ="+Arrays.toString(dropStatus));
		Log.d(TAG, "getDropStatus: old dropStartTik ="+dropStartTik);
		for (XunEfenceID efid: efids){
			Log.d(TAG, "getDropStatus: get efence index="+efid.index);
			if(dropStatus[efid.index] == 1) {
				boolean in = false;
				if (record.getWifiRecord().isAvaliable()) {
					if (2 <= getSameWifiCount(efid.wifiInfos, record.getWifiRecord().getWifiInfo())) {
						Log.d(TAG, "getDropStatus: now in efence "+efid.index);
						in = true;
					}
				}

				if (in == true) {
					long time_diff = SystemClock.elapsedRealtime() - dropStartTik;
					Log.d(TAG, "getDropStatus:  diff_tik="+time_diff);

					if (time_diff < 0 || time_diff > 3600 * 1000) {
						setEntryEfence(efid.index);
						return false;
					}else{
						return true;
					}
				}else{
					clearAllDropStatus();
					return false;
				}
			}
		}
		clearAllDropStatus();
		return false;
	}

	public String getFeatureString(XunLocRecord record){
		StringBuilder outString = new StringBuilder();
		ArrayList<XunEfenceID> efids = new ArrayList<XunEfenceID>();

		isEfenceChange = false;

		XunEfenceDAO.getInstance(XunLocation.getmContext()).readEfence(efids);

		if(efids.size()==0){
			Log.d(TAG, "getFeatureString: efence empty");
			return null;
		}



		for (XunEfenceID efid: efids){
			Boolean in = false;
			if(record.getGpsRecord().isAvaliable()){
				float[] results = new float[3];
				Location.distanceBetween(record.getGpsRecord().getGpsPos().getLatitude(),
																record.getGpsRecord().getGpsPos().getLongitude(),
																efid.lat, efid.lng, results);
				Log.d(TAG, "getFeatureString: distance = "+results);
				if(results[0] <= efid.radius){
					in = true;
				}
			}else if(record.getWifiRecord().isAvaliable()){
				if(2<=getSameWifiCount(efid.wifiInfos, record.getWifiRecord().getWifiInfo())){
					in = true;
				}
			}

			Log.d(TAG, "getFeatureString: new efenceStatus="+in);
			Log.d(TAG, "getFeatureString: old efenceStatus="+Arrays.toString(efenceStatus));
			if((in == true)||(efenceStatus[efid.index] == 1)) {
				if(outString.toString().length() > 0) {
					outString.append(",");
				}
				outString.append("EFID");
				outString.append(efid.index+1);
				outString.append("|");
				outString.append((in==true ?  1:0));
				outString.append("|");
				outString.append(efenceStatus[efid.index]);
				Log.d(TAG, "getFeatureString: index="+efid.index+" "+outString.toString());
			}

			int cur_status = (in== true? 1:0);

			if(isEfenceChange == false) {
				if (cur_status != efenceStatus[efid.index]) {
					Log.d(TAG, "getFeatureString: efence change");
					isEfenceChange = true;
				}
			}

			efenceStatus[efid.index] = cur_status;
			Log.d(TAG, "getFeatureString: new efenceStatus="+Arrays.toString(efenceStatus));

		}
		return outString.toString();
	}


	private int getSameWifiCount(ArrayList<XunLocWifiInfo> efeceWifis, ArrayList<XunLocWifiInfo> wifiLists){
		int sameCount = 0;
		//
		if((efeceWifis == null)||(efeceWifis.size() == 0)||(wifiLists == null)||(wifiLists.size() == 0)){
			Log.d(TAG, "getSameWifiCount: empty");
			return 0;
		}

		Log.d(TAG, "getSameWifiCount: efenceWifis"+efeceWifis.toString());
		Log.d(TAG, "getSameWifiCount: wifiLists"+wifiLists.toString());

		for (XunLocWifiInfo feature : efeceWifis) {
			for(XunLocWifiInfo wifi: wifiLists){
				if(feature.BSSID.equalsIgnoreCase(wifi.BSSID)){
					sameCount++;
				}
			}
		}
		Log.d(TAG, "getSameWifiCount: sameCount="+sameCount);
		return sameCount;
	}


	private int getSameWifiWeight(ArrayList<XunLocWifiInfo> efneceWifis, ArrayList<XunLocWifiInfo> wifiLists){
		int weight = 0;

		//
		if((efneceWifis == null)||(efneceWifis.size() == 0)||(wifiLists == null)||(wifiLists.size() == 0)){
			Log.d(TAG, "getSameWifiWeight: empty");
			return 0;
		}
		for (XunLocWifiInfo feature : efneceWifis) {
			for(XunLocWifiInfo wifi: wifiLists){
				if(feature.BSSID.equalsIgnoreCase(wifi.BSSID)){
					weight+= feature.level;
				}
			}
		}
		Log.d(TAG, "getSameWifiWeight: weight="+weight);
		return weight;
	}


	public boolean isEfenceChange(){
		Log.d(TAG, "isEfenceChange: "+isEfenceChange);
		return isEfenceChange;
	}
	
    public class CallBack extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {}
           @Override
           public void onError(int i, String s) {}    
    }		
}


