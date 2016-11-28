package com.jiaying.workstation.activity.plasmacollection;

/**
 * Created by hipil on 2016/5/11.
 */
public enum Res {
    //一体机向服务器发送登记请求后，服务器应答允许献浆；
    REG_SERVERRES_PASS,

    //一体机向服务器请求分配机器后，服务器应答不允许献浆；
    REG_SERVERRES_NOT_PASS,

    //一体机向服务器请求分配机器后，服务器应答允许献浆；
    ALOC_SERVERRES_PASS,

    //一体机向服务器请求分配机器后，服务器应答不允许献浆；
    ALOC_SERVERRES_NOT_PASS,

    //一体机向服务器请求分配机器后，单采机应答收到分配请求；
    ALOC_ZXDCRES,

    //一体机向服务器请求分配机器后，多媒体平板应答收到分配请求；
    ALOC_TABLETRES,

    //通过物联网协议，登陆服务器后，获得服务器发送的时间信号
    TIMESTAMP,

    //现在设备的可用状态发生变化，
    ZXDC_STATE_CHANGE,

    NOTHING
}
