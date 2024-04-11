package com.example.spam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpamActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseHelper dbHelper;

    private SmsAdapter adapter;

    private List<Sms> sms_List ;

    TextView textView;
    Button button;
    ListView listView;
    SwipeRefreshLayout srl_my_refresh;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_spam);
        textView=findViewById(R.id.textView_null2);
        button=findViewById(R.id.back2);
        listView=findViewById(R.id.list_spam);
        srl_my_refresh = findViewById(R.id.srl_my_refresh2);
        textView.setVisibility(View.GONE);
        srl_my_refresh.setColorSchemeColors(Color.parseColor("#ff0000"), Color.parseColor("#00ff00"));
        srl_my_refresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#0000ff"));
        dbHelper=DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        button.setOnClickListener(v -> {
            finish();
        });
        String[] projection = {
                "address",
                "body",
                "date",
        };
        String selection = "type = ?";
        String[] selectionArgs = { "1" };

// How you want the results sorted in the resulting Cursor
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
                String address;
                String body;
                String date;


                int iAddress = cursor.getColumnIndex("address");

                int iBody = cursor.getColumnIndex("body");
                int iDate = cursor.getColumnIndex("date");

                address = cursor.getString(iAddress);

                body = cursor.getString(iBody);
                date = cursor.getString(iDate);

                Sms sms=new Sms(address,body,date);
                sms_List.add(sms);
                // 处理数据
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
//        SmsAdapter adapter=new SmsAdapter(this,R.layout.sms_item,sms_List);
        adapter=new SmsAdapter(this,sms_List);
        listView.setAdapter(adapter);
//        listView.setOnItemClickListener((parent, view, position, id) -> {
//            // 第二步：通过Intent跳转至新的页面
//            Intent intent = new Intent(SpamActivity.this, BodyActivity.class);
//            startActivity(intent);
//        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 获取点击的项对应的数据
            String address = adapter.get_address(position);
            String date = adapter.get_date(position);
            String body = adapter.get_body(position);

            // 创建 Intent 跳转到子界面，并传递数据
            Intent intent = new Intent();
            intent.setClass(SpamActivity.this, BodyActivity.class);
            intent.putExtra("address", address);
            intent.putExtra("date", date);
            intent.putExtra("body", body);
            startActivity(intent);
        });

        srl_my_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //判断是否在刷新
                Toast.makeText(SpamActivity.this,srl_my_refresh.isRefreshing()?"正在刷新":"刷新完成"
                        ,Toast.LENGTH_SHORT).show();

                sms_List.clear();
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(
                        "sms",   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        selection,              // The columns for the WHERE clause
                        selectionArgs,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order
                );
                if (cursor.moveToFirst()) {
                    do {
                        // 读取数据
                        String address;
                        String body;
                        String date;


                        int iAddress = cursor.getColumnIndex("address");

                        int iBody = cursor.getColumnIndex("body");
                        int iDate = cursor.getColumnIndex("date");

                        address = cursor.getString(iAddress);

                        body = cursor.getString(iBody);
                        date = cursor.getString(iDate);

                        Sms sms=new Sms(address,body,date);
                        sms_List.add(sms);
                        // 处理数据
                    } while (cursor.moveToNext());
                }
                cursor.close();
                db.close();
                adapter.notifyDataSetChanged();
                srl_my_refresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //关闭刷新
                        srl_my_refresh.setRefreshing(false);
                    }
                },1000);
            }
        });
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
            String address = adapter.get_address(position);
            String date = adapter.get_date(position);
            String body = adapter.get_body(position);
            dbHelper.deleteItem(address,date,body);
            sms_List.remove(position);
            adapter.notifyDataSetChanged();
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }


}