package com.example.spam;

import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BodyActivity extends AppCompatActivity {

    private TextView date;
    private TextView body;
    private TextView address;
    private Button back;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_body);
        back=findViewById(R.id.button_back);
        address=findViewById(R.id.textView2);
        date=findViewById(R.id.textView3);
        body=findViewById(R.id.textView);
        back.setOnClickListener(v -> {
            finish();
        });
        String body1=getIntent().getStringExtra("body");
        String date1=getIntent().getStringExtra("date");
        String address1=getIntent().getStringExtra("address");
        long num = Long.parseLong(date1);
        String date11=getDate(num);
        address.setText(address1);
        date.setText(date11);
        body.setText(body1);
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal.getTime()).toString();
        return date;
    }
}
