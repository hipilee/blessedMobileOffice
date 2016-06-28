package com.jiaying.workstation.app;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * 作者：lenovo on 2016/6/23 21:41
 * 邮箱：353510746@qq.com
 * 功能：application
 */
public class MobileofficeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
