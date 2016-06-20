package com.andros230.demo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DBOpenHelper extends SQLiteOpenHelper {
    private String TAG = "DBOpneHelper";
    private static final String TABLE_NAME = "bmob_table";

    public DBOpenHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (id INTEGER primary key autoincrement, mac text, lat text, log text);";
        sqLiteDatabase.execSQL(sql);
        Log.d(TAG, "创建数据库");
    }

    //增加操作
    public void insert(LatLonKit kit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mac", kit.getMac());
        cv.put("lat", kit.getLatitude());
        cv.put("log", kit.getLongitude());
        long row = db.insert(TABLE_NAME, null, cv);
        if (row != 0) {
            Log.d(TAG, "增加成功: " + kit.toString());
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
            Log.e(TAG, "searchMacExist查询失败,mac:" + mac);
        }
        return rs;
    }

    //修改操作
    public void update(LatLonKit kit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("lat", kit.getLatitude());
        cv.put("log", kit.getLongitude());
        int rs = db.update(TABLE_NAME, cv, "mac = ?", new String[]{kit.getMac()});
        if (rs != 0) {
            Log.d(TAG, "修改成功: " + kit.toString());
        } else {
            Log.e(TAG, "修改失败, " + kit.toString());
        }
    }

    //删除操作
    public void delete(String mac) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rs = db.delete(TABLE_NAME, "mac = ?", new String[]{mac});
        if (rs != 0) {
            Log.d(TAG, "删除成功,mac:" + mac);
        } else {
            Log.e(TAG, "删除失败,mac:" + mac);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
