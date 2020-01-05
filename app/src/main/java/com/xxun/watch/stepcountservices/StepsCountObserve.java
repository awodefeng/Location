package com.xxun.watch.stepcountservices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.database.ContentObserver;
import android.os.Handler;

//import com.xiaoxun.sdk.ResponseData;
//import com.xiaoxun.sdk.IResponseDataCallBack;
//import com.xiaoxun.sdk.XiaoXunNetworkManager;

import com.xiaoxun.sdk.XiaoXunNetworkManager;
import com.xxun.watch.location.XunLocation;

public class StepsCountObserve{

    public StepsCountObserve() {
		initStepService();
		RegisterTargetStepObserve();
		RegisterSleepListStepObserve();
    }

	private void initStepService(){

        String targetLevel = android.provider.Settings.System.getString(XunLocation.getmContext().getContentResolver(),"steps_target_level");
        if(targetLevel== null || targetLevel.equals("")){
            targetLevel = "8000";
        }
        if(targetLevel != null){
            StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.STEPS_TARGET_LEVEL,targetLevel);
        }

        String sleepTime = android.provider.Settings.System.getString(XunLocation.getmContext().getContentResolver(),"SleepList");
        StepsCountUtils.sdcardLog("sleepTime:"+sleepTime);
        Log.e("sleepTime",sleepTime+":stepService");
        if(sleepTime == null || sleepTime.equals("")){
            sleepTime = "{}";
        }
        if(sleepTime != null){
            StepsCountUtils.setValue(XunLocation.getmContext(),Const.PREF_STEPS_FILE,Const.SLEEP_LIST, sleepTime);
        }
    }

	private void RegisterTargetStepObserve(){
        XunLocation.getmContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.getUriFor("steps_target_level"),
                true, mTargetObserver);
    }

    final private ContentObserver mTargetObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            String targetLevel = android.provider.Settings.System.getString(XunLocation.getmContext().getContentResolver(),"steps_target_level");
            Log.d("target change", targetLevel);
            if(targetLevel != null){
				XunLocation.mApp.setValue(Const.STEPS_TARGET_LEVEL,targetLevel);
            }
        }
    };

    private void RegisterSleepListStepObserve(){
		XunLocation.getmContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.getUriFor("SleepList"),
                true, mSleepTimeObserver);
    }

    final private ContentObserver mSleepTimeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            String mSleepList = android.provider.Settings.System.getString(XunLocation.getmContext().getContentResolver(),"SleepList");
            Log.d("target change", mSleepList);
            if(mSleepList != null){
                XunLocation.mApp.setValue(Const.SLEEP_LIST, mSleepList);
            }
        }
    };



}
