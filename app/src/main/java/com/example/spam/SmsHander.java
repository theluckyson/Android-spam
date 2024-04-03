package com.example.spam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class SmsHander  {
    SQLiteDatabase db;
    Context context;

    public SmsHander(Context context) {
        this.context = context;
    }

    public void createSMSDatabase() {
        String sql = "create table if not exists sms("
                + "_id integer primary key autoincrement,"
                + "address varchar(255)," + "person varchar(255),"
                + "body varchar(1024)," + "date varchar(255),"
                + "type integer)";
        db = SQLiteDatabase.openOrCreateDatabase("/smss.db", null);			// 创建数据库
        db.execSQL(sql);
    }

    // 获取手机短信
    @SuppressLint("Range")
    private Cursor getSMSInPhone() {
        Uri SMS_CONTENT = Uri.parse("content://sms/");
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        Cursor cursor = context.getContentResolver().query(SMS_CONTENT, projection, null, null, "date desc");	// 获取手机短信

        while (cursor.moveToNext()) {
            System.out.println("--sms-- : " + cursor.getString(cursor.getColumnIndex("body")));
        }

        return cursor;
    }

    // 保存手机短信到 SQLite 数据库
    @SuppressLint("Range")
    public void insertSMSToDatabase() {
        Long lastTime;
        Cursor dbCount = db.rawQuery("select count(*) from sms", null);
        dbCount.moveToFirst();
        if (dbCount.getInt(0) > 0) {
            Cursor dbcur = db.rawQuery("select * from sms order by date desc limit 1", null);
            dbcur.moveToFirst();
            lastTime = Long.parseLong(dbcur.getString(dbcur.getColumnIndex("date")));
        } else {
            lastTime = new Long(0);
        }
        dbCount.close();
        dbCount = null;

        Cursor cur = getSMSInPhone(); // 获取短信（游标）
        db.beginTransaction(); // 开始事务处理
        if (cur.moveToFirst()) {
            String address;
            String person;
            String body;
            String date;
            int type;

            int iAddress = cur.getColumnIndex("address");
            int iPerson = cur.getColumnIndex("person");
            int iBody = cur.getColumnIndex("body");
            int iDate = cur.getColumnIndex("date");
            int iType = cur.getColumnIndex("type");

            do {
                address = cur.getString(iAddress);
                person = cur.getString(iPerson);
                body = cur.getString(iBody);
                date = cur.getString(iDate);
                type = cur.getInt(iType);

                if (Long.parseLong(date) > lastTime) {
                    String sql = "insert into sms values(null, ?, ?, ?, ?, ?)";
                    Object[] bindArgs = new Object[] { address, person, body, date, type };
                    db.execSQL(sql, bindArgs);
                } else {
                    break;
                }
            } while (cur.moveToNext());

            cur.close();
            cur = null;
            db.setTransactionSuccessful(); 	// 设置事务处理成功，不设置会自动回滚不提交
            db.endTransaction(); 			// 结束事务处理
        }

    }

    // 获取 SQLite 数据库中的全部短信
    public Cursor querySMSFromDatabase() {
        String sql = "select * from sms order by date desc";
        return db.rawQuery(sql, null);
    }

    // 获取 SQLite 数据库中的最新 size 条短信
    public Cursor querySMSInDatabase(int size) {
        String sql;

        Cursor dbCount = db.rawQuery("select count(*) from sms", null);
        dbCount.moveToFirst();
        if (size < dbCount.getInt(0)) { // 不足 size 条短信，则取前 size 条
            sql = "select * from sms order by date desc limit " + size;
        } else {
            sql = "select * from sms order by date desc";
        }
        dbCount.close();
        dbCount = null;

        return db.rawQuery(sql, null);
    }

    // 获取 SQLite数据库的前 second秒短信
    public Cursor getSMSInDatabaseFrom(long second) {
        long time = System.currentTimeMillis() / 1000 - second;
        String sql = "select * from sms order by date desc where date > " + time;
        return db.rawQuery(sql, null);
    }

    // 关闭数据库
    public void closeSMSDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
            db = null;
        }
    }


}

