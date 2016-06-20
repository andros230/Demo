package com.andros230.demo;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;

/**
 * Created by andros230 on 2016/6/20.
 */
public class BmobDao {
    private Context context;
    private BmobRealTimeData data;
    private DBOpenHelper db;
    private String id = null;
    private String TAG = "BmobDao";

    public BmobDao(Context context) {
        Bmob.initialize(context, "5b9353d27ae18dc5aafb5bf57b85a06b");
        this.context = context;
        data = new BmobRealTimeData();
        db = new DBOpenHelper(context);
        query();
    }

    public String getId() {
        return id;
    }

    //更新数据
    public void update(LatLonKit kit) {
        Log.d("id---", id);
        kit.update(context, id, new UpdateListener() {
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

    //查询数据
    public void query() {
        BmobQuery<LatLonKit> query = new BmobQuery<>();
        LatLonKit kit = new LatLonKit();
        query.addWhereEqualTo("mac", kit.getLocalMac(context));
        query.findObjects(context, new FindListener<LatLonKit>() {
            @Override
            public void onSuccess(List<LatLonKit> list) {
                Log.d("查询数据size---", list.size() + "");
                if (list.size() == 0) {
                    save();
                } else {
                    for (LatLonKit kit : list) {
                        id = kit.getObjectId();
                        Log.d("查询数据---", "id:" + id);
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e("查询数据---", "onError");
            }
        });
    }

    //保存数据
    public void save() {
        LatLonKit kit = new LatLonKit();
        kit.setMac(kit.getLocalMac(context));
        kit.setLatitude("0");
        kit.setLongitude("0");
        kit.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.d("保存数据---", "onSuccess");
                query();
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("保存数据---", "onFailure");
            }
        });
    }

    //删除数据
    public void delete(String id) {
        LatLonKit kit = new LatLonKit();
        kit.setObjectId(id);
        kit.delete(context, new DeleteListener() {
            @Override
            public void onSuccess() {
                Log.d("删除成功---", "onSuccess");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e("删除失败---", "onFailure");
            }
        });
    }

    //实时数据监听
    public void realTimeData() {
        if (!data.isConnected()) {
            data.start(context, new ValueEventListener() {
                @Override
                public void onDataChange(JSONObject arg0) {
                    // TODO Auto-generated method stub\
                    JSONObject data = arg0.optJSONObject("data");
                    LatLonKit kit = new LatLonKit();
                    kit.setMac(data.optString("mac"));
                    kit.setLatitude(data.optString("latitude"));
                    kit.setLongitude(data.optString("longitude"));
                    Log.d("----realTimeData", kit.getMac());
                    Log.d("----realTimeData", kit.getLatitude());
                    Log.d("----realTimeData", kit.getLongitude());
                    if (!kit.getMac().equals("") && !kit.getLatitude().equals("") && !kit.getLongitude().equals("")) {
                        if (db.queryMacExist(kit.getMac())) {
                            db.update(kit);
                        } else {
                            db.insert(kit);
                        }
                    }
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

    //地图标注
    public void addMarker(AMap aMap) {
        aMap.clear(true);
        Cursor cur = db.query();
        Log.d("---", "sqlite查询结果------------------------------");
        while (cur.moveToNext()) {

            String mac = cur.getString(1);
            String lat = cur.getString(2);
            String log = cur.getString(3);
            Log.d("---", "mac: " + mac);
            Log.d("---", "lat: " + lat);
            Log.d("---", "log: " + log);
            LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(log));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(mac);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            aMap.addMarker(markerOptions);
        }
    }
}
