package com.xxun.watch.location;


import android.os.Handler;
//import android.util.Log;
import android.content.Intent;

import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IMessageReceiveListener;
import com.xiaoxun.sdk.IResponseDataCallBack;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xiaoxun.sdk.utils.CloudBridgeUtil;
import com.xiaoxun.statistics.XiaoXunStatisticsManager;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;


import java.util.ArrayList;


/**
 * Created by xiaoxun on 2017/10/10.
 */

/*
public int ERROR_CODE_COMMOM_UNKNOWN_MESSAGE 	= -11;
public int ERROR_CODE_COMMOM_GENERAL_EXCEPTION  = -12;
public int ERROR_CODE_COMMOM_MESSAGE_ILLEGAL    = -13;
public int ERROR_CODE_COMMOM_SESSION_ILLEGAL    = -14;
public int ERROR_CODE_COMMOM_SERVER_LIMIT    = -15;
public int ERROR_CODE_COMMOM_DATA_NOT_FOUND  = -120;
public int ERROR_CODE_COMMOM_DATA_LIMIT  = -121;
public int ERROR_CODE_COMMOM_ECT_ERROR  = -131;
public int ERROR_CODE_COMMOM_DATA_REPEAT  = -141;
public int ERROR_CODE_SECURITY_PASSWORD_INVALID               = -101;
public int ERROR_CODE_SECURITY_PASSWORD_EMPTY                 = -102;
public int ERROR_CODE_SECURITY_USER_NOT_EXIST                 = -103;
public int ERROR_CODE_SECURITY_USER_ALREADY_EXIST             = -104;
public int ERROR_CODE_SECURITY_USER_EMAIL_ALREADY_USED        = -105;
public int ERROR_CODE_SECURITY_USER_PHONE_ALREADY_USED        = -106;
public int ERROR_CODE_SECURITY_SEND_EMAIL_FAILED              = -107;
public int ERROR_CODE_SECURITY_SEND_SMS_FAILED                = -108;
public int ERROR_CODE_SECURITY_ACCOUNT_HAS_BEEN_ACTIVATED     = -109;
public int ERROR_CODE_SECURITY_ACTIVATIONCODE_ILLEGAL         = -110;
public int ERROR_CODE_SECURITY_ACCOUNT_HAS_NOT_BEEN_ACTIVATED = -111;
public int ERROR_CODE_SECURITY_CAPTCHA_ILLEGAL                = -112;
public int ERROR_CODE_SECURITY_ACCOUNT_EXPIRED                = -113;

// group related
public int ERROR_CODE_GROUP_NOT_EXIST                           = -150;
public int ERROR_CODE_ENDPOINT_NOT_EXIST                        = -151;
public int ERROR_CODE_GROUP_ALREADY_HAS_ENDPOINT                = -152;
public int ERROR_CODE_GROUP_HAS_NOT_ENDPOINT                    = -153;
public int ERROR_CODE_SUB_ACTION_NOT_EXIST                      = -154;
public int ERROR_CODE_GROUP_HAS_NOT_ADMIN                       = -155;
public int ERROR_CODE_GROUP_ADD_REFUSE                          = -156;

public int ERROR_CODE_ENDPOINT_OFFLINE                          = -160;
public int ERROR_CODE_VERIFYCODE_NOT_EXIST                      = -161;
public int CODE_ENDPOINT_BIND_ISRECEIVE                         = -171;
public int ERROR_USER_ISIN_GROUP                                = -181;
public int ERROR_DEVICETYPE_NOT_REPEAT                          = -191;
*/
public class XunLocPosUploadPolicy {
	private static final String TAG = "[XunLoc]XunLocPosUploadPolicy";
	private static final int UPLOAD_TIME_OUT = 10*60*1000; // 10 min for padding upload
	private static final int FLUSH_UPLOAD_TIME_OUT = 60*1000; // 1 min for flush wait time

