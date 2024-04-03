package com.example.spam;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.pytorch.IValue;
import org.pytorch.Module;

import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {

    private SmsReceiver smsReceiver;
    DatabaseHelper dbHelper=new DatabaseHelper(this);

    Button button1;
    Button button2;

    private SmsHander smsHander;
    private TextView smsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        smsTextView = findViewById(R.id.smsTextView);
        button1 = findViewById(R.id.norm);
        button2 = findViewById(R.id.spam);
//        smsHander = new SmsHander(this);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SpamActivity.class);
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, NormActivity.class);
                startActivity(intent);

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 1000);

        }
        smsReceiver = new SmsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
//        smsHander.closeSMSDatabase();
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Permission not Granted!.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private class SmsReceiver extends BroadcastReceiver {
        @SuppressLint({"Range", "SetTextI18n"})
        @Override
        public void onReceive(Context context, Intent intent) {
            // 在这里处理接收到的短信
            // 获取短信内容
            Bundle bundle = intent.getExtras();
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
//                    StringBuilder smsContent = new StringBuilder();
//                    for (Object pdu : pdus) {
//                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
//                        String sender = smsMessage.getDisplayOriginatingAddress();
//                        String messageBody = smsMessage.getMessageBody();
//                        String person=smsMessage.getOriginatingAddress();
//                        long timestamp = smsMessage.getTimestampMillis();
//                        values.put("address", sender);
//                        values.put("person", person);
//                        values.put("body", messageBody);
//                        values.put("date", timestamp);
//                        values.put("type", 0);
//                        long newRowId = db.insert("sms", null, values);
//                        smsContent.append("Sender: ").append(sender).append("\n");
//                        smsContent.append("Message: ").append(messageBody).append("\n\n");
//                    }
//                    // 更新UI，将短信内容显示在TextView中
//                    smsTextView.setText(smsContent.toString());
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

            }
        }

        }

    @SuppressLint("Range")
    private Cursor getSMSInPhone() {
        Uri SMS_CONTENT = Uri.parse("content://sms/");
        String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
        Cursor cursor = this.getContentResolver().query(SMS_CONTENT, projection, null, null, "date desc");	// 获取手机短信

        while (cursor.moveToNext()) {
            System.out.println("--sms-- : " + cursor.getString(cursor.getColumnIndex("body")));
        }

        return cursor;
    }

    }



