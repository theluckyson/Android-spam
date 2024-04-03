package com.example.spam;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.BreakIterator;

public class SmsReceiver extends BroadcastReceiver {
    //    private SmsHander smsHander;
//    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            // 获取短信相关信息
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (smsMessages != null && smsMessages.length > 0) {
                for (SmsMessage smsMessage : smsMessages) {
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();
                    long timestamp = smsMessage.getTimestampMillis();

                    // 将短信存储到数据库中
//                    saveSmsToDatabase(context, sender, messageBody, timestamp);
                }
            }
        }
    }



//    private void saveSmsToDatabase(Context context, String sender, String messageBody, long timestamp) {
//        // 获取数据库实例
//        SmsDatabaseHelper dbHelper = new SmsDatabaseHelper(context);
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        // 创建一个ContentValues对象，用于存储数据
//        ContentValues values = new ContentValues();
//        values.put(sms.SmsEntry.COLUMN_ADDRESS, sender);
//        values.put(SmsContract.SmsEntry.COLUMN_MESSAGE_BODY, messageBody);
//        values.put(SmsContract.SmsEntry.COLUMN_TIMESTAMP, timestamp);
//
//        // 插入数据
//        long newRowId = db.insert(SmsContract.SmsEntry.TABLE_NAME, null, values);
//
//        // 关闭数据库连接
//        db.close();
//    }

//    DatabaseHelper SmsdbHelper=DatabaseHelper.getInstance(getApplicationContext());
//
//
//
//    @SuppressLint({"Range", "SetTextI18n"})
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        // 在这里处理接收到的短信
//        // 获取短信内容
//        Bundle bundle = intent.getExtras();
//        SQLiteDatabase db = SmsdbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        if (bundle != null) {
//            Object[] pdus = (Object[]) bundle.get("pdus");
//            if (pdus != null && pdus.length > 0) {
////                    StringBuilder smsContent = new StringBuilder();
////                    for (Object pdu : pdus) {
////                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
////                        String sender = smsMessage.getDisplayOriginatingAddress();
////                        String messageBody = smsMessage.getMessageBody();
////                        String person=smsMessage.getOriginatingAddress();
////                        long timestamp = smsMessage.getTimestampMillis();
////                        values.put("address", sender);
////                        values.put("person", person);
////                        values.put("body", messageBody);
////                        values.put("date", timestamp);
////                        values.put("type", 0);
////                        long newRowId = db.insert("sms", null, values);
////                        smsContent.append("Sender: ").append(sender).append("\n");
////                        smsContent.append("Message: ").append(messageBody).append("\n\n");
////                    }
////                    // 更新UI，将短信内容显示在TextView中
////                    smsTextView.setText(smsContent.toString());
//                Long lastTime;
//                Cursor dbCount = db.rawQuery("select count(*) from sms", null);
//                dbCount.moveToFirst();
//                if (dbCount.getInt(0) > 0) {
//                    Cursor dbcur = db.rawQuery("select * from sms order by date desc limit 1", null);
//                    dbcur.moveToFirst();
//                    lastTime = Long.parseLong(dbcur.getString(dbcur.getColumnIndex("date")));
//                } else {
//                    lastTime = new Long(0);
//                }
//                dbCount.close();
//                dbCount = null;
//
//                Cursor cur = getSMSInPhone(); // 获取短信（游标）
//                db.beginTransaction(); // 开始事务处理
//                if (cur.moveToFirst()) {
//                    String address;
//                    String person;
//                    String body;
//                    String date;
//                    int type;
//
//                    int iAddress = cur.getColumnIndex("address");
//                    int iPerson = cur.getColumnIndex("person");
//                    int iBody = cur.getColumnIndex("body");
//                    int iDate = cur.getColumnIndex("date");
//                    int iType = cur.getColumnIndex("type");
//
//                    do {
//                        address = cur.getString(iAddress);
//                        person = cur.getString(iPerson);
//                        body = cur.getString(iBody);
//                        date = cur.getString(iDate);
//                        type = cur.getInt(iType);
//
//                        if (Long.parseLong(date) > lastTime) {
//                            String sql = "insert into sms values(null, ?, ?, ?, ?, ?)";
//                            Object[] bindArgs = new Object[] { address, person, body, date, type };
//                            db.execSQL(sql, bindArgs);
//                        } else {
//                            break;
//                        }
//                    } while (cur.moveToNext());
//
//                    cur.close();
//                    cur = null;
//                    db.setTransactionSuccessful(); 	// 设置事务处理成功，不设置会自动回滚不提交
//                    db.endTransaction(); 			// 结束事务处理
//                }
//
//            }
//
//        }
//    }
//
//
//
//    @SuppressLint("Range")
//    private Cursor getSMSInPhone() {
//        Uri SMS_CONTENT = Uri.parse("content://sms/");
//        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
//        Cursor cursor = this.getContentResolver().query(SMS_CONTENT, projection, null, null, "date desc");	// 获取手机短信
//
//        while (cursor.moveToNext()) {
//            System.out.println("--sms-- : " + cursor.getString(cursor.getColumnIndex("body")));
//        }
//
//        return cursor;
//    }


}
