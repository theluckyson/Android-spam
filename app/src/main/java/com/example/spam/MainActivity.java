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
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
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

import java.util.Locale;
import java.util.StringTokenizer;


public class MainActivity extends AppCompatActivity {

    private SmsReceiver smsReceiver;

    private DatabaseHelper dbHelper;

    private TextView smsTextView;


    Button button1;
    Button button2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.norm);
        button2 = findViewById(R.id.spam);
        smsTextView = findViewById(R.id.smsTextView);
        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
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
        dbHelper.close();
    }

    @Override
    protected void onDestroy() {
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
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
            SQLiteDatabase db = dbHelper.getWritableDatabase();
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
                        Object[] bindArgs = new Object[]{address, person, body, date, type};
                        db.execSQL(sql, bindArgs);
                    } else {
                        break;
                    }
                } while (cur.moveToNext());

                cur.close();
                cur = null;
                db.setTransactionSuccessful();    // 设置事务处理成功，不设置会自动回滚不提交
                db.endTransaction();// 结束事务处理
            }


//            Bundle bundle = intent.getExtras();
//            if (bundle != null) {
//                Object[] pdus = (Object[]) bundle.get("pdus");
//                if (pdus != null && pdus.length > 0) {
//                    StringBuilder smsContent = new StringBuilder();
//                    for (Object pdu : pdus) {
//                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
//                        String sender = smsMessage.getDisplayOriginatingAddress();
//                        String messageBody = smsMessage.getMessageBody();
//                        smsContent.append("Sender: ").append(sender).append("\n");
//                        smsContent.append("Message: ").append(messageBody).append("\n\n");
//                    }
//                    smsTextView.setText(smsContent.toString());
//                }
//            }
        }

        }

        @SuppressLint("Range")
        private Cursor getSMSInPhone() {
            Uri SMS_CONTENT = Uri.parse("content://sms/");
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cursor = this.getContentResolver().query(SMS_CONTENT, projection, null, null, "date desc");    // 获取手机短信

            while (cursor.moveToNext()) {
                System.out.println("--sms-- : " + cursor.getString(cursor.getColumnIndex("body")));
            }

            return cursor;
        }



}





