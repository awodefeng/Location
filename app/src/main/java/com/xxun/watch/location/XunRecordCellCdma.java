package com.xxun.watch.location;

import android.telephony.CellInfoCdma;
//import android.util.Log;

/**
 * Created by xiaoxun on 2017/11/24.
 */

public class XunRecordCellCdma {
	private static final String TAG = "[XunLoc]XunRecordCellCdma";
	public boolean registered;
	public int networkId;
	public int systemId;
	public int basestationId;
	public int longitude;
	public int latitude;
	public int signalStrength;


	public XunRecordCellCdma(CellInfoCdma cdma){
		registered = cdma.isRegistered();
		networkId = cdma.getCellIdentity().getNetworkId();
		systemId = cdma.getCellIdentity().getSystemId();
		basestationId = cdma.getCellIdentity().getBasestationId();
		longitude = cdma.getCellIdentity().getLongitude();
		latitude = cdma.getCellIdentity().getLatitude();
		signalStrength = cdma.getCellSignalStrength().getDbm();
		Log.d(TAG, "XunRecordCellCdma1: "+toString());
	}

	public XunRecordCellCdma(XunRecordCellCdma cdma){
		registered = cdma.registered;
		networkId = cdma.networkId;
		systemId = cdma.systemId;
		basestationId = cdma.basestationId;
		longitude = cdma.longitude;
		latitude = cdma.latitude;
		signalStrength = cdma.signalStrength;
		Log.d(TAG, "XunRecordCellCdma2: "+toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(networkId);
		sb.append(",");
		sb.append(systemId);
		sb.append(",");
		sb.append(basestationId);
		sb.append(",");
		sb.append(longitude);
		sb.append(",");
		sb.append(latitude);
		sb.append(",");
		sb.append(signalStrength);
		return sb.toString();
	}
}
