package com.xxun.watch.location;

import android.os.PowerManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
//import android.util.Log;

import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxun on 2017/9/29.
 */

public class XunRecordCell {
	public static final String TAG = "[XunLoc]XunRecordCell";
	public static final int TYPE_UNKNOW = 0;
	public static final int TYPE_GSM = 1;
	public static final int TYPE_CDMA = 2;
	public static final int TYPE_LTE = 3;
	public static final int TYPE_WCDMA = 4;

	private boolean avaliable;
	protected  ArrayList<XunRecordCellGsm> gsmCellList;
	protected  ArrayList<XunRecordCellCdma> cdmaCellList;
	protected  ArrayList<XunRecordCellWcdma> wcdmaCellList;
	protected  ArrayList<XunRecordCellLte> lteCellList;
	private int netWork;
	private int type;
	private int region;


	public XunRecordCell(){
		Log.d(TAG, "XunRecordCell: ");
		clearAllInfos();
	}

	public void setCellInfo(){
		Log.d(TAG, "setCellInfo: ");
		TelephonyManager tm =  XunLocation.getmTelephonyManager();
		setCellInfo(tm.getAllCellInfo(), tm.getNetworkType());
	}

	public int getCellType() {
		Log.d(TAG, "getCellType: "+String.valueOf(type));
		return type;
	}

	private void clearAllInfos(){
		Log.d(TAG, "clearAllInfos: ");
		avaliable = false;
		netWork = TelephonyManager.NETWORK_TYPE_UNKNOWN;
		gsmCellList = null;
		cdmaCellList = null;
		wcdmaCellList = null;
		lteCellList = null;
		type = TYPE_UNKNOW;
		region = 0;
	}

	public void setCellInfo(List<CellInfo> infos, int netWorkType){
		Log.d(TAG, "setCellInfo: ");
		clearAllInfos();
		if(infos == null){
			Log.d(TAG, "setCellInfo: infos null");
			return;
		}

		if(infos.size() == 0){
			Log.d(TAG, "setCellInfo: infos size 0");
			return;
		}

		netWork = netWorkType;
		Log.d(TAG, "setCellInfo: network="+String.valueOf(netWork));
		avaliable = true;
		ArrayList<Integer> cidList = new ArrayList<Integer>();

		for(CellInfo cellInfo:infos){
			if(cellInfo instanceof CellInfoGsm){
				CellInfoGsm gsm = (CellInfoGsm)cellInfo;
				if(cidList.contains(Integer.valueOf(gsm.getCellIdentity().getCid())) == false){
					if(gsmCellList == null){
						gsmCellList = new ArrayList<XunRecordCellGsm>();
						region = gsm.getCellIdentity().getMcc();
						Log.d(TAG, "setCellInfo: gsm region"+String.valueOf(region));
					}
					gsmCellList.add(new XunRecordCellGsm(gsm));
					cidList.add(Integer.valueOf(gsm.getCellIdentity().getCid()));
					type = TYPE_GSM;
				}
			}else if(cellInfo instanceof CellInfoCdma){
				CellInfoCdma cdma = (CellInfoCdma)cellInfo;
				if(cidList.contains(Integer.valueOf(cdma.getCellIdentity().getBasestationId())) == false){
					if(cdmaCellList == null){
						cdmaCellList = new ArrayList<XunRecordCellCdma>();
					}
					cdmaCellList.add(new XunRecordCellCdma(cdma));
					cidList.add(Integer.valueOf(cdma.getCellIdentity().getBasestationId()));
					type = TYPE_CDMA;
				}
			}else if(cellInfo instanceof CellInfoWcdma){
				CellInfoWcdma wcdma = (CellInfoWcdma)cellInfo;
				if(cidList.contains(Integer.valueOf(wcdma.getCellIdentity().getCid())) == false){
					if(wcdmaCellList == null){
						wcdmaCellList = new ArrayList<XunRecordCellWcdma>();
						region = wcdma.getCellIdentity().getMcc();
					}
					wcdmaCellList.add(new XunRecordCellWcdma(wcdma));
					cidList.add(Integer.valueOf(wcdma.getCellIdentity().getCid()));
					type = TYPE_WCDMA;
				}
			}else if(cellInfo instanceof CellInfoLte){
				CellInfoLte lte = (CellInfoLte)cellInfo;
				if(cidList.contains(Integer.valueOf(lte.getCellIdentity().getCi())) == false){
					if(lteCellList == null){
						lteCellList = new ArrayList<XunRecordCellLte>();
						region = lte.getCellIdentity().getMcc();
					}
					lteCellList.add(new XunRecordCellLte(lte));
					cidList.add(Integer.valueOf(lte.getCellIdentity().getCi()));
					type = TYPE_LTE;
				}
			}
		}
		if(netWork == 0){
			Log.d(TAG, "setCellInfo: netWork unknow recheck netWork by cell instance");
			switch (type) {
				case TYPE_GSM:
					netWork = TelephonyManager.NETWORK_TYPE_GPRS;
					break;
				case TYPE_CDMA:
					netWork = TelephonyManager.NETWORK_TYPE_CDMA;
					break;
				case TYPE_WCDMA:
					netWork = TelephonyManager.NETWORK_TYPE_UMTS;
					break;
				case TYPE_LTE:
					netWork = TelephonyManager.NETWORK_TYPE_LTE;
					break;
				default:
					netWork = TelephonyManager.NETWORK_TYPE_GPRS;
			}
		}
	}


