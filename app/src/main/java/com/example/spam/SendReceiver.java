package com.example.spam;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SendReceiver extends BroadcastReceiver {

    public static final String ACTION = "action.send.sms";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION.equals(action)) {
            int resultCode = getResultCode();
            if (resultCode == Activity.RESULT_OK) {
                // 发送成功
                Toast.makeText(context, "发送成功", Toast.LENGTH_LONG).show();
            } else {
                // 发送失败
                Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
            }
        }
    }
}
