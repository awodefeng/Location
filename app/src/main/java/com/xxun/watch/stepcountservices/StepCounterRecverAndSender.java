package com.xxun.watch.stepcountservices;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.xiaoxun.sdk.ResponseData;
import com.xiaoxun.sdk.IMessageReceiveListener;
import com.xiaoxun.sdk.IResponseDataCallBack;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xxun.watch.location.XunLocation;
import com.xxun.watch.location.XunSensorProc;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import java.util.Date;

import java.util.Set;

/**
 * Created by zhangjun5 on 2017/10/25.
 */

public class StepCounterRecverAndSender{
	private static final String TAG = "[xunstep]StepCounterRecverAndSender";
	private static final int STEP_CHECK_PRODIC = 30*60*1000;//one hour

	static private StepCounterRecverAndSender instance = null;
	private long timeStamp = 0;
	private StepsCountObserve stepsCountObserve = null;

	private Handler handler = null;
	private Runnable runable = new Runnable() {
		@Override
		public void run() {
			checkStep();
			handler.postDelayed(this, 10000);
		}
	};

	static public StepCounterRecverAndSender getInstance(){
		if(instance == null) {
			instance = new StepCounterRecverAndSender();
		}
		return instance;
	};

	public StepCounterRecverAndSender() {
		handler = new Handler();
		handler.postDelayed(runable, 1000);
		stepsCountObserve = new StepsCountObserve();
		Log.d(TAG, "StepCounterRecverAndSender: start");
	};


	private void checkStep(){

		long cur_time = SystemClock.elapsedRealtime();

		if(((cur_time - timeStamp)>STEP_CHECK_PRODIC)||(timeStamp == 0)){
			Log.d(TAG, "checkStep: sendStep="+ XunSensorProc.getInstance().stepcounter);
			
		        StepsCountUtils.insertStepByType("0", XunSensorProc.getInstance().stepcounter);
			
			timeStamp = cur_time;
		}
	}

