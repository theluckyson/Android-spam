package com.example.spam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sms1.db";

    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists sms("
                + "_id integer primary key autoincrement,"
                + "address varchar(255)," + "person varchar(255),"
                + "body varchar(1024)," + "date varchar(255),"
                + "type integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "drop table if exists sms";
        db.execSQL(sql);
        onCreate(db);
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }

//    public void delete(SQLiteDatabase db,String table_name){
//        String sql = "drop table if exists "+table_name;
//        db.execSQL(sql);
//    }

}
