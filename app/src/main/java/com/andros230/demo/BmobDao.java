package com.andros230.demo;

import android.app.ActionBar;
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
    private BmobRealTimeData data = null;
    private DBOpenHelper db;
    private String id = null;
    private String TAG = "BmobDao";

    public BmobDao(Context context) {
        Bmob.initialize(context, "5b9353d27ae18dc5aafb5bf57b85a06b");
        this.context = context;
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
                Log.i(TAG, "id:" + id + "  坐标更新成功");
            }

            @Override
            public void onFailure(int i, String s) {
                Log.e(TAG, "id:" + id + "  坐标更新失败");
            }
        });
    }

    //查询数据
    public void query(final LatLonKit kit) {
        BmobQuery<LatLonKit> query = new BmobQuery<>();
        query.addWhereEqualTo("mac", new LatLonKit().getLocalMac(context));
        query.findObjects(context, new FindListener<LatLonKit>() {
            @Override
            public void onSuccess(List<LatLonKit> list) {
                if (list.size() != 0) {
                    for (LatLonKit kit : list) {
                        id = kit.getObjectId();
                        Log.i(TAG, "第一次添加数据成功 id:" + id);
                    }
                } else {
                    save(kit);
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
        if (data == null) {
            data = new BmobRealTimeData();
        }
        if (!data.isConnected()) {
            Log.i(TAG, "实时数据监听正在开启中......");
            data.start(context, new ValueEventListener() {
                @Override
                public void onDataChange(JSONObject arg0) {
                    // TODO Auto-generated method stub\
                    JSONObject data = arg0.optJSONObject("data");
                    String mac = data.optString("mac");
                    String lat = data.optString("latitude");
                    String log = data.optString("longitude");

                    LatLonKit kit = new LatLonKit();
                    kit.setMac(mac);
                    kit.setLatitude(lat);
                    kit.setLongitude(log);
                    if (!mac.equals("") && !lat.equals("") && !log.equals("") && !mac.equals(kit.getLocalMac(context))) {
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
                        Log.i(TAG, "实时数据监听开启成功");
                    } else {
                        Log.e(TAG, "实时数据监听开启失败");
                    }
                }
            });
        }
    }

    //取消实时监听
    public void unRealTimeData() {
        if (data != null) {
            if (data.isConnected()) {
                data.unsubTableUpdate("LatLonKit");
            }
            data = null;
            Log.i(TAG, "关闭实时数据监听");
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
            String time = cur.getString(4);
            Log.i(TAG, "添加标注: mac: " + mac + " lat: " + lat + " log: " + log);
            LatLng latLng = new LatLng(Double.valueOf(lat), Double.valueOf(log));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("mac: " + mac + "\n" + "更新时间: " + time);

            if (db.compareTime(time) <= 30) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                aMap.addMarker(markerOptions);
            } else if (db.compareTime(time) > 30 && db.compareTime(time) < 1440) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                aMap.addMarker(markerOptions);
            } else {
                db.delete(mac);
                Log.i(TAG, "删除标注,定位时间:" + time + " 比对时间: " + db.compareTime(time));
            }
        }
    }

}