	public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive:"+intent.getAction());
		StepsCountUtils.sdcardLog("receive onclock:"+intent.getAction());
		String action = intent.getAction();
		if(action.equals("brocast.action.step.current.noti")) {
			//StepsCountUtils.initSensor(context,"1");
			StepsCountUtils.insertStepByType("1", XunSensorProc.getInstance().stepcounter);

		}else if(action.equals(Const.ACTION_BROAST_SENSOR_STEPS)){
			handerStepsManager(context,intent);
        }else if(action.equals("xxun.steps.action.broast.sensor.steps")){
			StepsCountUtils.insertStepByType("2", XunSensorProc.getInstance().stepcounter);
			Intent _intent = new Intent("xxun.steps.action.broast.sensor.steps.req");
			_intent.putExtra("sensor_steps",String.valueOf(XunSensorProc.getInstance().stepcounter));
                        _intent.setPackage("com.xxun.watch.stepstart");
			XunLocation.getmContext().sendBroadcast(_intent);
		}
    }

    public class CallBack extends IResponseDataCallBack.Stub{
           @Override
           public void onSuccess(ResponseData responseData) {}
           @Override
           public void onError(int i, String s) {}    
    }
	
	
	private void handerStepsManager(Context context,Intent intent){
		Log.d(TAG, "handerStepsManager: "+intent.toString());
		String sensorSteps = intent.getStringExtra("sensor_steps");
		String sensorType = intent.getStringExtra("sensor_type");
		String sensorTimeStamp = intent.getStringExtra("sensor_timestamp");
		final StepsRanksApp mApp = XunLocation.mApp;
		final XiaoXunNetworkManager nerservice = (XiaoXunNetworkManager)context.getSystemService
				("xun.network.Service");
				
		//判断是否是新的一天 1：更新达标标志位  2：发送前一天的计步数据（40111）  log
		boolean isYestoryData = StepsCountUtils.getPhoneStepsStatueByFirstSteps(context, sensorSteps);
		if(isYestoryData){
			mApp.setStep_half_flag(false);
			mApp.setStep_compele_flag(false);
			mApp.setStep_ranks_upload_flag(false);
			mApp.setStep_yesterday_upload_flag(false);
			String stepVaule = StepsCountUtils.getTimeStampLocal()+"_0";
			StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_HALF_FLAG,stepVaule);
			StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_COMPELE_FLAG,stepVaule);
			StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_RANKS_UPLOAD_FLAG,stepVaule);
			StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_YESTERDAY_UPLOAD_FLAG,stepVaule);
			StepsCountUtils.sdcardLog("STEP VAULE:"+stepVaule);
		}

		if(!mApp.getStep_yesterday_upload_flag() &&
				(StepsCountUtils.compareTimeHourDiff(new Date(), "0930")
				|| sensorType.equals("1"))){
			String oldData = StepsCountUtils.getOldDataFromLocal(context,Const.PREF_STEPS_FILE);
			try {
				net.minidev.json.JSONObject jsonObject = (net.minidev.json.JSONObject) JSONValue.parse(oldData);
				Set<String> keys = jsonObject.keySet();
				net.minidev.json.JSONObject sendJson = new net.minidev.json.JSONObject();
				for(String key:keys){
					String sendKey = StepsCountUtils.getStepsKeyByData(nerservice.getWatchEid(),
							key.substring(0,8));
					net.minidev.json.JSONObject stepJson = new net.minidev.json.JSONObject();
					stepJson.put("Steps",Integer.valueOf((String)jsonObject.get(key)));
					sendJson.put(sendKey,stepJson);
				}
				StepsCountUtils.sdcardLog("oldData Upload:"+sendJson+nerservice.isWebSocketOK());
				if(sendJson.size() == 0){
					Log.e("stepsRanksApp:"," senddata size() = 0");
				}else{
					String sendData = StepsCountUtils.obtainCloudMsgContent(
							Const.CID_STEPS_YESTERDAY_UPLOAD,nerservice.getMsgSN(),nerservice.getSID(),sendJson).toJSONString();
					Log.e("stepsRanksApp(40111):"," senddata:"+sendData+":"+sendJson.size());
					nerservice.paddingSendJsonMessage(sendData,
							new CallBack(){
								//new IResponseDataCallBack<ResponseData>() {
								@Override
								public void onSuccess(ResponseData responseData) {
									try{
										Log.e("MyReceiver","responseData:"+responseData.toString());
										StepsCountUtils.sdcardLog("responseData:"+responseData.toString());
										JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
										int responRc = (int)jsonData.get("RC");
										if(responRc == 1){
											StepsCountUtils.clearOldDataToLocal(XunLocation.getmContext(), Const.PREF_STEPS_FILE);
											StepsRanksApp mApp = XunLocation.mApp;
											mApp.setStep_yesterday_upload_flag(true);
											String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
											StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.STEPS_YESTERDAY_UPLOAD_FLAG,stepVaule);

										}
									}catch(Exception e){
										e.printStackTrace();
									}
								}

								@Override
								public void onError(int i, String s) {
									Log.e("MyReceiver","onError"+i+":"+s);
								}
							}

					);
				}
			}catch (Exception e){
				Log.e("oldData error:",e.toString());
			}
		}

		//获取当前步数，需要在计算前一天数据之前才可以
		String saveFirstPref = StepsCountUtils.getStringValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, "0");
		final String curSteps = StepsCountUtils.getPhoneStepsByFirstSteps(context, sensorSteps);
               
		/*一天步数超过65535步，判定异常，放弃上传---pzh add start*/
                int curStepsInt = Integer.parseInt(curSteps);
		if(curStepsInt > 65535)
		{
 			Log.d(TAG,"curSteps date exception !!! curSteps:"+curSteps);
			return;
		}
       		 /*pzh add end*/

		if(sensorType.equals("2")){
			return;
		}
		StepsCountUtils.sdcardLog("RECEIVE get save first pref1:"+saveFirstPref+":"+curSteps+":"+sensorSteps);
		if(sensorType.equals("1")){//830 current Steps Upload
			JSONObject sendJson = new JSONObject();
			sendJson.put(Const.KEY_NAME_TGID, nerservice.getWatchGid());
			sendJson.put(Const.KEY_NAME_CUR_STEPS, StepsCountUtils.getTimeStampLocal()+"_"+curSteps);
			String sendData = StepsCountUtils.obtainCloudMsgContent(
					Const.CID_STEPS_VALUE,nerservice.getMsgSN(),nerservice.getSID(),sendJson).toJSONString();
			Log.d("stepsRanksApp:"," senddata:"+sendData);
			StepsCountUtils.sdcardLog("sendData:"+sendData);
			nerservice.sendJsonMessage(sendData,
					new CallBack(){
					//new IResponseDataCallBack<ResponseData>() {
						@Override
						public void onSuccess(ResponseData responseData) {
							try{
								Log.e("MyReceiver","responseData:"+responseData.toString());
								StepsCountUtils.sdcardLog("responseData:"+responseData.toString());
								JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
								int responRc = (int)jsonData.get("RC");
								if(responRc == 1){

								}
							}catch(Exception e){
								e.printStackTrace();
							}
						}

						@Override
						public void onError(int i, String s) {
							Log.e("MyReceiver","onError"+i+":"+s);
						}
					}
			);
			return;
		}


		//休眠时段之前，发送计步数据，用于第二天夜里2点计算第一天的地区和全国排名
		try{
			String sleepTime = mApp.getStringValue(Const.SLEEP_LIST,"{}");
			net.minidev.json.JSONObject jsonObject = (net.minidev.json.JSONObject) JSONValue.parse(sleepTime);
			String sleepOnOff = (String) jsonObject.get("onoff");
			String sleepStartHour = (String) jsonObject.get("starthour");
			Log.e("sleep:",sleepOnOff+":"+sleepStartHour);
			StepsCountUtils.sdcardLog("sleep:"+sleepOnOff+":"+sleepStartHour);
			if(sleepOnOff == null){
				sleepOnOff = "1";
			}
			if(sleepStartHour == null){
				sleepStartHour = "21";
			}
			int sleepStart = Integer.valueOf(sleepStartHour);
			int sendStepsData = 0;
			if(sleepOnOff.equals("0") || sleepStart == 0){
				sleepStart = 24;
			}
			sendStepsData = sleepStart - 12;
			if(sendStepsData <= 0){
				sendStepsData = 22;
			}else if(sendStepsData > 0){
				sendStepsData+=10;
			}
			String curTime = StepsCountUtils.getTimeStampLocal();
			final String hourTime = curTime.substring(8,10);
			Log.e("hour:",hourTime+":"+curTime+":"+sendStepsData);
			if(sendStepsData <= Integer.valueOf(hourTime) && Integer.valueOf(hourTime) <= sleepStart && !mApp.getStep_ranks_upload_flag()){
				Log.e("curSteps:", curSteps);
				net.minidev.json.JSONObject sendJson = new net.minidev.json.JSONObject();
				sendJson.put(Const.KEY_NAME_TGID, nerservice.getWatchEid());
				sendJson.put(Const.KEY_NAME_CUR_STEPS, StepsCountUtils.getTimeStampLocal()+"_"+curSteps);
				String sendData = StepsCountUtils.obtainCloudMsgContent(
						Const.CID_STEPS_VALUE,nerservice.getMsgSN(),nerservice.getSID(),sendJson).toJSONString();
				Log.e("stepsRanksApp:"," senddata:"+sendData);
				StepsCountUtils.sdcardLog("hour:"+hourTime+":"+curTime+":"+sendStepsData+"sendData:"+sendData);
				nerservice.paddingSendJsonMessage(sendData,
						new CallBack(){
						//new IResponseDataCallBack<ResponseData>() {
							@Override
							public void onSuccess(ResponseData responseData) {
								try{
									Log.e("MyReceiver","responseData:"+responseData.toString());
									StepsCountUtils.sdcardLog("MyReceiver responseData:"+responseData.toString());
									JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
									int responRc = (int)jsonData.get("RC");
									if(responRc == 1){
										StepsRanksApp mApp = XunLocation.mApp;
										String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
										StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.STEPS_RANKS_UPLOAD_FLAG,stepVaule);
									}
								}catch(Exception e){
									e.printStackTrace();
								}
							}

							@Override
							public void onError(int i, String s) {
								Log.e("MyReceiver","onError"+i+":"+s);
							}
						}
				);
			}
		}catch (Exception e){
			Log.e("oldData error:",e.toString());
		}

		//判断计步达标信息
		String phoneStepsPref = StepsCountUtils.getStringValue(context,Const.PREF_STEPS_FILE,
				Const.SHARE_PREF_PHONT_STEPS_NEW, "0");
		//达标步数发送通知信息
		final String targetSteps = mApp.getStringValue(Const.STEPS_TARGET_LEVEL,"8000");
		Log.e("phoneSteps:",curSteps+":"+phoneStepsPref+
				":"+targetSteps+":"+mApp.getStep_half_flag()+":"+mApp.getStep_compele_flag());
		StepsCountUtils.sdcardLog("phoneStepspref:"+phoneStepsPref+"/////phoneSteps:"+curSteps
				+":"+targetSteps+":"+mApp.getStep_half_flag()+":"+mApp.getStep_compele_flag());
		if( ((Integer.valueOf(curSteps) > (Integer.valueOf(targetSteps)/2)
				&& Integer.valueOf(curSteps) < Integer.valueOf(targetSteps) && !mApp.getStep_half_flag())
				||(Integer.valueOf(curSteps) > Integer.valueOf(targetSteps) && !mApp.getStep_compele_flag()))
				&& nerservice.isLoginOK()){
			if(mApp.getStep_compele_flag()){
				return ;
			}

			try {
				Log.e("curSteps:", curSteps);
				net.minidev.json.JSONObject sendJson = new net.minidev.json.JSONObject();
				sendJson.put(Const.KEY_NAME_TGID, nerservice.getWatchGid());

				StringBuilder key = new StringBuilder("GP/");
				key.append(nerservice.getWatchGid());
				key.append("/MSG/");
				key.append(StepsCountUtils.getReversedOrderTime());
				sendJson.put(Const.KEY_NAME_KEY, key.toString());

				net.minidev.json.JSONObject sendList = new net.minidev.json.JSONObject();
				sendList.put(Const.KEY_NAME_EID, nerservice.getWatchEid());
				sendList.put(Const.KEY_NAME_TYPE, "steps");
				sendList.put(Const.KEY_NAME_CONTENT, curSteps+"_"+targetSteps);
				sendList.put(Const.KEY_NAME_DURATION, 100);
				sendJson.put("Value",sendList);
				String sendData = StepsCountUtils.obtainCloudMsgContent(
						Const.CID_STEPS_UPLOAD,nerservice.getMsgSN(),nerservice.getSID(),sendJson).toJSONString();
				Log.e("stepsRanksApp:"," senddata:"+sendData);
				StepsCountUtils.sdcardLog("stepsRanksApp: (load target Steps)"+sendData);
				nerservice.paddingSendJsonMessage(sendData,
						new CallBack(){
						//new IResponseDataCallBack<ResponseData>() {
							@Override
							public void onSuccess(ResponseData responseData) {
								try{
									Log.e("MyReceiver","responseData:"+responseData.toString());
									StepsCountUtils.sdcardLog("MyReceiver responseData:"+responseData.toString());
									JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
									int responRc = (int)jsonData.get("RC");
									if(responRc == 1){
										StepsRanksApp mApp = XunLocation.mApp;
										if(Integer.valueOf(curSteps) > (Integer.valueOf(targetSteps)/2)
												&& Integer.valueOf(curSteps) < Integer.valueOf(targetSteps)){
											mApp.setStep_half_flag(true);
											String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
											StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.STEPS_HALF_FLAG,stepVaule);
										}
										if(Integer.valueOf(curSteps) > Integer.valueOf(targetSteps)){
											mApp.setStep_compele_flag(true);
											String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
											StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.STEPS_COMPELE_FLAG,stepVaule);
										}

									}
								}catch(Exception e){
									e.printStackTrace();
								}
							}

							@Override
							public void onError(int i, String s) {
								Log.e("MyReceiver","onError"+i+":"+s);
							}

						}
				);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
