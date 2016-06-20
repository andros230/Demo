package com.andros230.demo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import cn.bmob.v3.BmobObject;

/**
 * Created by andros230 on 2016/6/14.
 */
public class LatLonKit extends BmobObject {
    private String mac;
    private String longitude;
    private String latitude;

    public LatLonKit(Context context) {
        //获取Mac
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        this.mac = info.getMacAddress();
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }


}