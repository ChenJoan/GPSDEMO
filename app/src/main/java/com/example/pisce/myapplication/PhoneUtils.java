package com.example.pisce.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Formatter;
import android.util.Log;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.List;

/**
 * Created by pisce on 2016/6/30.
 */
public class PhoneUtils {

    // / 没有连接
    public static final int NETWORK_NONE = 0;

    // / wifi连接
    public static final int NETWORK_WIFI = 1;

    // / 手机网络数据连接
    public static final int NETWORK_2G = 2;
    public static final int NETWORK_3G = 3;
    public static final int NETWORK_4G = 4;
    public static final int NETWORK_MOBILE = 5;

    /**
     * 获取网络类型(具体类型)
     * @param context
     * @return
     */
    public static int getNetworkState(Context context) {

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connManager)
            return NETWORK_NONE;

        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();

        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORK_NONE;
        }

        // Wifi
        NetworkInfo wifiInfo=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if(null!=wifiInfo){
            State state = wifiInfo.getState();
            if(null!=state)
                if (state == State.CONNECTED || state == State.CONNECTING) {
                    return NETWORK_WIFI;
                }
        }

        // 网络
        NetworkInfo networkInfo=connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if(null!=networkInfo){
            State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();

            if(null!=state)
                if (state == State.CONNECTED || state == State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_2G;

                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_3G;

                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_4G;

                        default://有机型返回16,17
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")){
                                return NETWORK_3G;
                            }else{
                                return NETWORK_MOBILE;
                            }
                    }
                }
        }
        return NETWORK_NONE;
    }


    /**
     * 获取手机网络类型，用于区分手机网络和wifi
     * @param ctx
     * @return  1表示wifi联网；0表示手机网络联网，-1默认返回值
     */
    public static int getPhoneAccessType(Context ctx){
        ConnectivityManager connectMager = getConnectivityManager(ctx);
        NetworkInfo info = connectMager.getActiveNetworkInfo();

        // 判断当前info非空，类型为wifi
        if(null != info && ConnectivityManager.TYPE_WIFI == info.getType()){
            return 1;
        }

        // 判断当前info非空，类型为手机网络
        if(null != info && ConnectivityManager.TYPE_MOBILE == info.getType()){
            return 0;
        }
        return -1;
    }


    /**
     * 获取手机的IMEI号码
     * @param ctx
     * @return
     */
    public static String getPhoneImei(Context ctx){
        return getTelephonyManager(ctx).getDeviceId();
    }

    /**
     * 获取手机的IMSI号码
     * @param ctx
     * @return
     */
    public static String getPhoneImsi(Context ctx){
        return getTelephonyManager(ctx).getSubscriberId();
    }

    /**
     * 获取手机的mac地址
     * @param ctx
     * @return
     */
    public static String getPhoneMacAddress(Context ctx) {
        //原来的获取mac地址的方法
        //在wifi未开启状态下，仍然可以获取MAC地址，但是IP地址必须在已连接状态下否则为0
//        String macAddress = null, ip = null;
//        WifiManager wifiMgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
//        if (null != info) {
//            macAddress = info.getMacAddress();
//        }
//        return macAddress;

        //android6.0的新获取mac地址的方法
        String str="";
        String macSerial="";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (macSerial == null || "".equals(macSerial)) {
            try {
                return loadFileAsString("/sys/class/net/eth0/address")
                        .toUpperCase().substring(0, 17);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return macSerial;
    }

    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }
    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    /**
     * 获取手机的TelephonyManager
     * @param ctx
     * @return
     */
    public static TelephonyManager getTelephonyManager(Context ctx){
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return null == telephonyManager ? null : telephonyManager;
    }

    /**
     * 获取手机的wifiManager
     * @param ctx
     * @return
     */
    public static WifiManager getWifiManager(Context ctx){
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        return null == wifiManager ? null : wifiManager;
    }

    /**
     * 获取手机的ConnectivityManager
     * @param ctx
     * @return
     */
    public static ConnectivityManager getConnectivityManager(Context ctx){
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        return null == connectivityManager ? null : connectivityManager;
    }

    /**
     * 获取wifi列表中的wifi信息
     * @param ctx
     */
    public static void getWifiInfos(Context ctx){
        WifiManager manager = getWifiManager(ctx);
        List<ScanResult> results = manager.getScanResults();
        Log.i("CC","==========results.size======="+results.size());
        for (int i = 0; i<3;i++){  ///此处只取得前三个即可
            Log.i("CC","========ScanResult==="+i+".BSSID===="+results.get(i).BSSID);//mac地址
            Log.i("CC", "========ScanResult==="+i+".SSID====" + results.get(i).SSID);//wifi名称
            Log.i("CC", "========ScanResult==="+i+".level====" + results.get(i).level);//wifi信号强度
        }
    }


    /**
     *  MCC，Mobile Country Code，移动国家代码（中国的为460）；
     *  MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
     *  LAC，Location Area Code，位置区域码；
     *  CID，Cell Identity，基站编号；
     *  BSSS，Base station signal strength，基站信号强度。
     */
    public static void getGSMCellLocationInfo(Context ctx){
        TelephonyManager manager = getTelephonyManager(ctx);

        String operator = manager.getNetworkOperator();
        /**通过operator获取 MCC 和MNC */
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));

        GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();

        /**通过GsmCellLocation获取中国移动和联通 LAC 和cellID */
        int lac = location.getLac();
        int cellid = location.getCid();

        CdmaCellLocation cdmaLocation = (CdmaCellLocation) manager.getCellLocation();
        int cellId1 = cdmaLocation.getBaseStationId();
        int sid = cdmaLocation.getSystemId();//cdma系统识别码
        int nid = cdmaLocation.getNetworkId();//cdma网络识别码
        int bid = cdmaLocation.getBaseStationId();//cdma小区唯一识别码
        int lon = cdmaLocation.getBaseStationLongitude();//cdma经度值，手机平台接口读出的数值
        int lat = cdmaLocation.getBaseStationLatitude();// cdma纬度值，手机平台接口读出的数值

        int strength = 0;
        /**通过getNeighboringCellInfo获取BSSS */
        List<NeighboringCellInfo> infoLists = manager.getNeighboringCellInfo();
        for (NeighboringCellInfo info : infoLists) {
            strength+=(-133+2*info.getRssi());// 获取邻区基站信号强度
            //info.getLac();// 取出当前邻区的LAC
            //info.getCid();// 取出当前邻区的CID
            System.out.println("rssi:"+info.getRssi()+"   strength:"+strength);
        }

        Log.i("CC","=====mcc====="+mcc);
        Log.i("CC","=====mnc====="+mnc);
        Log.i("CC","=====lac====="+lac);
        Log.i("CC","=====cellid====="+cellid);
        Log.i("CC","=====signal====="+strength);
    }

    /**
     * 获取手机当前连接的wifi信息
     * @param ctx
     * @return WifiInfo对象
     */
    public static WifiInfo getPhoneWifiInfo(Context ctx){
        WifiManager wifiMgr = getWifiManager(ctx);
        WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
        Log.i("CC","====获取BSSID属性====wifiMac==="+info.getBSSID());
        Log.i("CC","===== 获取SSID===wifiName==="+info.getSSID());
        Log.i("CC","=====获取802.11n 网络的信号======"+info.getRssi());
        Log.i("CC","=====获取SSID 是否被隐藏======"+info.getHiddenSSID());
        Log.i("CC","=====获取连接的速度======"+info.getLinkSpeed());
        Log.i("CC","======获取具体客户端状态的信息====="+info.getSupplicantState());
        return info;
    }

    /**
     * 获取设备接入基站时对应的网关IP
     * 当wifi联网的时候，获取到的是路由器的IP
     * @param ctx
     * @return
     */
    public static String getPhoneServerIP(Context ctx){
        WifiManager wifi_service = getWifiManager(ctx);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        WifiInfo wifiinfo = wifi_service.getConnectionInfo();
//        Log.i("CC", "DHCP info gateway---路由器IP-->" + Formatter.formatIpAddress(dhcpInfo.gateway));
//        Log.i("CC", "DHCP info netmask----->" + Formatter.formatIpAddress(dhcpInfo.netmask));
        //DhcpInfo中的gateway是一个int型的变量，通过Formatter将其转化为字符串IP地址
        return Formatter.formatIpAddress(dhcpInfo.gateway);
    }






}
