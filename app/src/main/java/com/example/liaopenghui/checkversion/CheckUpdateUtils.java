package com.example.liaopenghui.checkversion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by liaopenghui on 16/6/15.
 */
public class CheckUpdateUtils {
    private static CheckUpdateUtils checkUpdate = null;
    private File apkFile;//apk的路径
    private static final String TAG = "CheckUpdateUtils";
    private Context mContext;
    private  String downloadURL;//服务器穿过来的apk下载地址
    String serviceMD5;//服务器传过来的MD5
    public CheckUpdateUtils(){
    }
    public static CheckUpdateUtils getInstance() {
        if (checkUpdate == null) {
            checkUpdate = new CheckUpdateUtils();
        }
        return checkUpdate;
    }



    public void checkVersion(Context context, String serviceMD5, String downloadURL) {
        mContext =context;
        this.serviceMD5 = serviceMD5;
        this.downloadURL = downloadURL;
        apkFile = getApkFile();
        if(apkFile.exists()){
            //文件存在
            Log.d(TAG,"文件存在 ");
            if(serviceMD5.equals(getFileMD5(apkFile))){
                Log.d(TAG,"MD5相同直接安装 ");
                installApk(apkFile);
            }else{
                Log.d(TAG,"MD5不相同删除,下载 ");
                apkFile.delete();
                startServiceToDownLoad(apkFile);
            }
        }else{
            Log.d(TAG,"文件存在,开始下载 ");
            //文件不存在,下载
            startServiceToDownLoad(apkFile);
//                    downLoadApk();

        }
    }


    private File getApkFile() {
        File file = new File( mContext.getExternalCacheDir() + "/download");
        if (!file.exists()) file.mkdirs();
        File apk1File = new File(file, "mobile.apk");
        Log.d(TAG,"得到的apk路径为:"+apk1File.getAbsolutePath());
        return apk1File;
    }

    /**
     * 获取单个文件的MD5值！

     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }

    //开始安装apk
    protected void installApk(File file) {
        Log.e(TAG,"安装时候的路径:"+file.getAbsolutePath());
        Intent intent = new Intent();
        //执行动作
        intent.setAction(Intent.ACTION_VIEW);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");//编者按：此处Android应为android，否则造成安装不了
        mContext.startActivity(intent);
    }


    //开启服务进行下载
    private void startServiceToDownLoad(File file) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("url", downloadURL);
        intent.putExtra("path",file.getAbsolutePath());
        mContext.startService(intent);
    }

}
