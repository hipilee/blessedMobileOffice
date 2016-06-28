package com.jiaying.workstation.interfaces;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

/**
 * 作者：lenovo on 2016/3/15 07:17
 * 邮箱：353510746@qq.com
 * 功能：人脸采集
 */
public interface IfaceCollector {
    int open();
    int close();
    void collect();

    void setOnCollectCallback(OnFaceCollectCallback onFaceCollectCallback);

     interface OnFaceCollectCallback {
        public void onCollect(Bitmap bitmap,int originX,int originY,int width,int height);
    }
}
