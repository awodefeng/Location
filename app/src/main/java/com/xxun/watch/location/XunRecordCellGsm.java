package com.xxun.watch.location;

import android.telephony.CellInfoGsm;
//import android.util.Log;

/**
 * Created by xiaoxun on 2017/11/24.
 */

public class XunRecordCellGsm {
	private static final String TAG = "[XunLoc]XunRecordCellGsm";
	public boolean registered;
	public int mcc;
	public int mnc;
	public int lac;
	public int cid;
	public int signalStrength;

	public XunRecordCellGsm(CellInfoGsm gsm){
		registered = gsm.isRegistered();
		mcc = gsm.getCellIdentity().getMcc();
		mnc = gsm.getCellIdentity().getMnc();
		lac = gsm.getCellIdentity().getLac();
		cid = gsm.getCellIdentity().getCid();
		signalStrength = gsm.getCellSignalStrength().getDbm();

		Log.d(TAG, "XunRecordCellGsm1: "+toString());
	}

	public XunRecordCellGsm(XunRecordCellGsm gsm){
		registered = gsm.registered;
		mcc = gsm.mcc;
		mnc = gsm.mnc;
		lac = gsm.lac;
		cid = gsm.cid;
		signalStrength = gsm.signalStrength;
		Log.d(TAG, "XunRecordCellGsm2: "+toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mcc);
		sb.append(",");
		sb.append(mnc);
		sb.append(",");
		sb.append(lac);
		sb.append(",");
		sb.append(cid);
		sb.append(",");
		sb.append(signalStrength);
		return sb.toString();
	}
}
