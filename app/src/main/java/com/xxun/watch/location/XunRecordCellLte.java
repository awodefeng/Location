package com.xxun.watch.location;

import android.telephony.CellInfoLte;
//import android.util.Log;

/**
 * Created by xiaoxun on 2017/11/24.
 */

public class XunRecordCellLte {
	private static final String TAG = "[XunLoc]XunRecordCellLte";
	public boolean registered;
	public int mcc;
	public int mnc;
	public int tac;
	public int ci;
	public int pci;
	public int signalStrength;

	public XunRecordCellLte(CellInfoLte lte) {
		registered = lte.isRegistered();
		mcc = lte.getCellIdentity().getMcc();
		mnc = lte.getCellIdentity().getMnc();
		tac = lte.getCellIdentity().getTac();
		ci = lte.getCellIdentity().getCi();
		pci = lte.getCellIdentity().getPci();
		signalStrength = lte.getCellSignalStrength().getDbm();
		Log.d(TAG, "XunRecordCellLte: "+toString()+",pci="+pci);
	}

	public XunRecordCellLte(XunRecordCellLte lte){
		registered = lte.registered;
		mcc = lte.mcc;
		mnc = lte.mnc;
		tac = lte.tac;
		ci = lte.ci;
		pci = lte.pci;
		signalStrength = lte.signalStrength;
		Log.d(TAG, "XunRecordCellLte: "+toString());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mcc);
		sb.append(",");
		sb.append(mnc);
		sb.append(",");
		sb.append(tac);
		sb.append(",");
		sb.append(ci);
		sb.append(",");
		sb.append(signalStrength);
		return sb.toString();
	}

}
