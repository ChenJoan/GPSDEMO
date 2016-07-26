package com.example.pisce.myapplication;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity1 extends Activity implements View.OnClickListener {

    private Button btnClick;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main1);

        btnClick = (Button) findViewById(R.id.btn_click);
        btnClick.setOnClickListener(this);

        text = (TextView) findViewById(R.id.tv_text);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_click:
                int type = PhoneUtils.getPhoneAccessType(this);  //accessType
                if(1==type){
                    Log.i("CC","=========wifi联网=====");
                }else if(0==type){
                    Log.i("CC","========手机网络联网=========");
                }else{
                    Log.i("CC","========返回了默认值======");
                }

                String imei = PhoneUtils.getPhoneImei(this);///手机的IMEI号码
                Log.i("CC","===getPhoneImei==="+imei);

                String smac = PhoneUtils.getPhoneMacAddress(this);//手机的mac地址，IPV4
                Log.i("CC", "===getPhoneMacAddressIPV4===" + smac);

                String serverIP = PhoneUtils.getPhoneServerIP(this);
                Log.i("CC","======phoneServerIP===="+serverIP);

                String imsi = PhoneUtils.getPhoneImsi(this);
                Log.i("CC", "======getPhoneImsi======" + imsi);

                WifiInfo info = PhoneUtils.getPhoneWifiInfo(this);
                String mmacMac = info.getBSSID();//连接wifi时手机的Mac地址
                String mmacName = info.getSSID();//连接的wifi名称
                int mmacSignal = info.getRssi();//连接的wifi信号强度
                Log.i("CC","======mmacMac====="+mmacMac);
                Log.i("CC","======mmacName====="+mmacName);
                Log.i("CC","======mmacSignal====="+mmacSignal);

                PhoneUtils.getWifiInfos(this);  //macs必填
                Log.i("CC", "===getNetworkState===" + PhoneUtils.getNetworkState(this));
                PhoneUtils.getGSMCellLocationInfo(this);
                getJsonStr();
                break;
            default:
                break;
        }
    }

    private void getJsonStr() {
        //测试wifi情况下
        String str = "http://apilocate.amap.com/position?accesstype=1&imei=867831024024945&smac=f4:8b:32:a7:fc:6e&mmac=d0:17:c2:df:e1:f4,-77,HTmapabc_5G&macs=d0:17:c2:df:e1:f4,-77,HTmapabc_5G|40:5d:82:d3:e5:5c,-78,HC360-5G|40:5d:82:d3:e5:58,-62,HC360-2.4G&serverip=192.168.11.1&output=json&key=bd10910d7fb63fd5690b56f0f305d280";
//        String str = "http://apilocate.amap.com/position?accesstype=1&imei=352315052834187&smac=E0:DB:55:E4:C7:49&mmac=22:27:1d:20:08:d5,-55,CMCC-EDU&macs=22:27:1d:20:08:d5,-55,CMCC-EDU|5c:63:bf:a4:bf:56,-86,TP-LINK|d8:c7:c8:a8:1a:13,-42,TP-LINK&serverip=10.2.166.4&output=json&key=bd10910d7fb63fd5690b56f0f305d280";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(str, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                text.setText(s);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                text.setText("请求发生错误");
            }
        });
        queue.add(request);
    }

    final static int BUFFER_SIZE = 4096;
    private void getJsonString(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = "http://apilocate.amap.com/position?accesstype=1&imei=867831024024945&smac=f4:8b:32:a7:fc:6e&mmac=d0:17:c2:df:e1:f4,-77,HTmapabc_5G&macs=d0:17:c2:df:e1:f4,-77,HTmapabc_5G|40:5d:82:d3:e5:5c,-78,HC360-5G|40:5d:82:d3:e5:58,-62,HC360-2.4G&serverip=192.168.11.1&output=json&key=bd10910d7fb63fd5690b56f0f305d280";
                URL url = null;
                try {
                    url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(5*1000);
                    conn.setRequestMethod("GET");
                    InputStream inStream = conn.getInputStream();
                    String result = InputStreamUtils.InputStreamTOString(inStream);
                    text.setText(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}