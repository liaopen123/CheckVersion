package com.example.liaopenghui.checkversion;

/**
 * Created by liaopenghui on 16/6/15.
 */
public interface DownLoadListener {
    public void onDownLoading(int progress);
    public void onFailed();
}
