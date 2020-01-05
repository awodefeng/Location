package com.xxun.watch.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.Activity;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//import com.xiaoxun.sdk.XiaoXunNetworkManager;
//import com.xiaoxun.sdk.bean.ResponseData;
//import com.xiaoxun.sdk.interfaces.IMessageReceiveListener;
//import com.xiaoxun.sdk.interfaces.IResponseDataCallBack;

//import net.minidev.json.JSONArray;
//import net.minidev.json.JSONObject;

import java.util.List;


public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
//	private XiaoXunNetworkManager mNetService;


/*
	private IMessageReceiveListener mMessageReceiveListener = new IMessageReceiveListener() {
		@Override
		public void onReceive(ResponseData responseData) {
			Log.e(TAG, "onReceive: " + responseData.getResponseCode() + " " + responseData.getResponseData().toJSONString());
			if (responseData.getResponseCode() == ResponseData.RESPONSE_CODE_SOCKET_OK) {
				Toast.makeText(getApplicationContext(), responseData.getResponseData().toJSONString(), Toast.LENGTH_SHORT).show();
				sendBroadcast(new Intent("action.socket.connect"));
				login();
			} else if (responseData.getResponseCode() == ResponseData.RESPONSE_CODE_NOTICE) {
				Toast.makeText(getApplicationContext(), responseData.getResponseData().toJSONString(), Toast.LENGTH_SHORT).show();
				JSONObject pl = (JSONObject) responseData.getResponseData().get("PL");
				if (pl != null) {
					int action = (Integer) pl.get("sub_action");
					if (action == 200) {
						Intent it = new Intent("action.bind.request");
						it.putExtra("data", responseData.getResponseData().toJSONString());
						sendBroadcast(it);
					}
					else if(action == 100){
						if(getPermission() == false)
							return;
						Intent it = new Intent("action.location.single");
						it.putExtra("data", responseData.getResponseData().toJSONString());
						sendBroadcast(it);
					}
					else if(action == 106){
						if(getPermission() == false)
							return;
						Intent it = new Intent("action.location.track");
						it.putExtra("data", responseData.getResponseData().toJSONString());
						sendBroadcast(it);
					}
					else if(action == 108){
						if(getPermission() == false)
							return;
						Intent it = new Intent("action.location.realTimeLocation");
						it.putExtra("data", responseData.getResponseData().toJSONString());
						sendBroadcast(it);
					}
				}
			} else if (responseData.getResponseCode() == ResponseData.RESPONSE_CODE_FILE_UPLOAD_PROGRESS) {
				Intent it = new Intent("action.upload.progress");
				it.putExtra("data", responseData.getResponseData().toJSONString());
				sendBroadcast(it);
			}
		}
	};


*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
/*
		//初始化代码
		mNetService = XiaoXunNetworkManager.getInstance();
		mNetService.setMessageReceiveListener(mMessageReceiveListener);
		mNetService.init(this, new IResponseDataCallBack<ResponseData>() {
			@Override
			public void onSuccess(ResponseData responseData) {
				Log.e(TAG, "onSuccess: " + responseData.getResponseData().toJSONString());
				Toast.makeText(MainActivity.this, responseData.getResponseData().toJSONString(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(int i, String s) {
				Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
			}
		});

		Intent intent = new Intent(MainActivity.this, XunLocation.class);
		startService(intent);


		Button button_start = (Button)findViewById(R.id.button1);
		button_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent it = new Intent("action.location.single.test");
				sendBroadcast(it);
			}
		});
*/		

	}

/*

	private void login(){
		String username = "359076060015580";//imei  请使用自己的imei测试，tks
		String msn = "60015580";//sn  请使用自己的sn测试，tks！
		mNetService.login(username, msn, new IResponseDataCallBack<ResponseData>() {
			@Override
			public void onSuccess(ResponseData responseData) {
				//Log.e(responseData.getResponseCode() + " " + responseData.getResponseData().toJSONString());
				String eid = (String) responseData.getResponseData().get("EID");
				//LogUtil.e(TAG+":Eid"+eid);
				getApplicationContext();
				JSONArray array = (JSONArray) responseData.getResponseData().get("GID");
				try {
					String gid = (String) array.get(0);
					//Log.e(TAG, "onSuccess: " + gid);
				} catch (Exception e) {
				}
			}

			@Override
			public void onError(int i, String s) {
				//Log.e(TAG+i + " " + s);
			}
		});

	}

*/
	private boolean getPermission(){
/*
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
			return false;
		}
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
			return false;
		}
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);
			return false;
		}
*/
		return true;
	}
}
