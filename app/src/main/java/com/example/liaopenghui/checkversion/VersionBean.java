package com.example.liaopenghui.checkversion;

/**
 * Created by liaopenghui on 16/5/30.
 */
public class VersionBean {
    public String appkey;
    public String old_md5;
    public String version_code;

    @Override
    public String toString() {
        return "VersionBean{" +
                "appkey='" + appkey + '\'' +
                ", old_md5='" + old_md5 + '\'' +
                ", version_code='" + version_code + '\'' +
                '}';
    }
}
