package com.example.pisce.myapplication;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by pisce on 2016/8/16.
 */
public class WifiUtils {


    private static final int  WIFI_NEED_PASSWORD = 0;
    private static final int  WIFI_NO_PASSWORD = 1;
    private static final int  WIFI_NOT_CONNECTED = 2;

    public static final String WIFI_AUTH_OPEN = "";
    public static final String WIFI_AUTH_ROAM = "[ESS]";

    public static int checkWifiPassword(Context context) {
        WifiInfo wifiInfo = null;
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        try {
            mWifiManager.startScan();
            // 得到当前连接的wifi热点的信息
            wifiInfo = mWifiManager.getConnectionInfo();
        } catch (SecurityException e) {
            return WIFI_NEED_PASSWORD;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (wifiInfo == null) {
            Log.i("CC", "wifi not connected");
            return WIFI_NOT_CONNECTED;
        }

        String ssid = wifiInfo.getSSID();
        if (ssid == null) {
            return WIFI_NOT_CONNECTED;
        } else if (ssid.length() <= 2) {
            return WIFI_NOT_CONNECTED;
        }
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }

        List<ScanResult> mWifiList = null;
        try {
            // 得到扫描结果
            mWifiList = mWifiManager.getScanResults();
        } catch (SecurityException e) {
            return WIFI_NEED_PASSWORD;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mWifiList != null && mWifiList.size() > 0) {
            try {
                for (ScanResult sr : mWifiList) {
                    if (sr.SSID.equals(ssid) && sr.BSSID.equals(wifiInfo.getBSSID())) {
                        if (sr.capabilities != null) {
                            String capabilities = sr.capabilities.trim();
                            if (capabilities != null && (capabilities.equals(WIFI_AUTH_OPEN) || capabilities.equals(WIFI_AUTH_ROAM))) {
                                return WIFI_NO_PASSWORD;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                return WIFI_NEED_PASSWORD;
            }
        }
        return WIFI_NEED_PASSWORD;
    }



    /**
     * 利用WifiConfiguration.KeyMgmt的管理机制，来判断当前wifi是否需要连接密码
     * @param wifiManager
     * @return true：需要密码连接，false：不需要密码连接
     */
    public static boolean checkCurrentWifiHasPassword(String currentSSID, Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//            // 得到当前连接的wifi热点的信息
//            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            String currentSSID = wifiInfo.getSSID();

            // 得到当前WifiConfiguration列表，此列表包含所有已经连过的wifi的热点信息，未连过的热点不包含在此表中
            List<WifiConfiguration> wifiConfiguration = wifiManager.getConfiguredNetworks();

            if (currentSSID != null && currentSSID.length() > 2) {
                if (currentSSID.startsWith("\"") && currentSSID.endsWith("\"")) {
                    currentSSID = currentSSID.substring(1, currentSSID.length() - 1);
                }

                if (wifiConfiguration != null && wifiConfiguration.size() > 0) {
                    for (WifiConfiguration configuration : wifiConfiguration) {
                        if (configuration != null && configuration.status == WifiConfiguration.Status.CURRENT) {
                            String ssid = null;
                            if (!TextUtils.isEmpty(configuration.SSID)) {
                                ssid = configuration.SSID;
                                if (configuration.SSID.startsWith("\"") && configuration.SSID.endsWith("\"")) {
                                    ssid = configuration.SSID.substring(1, configuration.SSID.length() - 1);
                                }
                            }
                            if (TextUtils.isEmpty(currentSSID) || currentSSID.equalsIgnoreCase(ssid)) {
                                //KeyMgmt.NONE表示无需密码
                                return (!configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE));
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            //........
        }
        //默认为需要连接密码
        return true;
    }



}
