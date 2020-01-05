package com.xxun.watch.location;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
//import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by xiaoxun on 2017/9/28.
 */


public class XunSensorProc {
	final private static String TAG = "[XunLoc]XunSensorProc";
	public boolean logStart = false;
	private static final String LOG_NAME = "sensor.zip";
	private	StringBuilder sensor_sb = new StringBuilder();
	private static final float sw703coefficient = 0.019153613f;//9.80665*4*2/4096.0;
	private static final String step_interrupt_flag = "sys/bus/platform/drivers/gsensor/step_interrupt_flag";
	private static final String step_interrupt_count = "sys/bus/platform/drivers/gsensor/step_interrupt_count";
	private onStepInterrupt stepInterrupt = null;
	private static XunSensorProc instance = null;
	private static int motionTabIndex = 0;
	private static int motionTab[] = new int[25];
	private float mx = 0.0f;
	private float my = 0.0f;
	private float mz = 0.0f;
	private long timestamp = 0;
	private double simp_x = 0.0;
	private double simp_y = 0.0;
	private double simp_z = 0.0;
	private int simp_count = 0;
	final private static int motionProdic = 120* 1000;
	public int region = 0;
	private Handler handler;
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			//onMotionCheck();
			handler.postDelayed(this, motionProdic);
		}
	};

	public long stepcounter = 0;
	private Sensor acc_sensor;
	private Sensor step_sensor;
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor == acc_sensor) {
				long timeDiff = event.timestamp - timestamp;

				float x, y, z;
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];
				//Log.d(TAG, "onSensorChanged acc_sesor: x="+x+"y="+y+"z="+z+"timeDiff="+timeDiff);
//				if(logStart) {
//					SensorLog(x, y, z);
//				}
				if ((timeDiff<0)||(timeDiff>1000000000)){
					if(simp_count > 0) {
						motionCheckByGsensor( (float)simp_x/simp_count,  (float)simp_y/simp_count ,  (float)simp_z/simp_count);
						mx =  (float)simp_x/simp_count;
						my =  (float)simp_y/simp_count;
						mz =  (float)simp_z/simp_count;
						simp_count = 0;
						simp_x = 0;
						simp_y = 0;
						simp_z = 0;
					}
					timestamp = event.timestamp;
				}
				insertAverageData(x, y, z);

			} else if (event.sensor == step_sensor) {
				float steps = event.values[0];
				if(steps != stepcounter) {
					Log.d(TAG, "onSensorChanged:step_sensor"+steps);
					stepcounter = (long) steps;
				}
			}
		};

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};


	private void startSensor() {
		Log.d(TAG, "startSensor: "+XunLocation.getmSensorManager().getSensorList(Sensor.TYPE_ALL));

		step_sensor = XunLocation.getmSensorManager().getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
		Log.d(TAG, "startSensor: " + step_sensor.toString());
		acc_sensor = XunLocation.getmSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Log.d(TAG, "startSensor: " + acc_sensor.toString());
		XunLocation.getmSensorManager().registerListener(sensorEventListener, acc_sensor, SensorManager.SENSOR_DELAY_NORMAL);
		XunLocation.getmSensorManager().registerListener(sensorEventListener, step_sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}


	private void stopSensor() {
		Log.d(TAG, "stopSensor: ");
		XunLocation.getmSensorManager().unregisterListener(sensorEventListener, acc_sensor);
		XunLocation.getmSensorManager().unregisterListener(sensorEventListener, step_sensor);
	}

	public XunSensorProc() {
		Log.d(TAG, "XunSensorProc: init");
		startSensor();
		onMotionCheck();
		closeStepInterrupt();

		for (int i = 0; i < 25; i++) {
			motionTab[i] = 255;
		}
		handler = new Handler();
		handler.postDelayed(runnable, motionProdic);
	}


	public static XunSensorProc getInstance() {
		if (instance == null) {
			instance = new XunSensorProc();
		}
		return instance;
	}

	private void onMotionCheck() {

		//writeSysFile(step_interrupt_flag, 1);

		//Log.d(TAG, "onMotionCheck: "+read(step_interrupt_flag));
		//writeSysFile(step_interrupt_flag, 0);
		//Log.d(TAG, "onMotionCheck: "+read(step_interrupt_flag));


		//Log.d(TAG, "onMotionCheck: "+read(step_interrupt_count));
		//writeSysFile(step_interrupt_count, 1);
		//Log.d(TAG, "onMotionCheck: "+read(step_interrupt_count));

	}

	public String read(String sys_path){
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec("cat " + sys_path); // 此处进行读操作
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line ;
			while (null != (line = br.readLine())) {
				Log.d(TAG, "read data ---> " + line);
				return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "*** ERROR *** Here is what I know: " + e.getMessage());
		}
		return null;
	}

	public void writeSysFile(String sys_path, int data){

		try {
			BufferedWriter bufWriter = null;
			bufWriter = new BufferedWriter(new FileWriter(sys_path));
			bufWriter.write(Integer.toString(data));  // 写操作
			bufWriter.close();
			Log.d(TAG, "writeSysFile: succ");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG,"can't write the " + sys_path);
		}

