package com.example.spam;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import androidx.core.app.ActivityCompat;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1000;

    private SmsReceiver smsReceiver;

//    private DatabaseHelper dbHelper;

    private TextView smsTextView;

    private Module mModule;

    private HashMap<String, Long> mTokenIdMap;
    private HashMap<Long, String> mIdTokenMap;

    private final int MODEL_INPUT_LENGTH = 64;
    private final int EXTRA_ID_NUM = 2;  // In single sentence, we has [CLS] and [SEP]
    private final String CLS = "[CLS]";
    private final String SEP = "[SEP]";
    private final String PAD = "[PAD]";
    private final String UNK = "[UNK]";
    public long inferenceTime = 0L;

    Button button1;
    Button button2;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        button1 = findViewById(R.id.norm);
        button2 = findViewById(R.id.spam);
        smsTextView = findViewById(R.id.smsTextView);
        smsTextView.setVisibility(View.GONE);
//        DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
        button1.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SpamActivity.class);
            startActivity(intent);
        });
        button2.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, NormActivity.class);
            startActivity(intent);

        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
//        }

//         检查是否有读取短信权限，如果没有，则请求权限
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_READ_SMS);
        }


        try {
            mModule=Module.load(MainActivity.assetFilePath(getApplicationContext(), "model.pt"));
        } catch (IOException e) {
            Log.e("BERT Inference", "Error reading assets", e);
            finish();
        }

        InputStreamReader ir = null; // need to use inpustreamreader and getassets method
        try {
            ir = new InputStreamReader(this.getAssets().open("vocab.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(ir);

        String line = null; // Initialize the variable line
        this.mTokenIdMap = new HashMap<>(); // create the HashMap that maps word and id
        this.mIdTokenMap = new HashMap<>(); // create the HashMap that maps id and word
        long count = 0L;
        while(true) {
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line != null) {
//                System.out.println(line+count);
                this.mTokenIdMap.put(line, count); // HashMap that maps word and id
                this.mIdTokenMap.put(count, line); // HashMap that maps id and word
                count++; // count++ and give each word a id
            } else {
                break;
            }
        }
        smsReceiver = new SmsReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
        startDeleteService();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        if (mModule!=null){
            mModule.destroy();
        }
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
//        dbHelper.close();
        super.onDestroy();
        stopDeleteService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == MY_PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not Granted!.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private class SmsReceiver extends BroadcastReceiver {
        @SuppressLint({"Range", "SetTextI18n"})
        @Override
        public void onReceive(Context context, Intent intent) {
            // 在这里处理接收到的短信
            // 获取短信内容
            DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
//            SQLiteDatabase db =GetDb();
            Long lastTime;
            Cursor dbCount = db.rawQuery("select count(*) from sms", null);
            dbCount.moveToFirst();
            if (dbCount.getInt(0) > 0) {
                Cursor dbcur = db.rawQuery("select * from sms order by date desc limit 1", null);
                dbcur.moveToFirst();
                lastTime = Long.parseLong(dbcur.getString(dbcur.getColumnIndex("date")));
            } else {
                lastTime = new Long(0);
            }
            dbCount.close();
            dbCount = null;

            Cursor cur = getSMSInPhone(); // 获取短信（游标）
            db.beginTransaction(); // 开始事务处理
            if (cur.moveToFirst()) {
                String address;
                String person;
                String body;
                String date;
                int type;

                int iAddress = cur.getColumnIndex("address");
                int iPerson = cur.getColumnIndex("person");
                int iBody = cur.getColumnIndex("body");
                int iDate = cur.getColumnIndex("date");
                int iType = cur.getColumnIndex("type");

                do {
                    address = cur.getString(iAddress);
                    person = cur.getString(iPerson);
                    body = cur.getString(iBody);
                    date = cur.getString(iDate);
                    type = cur.getInt(iType);

                    //短信应用的时间磋和数据库里的数据比较
                    if (Long.parseLong(date) > lastTime) {
                        try {
                            type =GetInference(body);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String sql = "insert into sms values(null, ?, ?, ?, ?, ?)";
                        Object[] bindArgs = new Object[]{address, person, body, date, type};
                        db.execSQL(sql, bindArgs);
                    } else {
                        break;
                    }
                } while (cur.moveToNext());

                cur.close();
                cur = null;
                db.setTransactionSuccessful();    // 设置事务处理成功，不设置会自动回滚不提交
                db.endTransaction();// 结束事务处理
            }
            db.close();
            dbHelper.close();
        }

        }

        @SuppressLint("Range")
        private Cursor getSMSInPhone() {
            Uri SMS_CONTENT = Uri.parse("content://sms/");
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cursor = this.getContentResolver().query(SMS_CONTENT, projection, null, null, "date desc limit 1");    // 获取手机短信

            while (cursor.moveToNext()) {
                System.out.println("--sms-- : " + cursor.getString(cursor.getColumnIndex("body")));
            }

            return cursor;
        }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private long[] tokenizer(String text) throws IOException {
        List<Long> tokenIdsText = this.wordPieceTokenizer(text);
        int inputLength = tokenIdsText.size() + this.EXTRA_ID_NUM;
        long[] ids = new long[Math.min(this.MODEL_INPUT_LENGTH, inputLength)];
        ids[0] = this.mTokenIdMap.get(this.CLS);
//        System.out.println(ids[0]);
//        System.out.println(tokenIdsText.size()); // The size will be equal to the input length e.g., like the = 2
        for(int i = 0; i < tokenIdsText.size(); ++i) {
            ids[i + 1] = tokenIdsText.get(i); // put word ids into ids List
        }
        ids[tokenIdsText.size() + 1] = this.mTokenIdMap.get(this.SEP);
        return ids;
    }

    private List<Long> wordPieceTokenizer(String questionOrText) {
        // for each token, if it's in the vocab.txt (a key in mTokenIdMap), return its Id
        // else do: a. find the largest sub-token (at least the first letter) that exists in vocab;
        // b. add "##" to the rest (even if the rest is a valid token) and get the largest sub-token "##..." that exists in vocab;
        // and c. repeat b.
        List<Long> tokenIds = new ArrayList<>();
        Pattern p = Pattern.compile("\\w+|\\S");
        Matcher m = p.matcher((CharSequence)questionOrText); //seprate the tokens
        while (m.find()) {
            String token = m.group().toLowerCase();
            // if the token in the mTokenIdMap then add its id to tokenIds
            if (this.mTokenIdMap.containsKey(token)) {
                tokenIds.add(this.mTokenIdMap.get(token));
            } else {
                for (int i = 0; i < token.length(); ++i) {
//                    System.out.println("jinlaile"+token.substring(0, token.length() - i - 1));
                    if (this.mTokenIdMap.containsKey(token.substring(0, token.length() - i - 1))) {
                        tokenIds.add(this.mTokenIdMap.get(token.substring(0, token.length() - i - 1)));
                        String subToken = token.substring(token.length() - i - 1); // latter part of the word
                        int j = 0;
//                        System.out.println(this.mTokenIdMap.get(token.substring(0, token.length() - i - 1)));
//                        System.out.println(subToken);
                        while (j < subToken.length()) {
                            if (this.mTokenIdMap.containsKey("##" + subToken.substring(0, subToken.length() - j))) {
//                                System.out.println(this.mTokenIdMap.get("##" + subToken.substring(0, subToken.length() - j)));
                                tokenIds.add(this.mTokenIdMap.get("##" + subToken.substring(0, subToken.length() - j)));
                                subToken = subToken.substring(subToken.length() - j);
                                j = subToken.length() - j;
                            } else if (j == subToken.length() - 1) {
                                tokenIds.add(this.mTokenIdMap.get("##$subToken"));
                                break;
                            } else {
                                j++;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return tokenIds;
    }

    private int Inference(String input) throws IOException {
        int result;
        long[] tokenIds = this.tokenizer(input);

        LongBuffer inTensorBuffer = Tensor.allocateLongBuffer(this.MODEL_INPUT_LENGTH);
        // put token ids to inTensorBuffer
        for (long n : tokenIds) {
            inTensorBuffer.put(n);
        }
        // Fill paddings
        for (int i = 0; i < MODEL_INPUT_LENGTH - tokenIds.length; ++i) {
            inTensorBuffer.put(this.mTokenIdMap.get(this.PAD));
        }

        Tensor inTensor = Tensor.fromBlob(inTensorBuffer, new long[]{1L, this.MODEL_INPUT_LENGTH}); // fromBlob (input_buffer, input_shape)

        LongBuffer attention_mask_Buffer = Tensor.allocateLongBuffer(this.MODEL_INPUT_LENGTH);
        // Set attention mask 1
        for (int i = 0; i < tokenIds.length; ++i) {
            attention_mask_Buffer.put(1L);
        }
        for (int i = 0; i < MODEL_INPUT_LENGTH-tokenIds.length; ++i) {
            attention_mask_Buffer.put(0L);
        }
        Tensor attention_mask = Tensor.fromBlob(attention_mask_Buffer, new long[]{1L, this.MODEL_INPUT_LENGTH});

        LongBuffer token_type_ids_Buffer = Tensor.allocateLongBuffer(this.MODEL_INPUT_LENGTH);
        // put token ids to inTensorBuffer
        for (int i = 0; i < MODEL_INPUT_LENGTH; ++i) {
            token_type_ids_Buffer.put(0L);
        }
        Tensor token_type_ids = Tensor.fromBlob(token_type_ids_Buffer, new long[]{1L, this.MODEL_INPUT_LENGTH});

//        System.out.println(inTensor);
//        long[] test = inTensor.getDataAsLongArray();
//        for (long i : test) {
//            System.out.println(i);
//        }

//        final long startTime = SystemClock.elapsedRealtime();

        //1
//        IValue outIValue = mModule.forward(IValue.from(inTensor), IValue.from(token_type_ids), IValue.from(attention_mask));

//        IValue outIValue = mModule.forward(IValue.from(inTensor), IValue.from(attention_mask));

//        Tensor outTensor = mModule.forward(IValue.from(inTensor), IValue.from(attention_mask)).toTensor();

//        IValue outTensors = mModule.forward(IValue.from(inTensor)); // the output of BERT is a tuple

//        Tensor outTensor = mModule.forward(IValue.from(inTensor), IValue.from(attention_mask)).toTensor();
//        long inferenceTime_temp = SystemClock.elapsedRealtime() - startTime;
//        inferenceTime += inferenceTime_temp;
//        Log.d("BERTINFERNCE",  "inference time (ms): " + inferenceTime);

//        Map<String, IValue> outTensors = mModule.forward(IValue.from(inTensor), IValue.from(attention_mask)).toDictStringKey();
        Map<String, IValue> outTensors = mModule.forward(IValue.from(inTensor), IValue.from(attention_mask),IValue.from(token_type_ids)).toDictStringKey();

        //1
        //java.lang.IllegalStateException: Expected IValue type Tuple, actual type DictStringKey
//        IValue[] outTuple = outIValue.toTuple();

//        Map<String, IValue> outTensors = mModule.forward(IValue.from(inTensor)).toDictStringKey();
//        System.out.println(outTuple.length);

        //1
//        Tensor outTensor = outTuples[0].toTensor();
        Tensor outTensor = Objects.requireNonNull(outTensors.get("logits")).toTensor();

        float[] outTensorFloatArray = outTensor.getDataAsFloatArray();

        // Add a simple prediction label
        if (outTensorFloatArray[0] > outTensorFloatArray[1]) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }

    private int GetInference(String input) throws IOException {
        int a = this.Inference(input);
        return a;
    }

//    private SQLiteDatabase GetDb(){
//        DatabaseHelper dbHelper=DatabaseHelper.getInstance(getApplicationContext());
//        SQLiteDatabase db =dbHelper.getWritableDatabase();
//        return db;
//    }

    private void startDeleteService() {
        Intent intent = new Intent(this, DeleteDataService.class);
        startService(intent);
    }

    private void stopDeleteService() {
        Intent intent = new Intent(this, DeleteDataService.class);
        stopService(intent);
    }


}





