package com.xxun.watch.location;

import android.location.Location;
import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by xiaoxun on 2017/11/10.
 */

public class XunLocGetPos {
	private XunLocRecord xunLocRecord;

	public XunLocGetPos(){
		xunLocRecord = new XunLocRecord();
		XunLocWifiScan.start(new XunLocWifiScan.WifiScanFinished() {
			@Override
			public void onWifiScanFinished(List<ScanResult> scanResults) {
				xunLocRecord.setWifiRecord(scanResults);
				xunLocRecord.setCellRecord();

				Location lastGps = XunLocGpsCtrl.getLastSuccPosByInterval(60);
				if(lastGps != null){
					xunLocRecord.setGpsRecord(lastGps);
				}
				xunLocRecord.compeleteInfoData();
				xunLocRecord.setFeedback(1);
				XunLocPosUploadPolicy.getInstance().feedbackLocUpload(xunLocRecord);
			}
		});
	}
}
