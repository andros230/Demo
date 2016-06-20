package com.andros230.demo;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import cn.bmob.v3.BmobObject;

public class LatLonKit extends BmobObject {
    private String mac;
    private String longitude;
    private String latitude;

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

    public String toString() {
        return "mac:" + mac + " lat:" + latitude + " log:" + longitude;
    }

    public String getLocalMac(Context context){
        //获取Mac
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }
}