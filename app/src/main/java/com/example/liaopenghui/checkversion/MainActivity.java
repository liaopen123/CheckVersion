package com.example.liaopenghui.checkversion;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PROG = 1;
    private static final int OK = 2;
    public String jsonContent="{appkey: xxxxxxxxxx,old_md5: xxxxxxxxxxxxxxxxx,version_code:1}";//模拟json数据
    Handler han = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case PROG:
                    tv_hello.setText("下载中:" + msg.obj + "%");
                    break;
                case OK:
                    tv_hello.setText("下载完成");
                    installApk(file);
                    break;
            }
        }
    };
    private TextView tv_hello;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_hello = (TextView)findViewById(R.id.hahaha);
        if(isWifiState(MainActivity.this)){
            //调用版本检测的接口
            VersionBean versionBean = new Gson().fromJson(
                    jsonContent, VersionBean.class);
            Log.d(TAG,versionBean.toString());
            if(true) {
                showDialog();
            }

        }
    }


    //检查是否是wifi网络
    public boolean isWifiState(Context mContext){
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }




    public void showDialog(){
        AlertDialog.Builder builer = new AlertDialog.Builder(this) ;
        builer.setTitle("版本升级");
        builer.setMessage("这是描述内容");
        //当点确定按钮时从服务器上下载 新的apk 然后安装
        builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG,"下载apk,更新");
                downLoadApk();
            }
        });
        //当点取消按钮时进行登录
        builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
             Log.d(TAG,"用户已经取消");
            }
        });
        AlertDialog dialog = builer.create();
        dialog.show();
    }
    //访问网络下载apk
    private void downLoadApk() {
        Request request = new Request.Builder().url("http://app.e.sm.cn/fs/appsource/13073292/appsource_13073292_17129eeb14bfde4f3402960b45ce314e.apk").tag(this).build();
        OkHttpClient okHttpClient =  new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Response response) throws IOException {

                saveFile(response);
                han.obtainMessage(OK, "下载完成").sendToTarget();
            }
        });

    }
    //开始安装apk
    protected void installApk(File file) {
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");//编者按：此处Android应为android，否则造成安装不了
        startActivity(intent);
    }

    public File saveFile(Response response) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            Log.i("TAG", "total--" + total);
            long sum = 0;
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/download");
            if (!dir.exists()) dir.mkdirs();
             file = new File(dir, "haha.apk");
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                han.obtainMessage(PROG, String.valueOf(finalSum * 1.0f / total)).sendToTarget();
            }
            fos.flush();
            return file;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }
        }
    }
}
