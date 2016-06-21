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

    }

    public String getId() {
        return id;
    }

    //更新数据
    public void update(LatLonKit kit) {
        kit.update(context, id, new UpdateListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "id:" + id + "  更新成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "id:" + id + "  更新失败");
            }
        });
    }

    //查询数据
    public void query(final LatLonKit akit) {
        BmobQuery<LatLonKit> query = new BmobQuery<>();
        final LatLonKit kit = new LatLonKit();
        query.addWhereEqualTo("mac", kit.getLocalMac(context));
        query.findObjects(context, new FindListener<LatLonKit>() {
            @Override
            public void onSuccess(List<LatLonKit> list) {
                if (list.size() != 0) {
                    for (LatLonKit kit : list) {
                        id = kit.getObjectId();
                        Log.i(TAG, "第一次添加数据成功 id:" + id);
                    }
                }else {
                    save(akit);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.e(TAG, "查询数据出错 id:" + id);
            }
        });
    }

    //保存数据
    public void save(final LatLonKit kit) {
        kit.save(context, new SaveListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "数据保存成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "数据保存失败");
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
                Log.i(TAG, "数据删除成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "数据删除失败");
            }
        });
    }

    //实时数据监听
    public void realTimeData() {
        if (!data.isConnected()) {
            Log.e(TAG, "数据监听为关闭状态");
            data.start(context, new ValueEventListener() {
                @Override
                public void onDataChange(JSONObject arg0) {
                    // TODO Auto-generated method stub\
                    JSONObject data = arg0.optJSONObject("data");
                    LatLonKit kit = new LatLonKit();
                    kit.setMac(data.optString("mac"));
                    kit.setLatitude(data.optString("latitude"));
                    kit.setLongitude(data.optString("longitude"));
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
        } else {
            Log.i(TAG, "数据监听中...");
        }
    }

    //地图标注
    public void addMarker(AMap aMap) {
        aMap.clear(true);
        Cursor cur = db.query();
        while (cur.moveToNext()) {
            String mac = cur.getString(1);
            String lat = cur.getString(2);
            String log = cur.getString(3);
            if (!mac.equals(new LatLonKit().getLocalMac(context))) {
                Log.i(TAG, "数据库查询数据: mac: " + mac + " lat: " + lat + " log: " + log);
                LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(log));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(mac);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                aMap.addMarker(markerOptions);
            }
        }
    }
}
