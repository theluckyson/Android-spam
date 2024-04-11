package com.example.spam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class SmsAdapter extends BaseAdapter {

    private final View.OnClickListener listener;
    private final List<Sms>         dataList;


    public SmsAdapter(View.OnClickListener listener, List<Sms> dataList) {
        this.listener = listener;
        this.dataList = dataList;
    }
//    public SmsAdapter(@NonNull Context context, int resource, @NonNull List<Sms> objects) {
//        super(context, resource, objects);
//    }

    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String get_address(int position){
       return dataList.get(position).getAddress();
    }

    public String get_body(int position){
        return dataList.get(position).getBody();
    }

    public String get_date(int position){
        return dataList.get(position).getDate();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        Sms sms=getItem(position);//得到当前项的 Fruit 实例
//        //为每一个子项加载设定的布局
//        @SuppressLint("ViewHolder") View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item,parent,false);
//        //分别获取 image view 和 textview 的实例
//        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_address =view.findViewById(R.id.sms_address);
//        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_date=view.findViewById(R.id.sms_date);
//        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView sms_body =view.findViewById(R.id.sms_body);
//        Button sms_delete=view.findViewById(R.id.sms_delete);
//        sms_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                SQLiteDatabase db=dbHelper.getWritableDatabase();
////                db.delete("information","name=?",new String[]{name.getText().toString()});
//            }
//        });
//        // 设置要显示的图片和文字
//        if (sms != null) {
//            sms_address.setText(sms.getAddress());
//        }
//        if (sms != null) {
//            sms_date.setText(sms.getDate());
//        }
//        if (sms != null) {
//            sms_body.setText(sms.getBody());
//        }
//        return view;

        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_item, parent,false);
            holder.sms_address = (TextView) convertView.findViewById(R.id.sms_address);
            holder.sms_date = (TextView) convertView.findViewById(R.id.sms_date);
            holder.sms_body = (TextView) convertView.findViewById(R.id.sms_body);
            holder.sms_delete = (Button) convertView.findViewById(R.id.sms_delete);
            holder.sms_time=(TextView) convertView.findViewById(R.id.sms_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.sms_address.setText(dataList.get(position).getAddress());
        long num = Long.parseLong(dataList.get(position).getDate());
        String date11=getDate(num);
        holder.sms_date.setVisibility(View.GONE);
        holder.sms_time.setText(date11);
        holder.sms_body.setText(dataList.get(position).getBody());

        //给要被点击的控件加入点击监听，具体事件在创建adapter的地方实现
        holder.sms_delete.setOnClickListener(listener);
        //通过setTag 将被点击控件所在条目的位置传递出去
        holder.sms_delete.setTag(position);

        return convertView;
    }

    class ViewHolder {
        TextView sms_address;
        TextView sms_date;
        TextView sms_body;
        TextView sms_time;
        Button sms_delete;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("yyyy-MM-dd HH:mm:ss", cal.getTime()).toString();
        return date;
    }


}
