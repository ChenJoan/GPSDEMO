package com.example.pisce.myapplication;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private LocationManager lm;

    private EditText etInterval;
    private Button btLocation;
    private TextView tvResult;

    private float minDistance = 8; //默认最小距离
    private Long minTime = 2000L;  //默认轮询间隔

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etInterval = (EditText) findViewById(R.id.et_interval);
        btLocation = (Button) findViewById(R.id.bt_location);
        btLocation.setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.tv_result);

        //创建LocationManager对象
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //从GPS获取最近的定位信息
        Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //更新显示定位信息
        updateView(lc);

        if (!TextUtils.isEmpty(etInterval.getText().toString())) {
            minTime = Long.getLong(etInterval.getText().toString());
        }
        //设置每3秒 获取一次GPS定位信息
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub
                Log.i("CC", "=======onStatusChanged========");
            }

            @Override
            public void onProviderEnabled(String provider) {
                // 当GPS LocationProvider可用时，更新定位
                Log.i("CC", "=======onProviderEnabled========");
                updateView(lm.getLastKnownLocation(provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("CC", "=======onProviderDisabled========");
                updateView(null);
            }

            @Override
            public void onLocationChanged(Location location) {
                // 当GPS定位信息发生改变时，更新定位
                Log.i("CC", "=======onLocationChanged========");
                updateView(location);
            }
        });
    }

    public void updateView(Location newLocation) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        if (newLocation != null) {
            sb.append("实时位置信息：" + "\n");
            sb.append("经度： ");
            sb.append(newLocation.getLongitude() + "\n");
            sb.append("纬度：");
            sb.append(newLocation.getLatitude() + "\n");
            sb.append("高度：");
            sb.append(newLocation.getAltitude() + "\n");
            sb.append("速度：");
            sb.append(newLocation.getSpeed() + "\n");
            sb.append("方向：");
            sb.append(newLocation.getBearing() + "\n");
            sb.append("定位精度：");
            sb.append(newLocation.getAccuracy() + "\n");
        }

        if ("停止定位".equals(btLocation.getText().toString())) {
            tvResult.setText(sb);
        } else {
            tvResult.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_location) {
            if (btLocation.getText().equals("开始定位")) {
                btLocation.setText("停止定位");
                tvResult.setText("");
            } else {
                btLocation.setText("开始定位");
                tvResult.setText("定位停止");
            }
        }
    }
}