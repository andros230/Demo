package com.andros230.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DBOpenHelper extends SQLiteOpenHelper {
    private String TAG = "DBOpneHelper";
    private static final String TABLE_NAME = "bmob_table";

    public DBOpenHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (id INTEGER primary key autoincrement, mac text, lat text, log text, time text);";
        db.execSQL(sql);
        Log.i(TAG, "创建数据库");
    }

    //增加操作
    public void insert(LatLonKit kit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mac", kit.getMac());
        cv.put("lat", kit.getLatitude());
        cv.put("log", kit.getLongitude());
        cv.put("time", getNowTime());
        long row = db.insert(TABLE_NAME, null, cv);
        if (row != 0) {
            Log.i(TAG, "增加成功: " + kit.toString());
        } else {
            Log.e(TAG, "增加失败: " + kit.toString());
        }
    }

    //查询数据
    public Cursor query() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "select * from " + TABLE_NAME;
        Cursor cur = db.rawQuery(sql, null);
        return cur;
    }


    //查询数据
    public boolean queryMacExist(String mac) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean rs = false;
        try {
            String sql = "select * from " + TABLE_NAME + " where mac = ?";
            Cursor cur = db.rawQuery(sql, new String[]{mac});
            while (cur.moveToNext()) {
                rs = true;
            }
            cur.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "查询出错,mac:" + mac);
        }
        return rs;
    }

    //修改操作
    public void update(LatLonKit kit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("lat", kit.getLatitude());
        cv.put("log", kit.getLongitude());
        cv.put("time", getNowTime());
        int rs = db.update(TABLE_NAME, cv, "mac = ?", new String[]{kit.getMac()});
        if (rs != 0) {
            Log.i(TAG, "本地数据修改成功: " + kit.toString());
        } else {
            Log.e(TAG, "本地数据修改失败, " + kit.toString());
        }
    }

    //删除操作
    public void delete(String mac) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rs = db.delete(TABLE_NAME, "mac = ?", new String[]{mac});
        if (rs != 0) {
            Log.i(TAG, "删除成功,mac:" + mac);
        } else {
            Log.e(TAG, "删除失败,mac:" + mac);
        }
    }

    public int compareTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    //设置时间格式
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = sdf.format(curDate);
            Date d1 = sdf.parse(str);
            Date d2 = sdf.parse(time);
            long l = d1.getTime() - d2.getTime();
            long min = ((l / (60 * 1000)));//分
            return (int) min;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getNowTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    //设置时间格式
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String str = sdf.format(curDate);
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }


}
