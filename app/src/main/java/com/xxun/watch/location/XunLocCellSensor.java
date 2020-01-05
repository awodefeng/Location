package com.xxun.watch.location;

import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
//import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxun on 2017/11/21.
 */

public class XunLocCellSensor extends XunRecordCell{
	private static final String TAG = "[XunLoc]XunLocCellSensor";
	private static long tik = 0; 



	public void onCellChange(){
		Log.d(TAG, "onCellChange: ");
		//cellStatusLog();

		if(isAvaliable() == false){
			return;
		}

		if((System.currentTimeMillis() - tik) < 60000){
			return;
		}else{
			tik = System.currentTimeMillis();
		}

		if(XunFlightModeModeSwitcher.getInstance().isAirPlaneModeOn()){
			return;
		}

		TelephonyManager tm = XunLocation.getmTelephonyManager();
		List<CellInfo> cellInfoList = tm.getAllCellInfo();

		if ((cellInfoList != null) && (cellInfoList.size() > 0)) {
			for (CellInfo cellInfo : cellInfoList) {
				Log.d(TAG, "cellStatusLog: "+cellInfo.toString());
			}
		}		

		
		int type;
		if ((cellInfoList != null) && (cellInfoList.size() > 0)) {
			if(cellInfoList.get(0) instanceof CellInfoGsm){
				type = TYPE_GSM;
			}else if(cellInfoList.get(0) instanceof CellInfoCdma){
				type = TYPE_CDMA;
			}else if(cellInfoList.get(0) instanceof CellInfoWcdma){
				type = TYPE_WCDMA;
			}else if(cellInfoList.get(0) instanceof CellInfoLte){
				type = TYPE_LTE;
			}else{
				return;
			}

			if(getCellType() != type){
				Log.d(TAG, "onCellChange: insert new type cell="+String.valueOf(type));
				ArrayList<Integer> cidList = new ArrayList<Integer>();
				for(CellInfo cellInfo: cellInfoList){
					try{
						switch (type){
							case TYPE_GSM:
								CellInfoGsm gsm = (CellInfoGsm)cellInfo;
								if(gsmCellList == null) {
									gsmCellList = new ArrayList<XunRecordCellGsm>();
								}
								if(cidList.contains(Integer.valueOf(gsm.getCellIdentity().getCid()))) {
									cidList.add(Integer.valueOf(gsm.getCellIdentity().getCid()));
									gsmCellList.add(new XunRecordCellGsm(gsm));
								}
								Log.d(TAG, "onCellChange: insert add"+gsmCellList.toString());
								break;
							case TYPE_CDMA:
								CellInfoCdma cdma = (CellInfoCdma)cellInfo;
								if(cdmaCellList == null) {
									cdmaCellList = new ArrayList<XunRecordCellCdma>();
								}
								if(cidList.contains(Integer.valueOf(cdma.getCellIdentity().getBasestationId()))) {
									cidList.add(Integer.valueOf(cdma.getCellIdentity().getBasestationId()));
									cdmaCellList.add(new XunRecordCellCdma(cdma));
								}
								Log.d(TAG, "onCellChange: cdma insert"+cdmaCellList.toString());
								break;
							case TYPE_WCDMA:
								CellInfoWcdma wcdma = (CellInfoWcdma)cellInfo;
								if(wcdmaCellList == null) {
									wcdmaCellList = new ArrayList<XunRecordCellWcdma>();
								}
								if(cidList.contains(Integer.valueOf(wcdma.getCellIdentity().getCid()))) {
									cidList.add(Integer.valueOf(wcdma.getCellIdentity().getCid()));
									wcdmaCellList.add(new XunRecordCellWcdma(wcdma));
								}
								Log.d(TAG, "onCellChange: wcdma insert"+wcdmaCellList.toString());
								break;
							case TYPE_LTE:
								CellInfoLte lte = (CellInfoLte)cellInfo;
								if(lteCellList == null) {
									lteCellList = new ArrayList<XunRecordCellLte>();
									lteCellList.add(new XunRecordCellLte(lte));
									cidList.add(Integer.valueOf(lte.getCellIdentity().getCi()));
								}
								Log.d(TAG, "onCellChange: lte insert"+lteCellList.toString());
								break;
						}
					}catch (Exception e){
						Log.d(TAG, "onCellChange: "+e.toString());
					}
				}
			}
		}
	}

	public void clearAndRecordNewCell(XunRecordCell cellRecord){
		Log.d(TAG, "clearAndRecordNewCell: ");
		super.setCellInfo();
	}


	private void cellStatusLog(){
		TelephonyManager tm = XunLocation.getmTelephonyManager();
		List<CellInfo> cellInfoList = tm.getAllCellInfo();
		if ((cellInfoList != null) && (cellInfoList.size() > 0)) {
			for (CellInfo cellInfo : cellInfoList) {
				Log.d(TAG, "cellStatusLog: "+cellInfo.toString());
			}
		}
	}

}