	public void setCellInfo(XunRecordCell recordCell){
		clearAllInfos();
		if(recordCell == null){
			Log.d(TAG, "setCellInfo: recordCell null");
			return;
		}

		if(recordCell.isAvaliable() == false){
			Log.d(TAG, "setCellInfo: recordCell isAvaliable = false");
			return;
		}

		netWork = recordCell.getNetWork();
		Log.d(TAG, "setCellInfo: "+String.valueOf(netWork));
		avaliable = true;
		type = recordCell.getCellType();
		switch (type) {
			case TYPE_GSM:
				if(recordCell.getGsmCellInfo() == null){
					Log.d(TAG, "setCellInfo: error getGsmCellInfo");
					break;
				}
				if (recordCell.getGsmCellInfo().size() > 0) {
					for (XunRecordCellGsm gsm : recordCell.getGsmCellInfo()) {
						if (gsmCellList == null) {
							gsmCellList = new ArrayList<XunRecordCellGsm>();
							region = gsm.mcc;
						}
						gsmCellList.add(new XunRecordCellGsm(gsm));
					}
				}
				break;
			case TYPE_CDMA:
				if(recordCell.getCdmaCellInfo() == null){
					Log.d(TAG, "setCellInfo: error getCdmaCellInfo");
					break;
				}
				if (recordCell.getCdmaCellInfo().size() > 0) {
					for (XunRecordCellCdma cdma : recordCell.getCdmaCellInfo()) {
						if (cdmaCellList == null) {
							cdmaCellList = new ArrayList<XunRecordCellCdma>();
						}
						cdmaCellList.add(new XunRecordCellCdma(cdma));
					}
				}
				break;
			case TYPE_WCDMA:
				if(recordCell.getWcdmaCellInfo() == null){
					Log.d(TAG, "setCellInfo: error getWcdmaCellInfo");
					break;
				}
				if (recordCell.getWcdmaCellInfo().size() > 0) {
					for (XunRecordCellWcdma wcdma : recordCell.getWcdmaCellInfo()) {
						if (wcdmaCellList == null) {
							wcdmaCellList = new ArrayList<XunRecordCellWcdma>();
							region = wcdma.mcc;
						}
						wcdmaCellList.add(new XunRecordCellWcdma(wcdma));
					}
				}
				break;
			case TYPE_LTE:
				if(recordCell.getLteCellInfo() == null){
					Log.d(TAG, "setCellInfo: error getLteCellInfo");
					break;
				}
				if (recordCell.getLteCellInfo().size() > 0) {
					for (XunRecordCellLte lte : recordCell.getLteCellInfo()) {
						if (lteCellList == null) {
							lteCellList = new ArrayList<XunRecordCellLte>();
							region = lte.mcc;
						}
						lteCellList.add(new XunRecordCellLte(lte));
					}
				}
				break;
			default:
				break;
		}
	}

