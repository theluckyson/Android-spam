package com.example.spam;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class DeleteDataService extends Service {
//    private static final long INTERVAL = 24 * 60 * 60 * 1000; // 一天
private static final long INTERVAL =  60 * 60 * 1000; // 一小时

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @SuppressLint("Range")
        @Override
        public void run() {
            // 执行数据库删除操作
            DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();


            String selection = "type = ?";
            String[] selectionArgs = { "1" };
            String sortOrder =
                    "date ASC limit 1";
            Cursor cursor = db.query(
                    "sms",   // The table to query
                    null,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            if (cursor.moveToFirst()) {
                // 获取最早的短信的 ID
//                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("_id"));
                // 删除最早的短信
//                String date;
//                lastTime = Long.parseLong(cursor.getString(cursor.getColumnIndex("date")));
//                db.delete("sms", "_id = ?", new String[]{String.valueOf(id)});
//                String date;
//                int iDate = cursor.getColumnIndex("date");
//                date = cursor.getString(iDate);
//                db.delete("sms", "date = ?", new String[]{date});

                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                db.delete("sms", "_id = ?", new String[]{String.valueOf(id)});
            }
            cursor.close();
            db.close();
            dbHelper.close();
            // 每隔一段时间后再次执行
            handler.postDelayed(this, INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(runnable, INTERVAL);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
