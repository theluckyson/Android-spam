package com.example.spam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Telephony;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class NormActivity extends AppCompatActivity implements View.OnClickListener{

//    private DatabaseHelper dbHelper;

    private SmsAdapter adapter;

    private List<Sms> sms_List ;

    TextView textView;
    Button button;
    ListView listView;
    SwipeRefreshLayout srl_my_refresh;

    @SuppressLint({"ResourceAsColor", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_norm);
        button=findViewById(R.id.back1);
        listView=findViewById(R.id.list_norm);
        textView=findViewById(R.id.textView_null1);
        textView.setVisibility(View.GONE);
        srl_my_refresh = findViewById(R.id.srl_my_refresh1);
        srl_my_refresh.setColorSchemeColors(Color.parseColor("#ff0000"), Color.parseColor("#00ff00"));
        srl_my_refresh.setProgressBackgroundColorSchemeColor(Color.parseColor("#0000ff"));
        DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
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
        String[] selectionArgs = { "0" };
//        String selection = "address IN (SELECT address FROM sms GROUP BY address HAVING MAX(date)) AND type = ?";
//        String[] selectionArgs = { "0" };
//        String selection = "type = ?";
//        String[] selectionArgs = { "0" };



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
//        SmsAdapter adapter=new SmsAdapter(this,R.layout.sms_item,sms_List);
        db.close();
        dbHelper.close();
        Map<String, Sms> map = new HashMap<>();

        for (Sms sms : sms_List) {
            if (!map.containsKey(sms.getAddress()) || Long.parseLong(sms.getDate()) > Long.parseLong(map.get(sms.getAddress()).getDate())) {
                map.put(sms.getAddress(), sms);
            }
        }

        // 将 Map 中的值转换为列表，即保留每个地址中最大日期的短信
        List<Sms> resultList = new ArrayList<>(map.values());
        // 更新原始列表
        sms_List.clear();
        sms_List.addAll(resultList);
        Collections.sort(sms_List, comparator);
        adapter=new SmsAdapter(this,sms_List);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击的项对应的数据
//                Sms item = sms_List.get(position);
                String address = adapter.get_address(position);
                String type="0";
//                String date = adapter.get_date(position);
//                String body = adapter.get_body(position);

                // 创建 Intent 跳转到子界面，并传递数据
                Intent intent = new Intent();
                intent.setClass(NormActivity.this, BodyActivity.class);
                intent.putExtra("address", address);
                intent.putExtra("type", type);
//                intent.putExtra("date", date);
//                intent.putExtra("body", body);
                startActivity(intent);
            }
        });

        srl_my_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //判断是否在刷新
//                Toast.makeText(NormActivity.this,srl_my_refresh.isRefreshing()?"正在刷新":"刷新完成"
//                        ,Toast.LENGTH_SHORT).show();
//
//                sms_List.clear();
//                DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
//                SQLiteDatabase db = dbHelper.getReadableDatabase();
//                Cursor cursor = db.query(
//                        "sms",   // The table to query
//                        projection,             // The array of columns to return (pass null to get all)
//                        selection,              // The columns for the WHERE clause
//                        selectionArgs,          // The values for the WHERE clause
//                        null,                   // don't group the rows
//                        null,                   // don't filter by row groups
//                        sortOrder               // The sort order
//                );
//                if (cursor.moveToFirst()) {
//                    do {
//                        // 读取数据
//                        String address;
//                        String body;
//                        String date;
//
//                        int iAddress = cursor.getColumnIndex("address");
//                        int iBody = cursor.getColumnIndex("body");
//                        int iDate = cursor.getColumnIndex("date");
//
//                        address = cursor.getString(iAddress);
//                        body = cursor.getString(iBody);
//                        date = cursor.getString(iDate);
//
//                        Sms sms=new Sms(address,body,date);
//                        sms_List.add(sms);
//                        // 处理数据
//                    } while (cursor.moveToNext());
//                }
//                cursor.close();
//                db.close();
//                dbHelper.close();
//                for (Sms sms : sms_List) {
//                    if (!map.containsKey(sms.getAddress()) || Long.parseLong(sms.getDate()) > Long.parseLong(map.get(sms.getAddress()).getDate())) {
//                        map.put(sms.getAddress(), sms);
//                    }
//                }
//
//                // 将 Map 中的值转换为列表，即保留每个地址中最大日期的短信
//                List<Sms> resultList = new ArrayList<>(map.values());
//                // 更新原始列表
//                sms_List.clear();
//                sms_List.addAll(resultList);
//                Collections.sort(sms_List, comparator);
//                adapter.notifyDataSetChanged();
//                srl_my_refresh.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //关闭刷新
//                        srl_my_refresh.setRefreshing(false);
//                    }
//                },1000);
                load();
            }
        });
    }


    public void load(){
            Toast.makeText(NormActivity.this,srl_my_refresh.isRefreshing()?"正在刷新":"刷新完成"
                    ,Toast.LENGTH_SHORT).show();

            sms_List.clear();
            DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                "address",
                "body",
                "date",
        };
        String selection = "type = ?";
        String[] selectionArgs = { "0" };
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
            dbHelper.close();
            Map<String, Sms> map = new HashMap<>();
            for (Sms sms : sms_List) {
                if (!map.containsKey(sms.getAddress()) || Long.parseLong(sms.getDate()) > Long.parseLong(map.get(sms.getAddress()).getDate())) {
                    map.put(sms.getAddress(), sms);
                }
            }

            // 将 Map 中的值转换为列表，即保留每个地址中最大日期的短信
            List<Sms> resultList = new ArrayList<>(map.values());
            // 更新原始列表
            sms_List.clear();
            sms_List.addAll(resultList);
            Collections.sort(sms_List, comparator);
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
                    String address = adapter.get_address(position);
                    String date = adapter.get_date(position);
                    String body = adapter.get_body(position);
                    DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
                    SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("type", 1);
//                    db1.delete("sms", "address = ? and date = ? and body = ?", new String[]{address,date,body});
                    db1.update("sms", values, "address = ? AND date = ? AND body = ?", new String[]{address, date, body});
                    db1.close();
                    sms_List.remove(position);

                    Map<String, Sms> map = new HashMap<>();
                    sms_List.clear();
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    String[] projection = {
                            "address",
                            "body",
                            "date",
                    };
                    String selection = "type = ?";
                    String[] selectionArgs = { "0" };

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
                    if (cursor.moveToFirst()) {
                        do {
                            // 读取数据

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
                    dbHelper.close();
                    for (Sms sms : sms_List) {
                        if (!map.containsKey(sms.getAddress()) || Long.parseLong(sms.getDate()) > Long.parseLong(map.get(sms.getAddress()).getDate())) {
                            map.put(sms.getAddress(), sms);
                        }
                    }

                    // 将 Map 中的值转换为列表，即保留每个地址中最大日期的短信
                    List<Sms> resultList = new ArrayList<>(map.values());
                    // 更新原始列表
                    sms_List.clear();
                    sms_List.addAll(resultList);
                    Collections.sort(sms_List, comparator);

                    adapter.notifyDataSetChanged();
                });
                builder.show();
    }

    @Override
    protected void onDestroy() {
//        dbHelper.close();
        super.onDestroy();

    }

    Comparator<Sms> comparator = new Comparator<Sms>() {
        @Override
        public int compare(Sms sms1, Sms sms2) {
            // 降序排列
            return Long.compare(Long.parseLong(sms2.getDate()), Long.parseLong(sms1.getDate()));
        }
    };

}