	public ArrayList<XunRecordCellGsm> getGsmCellInfo(){
		return gsmCellList;
	}

	public ArrayList<XunRecordCellCdma> getCdmaCellInfo() {
		return cdmaCellList;
	}

	public ArrayList<XunRecordCellWcdma> getWcdmaCellInfo() {
		return wcdmaCellList;
	}

	public ArrayList<XunRecordCellLte> getLteCellInfo() {
		return lteCellList;
	}

	public int getNetWork(){
		return netWork;
	}

	public boolean isAvaliable(){
		return avaliable;
	}

	public int getRegion(){
		return region;
	}

	public void addInfoToJson(JSONObject pl){
		pl.put("network",formatNetWorkString());
		pl.put("bts", formatBtsString());
		pl.put("nearbts", formatNbtsString());
		pl.put("cdma", isCdma());
	}

	public int isCdma(){
		Log.d(TAG, "isCdma: "+String.valueOf(type));
		if(type == TYPE_CDMA) {
			return 1;
		}else{
			return 0;
		}
	}

	public String formatNetWorkString(){
		String networkString = new String(" ");
		if(avaliable){
			switch (netWork){
				/** Current network is GPRS */
				case TelephonyManager.NETWORK_TYPE_GPRS:
					networkString = new String("GPRS") ;
					break;

				/** Current network is EDGE */
				case TelephonyManager.NETWORK_TYPE_EDGE :
					networkString = new String("EDGE") ;
					break;

				/** Current network is UMTS */
				case TelephonyManager.NETWORK_TYPE_UMTS:
					networkString = new String("UMTS") ;
					break;

				/** Current network is CDMA: Either IS95A or IS95B*/
				case TelephonyManager.NETWORK_TYPE_CDMA:
					networkString = new String("CDMA") ;
					break;
				/** Current network is EVDO revision 0*/
				case TelephonyManager.NETWORK_TYPE_EVDO_0 :
					networkString = new String("EVDO_0") ;
					break;

				/** Current network is EVDO revision A*/
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					networkString = new String("EVDO_A") ;
					break;

				/** Current network is 1xRTT*/
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					networkString = new String("1xRTT") ;
					break;
				/** Current network is HSDPA */
				case TelephonyManager.NETWORK_TYPE_HSDPA:
					networkString = new String("HSDPA") ;
					break;

				/** Current network is HSUPA */
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					networkString = new String("HSUPA") ;
					break;

				/** Current network is HSPA */
				case TelephonyManager.NETWORK_TYPE_HSPA:
					networkString = new String("HSPA") ;
					break;
				/** Current network is iDen */
				case TelephonyManager.NETWORK_TYPE_IDEN:
					networkString = new String("IDEN") ;
					break;

				/** Current network is EVDO revision B*/
				case TelephonyManager.NETWORK_TYPE_EVDO_B:
					networkString = new String("EVDO_B") ;
					break;

				/** Current network is LTE */
				case TelephonyManager.NETWORK_TYPE_LTE :
					networkString = new String("LTE") ;
					break;

				/** Current network is eHRPD */
				case TelephonyManager.NETWORK_TYPE_EHRPD :
					networkString = new String("EHRPD") ;
					break;

				/** Current network is HSPA+ */
				case TelephonyManager.NETWORK_TYPE_HSPAP:
					networkString = new String("HSPA+") ;
					break;

			}
		}
		return networkString;
	}

