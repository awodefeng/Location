package com.xxun.watch.stepcountservices;

import android.hardware.Sensor;

/**
 * Created by zhangjun5 on 2017/10/25.
 */

public class Const {
    public static final String ACTION_HEAD = "com.xxun.watch.stepcountservices.";
    public static final String PREF_STEPS_FILE = "pref_steps_file";

    //系统计步累加值
    public static final String SHARE_PREF_PHONT_STEPS = "share_pref_phone_steps"; //保存的文件名
    public static final String SHARE_PREF_OLDDATE_STEPS = "share_pref_olddate_steps"; //保存历史计步数据
    public static final String SHARE_PREF_PHONT_STEPS_NEW = "share_pref_phone_steps_new";//计步初始值存储

    //计步发出的广播
    public static final String ACTION_BROAST_SENSOR_STEPS = ACTION_HEAD + "action.broast.sensor.steps";
    public static final String STEPS_COUNT_ALARM_FLAGS= ACTION_HEAD + "steps.count.alarm.flags";
    public static final String ACTION_BROAST_BOOT_START = "android.intent.action.BOOT_COMPLETED";
    public static final String ACTION_BROAST_CURRENT_STEP = "brocast_action_step_current_noti";//ACTION_HEAD + "action.broast.current.step";

    //设置数据
    public static final String STEPS_TARGET_LEVEL = "steps_target_level";
    public static final String SLEEP_LIST = "SleepList";
    public static final String STEPS_HALF_FLAG = "step_half_flag";
    public static final String STEPS_COMPELE_FLAG = "step_compele_flag";
    public static final String STEPS_RANKS_UPLOAD_FLAG = "step_ranks_upload_flag";
    public static final String STEPS_YESTERDAY_UPLOAD_FLAG = "step_yesterday_upload_flag";
	public static final String DEVICE_BIND_FLAG = "device_bind_flag";

    //发送数据的key
    public static final String KEY_NAME_KEYS = "Keys";
    public static final String KEY_NAME_EID = "EID";
    public static final String KEY_NAME_SID = "SID";
    public static final String KEY_NAME_TGID = "TGID";
    public static final String KEY_NAME_KEY = "Key";
    public static final String KEY_NAME_TYPE = "Type";
    public static final String KEY_NAME_CONTENT = "Content";
    public static final String KEY_NAME_DURATION = "Duration";
    public static final String KEY_NAME_CUR_STEPS = "cur_steps";

    //数据的消息号
    public static final int CID_MAPGET_MGET = 60051;
    public static final int CID_STEPS_UPLOAD = 70081;
    public static final int CID_STEPS_YESTERDAY_UPLOAD = 40111;
    public static final int CID_STEPS_VALUE = 60061;


    public static final long YMD_REVERSED_MASK_8 = 99999999;
    public static final long HMSS_REVERSED_MASK_9 = 999999999;

}
