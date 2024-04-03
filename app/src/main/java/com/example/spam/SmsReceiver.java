package com.example.spam;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.BreakIterator;

public class SmsReceiver extends BroadcastReceiver {
    //    private SmsHander smsHander;
//    @SuppressLint("UnsafeProtectedBroadcastReceiver")
//    @Override
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

//        Bundle bundle=intent.getExtras();
//        SmsMessage[]  msgs;
//        String msg_from ;
//        if(bundle!=null){
//            try {
//
//                Object[] puds=(Object[]) bundle.get("pdus");
//                msgs=new SmsMessage[puds.length];
//                for(int i=0;i<msgs.length;i++){
//                    msgs[i]=SmsMessage.createFromPdu((byte[])puds[i]);
//                    msg_from=msgs[i].getOriginatingAddress();
//                    String msgBody=msgs[i].getMessageBody();
//
//                    Toast.makeText(context,"From: "+msg_from +", Body: "+msgBody,Toast.LENGTH_SHORT).show();
//
//                }
//            }catch (Exception e){
//                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
//
//            }
//        }
//
//    }

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

    }
}
