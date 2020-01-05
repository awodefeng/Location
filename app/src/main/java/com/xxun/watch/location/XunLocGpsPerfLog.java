package com.xxun.watch.location;
import android.os.Environment;
//import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by xiaoxun on 2017/12/19.
 */

public class XunLocGpsPerfLog {
	private static final String TAG = "[XunLoc]XunLocGpsPerfLog";
	private static final String LOG_NAME = "gps_perf.log";
	private boolean gps_loc_succ;
	private long gps_start_tik;
	private long gps_first_snr_tik;
	private float gps_first_snr;
	private long gps_tri_snr_tik;
	private float[] gps_tri_snr;


	public XunLocGpsPerfLog() {
		gps_loc_succ = false;
		gps_start_tik = System.currentTimeMillis();
		gps_first_snr_tik = 0;
		gps_first_snr = 0;
		gps_tri_snr_tik = 0;
		gps_tri_snr = new float[]{0, 0, 0, 0};
	}

	public void onTimingRecord() {
		if (gps_first_snr_tik == 0) {
			float snr = XunLocGetGpsByPolicy.getSnrBySateIndex(0);
			if (snr > 0) {

				gps_first_snr_tik = System.currentTimeMillis();
				gps_first_snr = snr;
				Log.d(TAG, "onTimingRecord: get first snr in " + gps_first_snr_tik + "snr " + snr);
			}
		}

		if (gps_tri_snr_tik == 0) {
			float snr = XunLocGetGpsByPolicy.getSnrBySateIndex(2);
			if (snr > 0) {
				gps_tri_snr_tik = System.currentTimeMillis();
				gps_tri_snr[0] = XunLocGetGpsByPolicy.getSnrBySateIndex(0);
				gps_tri_snr[1] = XunLocGetGpsByPolicy.getSnrBySateIndex(1);
				gps_tri_snr[2] = XunLocGetGpsByPolicy.getSnrBySateIndex(2);
				Log.d(TAG, "onTimingRecord: get 3rd snr in " + gps_tri_snr_tik + "snr " + gps_tri_snr);
			}
		}
	}

	public void onFinish(boolean succ) {
		if (succ) {
			gps_loc_succ = true;
		}
		long cur_tik = System.currentTimeMillis();
		long first_time = 0;
		long tri_time = 0;
		long open_time = 0;

		open_time = (cur_tik - gps_start_tik) / 1000;
		if (gps_first_snr_tik != 0) {
			first_time = (gps_first_snr_tik - gps_start_tik) / 1000;
		}
		if (gps_tri_snr_tik != 0) {
			tri_time = (gps_tri_snr_tik - gps_start_tik) / 1000;
		}

		StringBuilder sb = new StringBuilder();
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd-HHmmss SSS");


		sb.append(formater.format(new Date(System.currentTimeMillis())));
		sb.append("\t");
		sb.append(gps_loc_succ);
		sb.append("\t");
		sb.append((cur_tik - gps_start_tik) / 1000);
		sb.append("\t");
		sb.append(first_time);
		sb.append("\t");
		sb.append(tri_time);
		sb.append("\t");
		sb.append(gps_first_snr);
		sb.append("\t");
		sb.append(gps_tri_snr[0]);
		sb.append("\t");
		sb.append(gps_tri_snr[1]);
		sb.append("\t");
		sb.append(gps_tri_snr[2]);
		sb.append("\t");
		sb.append(XunLocGetGpsByPolicy.getSnrBySateIndex(0));
		sb.append("\t");
		sb.append(XunLocGetGpsByPolicy.getSnrBySateIndex(1));
		sb.append("\t");
		sb.append(XunLocGetGpsByPolicy.getSnrBySateIndex(2));
		sb.append("\t");
		sb.append(XunLocGetGpsByPolicy.getSnrBySateIndex(3));
		sb.append("\n\r");

		Log.d(TAG, "onFinish: write" + sb.toString());
		writeLog(sb.toString());
	}

	private void writeLog(String log){
		try {
			File file = new File(Environment.getExternalStorageDirectory(), LOG_NAME);
			FileOutputStream fos = new FileOutputStream(file,true);
			fos.write(log.getBytes("UTF-8"));
			fos.close();
			Log.d(TAG, "writeLog: succ");
		} catch(Exception e) {
			e.printStackTrace();
			Log.d(TAG, "writeLog: fail");
		}
	}

}
