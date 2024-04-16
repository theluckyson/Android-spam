package com.example.spam;

import android.icu.util.Calendar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class BodyAdapter extends BaseAdapter {
    private final View.OnClickListener listener;
    private final List<Body> dataList;
    public BodyAdapter(View.OnClickListener listener,List<Body> dataList) {
        this.listener = listener;
        this.dataList = dataList;
    }

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


    public String get_body(int position){
        return dataList.get(position).getBody();
    }

    public String get_date(int position){
        return dataList.get(position).getDate();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BodyAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new BodyAdapter.ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.body_item, parent,false);
            holder.sms_date = (TextView) convertView.findViewById(R.id.textView_time);
            holder.sms_body = (TextView) convertView.findViewById(R.id.textView_body);
            holder.sms_time=(TextView) convertView.findViewById(R.id.textView_date);
            holder.sms_delete = (Button) convertView.findViewById(R.id.button_body);
            convertView.setTag(holder);
        } else {
            holder = (BodyAdapter.ViewHolder) convertView.getTag();
        }
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