	private static XunLocPosUploadPolicy instance = null;
	private long flushtik = 0L;
	private long uploadtik = 0L;
	private int sending_sn = -1;
	private ArrayList<Long> timeStamps = null;
	private boolean need_upload = false;
	private Handler handler = null;
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			onCheckAndUploadTiming();
			handler.postDelayed(runnable, 10000L);
		}
	};


	public XunLocPosUploadPolicy(){
		handler = new Handler();
		handler.postDelayed(runnable, 10000L);
	}


	public static XunLocPosUploadPolicy getInstance(){
		if(instance == null){
			instance = new XunLocPosUploadPolicy();
		}
		return instance;
	}

    public class CallBack extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {}
           @Override
           public void onError(int i, String s) {}    
    }


	public void ActiveLocUpload(long sn, XunLocRecord record){
		Log.d(TAG, "ActiveLocUpload: ");
		try{
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_ACTIVE_LOCATION_SEND);
		}catch (Exception e){
			e.printStackTrace();
		}
		JSONObject fmt = new JSONObject();
		JSONArray fmt_array = new JSONArray();
		if (record.convertToJson(fmt)) {
			fmt_array.add(fmt);
		}
		try{
			XunLocation.getmNetworkManager().uploadLocationWithSN(fmt_array.toJSONString(), new Long(sn).intValue(), new CallBack() {
				@Override
				public void onSuccess(ResponseData responseData) {

				}

				@Override
				public void onError(int i, String s) {
					try {
						XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_ACTIVE_LOCATION_SEND_FAILED);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void sosLocUpload(XunLocRecord record){
		Log.d(TAG, "sosLocUpload: ");
		try{
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_ACTIVE_LOCATION_SEND);
		}catch (Exception e){
			e.printStackTrace();
		}
		JSONObject fmt = new JSONObject();
		JSONArray fmt_array = new JSONArray();
		if (record.convertToJson(fmt)) {
			fmt_array.add(fmt);
		}
		try{
			XunLocation.getmNetworkManager().uploadLocation(fmt_array.toJSONString(), new CallBack() {
				@Override
				public void onSuccess(ResponseData responseData) {
					Log.d(TAG, "onSuccess: Sos Upload succ");
				}

				@Override
				public void onError(int i, String s) {
					Log.d(TAG, "onSuccess: Sos Upload fail");
					try{
						XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_ACTIVE_LOCATION_SEND_FAILED);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}	


	public void feedbackLocUpload(XunLocRecord record){
		Log.d(TAG, "feedbackLocUpload: ");
		JSONObject fmt = new JSONObject();
		JSONArray fmt_array = new JSONArray();
		if (record.convertToJson(fmt)) {
			fmt_array.add(fmt);
		}else{
			return;
		}

		sending_sn = XunLocation.getmNetworkManager().getMsgSN();
		String sid = XunLocation.getmNetworkManager().getSID();
		JSONObject msg = CloudBridgeUtil.obtainCloudMessageContent(CloudBridgeUtil.CID_UPLOAD_LOCATION, sending_sn, sid, fmt_array);
		XunLocation.getmNetworkManager().sendJsonMessage(msg.toJSONString(), new CallBack(){
				@Override
				public void onSuccess(ResponseData responseData) {
					Log.d(TAG, "feedbackLocUpload onSuccess: feedback Upload succ"+responseData.toString());
					sendCoutFeedbackLocation(responseData.getResponseData());
				}

				@Override
				public void onError(int i, String s) {
					Log.d(TAG, "feedbackLocUpload onError : feedback Upload fail");
				}

			});

	}

	private void sendCoutFeedbackLocation(String response){
		Log.d(TAG, "sendCoutFeedbackLocation :"+response);
		Intent intent = new Intent("com.xxun.watch.xunfriends.action.onReceive.location");
		intent.setPackage("com.xxun.watch.xunfriends");
		if(response != null){			
			intent.putExtra("lct", response);
		}else{
			intent.putExtra("lct", "-1");
		}
		XunLocation.getmContext().sendBroadcast(intent);
	}


	public void fastProdicLocUpload(XunLocRecord record){
		Log.d(TAG, "fastProdicLocUpload: ");
		try{
			XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND);
		}catch (Exception e){
			e.printStackTrace();
		}
		JSONObject fmt = new JSONObject();
		JSONArray fmt_array = new JSONArray();
		if (record.convertToJson(fmt)) {
			fmt_array.add(fmt);
		}
		try{
			XunLocation.getmNetworkManager().uploadCurrentLocation(fmt_array.toJSONString(), new CallBack() {
				@Override
				public void onSuccess(ResponseData responseData) {
					int rsp_sn = getSn(responseData.getResponseData());
					if(rsp_sn == sending_sn){

					}
				}

				@Override
				public void onError(int i, String s) {
					try{
						XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND_FAILED);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private int getSn(String data){
		JSONObject pl = null;
		if(data == null) {
			return -1;
		}

		try{
			pl = (JSONObject) JSONValue.parse(data);

			if(pl == null) {
				return -1;
			}

			Log.d(TAG, "ParseEfidDataInd: pl="+pl.toJSONString());

			Object objectSn = pl.get("SN");
			if(objectSn == null){
				return -1;
			}

			if(objectSn instanceof  Integer) {
				Integer sn = (Integer)objectSn;
				Log.d(TAG, "getSn int: ="+ sn);
				return sn.intValue();
			} else if(objectSn instanceof Long){
				Long sn = (Long)objectSn;
				Log.d(TAG, "getSn long: ="+ sn);
				return sn.intValue();
			}

		}catch (Exception e){
			e.printStackTrace();
			return -1;
		}
		return -1;
	}



	CallBack uploadTraceDataCallback = new CallBack() {
		@Override
		public void onSuccess(ResponseData responseData) {
			Log.d(TAG, "uploadTraceDataCallback: sending_sn="+sending_sn);
			try{
				Log.d(TAG, "onSuccess: "+responseData.toString());
				JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
				int responRc = (int)jsonData.get("RC");
				int responSn = (int)jsonData.get("SN");

				if(responSn == sending_sn){
					Log.d(TAG, "uploadTraceDataCallback: SN succ");
					if(responRc == CloudBridgeUtil.RC_SUCCESS){
						Log.d(TAG, "onSuccess: Send succ");
						removeUploadRecord();

						//sendnext
						uploadCacheLocData(false);
					}
					else if(responRc == -14){
						Log.d(TAG, "uploadTraceDataCallback: sid error");
						try{
							XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND_FAILED);
						}catch (Exception e){
							e.printStackTrace();
						}

						clearUploadingStatus();
					}else if((responRc == CloudBridgeUtil.RC_TIMEOUT)
									||(responRc == CloudBridgeUtil.RC_NETERROR)
									||(responRc == CloudBridgeUtil.RC_SOCKET_NOTREADY)
									||(responRc == CloudBridgeUtil.RC_NETERROR)){
						Log.d(TAG, "uploadTraceDataCallback: net error here");
						try{
							XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND_FAILED);
						}catch (Exception e){
							e.printStackTrace();
						}
						clearUploadingStatus();

					}
					else{
						Log.d(TAG, "uploadTraceDataCallback: other error from Server resp remove cur record");
						removeUploadRecord();
						//sendnext
					}
				}else{
					Log.d(TAG, "onSuccess: SN not avaliable nothing to do");
				}
			}catch(Exception e){
				e.printStackTrace();
				clearUploadingStatus();
			}
		}

		@Override
		public void onError(int i, String s) {
			Log.d(TAG, "onError: ");
			try{
				XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND_FAILED);
			}catch (Exception e){
				e.printStackTrace();
			}
			if((i >= -200)&&(i != -14)) {
				if (timeStamps != null) {
					XunLocRecordDAO.getInstance(XunLocation.getmContext()).removeLocation(timeStamps);
				}
			}
			clearUploadingStatus();
		}
	};

	private void removeUploadRecord(){
		Log.d(TAG, "removeUploadRecord: ");

		if(sending_sn == -1)
			return;
		if(timeStamps == null)
			return;
		if(timeStamps.size() == 0)
			return;

		XunLocRecordDAO.getInstance(XunLocation.getmContext()).removeLocation(timeStamps);

		clearUploadingStatus();
	}

	private void clearUploadingStatus(){
		Log.d(TAG, "clearUploadingStatus: ");
		sending_sn = -1;
		timeStamps = null;
		uploadtik = 0L;
		flushtik = 0L;
	}

	private boolean isSending(){
		if((sending_sn != -1)&&(uploadtik != 0L)&&(timeStamps!= null)){
			Log.d(TAG, "isSending: true");
			return true;
		}else{
			Log.d(TAG, "isSending: false");
			return false;
		}
	}

	private boolean isFlushing(){
		if(isSending() == true){
			if(flushtik != 0L){
				Log.d(TAG, "isFlushing: true");
				return true;
			}
		}
		Log.d(TAG, "isFlushing: false");
		return false;
	}



	public void uploadCacheLocData(boolean padding_upload){
		Log.d(TAG, "uploadCacheLocData: padding ="+ padding_upload);


		if(isSending()){
			if(padding_upload){

				//in sending not upload now
				Log.d(TAG, "uploadCacheLocData: padding uploading");
				return;

			}else{
				if(isFlushing()){

					//in flushing wait
					Log.d(TAG, "uploadCacheLocData: in flushing wait");
					return;
				}else{

					//try to flushing pos
					try{
						if(XunLocation.getmNetworkManager().flushPaddingMessage(sending_sn)){

							// flushing start
							Log.d(TAG, "uploadCacheLocData: flushing start");
							flushtik = System.currentTimeMillis();
							return;

						}else{

							// no need flushing
							Log.d(TAG, "uploadCacheLocData: no need flush");
							flushtik = 0L;

						}
					}catch(Exception e){
						clearUploadingStatus();
						e.printStackTrace();
					}
				}
			}
		}


		XunLocRecordDAO recordDAO = XunLocRecordDAO.getInstance(XunLocation.getmContext());
		JSONArray fmt_array = new JSONArray();
		timeStamps = recordDAO.readLocation(fmt_array);
		if(timeStamps == null) {
			Log.d(TAG, "uploadCacheLocData: empty1");
			clearUploadingStatus();
			return;
		}
		if(timeStamps.size() == 0) {
			Log.d(TAG, "uploadCacheLocData: empty2");
			clearUploadingStatus();
			return;
		}
		Log.d(TAG, "uploadCacheLocData: send");
		//have record upload

		//start sending
		try {

			//Statistics func
			try{
				XunLocation.getStatisticsManager().stats(XiaoXunStatisticsManager.STATS_CYCLE_LOCATION_SEND);
			}catch (Exception e){
				e.printStackTrace();
			}


			sending_sn = XunLocation.getmNetworkManager().getMsgSN();
			String sid = XunLocation.getmNetworkManager().getSID();
			JSONObject msg = CloudBridgeUtil.obtainCloudMessageContent(CloudBridgeUtil.CID_UPLOAD_TRACE_DATA, sending_sn, sid, fmt_array);

			//record sending time
			uploadtik = System.currentTimeMillis();


			//sending
			if(padding_upload) {

				//sending with padding
				Log.d(TAG, "uploadCacheLocData:  upload with padding");
				XunLocation.getmNetworkManager().paddingSendJsonMessage(msg.toJSONString(), uploadTraceDataCallback);
				//sending_sn = XunLocation.getmNetworkManager().paddingUploadTraceData(fmt_array.toJSONString(), uploadTraceDataCallback);

			}else{


				//send it now
				Log.d(TAG, "uploadCacheLocData: upload now");
				XunLocation.getmNetworkManager().sendJsonMessage(msg.toJSONString(), uploadTraceDataCallback);
				//sending_sn = XunLocation.getmNetworkManager().uploadTraceData(fmt_array.toJSONString(), uploadTraceDataCallback);

			}

		}catch (Exception e) {

			e.printStackTrace();
			clearUploadingStatus();

		}
	}

	public void uploadOrSaveByNetworkStatus(XunLocRecord record){
		Log.d(TAG, "uploadOrSaveByNetworkStatus: ");
		if(XunLocation.getmNetworkManager().isLoginOK()){
			Log.d(TAG, "uploadOrSaveByNetworkStatus: is login");
			fastProdicLocUpload(record);
			//save to resend area;
		}else{
			Log.d(TAG, "uploadOrSaveByNetworkStatus: not login");
			record.saveToDb();
		}
	}


	public void onCheckAndUploadTiming(){
		Log.d(TAG, "onCheckAndUploadTiming: ");


		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			Log.d(TAG, "onCheckAndUploadTiming: isAirPlaneModeOn");
			return;
		}

		if(XunLocation.getmNetworkManager().isWebSocketOK() == false){
			Log.d(TAG, "onCheckAndUploadTiming: isWebSocketOK false");
			return;
		}

		if(isFlushing()){

			Log.d(TAG, "onCheckAndUploadTiming: Flushing");
			if((System.currentTimeMillis() - flushtik) > FLUSH_UPLOAD_TIME_OUT){
				Log.d(TAG, "onCheckAndUploadTiming: flushing timeout");
				clearUploadingStatus();
				uploadCacheLocData(false);

			}
		}


		if(isSending()){

			Log.d(TAG, "onCheckAndUploadTiming: uploading");
			if((System.currentTimeMillis() - uploadtik) > UPLOAD_TIME_OUT){
				Log.d(TAG, "onCheckAndUploadTiming: sending timeout");
				clearUploadingStatus();
				uploadCacheLocData(true);
			}
		}


		//other func
		//XunLocPolicyLastRecord.getInstance().getLastCell().onCellChange();
		XunLocTiming.getInstance().checkAlarmIsWorking();
	}
}
