package com.example.spam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BodyActivity extends AppCompatActivity implements View.OnClickListener{

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_body);
        DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        back=findViewById(R.id.button_back);
        address=findViewById(R.id.textView2);
        delete=findViewById(R.id.button_body);
//        date=findViewById(R.id.textView3);
        body=findViewById(R.id.textView);
        send=findViewById(R.id.button_send);
        back.setOnClickListener(v -> {
            finish();
        });

        String address1=getIntent().getStringExtra("address");
        String type=getIntent().getStringExtra("type");

        address.setText(address1);


        String[] projection = {
                "body",
                "date",
        };
        String selection = "address = ? AND type = ?";
        String[] selectionArgs = { address1,type};

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

        sms_List=new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // 读取数据
                String body;
                String date;

                int iBody = cursor.getColumnIndex("body");
                int iDate = cursor.getColumnIndex("date");

                body = cursor.getString(iBody);
                date = cursor.getString(iDate);

                Body sms=new Body(body,date);
                sms_List.add(sms);
                // 处理数据
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        adapter=new BodyAdapter(this,sms_List);
        body.setAdapter(adapter);
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

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal.getTime()).toString();
        return date;
    }
}
