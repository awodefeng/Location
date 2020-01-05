package com.xxun.watch.location;


import android.location.Location;
import android.net.wifi.ScanResult;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
//import android.util.Log;

import java.util.ArrayList;

/**
 * Created by xiaoxun on 2017/9/28.
 */

public class XunLocationPolicyUtil {

	final private static String TAG = "[XunLoc]XunLocationPolicy";
	private static int motion_count;
	private static long steps;
	private static boolean location;
	private static XunLocRecord lastcheckPotion = new XunLocRecord();
	private static ArrayList<CellInfoGsm> lastCellInfoGsms;
	private static ArrayList<CellInfoCdma> lastCellInfoCdmas;
	private static ArrayList<CellInfoWcdma> lastCellInfoWcdmas;
	private static ArrayList<CellInfoLte> lastCellInfoLtes;
	private static ArrayList<ScanResult> lastWifi;
	private static Location lastGps;
	private static long lastTimestamp;

	public boolean checkMotionByCell(ArrayList<CellInfo>cellInfos){
		int samecount = 0;
		for(CellInfo cellInfo: cellInfos){
			if(cellInfo instanceof CellInfoGsm){
				if((lastCellInfoGsms == null)||(lastCellInfoGsms.size() == 0)) {
					continue;
				}else{
					CellInfoGsm cellInfoGsm= (CellInfoGsm)cellInfo;
					for(CellInfoGsm lastGsm: lastCellInfoGsms){
						if(lastGsm.getCellIdentity().getCid() == cellInfoGsm.getCellIdentity().getCid()){
							samecount++;
						}
					}
				}
			}
			if(cellInfo instanceof CellInfoCdma){
				if((lastCellInfoCdmas == null)||(lastCellInfoCdmas.size() == 0)) {
					continue;
				}else{
					CellInfoCdma cellInfoCdma= (CellInfoCdma)cellInfo;
					for(CellInfoCdma lastCdma: lastCellInfoCdmas){
						if(lastCdma.getCellIdentity().getBasestationId() == cellInfoCdma.getCellIdentity().getBasestationId()){
							samecount++;
						}
					}
				}
			}
			if(cellInfo instanceof CellInfoWcdma){
				if((lastCellInfoWcdmas == null)||(lastCellInfoWcdmas.size() == 0)) {
					continue;
				}else{
					CellInfoWcdma cellInfoWcdma= (CellInfoWcdma)cellInfo;
					for(CellInfoWcdma lastWcdma: lastCellInfoWcdmas){
						if(lastWcdma.getCellIdentity().getCid() == cellInfoWcdma.getCellIdentity().getCid()){
							samecount++;
						}
					}
				}
			}
			if(cellInfo instanceof CellInfoLte){
				if((lastCellInfoLtes == null)||(lastCellInfoLtes.size() == 0)) {
					continue;
				}else{
					CellInfoLte cellInfoLte = (CellInfoLte)cellInfo;
					for(CellInfoLte lastLte: lastCellInfoLtes){
						if(lastLte.getCellIdentity().getPci() == cellInfoLte.getCellIdentity().getPci()){
							samecount++;
						}
					}
				}
			}
		}
		if(samecount == 0){
			return true;
		} else{
			return false;
		}

	}


	public static boolean loc_srv_ca_wifi_check_change(ArrayList<ScanResult> last_data, ArrayList<ScanResult> curr_data) {
		int total_wifi_count = 0;
		int same_wifi_count = 0;
		int i,j;
		//
		Log.d(TAG ,"[Loc_policy] loc_srv_ca_wifi_check_change" );
		if((last_data == null)||(curr_data == null)){
			return true;
		}

		if((last_data.size() == 0)||(curr_data.size() == 0)) {
			return true;
		}
		//get max wifi count
		total_wifi_count = last_data.size() ;
		//get same wifi count
		for(ScanResult lastResult : last_data) {
			for(ScanResult currResult : curr_data) {
				if(0 == lastResult.BSSID.compareTo(currResult.BSSID)){
					same_wifi_count++;
				}
			}
		}
		Log.d(TAG , String.format("[Loc_policy]same_wifi_count=%d, total_wifi_count=%d",same_wifi_count, total_wifi_count));
		if((same_wifi_count*100)/total_wifi_count > 20) {
			return false;
		} else {
			return true;
		}
	}

	public boolean checkMotionByWifi(ArrayList<ScanResult> curWifi){
		return loc_srv_ca_wifi_check_change(lastWifi, curWifi);
	}

	public int checkMotionBySensor(){
		return motion_count;
	}

	public double getGpsDistance(Location cur_gps){
		if(lastGps == null){
			return 30000.0;
		}
		return cur_gps.distanceTo(lastGps);
	}

//	public static int getPolicyOnStartScan() {

//	}

//	public static int getPolicyOnWifiScanFinish(ArrayList<ScanResult> scanResults) {

//	}

//	public static int getPolicyOnGpsFinish(Location location){

//	}


}
