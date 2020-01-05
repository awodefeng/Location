package com.xxun.watch.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;

/**
 * Created by xiaoxun on 2017/10/25.
 */

public class XunLocBootBroadcastReceiver extends BroadcastReceiver {
	private static final String TAG = "[XunLoc]XunLocBootBroadcastReceiver";	
	//private static final String KEY_BOOT_UP = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: ");
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Log.d(TAG, "onReceive: ACTION_BOOT_COMPLETED");
			Intent serviceIntent = new Intent(context, XunLocation.class);
			context.startService(serviceIntent);
		}		
	}
}