/*
	private class CurrentDataCallBack extends IResponseDataCallBack.Stub{

		private Context context;

		public CurrentDataCallBack(Context context){
			this.context = context;
		}

		@Override
		public void onSuccess(ResponseData responseData) {
			try{
				Log.e("MyReceiver","responseData:"+responseData.toString());
				StepsCountUtils.sdcardLog("responseData:"+responseData.toString());
				JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
				int responRc = (int)jsonData.get("RC");
				if(responRc == 1){

				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		@Override
		public void onError(int i, String s) {
			Log.e("MyReceiver","onError"+i+":"+s);
		}

	}

    private class YestoryDataCallBack extends IResponseDataCallBack.Stub{

	    private Context context;
            
	    public YestoryDataCallBack(Context context){
		this.context = context;
	    }

       	    @Override
	    public void onSuccess(ResponseData responseData) {
		try{
			Log.e("MyReceiver","responseData:"+responseData.toString());
			StepsCountUtils.sdcardLog("responseData:"+responseData.toString());
			JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
			int responRc = (int)jsonData.get("RC");
			if(responRc == 1){		    
			    StepsCountUtils.clearOldDataToLocal(context, Const.PREF_STEPS_FILE);
			    stepsRanksApp mApp = (stepsRanksApp)context.getApplicationContext();
			    mApp.setStep_yesterday_upload_flag(true);	
			    String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
				StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_YESTERDAY_UPLOAD_FLAG,stepVaule);

			}
		 }catch(Exception e){
                    e.printStackTrace();
                }
	    }

	    @Override
	    public void onError(int i, String s) {
		Log.e("MyReceiver","onError"+i+":"+s);
	    }

    }

    private class RanksDataCallBack extends IResponseDataCallBack.Stub{

	    private Context context;
            
	    public RanksDataCallBack(Context context){
		this.context = context;
	    }	

       	    @Override
	    public void onSuccess(ResponseData responseData) {
		try{
			Log.e("MyReceiver","responseData:"+responseData.toString());
			StepsCountUtils.sdcardLog("MyReceiver responseData:"+responseData.toString());
			JSONObject ssjsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
			int responRc = (int)jsonData.get("RC");
			if(responRc == 1){
	 	     	    stepsRanksApp mApp = (stepsRanksApp)context.getApplicationContext();	
			    mApp.setStep_ranks_upload_flag(true);
			    String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
				StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_RANKS_UPLOAD_FLAG,stepVaule);
			}
		}catch(Exception e){
                    e.printStackTrace();
                }
	    }

	    @Override
	    public void onError(int i, String s) {
		Log.e("MyReceiver","onError"+i+":"+s);
	    }

    }

    private class CompelePlanDataCallBack extends IResponseDataCallBack.Stub{

	    private Context context;
	    private String  curSteps;
	    private String  targetSteps;
            
	    public CompelePlanDataCallBack(Context context,String curstep,String targetstep){
		this.context = context;
 		this.curSteps = curstep;
		this.targetSteps = targetstep;
	    }
		
       	    @Override
	    public void onSuccess(ResponseData responseData) {
		try{
			Log.e("MyReceiver","responseData:"+responseData.toString());
			StepsCountUtils.sdcardLog("MyReceiver responseData:"+responseData.toString());
			JSONObject jsonData = (JSONObject)JSONValue.parse(responseData.getResponseData());
			int responRc = (int)jsonData.get("RC");
			if(responRc == 1){
			    stepsRanksApp mApp = (stepsRanksApp)context.getApplicationContext();
			    if(Integer.valueOf(curSteps) > (Integer.valueOf(targetSteps)/2)
			       && Integer.valueOf(curSteps) < Integer.valueOf(targetSteps)){
			 	mApp.setStep_half_flag(true);
				String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
				StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_HALF_FLAG,stepVaule);
			   }
			   if(Integer.valueOf(curSteps) > Integer.valueOf(targetSteps)){
				mApp.setStep_compele_flag(true);
				String stepVaule = StepsCountUtils.getTimeStampLocal()+"_1";
				StepsCountUtils.setValue(context,Const.PREF_STEPS_FILE,Const.STEPS_COMPELE_FLAG,stepVaule);
			   }

			}
		}catch(Exception e){
                    e.printStackTrace();
                }
	    }

	    @Override
	    public void onError(int i, String s) {
		Log.e("MyReceiver","onError"+i+":"+s);
	    }

    }
 */

}