	public String formatBtsString() {
		StringBuilder sb = new StringBuilder();
		if (avaliable) {
			switch (type) {
				case TYPE_GSM:
					if ((gsmCellList != null) && (gsmCellList.size() > 0)) {
						for (XunRecordCellGsm gsm : gsmCellList) {
							if (gsm.registered == true) {
								sb.append(gsm.mcc);
								sb.append(",");
								sb.append(gsm.mnc);
								sb.append(",");
								sb.append(gsm.lac);
								sb.append(",");
								sb.append(gsm.cid);
								sb.append(",");
								sb.append(gsm.signalStrength);
								break;
							}
						}
					}
					break;
				case TYPE_CDMA:
					if ((cdmaCellList != null) && (cdmaCellList.size() > 0)) {
						for (XunRecordCellCdma cdma : cdmaCellList) {
							if (cdma.registered == true) {
								sb.append(cdma.systemId);
								sb.append(",");
								sb.append(cdma.networkId);
								sb.append(",");
								sb.append(cdma.basestationId);
								sb.append(",");
								sb.append(cdma.longitude);
								sb.append(",");
								sb.append(cdma.latitude);
								sb.append(",");
								sb.append(cdma.signalStrength);
								break;
							}
						}
					}
					break;
				case TYPE_WCDMA:
					if ((wcdmaCellList != null) && (wcdmaCellList.size() > 0)) {
						for (XunRecordCellWcdma wcdma : wcdmaCellList) {
							if (wcdma.registered == true) {
								sb.append(wcdma.mcc);
								sb.append(",");
								sb.append(wcdma.mnc);
								sb.append(",");
								sb.append(wcdma.lac);
								sb.append(",");
								sb.append(wcdma.cid);
								sb.append(",");
								sb.append(wcdma.signalStrength);
								break;
							}
						}
					}
					break;
				case TYPE_LTE:
					if ((lteCellList != null) && (lteCellList.size() > 0)) {
						for (XunRecordCellLte lte : lteCellList) {
							if (lte.registered == true) {
								sb.append(lte.mcc);
								sb.append(",");
								sb.append(lte.mnc);
								sb.append(",");
								sb.append(lte.tac);
								sb.append(",");
								sb.append(lte.ci);
								sb.append(",");
								sb.append(lte.signalStrength);
								break;
							}
						}
					}
					break;
				default:
					break;
			}
		}
		return sb.toString();
	}

	public String formatNbtsString() {
		StringBuilder sb = new StringBuilder();
		if(avaliable) {
			switch (type){
				case TYPE_GSM:
					if((gsmCellList != null)&&(gsmCellList.size()>2)){
						for(XunRecordCellGsm gsm: gsmCellList){
							if(sb.length()>0){
								sb.append("|");
							}
							if(gsm.registered == false) {
								sb.append(gsm.mcc);
								sb.append(",");
								sb.append(gsm.mnc);
								sb.append(",");
								sb.append(gsm.lac);
								sb.append(",");
								sb.append(gsm.cid);
								sb.append(",");
								sb.append(gsm.signalStrength);
							}
						}
					}
					break;
				case TYPE_CDMA:
					if((cdmaCellList != null)&&(cdmaCellList.size()>2)){
						for(XunRecordCellCdma cdma: cdmaCellList){
							if(sb.length()>0){
								sb.append("|");
							}
							if(cdma.registered == false) {
								sb.append(cdma.systemId);
								sb.append(",");
								sb.append(cdma.networkId);
								sb.append(",");
								sb.append(cdma.basestationId);
								sb.append(",");
								sb.append(cdma.longitude);
								sb.append(",");
								sb.append(cdma.latitude);
								sb.append(",");
								sb.append(cdma.signalStrength);
							}
						}
					}
					break;
				case TYPE_WCDMA:
					if((wcdmaCellList != null)&&(wcdmaCellList.size()>2)){
						for(XunRecordCellWcdma wcdma: wcdmaCellList){
							if(sb.length()>0){
								sb.append("|");
							}
							if(wcdma.registered == false) {
								sb.append(wcdma.mcc);
								sb.append(",");
								sb.append(wcdma.mnc);
								sb.append(",");
								sb.append(wcdma.lac);
								sb.append(",");
								sb.append(wcdma.cid);
								sb.append(",");
								sb.append(wcdma.signalStrength);
							}
						}
					}
					break;
				case TYPE_LTE:
					break;
/*
					if((lteCellList != null)&&(lteCellList.size()>2)){
						for(XunRecordCellLte lte: lteCellList){
							if(sb.length()>0){
								sb.append("|");
							}
							if(lte.registered == false) {
								sb.append(lte.mcc);
								sb.append(",");
								sb.append(lte.mnc);
								sb.append(",");
								sb.append(lte.tac);
								sb.append(",");
								sb.append(lte.ci);
								sb.append(",");
								sb.append(lte.signalStrength);
							}
						}
					}
					break;
*/
			}

		}
		return sb.toString();
	}
}
