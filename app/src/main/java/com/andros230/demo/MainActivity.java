package com.andros230.demo;

import android.app.Activity;
import android.os.Bundle;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import org.json.JSONObject;


import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;

public class MainActivity extends Activity implements LocationSource, AMapLocationListener, AdapterView.OnItemSelectedListener, AMap.OnMarkerClickListener {
    private MapView mMapView;
    private AMap aMap;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationClientOption;
    BmobRealTimeData data = new BmobRealTimeData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState); // 此方法必须重写
        Bmob.initialize(this, "5b9353d27ae18dc5aafb5bf57b85a06b");
        init();
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
        deactivate();
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
                mListener.onLocationChanged(aMapLocation); // 显示系统小蓝点
                if (!data.isConnected()) {
                    realTimeData();
                    Log.d("监听状态---", "激活监听--------------");
                }

                Log.d("---", "经度：" + aMapLocation.getLongitude());
                Log.d("---", "纬度：" + aMapLocation.getLatitude());
                LatLonKit kit = new LatLonKit(this);
                kit.setLongitude(aMapLocation.getLongitude() + "");
                kit.setLatitude(aMapLocation.getLatitude() + "");
                update(kit);
            } else {
                Log.e("MainActivity", "定位失败,错误代码;" + aMapLocation.getErrorCode() + ",错误信息:" + aMapLocation.getErrorInfo());
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
            mLocationClientOption.setMockEnable(false);
            //定位时间间隔
            mLocationClientOption.setInterval(1000 * 2);
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

    public void addMarker(LatLng latLng, String mac) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(mac);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        aMap.addMarker(markerOptions);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }


    //Bmob
    //更新数据
    public void update(LatLonKit kit) {
        kit.update(this, "YcyFeEEG", new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.d("更新成功---", "onSuccess");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("更新失败---", "onFailure");
            }
        });
    }

    //实时数据监听
    public void realTimeData() {
        data.start(this, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject arg0) {
                // TODO Auto-generated method stub\
                JSONObject data = arg0.optJSONObject("data");
                double log = Double.valueOf(data.optString("longitude"));
                double lat = Double.valueOf(data.optString("latitude"));
                String mac = data.optString("mac");
                aMap.clear(true);
                addMarker(new LatLng(lat, log), mac);
            }

            @Override
            public void onConnectCompleted() {
                // TODO Auto-generated method stub
                if (data.isConnected()) {
                    data.subTableUpdate("LatLonKit");
                }
            }
        });
    }
}
