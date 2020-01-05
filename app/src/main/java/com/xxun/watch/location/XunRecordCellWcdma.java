package com.xxun.watch.location;

import android.telephony.CellInfo;
import android.telephony.CellInfoWcdma;
//import android.util.Log;

/**
 * Created by xiaoxun on 2017/11/24.
 */

public class XunRecordCellWcdma {
	private static final String TAG = "[XunLoc]XunRecordCellWcdma";
	public boolean registered;
	public int mcc;
	public int mnc;
	public int lac;
	public int cid;
	public int signalStrength;

	public XunRecordCellWcdma(CellInfoWcdma wcdma){
		registered = wcdma.isRegistered();
		mcc = wcdma.getCellIdentity().getMcc();
		mnc = wcdma.getCellIdentity().getMnc();
		lac = wcdma.getCellIdentity().getLac();
		cid = wcdma.getCellIdentity().getCid();
		signalStrength = wcdma.getCellSignalStrength().getDbm();
		Log.d(TAG, "XunRecordCellWcdma1: "+toString());
	}

	public XunRecordCellWcdma(XunRecordCellWcdma wcdma){
		registered = wcdma.registered;
		mcc = wcdma.mcc;
		mnc = wcdma.mnc;
		lac = wcdma.lac;
		cid = wcdma.cid;
		signalStrength = wcdma.signalStrength;
		Log.d(TAG, "XunRecordCellWcdma2: "+toString());
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
