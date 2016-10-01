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
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jiaying.workstation.R;

import com.jiaying.workstation.activity.MainActivity;
import com.jiaying.workstation.activity.ServerSettingActivity;
import com.jiaying.workstation.activity.loginandout.LoginActivity;
import com.jiaying.workstation.activity.plasmacollection.Res;

import com.jiaying.workstation.app.MobileofficeApp;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.DeviceEntity;
import com.jiaying.workstation.entity.NurseEntity;
import com.jiaying.workstation.entity.PlasmaMachineEntity;
import com.jiaying.workstation.entity.ServerTime;
import com.jiaying.workstation.net.serveraddress.LogServer;
import com.jiaying.workstation.net.serveraddress.SignalServer;
import com.jiaying.workstation.net.serveraddress.VideoServer;
import com.jiaying.workstation.service.TimeService;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.utils.ApiClient;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.ToastUtils;
import com.jiaying.workstation.utils.WifiAdmin;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 启动页面，自动连接网络，连接上网络后，连接服务器，得到时间同步信号后跳转到护士登录界面
 */
public class LaunchActivity extends Activity {
    private static final String TAG = "LaunchActivity";
    public static DataCenterClientService clientService = null;
    private TimeHandlerObserver timeHandlerObserver;
    private ObservableZXDCSignalListenerThread observableZXDCSignalListenerThread;
    private ResContext resContext;
    private TimeRes timeRes;
    private static final int MSG_SYNC_TIME = 1001;
    private static final int MSG_SYNC_TIME_OUT = 1002;
    private static final int SYNC_TIME_OUT = 60 * 1000;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MyLog.e(TAG, "sync time");
            if (msg.what == MSG_SYNC_TIME) {
                //连接网络成功后
                // 1.http请求设备状态
                // 2.连接物联网协议服务器,
                // 3.同时检测连接物联网协议是否超时
                loadData();
                connectTcpIpServer();
                checkSyncTimeOut();
            } else if (msg.what == MSG_SYNC_TIME_OUT) {
                //
                MyLog.e(TAG, "sync time out");
                if (!isFinishing()) {
                    LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, ServerSettingActivity.class));
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        MobileofficeApp app = (MobileofficeApp) getApplication();
        app.initCrash();
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

    @Override
    protected void onPause() {
        super.onPause();
        observableZXDCSignalListenerThread.deleteObserver(timeHandlerObserver);
    }
//检测等待时间信号是否超时

    private void checkSyncTimeOut() {
        SyncTimeoutThread syncTimeoutThread = new SyncTimeoutThread();
        syncTimeoutThread.start();
    }

    private class SyncTimeoutThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(SYNC_TIME_OUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(MSG_SYNC_TIME_OUT);
        }
    }

    //自动连接wifi
    private void autoWifiConnect() {
        ConnectWifiThread connectWifiThread = new ConnectWifiThread("JiaYing_ZXDC", "jyzxdcarm", 3, this);
//        ConnectWifiThread connectWifiThread = new ConnectWifiThread("TP-LINK_94D10A", "85673187", 3, this);
        connectWifiThread.start();
    }

    private void jumpActivity() {
        DataPreference preference = new DataPreference(LaunchActivity.this);
        String nurse_id = preference.readStr("nurse_id");
        MyLog.e(TAG, "nurse_id:" + nurse_id);

        if (nurse_id.equals("wrong")) {
            LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
        } else {
            //检查登录时效
            long loginedTime = preference.readLong("login_time");
            long currentTime = System.currentTimeMillis();
            MyLog.e(TAG, "loginedTime:" + loginedTime + ",currentTime:" + currentTime);
            if (loginedTime == -1 || ((currentTime - loginedTime >= 60 * 1000))) {
                LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
            } else {
                LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, MainActivity.class));
            }
        }
        finish();
    }


    //连服务器
    private void connectTcpIpServer() {
        observableZXDCSignalListenerThread = new ObservableZXDCSignalListenerThread();
        resContext = new ResContext();
        resContext.open();
        timeHandlerObserver = new TimeHandlerObserver();
        ObservableZXDCSignalListenerThread.addObserver(timeHandlerObserver);
        timeRes = new TimeRes();
        resContext.setCurState(timeRes);
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
                        mHandler.sendEmptyMessageDelayed(MSG_SYNC_TIME, 0);
                        break;
                    }
                } else {
                    MyLog.e(TAG, "open wifi");
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


    private class TimeHandlerObserver extends Handler implements Observer {
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
                    jumpActivity();
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


    //模拟得到浆机状态信息,正式数据需要删除
    private void getLocalTempPlasmaMachineList() {

        List<PlasmaMachineEntity> plasmaMachineEntityList = new ArrayList<PlasmaMachineEntity>();
        for (int i = 10001; i < 10020; i++) {
            PlasmaMachineEntity entity = new PlasmaMachineEntity();
            if (i % 2 == 0) {
                entity.setState(0);
            } else {
                entity.setState(1);
            }
            entity.setNurseName("name" + i);

            entity.setLocationID(i + "");
            plasmaMachineEntityList.add(entity);
        }
        MobileofficeApp.setPlasmaMachineEntityList(plasmaMachineEntityList);
    }

    private void loadData() {
        MyLog.e(TAG, "send locations request");
        ApiClient.get("locations", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                if (bytes != null && bytes.length > 0) {
                    String result = new String(bytes);
                    MyLog.e(TAG, "locations result:" + result);
                    if (!TextUtils.isEmpty(result)) {
                        List<PlasmaMachineEntity> plasmaMachineEntityList = JSON.parseArray(result, PlasmaMachineEntity.class);
                        if (plasmaMachineEntityList != null) {
                            MobileofficeApp.setPlasmaMachineEntityList(plasmaMachineEntityList);
                        } else {
                            getLocalTempPlasmaMachineList();
                        }
                    }
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                MyLog.e(TAG, "locations result fail reason:" + throwable.toString());
                getLocalTempPlasmaMachineList();
                ToastUtils.showToast(LaunchActivity.this, R.string.http_req_fail);
            }
        });
    }
}