package com.xxun.watch.stepcountservices;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhangjun5 on 2017/9/15.
 */

public class StepsRanksApp extends Application {
    private final String TAG = "stepsRanksApp";
    private boolean step_half_flag = false ;  //一半的达标的通知标志
    private boolean step_compele_flag = false ;//全部达标的通知标志
    private boolean step_ranks_upload_flag  = false;//休眠时段前上传数据成功标志
    private boolean step_yesterday_upload_flag = false;//昨天及之前的数据上传成功标志

    public void setStep_half_flag(boolean flag){
        step_half_flag = flag;
    }
    public void setStep_compele_flag(boolean flag){
        step_compele_flag = flag;
    }
    public void setStep_ranks_upload_flag(boolean flag){
        step_ranks_upload_flag = flag;
    }
    public void setStep_yesterday_upload_flag(boolean flag){
        step_yesterday_upload_flag = flag;
    }
    public boolean getStep_half_flag(){
        return step_half_flag;
    }
    public boolean getStep_compele_flag(){
        return step_compele_flag;
    }
    public boolean getStep_ranks_upload_flag(){
        return step_ranks_upload_flag;
    }
    public boolean getStep_yesterday_upload_flag(){
        return step_yesterday_upload_flag;
    }

    @Override
    public void onCreate() {
        super.onCreate();
	    initStepsFlag();
		//StepCounterRecverAndSender.getInstance();
    }

    private void initStepsFlag(){
	String stepHalf = StepsCountUtils.getStringValue(getApplicationContext(), Const.STEPS_HALF_FLAG,"0");
	String stepComp = StepsCountUtils.getStringValue(getApplicationContext(), Const.STEPS_COMPELE_FLAG,"0");
	String stepRankupload = StepsCountUtils.getStringValue(getApplicationContext(), Const.STEPS_RANKS_UPLOAD_FLAG,"0");
	String stepYesUpload = StepsCountUtils.getStringValue(getApplicationContext(), Const.STEPS_YESTERDAY_UPLOAD_FLAG,"0");

	step_half_flag = StepsCountUtils.stepStateValue(stepHalf);
	step_compele_flag = StepsCountUtils.stepStateValue(stepComp);
	step_ranks_upload_flag = StepsCountUtils.stepStateValue(stepRankupload);
	step_yesterday_upload_flag = StepsCountUtils.stepStateValue(stepYesUpload);
    }
	public void setValue(String key, String value) {
		final SharedPreferences preferences = getSharedPreferences(Const.PREF_STEPS_FILE, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	public String getStringValue(String key, String defValue) {
		String str = getSharedPreferences(Const.PREF_STEPS_FILE, Context.MODE_PRIVATE )
				.getString(key, defValue);
		return str;
	}
}
