package com.xxun.watch.stepcountservices;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.xxun.watch.location.XunLocation;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.provider.Settings;
/**
 * Created by zhangjun5 on 2017/10/25.
 */

public class StepsCountUtils {
	private static final String TAG = "[xunStep]StepsCountUtils";

    public static String getStringValue(Context context, String key, String defValue) {
        String str = context.getSharedPreferences(Const.PREF_STEPS_FILE, Context.MODE_PRIVATE )
                .getString(key, defValue);
        return str;
    }

    public static boolean stepStateValue(String stepValue){
        boolean retValue = false;
        if(stepValue.equals("0")){
            retValue = false;
        }
        String strArray[] = stepValue.split("_");

        if(strArray.length == 2 && StepsCountUtils.compareTodayToLastInfo(strArray[0])){
            if(strArray[1].equals("0")){
                retValue = false;
            }else{
                retValue = true;
            }
        }
        return retValue;
    }

	private static void HandlerDeviceBindStepDate(Context context){
		String isBinds = getStringValue(context,Const.PREF_STEPS_FILE,Const.DEVICE_BIND_FLAG,"0");
		Log.e("bind statue",isBinds+":"+XunLocation.isBinded());
		sdcardLog("bind statue"+isBinds+":"+XunLocation.isBinded());
		if(XunLocation.isBinded()){
			if(isBinds.equals("0")){
				clearOldDataToLocal(XunLocation.getmContext(), Const.PREF_STEPS_FILE);
				setValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, "0");
				sdcardLog("HandlerDeviceBindStepDate:"+Const.SHARE_PREF_PHONT_STEPS_NEW+":0");
				ContentValues contentValues = new ContentValues();
                contentValues.put("step_local","0");
                int count = context.getContentResolver().update(Uri.parse("content://com.xxun.watch.stepCountProvider/user"),contentValues
                        ,null,null);
                Log.e("line1:",count+"");
			}
			setValue(context,Const.PREF_STEPS_FILE,Const.DEVICE_BIND_FLAG,"1");
		}else{
			setValue(context,Const.PREF_STEPS_FILE,Const.DEVICE_BIND_FLAG,"0");
		}
	}

	public static void insertStepByType(final String getType, long steps) {
		sdcardLog("insertStepByType:"+getType+":"+steps);
		HandlerDeviceBindStepDate(XunLocation.getmContext());
		Intent _intent = new Intent(Const.ACTION_BROAST_SENSOR_STEPS);
		_intent.putExtra("sensor_steps",String.valueOf(steps));
		_intent.putExtra("sensor_type",getType);
		String timeStamp = getTimeStampLocal();
		_intent.putExtra("sensor_timestamp", timeStamp);
		XunLocation.getmContext().sendBroadcast(_intent);
	}

    public static void initSensor(final Context context, final String getType) {
         Log.e("steps","ender sersor steps!");
        final int sensorTypeC=Sensor.TYPE_STEP_COUNTER;
        final SensorManager mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        final Sensor mStepCount = mSensorManager.getDefaultSensor(sensorTypeC);
        if (mStepCount != null) {
            mSensorManager.registerListener(new SensorEventListener() {
                                                @Override
                                                public void onSensorChanged(SensorEvent sensorEvent) {
                                                    try {
                                                        if (sensorEvent.sensor.getType() == sensorTypeC) {
                                                            int totalSteps = (int) sensorEvent.values[0];
                                                            Log.e("send steps broast:",totalSteps+":");
                                                            Intent _intent = new Intent(Const.ACTION_BROAST_SENSOR_STEPS);
                                                            _intent.putExtra("sensor_steps",String.valueOf(totalSteps));
                                                            _intent.putExtra("sensor_type",getType);
                                                            String timeStamp = getTimeStampLocal();
                                                            _intent.putExtra("sensor_timestamp", timeStamp);
                                                            context.sendBroadcast(_intent);
                                                            mSensorManager.unregisterListener(this, mStepCount);
                                                        }
                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void onAccuracyChanged(Sensor sensor, int i) {

                                                }
                                            },
                    mStepCount, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public static String getPhoneStepsByFirstSteps(Context context, String mTotalSteps){
        String curSteps = "0";
        if(mTotalSteps == null){
            return curSteps;
        }
        try{
            String phoneStepsPref = getStringValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, "0");
			sdcardLog("getPhoneStepsByFirstSteps getString:"+phoneStepsPref+"");
            if(phoneStepsPref.equals("0")) {
                return curSteps;
            }
            String steps[] = phoneStepsPref.split("_");
	        String saveSteps = getTimeStampLocal() + "_" + (steps[1]) + "_" + mTotalSteps;
            if(steps.length >= 3 && compareTodayToLastInfo(steps[0])) {
                if (Integer.valueOf(mTotalSteps) < Integer.valueOf(steps[2])) {
                    int stepOffset = Integer.valueOf(steps[2]) - Integer.valueOf(steps[1]);
                    if (stepOffset < 0) {
                        stepOffset = 0;
                    }
                    saveSteps = getTimeStampLocal() + "_" + (-stepOffset) + "_" + mTotalSteps;
                    curSteps = String.valueOf(Integer.valueOf(mTotalSteps) + stepOffset);
                } else if (Integer.valueOf(mTotalSteps) >= Integer.valueOf(steps[1])) {
                    curSteps = String.valueOf(Integer.valueOf(mTotalSteps) - Integer.valueOf(steps[1]));
                } else {
                    saveSteps = getTimeStampLocal() + "_" + 0 + "_" + mTotalSteps;
                    curSteps = String.valueOf(Integer.valueOf(mTotalSteps));
                }
            }

            setValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, saveSteps);
	    	ContentValues contentValues = new ContentValues();
            Log.e("CurSteps save Data:",saveSteps);
			sdcardLog("getPhoneStepsByFirstSteps saveString:"+saveSteps+"");
            contentValues.put("step_local",saveSteps);
            int count = context.getContentResolver().update(Uri.parse("content://com.xxun.watch.stepCountProvider/user"),contentValues,null,null);
            Settings.Global.putString(context.getContentResolver(),"step_local", saveSteps);
            Log.d(TAG,"line1:"+count+"");
        }catch(Exception e){
            curSteps = "0";
            Log.e("exception:",e.toString());
        }

        return curSteps;
    }

    public static boolean getPhoneStepsStatueByFirstSteps(Context context, String mTotalSteps){
        boolean isYesdayData = false;
        if(mTotalSteps == null){
            return isYesdayData;
        }
        try {
            String phoneStepsPref = getStringValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, "0");
			sdcardLog("getPhoneStepsStatueByFirstSteps getString:"+phoneStepsPref+"");
            if(phoneStepsPref.equals("0")){
                String saveSteps = getTimeStampLocal() + "_" + mTotalSteps + "_" + mTotalSteps;
                setValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, saveSteps);
				sdcardLog("getPhoneStepsStatueByFirstSteps save String:"+saveSteps+"");
                ContentValues contentValues = new ContentValues();
                Log.e("CurSteps save Data:",saveSteps);
                contentValues.put("step_local",saveSteps);
                int count = context.getContentResolver().update(Uri.parse("content://com.xxun.watch.stepCountProvider/user"),contentValues
                        ,null,null);
                Log.e("line1:",count+"");
                isYesdayData = false;
                return isYesdayData;
            }
			
			
            String steps[] = phoneStepsPref.split("_");
            if (steps.length >= 2 && !compareTodayToLastInfo(steps[0])) {
                String stepsCalcYester = "0";
               
				if (Integer.valueOf(mTotalSteps) < Integer.valueOf(steps[2])) {
                    int stepOffset = Integer.valueOf(steps[2]) - Integer.valueOf(steps[1]);
                    if (stepOffset < 0) {
                        stepOffset = 0;
                    }
                    stepsCalcYester = String.valueOf(Integer.valueOf(mTotalSteps) + stepOffset);
                } else if (Integer.valueOf(mTotalSteps) >= Integer.valueOf(steps[1])) {
                    stepsCalcYester = String.valueOf(Integer.valueOf(mTotalSteps) - Integer.valueOf(steps[1]));
                } else {
                    stepsCalcYester = String.valueOf(Integer.valueOf(mTotalSteps));
                }				
				
				if(!stepsCalcYester.equals("0")){
					saveOldDataToLocal(context,Const.PREF_STEPS_FILE,steps[0],stepsCalcYester);
				}
                String saveSteps = getTimeStampLocal() + "_" + mTotalSteps + "_" + mTotalSteps;
                setValue(context,Const.PREF_STEPS_FILE,Const.SHARE_PREF_PHONT_STEPS_NEW, saveSteps);
                ContentValues contentValues = new ContentValues();
                Log.e("CurSteps save Data:",saveSteps);				
				sdcardLog("getPhoneStepsStatueByFirstSteps save String1:"+saveSteps+"");
                contentValues.put("step_local",saveSteps);
                int count = context.getContentResolver().update(Uri.parse("content://com.xxun.watch.stepCountProvider/user"),contentValues
                        ,null,null);
                Log.e("line1:",count+"");
                isYesdayData = true;

                return isYesdayData;
            }
//            if (Integer.valueOf(mTotalSteps) > Integer.valueOf(steps[1])) {
//                retStr = String.valueOf(Integer.valueOf(mTotalSteps) - Integer.valueOf(steps[1]));
//            }
        }catch (Exception e){
            isYesdayData = false;
        }
        return isYesdayData;
    }
    public static void clearOldDataToLocal(Context context,String pref_file_name){
        setValue(context,pref_file_name,Const.SHARE_PREF_OLDDATE_STEPS,"{}");
    }
    public static String getOldDataFromLocal(Context context,String pref_file_name){
        String phoneStepsPref = getStringValue(context,pref_file_name,Const.SHARE_PREF_OLDDATE_STEPS, "{}");

        return phoneStepsPref;
    }
    private static void saveOldDataToLocal(Context context,String pref_file_name,String timeStamp, String Steps){
        String phoneStepsPref = getStringValue(context,pref_file_name,Const.SHARE_PREF_OLDDATE_STEPS, "{}");
        JSONObject jsonObject = (JSONObject) JSONValue.parse(phoneStepsPref);
        jsonObject.put(timeStamp,Steps);
        setValue(context,pref_file_name,Const.SHARE_PREF_OLDDATE_STEPS,jsonObject.toString());
    }
    public static String getStepsKeyByData(String eid,String YYYYMMDD){
        StringBuilder key = new StringBuilder("EP/");
        key.append(eid);
        key.append("/STEPS/");
        key.append(getReversedTimeYYYYMMDD(YYYYMMDD));

        return key.toString();
    }
    public static String getReversedTimeYYYYMMDD(String YYYYMMDD) {
        StringBuilder timeStamp = new StringBuilder();
        timeStamp.append(String.format("%1$08d", Const.YMD_REVERSED_MASK_8 - Integer.parseInt(YYYYMMDD)));
        return timeStamp.toString();
    }

    public static void setValue(Context context,String pref_file_name,String key, String value) {
        final SharedPreferences preferences = context.getSharedPreferences(pref_file_name, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getStringValue(Context context, String pref_file_name, String key, String defValue) {
        String str = context.getSharedPreferences(pref_file_name, Context.MODE_PRIVATE)
                .getString(key, defValue);
        return str;
    }
    public static boolean compareTodayToLastInfo(String oldData) {
        boolean isToday = false;
        String curTime =  getTimeStampLocal();
        String curDate = curTime.substring(0, 8);
        String oldDate = oldData.substring(0, 8);
        if (curDate.equals(oldDate)) {
            isToday = true;
        }
        return isToday;
    }
    public static boolean compareTimeHourDiff(Date timeStamp,String timeHour){
        boolean isBig = false;
        DateFormat format = new SimpleDateFormat("HHmm");
        String curTimeStamp = format.format(timeStamp).toString();
        Log.e("time:",curTimeStamp+":"+timeHour);
        if(curTimeStamp.compareTo(timeHour) > 0 ){
            isBig = true;
        }else{
            isBig = false;
        }

        return isBig;
    }

    public static String getReversedOrderTime() {
        StringBuilder timeStamp = new StringBuilder();
        String test = null;
        test = getTimeStampLocal();

        timeStamp.append(String.format("%1$08d", Const.YMD_REVERSED_MASK_8 - Integer.parseInt(test.substring(0, 8))));
        timeStamp.append(String.format("%1$09d", Const.HMSS_REVERSED_MASK_9 - Integer.parseInt(test.substring(8, 17))));
        return timeStamp.toString();
    }

    public static String getTimeStampLocal() {
        Date d = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        // format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return format.format(d).toString();
    }

    public static JSONObject obtainCloudMsgContent(int cid, int sn, String sid, Object pl) {
        JSONObject msg = new JSONObject();
        msg.put("Version", "00190000");
        msg.put("CID", cid);
        if (sid != null) {
            msg.put(Const.KEY_NAME_SID, sid);
        }
        msg.put("SN", sn);
        if (pl != null) {
            msg.put("PL", pl);
        }
        return msg;
    }

    public static void sdcardLog(String sMsg) {
        Date nowtime = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String currTime = dateFormat.format(nowtime);

        String logText = currTime + " " + sMsg + "\n";
        try {
            File file = null;
            File baseDir;
            File dir;
            baseDir = new File(Environment.getExternalStorageDirectory(), "stepscount");
            if (baseDir.exists() && !baseDir.isDirectory()) {
                baseDir.delete();
            }
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            dir = new File(baseDir, "log");
            if (dir.exists() && !dir.isDirectory()) {
                dir.delete();
            }
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Date nowtime1 = new Date();
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
            String currDay = dateFormat1.format(nowtime1);
            StringBuilder fileNameBuff = new StringBuilder();
            fileNameBuff.append(currDay);//调整一下log文件命名方式，方便查找
            fileNameBuff.append("_");
            fileNameBuff.append("all");//修改sdcardlog,不区分eid，方便分析
            fileNameBuff.append(".log");

            file = new File(dir, fileNameBuff.toString());
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(logText.getBytes());
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
