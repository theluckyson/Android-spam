package com.example.spam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NormActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    Button button;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_norm);
        button=findViewById(R.id.back1);
        listView=findViewById(R.id.list_norm);
        dbHelper=DatabaseHelper.getInstance(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(NormActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        Cursor dbCount = db.rawQuery("select * from sms where type = 1 order by date desc", null);
        String[] projection = {
                "address",
                "body",
                "date",
        };
        String selection = "type = ?";
        String[] selectionArgs = { "0" };

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

        List<Sms> sms_List = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                String address;
                String body;
                String date;


                int iAddress = cursor.getColumnIndex("address");

                int iBody = cursor.getColumnIndex("body");
                int iDate = cursor.getColumnIndex("date");

                address = cursor.getString(iAddress);

                body = cursor.getString(iBody);
//                date = cursor.getString(iDate);
                long dateReceived = cursor.getLong(iDate);
                date = getDate(dateReceived);

                Sms sms=new Sms(address,body,date);
                sms_List.add(sms);
            }
        }
        cursor.close();
        SmsAdapter adapter=new SmsAdapter(this,R.layout.sms_item,sms_List);
        listView.setAdapter(adapter);

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal.getTime()).toString();
        return date;
    }


}