/*
		Process p = null;
		DataOutputStream os = null;
		try {
			p = Runtime.getRuntime().exec("sh");
			os = new DataOutputStream(p.getOutputStream());
			String cmd = "echo "+ data +" > "+sys_path + "\n";
			Log.d(TAG, "writeSysFile: "+cmd);
			os.writeBytes(cmd);
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, " can't write " + sys_path+e.getMessage());
		} finally {
			if(p != null){
				p.destroy();
			}
			if(os != null){
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		*/
	}

	private void motionCheckByGsensor(float x, float y, float z) {
		//Log.d(TAG, "motionCheckByGsensor: diff="+ Math.sqrt(Math.pow(mx - x, 2) + Math.pow(my - y, 2) + Math.pow(mz - z, 2)));
		//double diff = Math.sqrt(Math.pow(mx - x, 2) + Math.pow(my - y, 2) + Math.pow(mz - z, 2));
		double diff = Math.pow(mx - x, 2) + Math.pow(my - y, 2) + Math.pow(mz - z, 2);
		//
		if(diff > Math.pow(0.5,2)){
			Log.d(TAG, "motionCheckByGsensor: 2 = " + String.valueOf(diff));
			XunLocPolicyLastRecord.getInstance().setMotion();
			XunLocPolicyLastRecord.getInstance().setPeriodMotionCounter();
			XunLocPolicyLastRecord.getInstance().setFlightMotionCounter();
		} else if (diff >  Math.pow(0.05, 2)) {
			Log.d(TAG, "motionCheckByGsensor: 1 = " + String.valueOf(diff));
			XunLocPolicyLastRecord.getInstance().setMotion();
			XunLocPolicyLastRecord.getInstance().setPeriodMotionCounter();
		}
	}

	public int getMotioncountByMinuter(int count) {
		int ret = 0;
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("getMotioncountByMinuter ");
		stringBuilder.append("count=");
		stringBuilder.append(count);

		if (count >= 25) {
			count = 25;
		}

		for (int i = 0; i < count; i++) {
			ret += motionTab[(motionTabIndex - i) % 25];
			stringBuilder.append((motionTabIndex - i) % 25);
			stringBuilder.append(',');
			stringBuilder.append(motionTab[(motionTabIndex - i) % 25]);
			stringBuilder.append(' ');
		}

		stringBuilder.append("ret=");
		stringBuilder.append(ret);

		Log.d(TAG, stringBuilder.toString());
		return ret;
	}

	public void onStepIntent(){
		Log.d(TAG, "onStepIntent: ");
		if(stepInterrupt != null){
			stepInterrupt.run();
		}
	}

	public interface onStepInterrupt{
		void run();
	}

	public void openStepInterrupt(onStepInterrupt interrupt){
		Log.d(TAG, "openStepInterrupt: ");
		if(interrupt != null) {
			stepInterrupt = interrupt;
			writeSysFile(step_interrupt_flag, 1);
			writeSysFile(step_interrupt_flag, 0);
			writeSysFile(step_interrupt_count, 3);
		}
	}

	public void closeStepInterrupt(){
		Log.d(TAG, "closeStepInterrupt: ");
		stepInterrupt = null;
		writeSysFile(step_interrupt_flag, 0);
		writeSysFile(step_interrupt_flag, 1);
		writeSysFile(step_interrupt_count, 3);
	}

	private void insertAverageData(float x, float y,  float z){
		if(simp_count < 25){
			simp_x += x;
			simp_y += y;
			simp_z += z;
			simp_count ++;
			//Log.d(TAG, "insertAverageData: x="+simp_x+"y="+simp_y+"z="+simp_z+"count="+simp_count);
		}
	}

	private  void SensorLog(double simp_x, double simp_y, double simp_z){
		SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd-HHmmss SSS");
		sensor_sb.append(formater.format(new Date(System.currentTimeMillis())));
		sensor_sb.append(",");
		sensor_sb.append(new Double(simp_x/sw703coefficient).intValue());
		sensor_sb.append(",");
		sensor_sb.append(new Double(simp_y/sw703coefficient).intValue());
		sensor_sb.append(",");
		sensor_sb.append(new Double(simp_z/sw703coefficient).intValue());
		sensor_sb.append("\r\n");

		if(sensor_sb.length()>1024*8){
			writeLog(sensor_sb.toString());
			sensor_sb.setLength(0);
		}
	}

	private void writeLog(String log){
		try {
			File file = new File(Environment.getExternalStorageDirectory(), LOG_NAME);
			FileOutputStream fos = new FileOutputStream(file,true);
			GZIPOutputStream gos = new GZIPOutputStream(fos,true);
			gos.write(log.getBytes("UTF-8"));
			gos.close();
			fos.close();
			Log.d(TAG, "writeLog: succ");
		} catch(Exception e) {
			e.printStackTrace();
			Log.d(TAG, "writeLog: fail");		
		}
	}
}
