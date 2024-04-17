package com.example.spam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BodyActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    //    private TextView date;
    private List<Body> sms_List ;
//    private DatabaseHelper dbHelper;
    private BodyAdapter adapter;
    private ListView body;
    private TextView address;
    private Button back;
    private Button send;
    private Button delete;
    private EditText editText;
    private SendReceiver receiver;
    SwipeRefreshLayout srl_my_refresh;
    TextView textView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_body);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        back = findViewById(R.id.button_back);
        address = findViewById(R.id.textView2);
        delete = findViewById(R.id.button_body);
//        date=findViewById(R.id.textView3);
        body = findViewById(R.id.textView);
        send = findViewById(R.id.button_send);
        editText = findViewById(R.id.et2);
        textView = findViewById(R.id.textView_null2);
        textView.setVisibility(View.GONE);
        srl_my_refresh = findViewById(R.id.srl_my_refresh2);
        srl_my_refresh.setColorSchemeColors(Color.parseColor("#ff0000"), Color.parseColor("#00ff00"));
        srl_my_refresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#0000ff"));


        // 检查是否有发送短信权限，如果没有，则请求权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }

        back.setOnClickListener(v -> {
            finish();
        });

        String address1 = getIntent().getStringExtra("address");
        String type = getIntent().getStringExtra("type");

        address.setText(address1);

        send.setOnClickListener(v -> {
            // 检查是否有发送短信权限，如果没有，则请求权限
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.SEND_SMS},
//                        MY_PERMISSIONS_REQUEST_SEND_SMS);
//            }

            SmsManager smsManager = SmsManager.getDefault();
            List<String> list = smsManager.divideMessage(editText.getText().toString());
            for (String sms : list) {
                smsManager.sendTextMessage(address1, null, sms, null, null);
            }
            Toast.makeText(BodyActivity.this, "发送成功", Toast.LENGTH_LONG).show();
            editText.setText(" ");
        });

        String[] projection = {
                "body",
                "date",
        };
        String selection = "address = ? AND type = ?";
        String[] selectionArgs = {address1, type};

        String sortOrder =
                "date DESC";

        Cursor cursor = db.query(
                "sms",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        sms_List = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // 读取数据
                String body;
                String date;

                int iBody = cursor.getColumnIndex("body");
                int iDate = cursor.getColumnIndex("date");

                body = cursor.getString(iBody);
                date = cursor.getString(iDate);

                Body sms = new Body(body, date);
                sms_List.add(sms);
                // 处理数据
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        dbHelper.close();
        adapter = new BodyAdapter(this, sms_List);
        body.setAdapter(adapter);

        srl_my_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                load();
            }
        });
    }


//        receiver=new SendReceiver();
//        IntentFilter filter=new IntentFilter();
////        filter.addAction(SendReceiver.ACTION);
//        filter.addAction("action.send.sms");
//        registerReceiver(receiver,filter);




    public void load(){
        Toast.makeText(BodyActivity.this,srl_my_refresh.isRefreshing()?"正在刷新":"刷新完成"
                ,Toast.LENGTH_SHORT).show();

        sms_List.clear();
        DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                "body",
                "date",
        };
        String selection = "address = ? AND type = ?";

        String address1 = getIntent().getStringExtra("address");
        String type = getIntent().getStringExtra("type");

        String[] selectionArgs = {address1, type};

        String sortOrder =
                "date DESC";

        Cursor cursor = db.query(
                "sms",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

//        sms_List = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // 读取数据
                String body;
                String date;

                int iBody = cursor.getColumnIndex("body");
                int iDate = cursor.getColumnIndex("date");

                body = cursor.getString(iBody);
                date = cursor.getString(iDate);

                Body sms = new Body(body, date);
                sms_List.add(sms);
                // 处理数据
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        dbHelper.close();
        adapter.notifyDataSetChanged();
        srl_my_refresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                //关闭刷新
                srl_my_refresh.setRefreshing(false);
            }
        },1000);
    }

    public void onClick(View v) {
        //lv条目中 iv_del
        final int position = (int) v.getTag(); //获取被点击的控件所在item 的位置，setTag 存储的object，所以此处要强转

        //点击删除按钮之后，给出dialog提示
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle( position + "号位置的删除按钮被点击，确认删除?");
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        builder.setPositiveButton("确定", (dialog, which) -> {
            String date = adapter.get_date(position);
            DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete("sms", "date = ?", new String[]{date});
            db.close();
            dbHelper.close();
            sms_List.remove(position);
            adapter.notifyDataSetChanged();
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        if ( receiver!= null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }


    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal.getTime()).toString();
        return date;
    }

//    public class SendReceiver extends BroadcastReceiver {
//
//        public static final String ACTION = "action.send.sms";
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION.equals(action)) {
//                int resultCode = getResultCode();
//                if (resultCode == Activity.RESULT_OK) {
//                    // 发送成功
//                    Toast.makeText(context, "发送成功", Toast.LENGTH_LONG).show();
//                } else {
//                    // 发送失败
//                    Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予发送短信权限，执行发送短信的操作
                Toast.makeText(this, "Permission Granted!.", Toast.LENGTH_SHORT).show();
            } else {
                // 用户拒绝发送短信权限，可以给出相应的提示
                Toast.makeText(this, "Permission not Granted!.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
