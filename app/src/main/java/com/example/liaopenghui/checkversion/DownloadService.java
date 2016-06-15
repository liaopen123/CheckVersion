package com.example.liaopenghui.checkversion;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownloadService extends IntentService {
//"http://app.e.sm.cn/fs/appsource/13073292/appsource_13073292_17129eeb14bfde4f3402960b45ce314e.apk"

    private static final String TAG = "DownloadService";
    private NotificationManager notificationManager;
    private NotificationCompat.Builder mBuilder;
    private String apkFile;
    private String downloadURL;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
         apkFile = intent.getStringExtra("path");
        downloadURL = intent.getStringExtra("url");
        Log.d(TAG,"服务得到的URL:"+downloadURL);
         notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("正在下载新版本").setSmallIcon(R.mipmap.ic_launcher);
        downLoadApk();

    }






    //访问网络下载apk
    private void downLoadApk() {
        Request request = new Request.Builder().url(downloadURL).tag(this).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mBuilder.setContentText("下载失败");
                PendingIntent pendingintent = PendingIntent.getActivity(DownloadService.this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
                mBuilder.setContentIntent(pendingintent);
                notificationManager.notify(0, mBuilder.build());
            }

            @Override
            public void onResponse(Response response) throws IOException {

                saveFile(response);
                //
//                han.obtainMessage(OK, "下载完成").sendToTarget();
                // 下载完成
                mBuilder.setContentText(getString(R.string.download_success)).setProgress(0, 0, false);
                notificationManager.cancelAll();
                installApk(new File(apkFile));
            }

        });
    }
    //下载apk
    //开始安装apk
    protected void installApk(File file) {
        Log.e(TAG,"安装时候的路径:"+file.getAbsolutePath());
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");//编者按：此处Android应为android，否则造成安装不了
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void saveFile(Response response) throws IOException {
        InputStream is = null;
        int oldProgress = 0;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            Log.i("TAG", "total--" + total);
            long sum = 0;

            fos = new FileOutputStream(apkFile);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
//                han.obtainMessage(PROG, String.valueOf((int)(finalSum *100/ total))).sendToTarget();
                int progress = (int)(finalSum *100/ total);
                if (progress != oldProgress) {
                    if (total != -1) {
                        updateProgress(progress, false);
                    } else {
                        updateProgress(progress, true);
                    }
//                    LogUtils.e(progress);
                }
                oldProgress = progress;
            }
            fos.flush();
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


        //更新进度条
    private void updateProgress(int progress, boolean i) {
        if (i == false) {
            //如果获取不到文件大小，则不使用百分比进度条
            mBuilder.setContentText(this.getString(R.string.download_progress, progress)).setProgress(100, progress, i);
        } else {
            mBuilder.setContentText("正在下载:未知大小").setProgress(100, progress, i);
        }

        PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        notificationManager.notify(0, mBuilder.build());
    }
}
