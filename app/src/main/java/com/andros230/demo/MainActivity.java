package com.andros230.demo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener, AdapterView.OnItemSelectedListener, AMap.OnMarkerClickListener {
    private MapView mMapView;
    private AMap aMap;
    private BmobDao dao;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationClientOption;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState); // 此方法必须重写
        dao = new BmobDao(getApplicationContext());
        init();
        AlarmCPU();
    }

    //地图初始化
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.setLocationSource(this);  //设置定位监听
            aMap.setOnMarkerClickListener(this);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);  //设置默认定位按键是否显示
            aMap.setMyLocationEnabled(true); // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);  // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        }
        //地图模式切换下拉框
        Spinner spinner = (Spinner) findViewById(R.id.layers_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.layers_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //定位模式切换下拉框
        Spinner gps_spinner = (Spinner) findViewById(R.id.gps_spinner);
        ArrayAdapter<CharSequence> gps_adapter = ArrayAdapter.createFromResource(this, R.array.gps_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gps_spinner.setAdapter(gps_adapter);
        gps_spinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    //定位成功后回调函数
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                Log.i(TAG, "定位成功");
                mListener.onLocationChanged(aMapLocation); // 显示系统小蓝点
                dao.addMarker(aMap);
                dao.realTimeData();
                LatLonKit kit = new LatLonKit();
                kit.setMac(kit.getLocalMac(this));
                kit.setLongitude(aMapLocation.getLongitude() + "");
                kit.setLatitude(aMapLocation.getLatitude() + "");
                if (dao.getId() != null) {
                    dao.update(kit);
                } else {
                    dao.query(kit);
                }
            } else {
                Log.e(TAG, "定位失败,错误代码;" + aMapLocation.getErrorCode() + ",错误信息:" + aMapLocation.getErrorInfo());
            }
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationClientOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //是否允许模拟位置
            //定位时间间隔
            mLocationClientOption.setInterval(1000 * 5);
            mLocationClient.setLocationOption(mLocationClientOption);
            mLocationClient.startLocation();
        }
    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (aMap != null) {
            setLayer((String) parent.getItemAtPosition(position));
        }
    }

    /**
     * 下拉框的事件响应
     */
    private void setLayer(String layerName) {
        if (layerName.equals("矢量地图")) {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
        } else if (layerName.equals("卫星地图")) {
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        } else if (layerName.equals("夜景地图")) {
            aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
        } else if (layerName.equals("导航模式")) {
            aMap.setMapType(AMap.MAP_TYPE_NAVI);//导航模式
        } else if (layerName.equals("定位模式")) {
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE); //定位模式
        } else if (layerName.equals("跟随模式")) {
            aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_FOLLOW); //跟随模式
        } else if (layerName.equals("旋转模式")) {
            aMap.setMyLocationType(AMap.LOCATION_TYPE_MAP_ROTATE); // 设置定位的类型为根据地图面向方向旋转
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    //alarmManager可叫醒CPU,保证关闭屏后还可定位
    public void AlarmCPU() {
        Intent intentRepeat = new Intent(this, MainActivity.class);
        PendingIntent sender = PendingIntent.getService(this, 0, intentRepeat, 0);
        long triggerTime = SystemClock.elapsedRealtime() + 1000; // 第一次时间
        long intervalTime = 1000; // ms
        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, intervalTime, sender);
    }

}
