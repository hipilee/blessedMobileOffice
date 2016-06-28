package com.jiaying.workstation.activity.launch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.softfan.dataCenter.DataCenterClientService;
import android.text.TextUtils;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.loginandout.LoginActivity;
import com.jiaying.workstation.activity.plasmacollection.Res;
import com.jiaying.workstation.activity.sensor.FaceCollectionActivity;
import com.jiaying.workstation.activity.sensor.FingerprintActivity;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.DeviceEntity;
import com.jiaying.workstation.entity.ServerTime;
import com.jiaying.workstation.net.serveraddress.LogServer;
import com.jiaying.workstation.net.serveraddress.SignalServer;
import com.jiaying.workstation.net.serveraddress.VideoServer;
import com.jiaying.workstation.service.TimeService;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.WifiAdmin;

import java.util.Observable;
import java.util.Observer;

/**
 * 启动页面，自动连接网络，连接上网络后，连接服务器，得到时间同步信号后跳转到护士登录界面
 */
public class LaunchActivity extends Activity {
    private static final String TAG = "LaunchActivity";


    public static DataCenterClientService clientService = null;


    private ResponseHandler responseHandler;
    private ResContext resContext;
    private TimeRes timeRes;
    private  static final int MSG_SYNC_TIME=1001;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MyLog.e(TAG,"sync time");
            if(msg.what==MSG_SYNC_TIME){
                //连接服务器
                connectTcpIpServer();
                syncTime();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        initDdataPreference();
        autoWifiConnect();
    }

    private void initDdataPreference() {
        //初始化网络
        LogServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        SignalServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        VideoServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));

        //初始化设备
        DeviceEntity.getInstance().setDataPreference(new DataPreference(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //自动连接wifi
    private void autoWifiConnect() {
        ConnectWifiThread connectWifiThread = new ConnectWifiThread("JiaYing_ZXDC", "jyzxdcarm", 3, this);
//        ConnectWifiThread connectWifiThread = new ConnectWifiThread("TP-LINK_94D10A", "85673187", 3, this);
        connectWifiThread.start();
    }

    private void jumpToLoginActivity() {
        MyLog.e(TAG, "jumpToLoginActivity");
        DataPreference preference = new DataPreference(LaunchActivity.this);
       String nurse_id = preference.readStr("nurse_id");
        if(TextUtils.isEmpty(nurse_id)){
            LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
        }else{
            LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, FingerprintActivity.class));
        }
        finish();
    }

    //连服务器
    private void connectTcpIpServer() {
        ObservableZXDCSignalListenerThread observableZXDCSignalListenerThread = new ObservableZXDCSignalListenerThread();
        observableZXDCSignalListenerThread.start();
    }

    private class ConnectWifiThread extends Thread {
        private boolean wifiIsOk = false;
        private String SSID = null;
        private String PWD = null;
        private int TYPE = 0;
        private WifiAdmin wifiAdmin = null;

        public ConnectWifiThread(String SSID, String PWD, int TYPE, Context context) {
            this.SSID = SSID;
            this.PWD = PWD;
            this.TYPE = TYPE;
            wifiAdmin = new WifiAdmin(context);
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                //判断wifi是否已经打开
                if (wifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED) {
                    //连接网络
                    wifiIsOk = wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(SSID, PWD, TYPE));
                    //判断wifi是否已经连接上
                    MyLog.e(TAG, "wifiIsOk：" + wifiIsOk);
                    if (wifiIsOk) {
                        mHandler.sendEmptyMessageDelayed(MSG_SYNC_TIME,4000);
                        break;
                    }
                } else {
                    wifiAdmin.openWifi();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    private void startTimeService() {
        Intent it = new Intent(LaunchActivity.this, TimeService.class);
        it.putExtra("currenttime", ServerTime.curtime);
        startService(it);
    }
    private void syncTime() {
        resContext = new ResContext();
        resContext.open();
        responseHandler = new ResponseHandler();
        ObservableZXDCSignalListenerThread.addObserver(responseHandler);
        timeRes = new TimeRes();
        resContext.setCurState(timeRes);
    }

    private class ResponseHandler extends Handler implements Observer {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            msg.obj = (msg.obj == null) ? (Res.NOTHING) : (msg.obj);
            switch ((Res) msg.obj) {
                case TIMESTAMP:
                    resContext.handle((Res) msg.obj);
                    break;
            }

        }

        @Override
        public void update(Observable observable, Object data) {

            Message msg = Message.obtain();
            msg.obj = data;
            this.sendMessage(msg);

        }
    }
    private class TimeRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case TIMESTAMP:
                    resContext.setCurState(timeRes);
                    startTimeService();
                    jumpToLoginActivity();
                    break;

            }

        }
    }
    private class ResContext {
        private State state;

        private Boolean isOpen = true;

        public synchronized void open() {
            this.isOpen = true;
        }

        public synchronized void close() {
            this.isOpen = false;
        }

        public void setCurState(State state) {
            this.state = state;
        }

        private synchronized void handle(Res res) {
            if (isOpen) {
                state.handle(res);
            }
        }
    }

    private abstract class State {
        abstract void handle(Res res);
    }
}
