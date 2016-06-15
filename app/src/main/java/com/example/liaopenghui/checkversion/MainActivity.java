package com.example.liaopenghui.checkversion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PROG = 1;
    private static final int OK = 2;
    public String jsonContent="{appkey: xxxxxxxxxx,old_md5: 74629424208c92c8e8164c8b166635f0,version_code:1}";//模拟json数据
    private VersionBean versionBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //先注册广播  做好这边接受service发了的广播
         BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                TextView tv = (TextView) findViewById(R.id.hahaha);
                tv.setText("发送过来的内容："+intent.getExtras().getString("i"));
            }
        };
        IntentFilter filter = new IntentFilter("com.gdp2852.demo.service.broadcast");
        registerReceiver(receiver, filter);
        if(isWifiState(MainActivity.this)){
            //调用版本检测的接口
             versionBean = new Gson().fromJson(
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
                CheckUpdateUtils.getInstance().checkVersion(MainActivity.this,versionBean.old_md5,"http://app.e.sm.cn/fs/appsource/13073292/appsource_13073292_17129eeb14bfde4f3402960b45ce314e.apk");
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

}
