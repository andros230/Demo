package com.andros230.demo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {
    private MapView mMapView;
    private AMap aMap;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationClientOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 不显示程序的标题栏
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState); // 此方法必须重写
        init();

    }


    //地图初始化
    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            aMap.setLocationSource(this);  //设置定位监听
            aMap.getUiSettings().setMyLocationButtonEnabled(true);  //设置默认定位按键是否显示
            aMap.setMyLocationEnabled(true); // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);  // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        }
    }


    @Override
    protected void onDestroy() {
        Log.d("---", "onDestroy");
        super.onDestroy();
        mMapView.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        Log.d("---", "onResume");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("---", "onPause");
        super.onPause();
        mMapView.onPause();
        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("---", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    //定位成功后回调函数
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation); // 显示系统小蓝点

                Log.d("---", "经度：" + aMapLocation.getLongitude());
                Log.d("---", "纬度：" + aMapLocation.getLatitude());
                Log.d("---", "精度：" + aMapLocation.getAccuracy());
                sendLatLon("http://192.168.0.101:8080/testss/a230", aMapLocation.getLongitude(), aMapLocation.getLatitude(), aMapLocation.getAccuracy());

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

    //连接服务器
    public void sendLatLon(String url, final double Longitude, final double Latitude, final double Accuracy) {
        //获取Mac
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        final String Mac = info.getMacAddress();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // 返回数据
                Log.d("Response-------", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error.Response", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //POST 参数
                Map<String, String> params = new HashMap<>();
                params.put("Mac", Mac);
                params.put("Longitude", Longitude + "");
                params.put("Latitude", Latitude + "");
                params.put("Accuracy", Accuracy + "");
                return params;
            }
        };
        requestQueue.add(postRequest);
    }
}
