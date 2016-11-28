package com.jiaying.workstation.interfaces;

import android.graphics.Bitmap;

/**
 * 作者：李波 on 2016/3/4 13:08
 * 邮箱：353510746@qq.com
 * 功能：指纹识别
 */
public interface IfingerprintReader {
    void open();

    void read();

    int close();

    void setOnFingerprintReadCallback(OnFingerprintReadCallback onFingerprintReadCallback);

    void setOnFingerprintOpenCallback(OnFingerprintOpenCallback onFingerprintOpenCallback);


    /**
     * 作者：lenovo on 2016/3/4 17:14
     * 邮箱：353510746@qq.com
     * 功能：身份证回调
     */
    interface OnFingerprintReadCallback {
        /*
        bitmap:为null时表示读取异常，其他情况为指纹信息。
         */
        void onFingerPrintInfo(Bitmap bitmap);
    }

    interface OnFingerprintOpenCallback {
        void onFingerPrintOpenInfo(int status);
    }
}
