package com.example.spam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SmsAdapter extends ArrayAdapter<Sms> {
    public SmsAdapter(@NonNull Context context, int resource, @NonNull List<Sms> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Sms sms=getItem(position);//得到当前项的 Fruit 实例
        //为每一个子项加载设定的布局
        @SuppressLint("ViewHolder") View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item,parent,false);
        //分别获取 image view 和 textview 的实例
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_address =view.findViewById(R.id.sms_address);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_date=view.findViewById(R.id.sms_date);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_body =view.findViewById(R.id.sms_body);
        Button sms_delete=view.findViewById(R.id.sms_delete);
        sms_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SQLiteDatabase db=dbHelper.getWritableDatabase();
//                db.delete("information","name=?",new String[]{name.getText().toString()});
            }
        });
        // 设置要显示的图片和文字
        if (sms != null) {
            sms_address.setText(sms.getAddress());
        }
        if (sms != null) {
            sms_date.setText(sms.getDate());
        }
        if (sms != null) {
            sms_body.setText(sms.getBody());
        }
        return view;
    }


}
