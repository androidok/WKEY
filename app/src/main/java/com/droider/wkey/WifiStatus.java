package com.droider.wkey;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.List;

public class WifiStatus {

    WifiManager wifiManager;
    static List<ScanResult> wifiList;

    public WifiStatus(Context mContext){
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            Toast.makeText(mContext, "WIFI启动中...", Toast.LENGTH_SHORT).show();
        }
    }

    public void scanWifi(){
        wifiManager.startScan();
        wifiList = wifiManager.getScanResults();
    }
}
