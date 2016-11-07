package com.jiaying.workstation.activity.launch;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.softfan.dataCenter.DataCenterClientService;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.jiaying.workstation.R;

import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.ServerSettingActivity;
import com.jiaying.workstation.activity.loginandout.LoginActivity;
import com.jiaying.workstation.activity.plasmacollection.Res;
import com.jiaying.workstation.app.MobileofficeApp;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.DeviceEntity;
import com.jiaying.workstation.entity.PlasmaMachineEntity;
import com.jiaying.workstation.entity.ServerTime;
import com.jiaying.workstation.net.serveraddress.LogServer;
import com.jiaying.workstation.net.serveraddress.SignalServer;
import com.jiaying.workstation.net.serveraddress.VideoServer;
import com.jiaying.workstation.service.TimeService;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.net.http.ApiClient;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.ToastUtils;
import com.jiaying.workstation.utils.WifiAdmin;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 启动页面，自动连接网络，连接上网络后，连接服务器，得到时间同步信号后，获取现有设备具体信息，跳转到护士登录界面
 */
public class LaunchActivity extends BaseActivity {
    private static final String TAG = "LaunchActivity";
    private TimeHandlerObserver timeHandlerObserver;
    private ObservableZXDCSignalListenerThread observableZXDCSignalListenerThread;
    private ResContext resContext;
    private WaittingForTimeRes waittingForTimeRes;
    private static final int MSG_SYNC_TIME = 1001;
    private static final int MSG_SYNC_TIME_OUT = 1002;
    private static final int SYNC_TIME_OUT = 60 * 1000;
    private Handler mHandler = new TimeSynHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {
        MobileofficeApp app = (MobileofficeApp) getApplication();
        app.initCrash();
        initDdataPreference();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_launch);
    }

    @Override
    public void loadData() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoWifiConnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (observableZXDCSignalListenerThread != null && timeHandlerObserver != null)
            observableZXDCSignalListenerThread.deleteObserver(timeHandlerObserver);
    }

    private void initDdataPreference() {
        //初始化网络
        LogServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        SignalServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        VideoServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));

        //初始化设备
        DeviceEntity.getInstance().setDataPreference(new DataPreference(getApplicationContext()));
    }

    //自动连接wifi
    private void autoWifiConnect() {
        ConnectWifiThread connectWifiThread = new ConnectWifiThread("JiaYing_ZXDC", "jyzxdcarm", 3, this);
        //ConnectWifiThread connectWifiThread = new ConnectWifiThread("TP-LINK_94D10A", "85673187", 3, this);
        connectWifiThread.start();
    }

    //连接wifi的线程
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
            //无论何种情况都先关闭wifi，有些设备关闭打开wifi的时候可能都会有弹出框提示，
            // 这中提示是在wifi设置里面可以关闭的。
            wifiAdmin.closeWifi();
            while (true) {
                //判断wifi是否已经打开
                if (wifiAdmin.checkState() == WifiManager.WIFI_STATE_ENABLED) {
                    //连接网络
                    wifiIsOk = wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(SSID, PWD, TYPE));
                    //判断wifi是否已经连接上
                    MyLog.e(TAG, "wifiIsOk：" + wifiIsOk);
                    if (wifiIsOk) {
                        // wifi打开后，并和指定的wifi连上后，连接服务器
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

    private class TimeSynHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MyLog.e(TAG, "sync time");
            if (msg.what == MSG_SYNC_TIME) {
                //连接网络成功后

                // 1.连接物联网协议服务器
                connectTcpIpServer();

                // 2.同时检测连接物联网协议是否超时，超时时间为60S
                checkSyncTimeOut();
            } else if (msg.what == MSG_SYNC_TIME_OUT) {
                if (!isFinishing()) {
                    LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, ServerSettingActivity.class));
                    finish();
                }
            }
        }
    }

    //连服务器
    private void connectTcpIpServer() {
        //处理和服务器通信线程
        observableZXDCSignalListenerThread = new ObservableZXDCSignalListenerThread();

        //管理状态的上下文环境，设置当前状态为等待时间信号状态
        resContext = new ResContext();
        resContext.open();
        waittingForTimeRes = new WaittingForTimeRes();
        resContext.setCurState(waittingForTimeRes);

        //观察者开始观察服务器通信线程
        timeHandlerObserver = new TimeHandlerObserver();
        ObservableZXDCSignalListenerThread.addObserver(timeHandlerObserver);

        //服务器通信线程开始工作
        observableZXDCSignalListenerThread.start();
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

    private void startTimeService() {
        Intent itLauTimeSev = new Intent(LaunchActivity.this, TimeService.class);
        itLauTimeSev.putExtra("currenttime", ServerTime.curtime);
        startService(itLauTimeSev);
    }

    //TimeHandlerObserver WaittingForTimeRes ResContext State构建了观察者模式和状态模式
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

    private abstract class State {
        abstract void handle(Res res);
    }

    private class WaittingForTimeRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case TIMESTAMP:
                    //转换到时间已同步状态
                    resContext.setCurState(new NewState());

                    //启动时间服务
                    startTimeService();

                    //载入单采机信息
                    loadPlasmaMachineMsg();

                    break;
            }
        }
    }

    private class NewState extends State {

        @Override
        void handle(Res res) {

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


    //TimeHandlerObserver WaittingForTimeRes ResContext State构建了观察者模式和状态模式
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

    private void loadPlasmaMachineMsg() {
        ApiClient.get("locations", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                //网络是通的，都是进入该流程
                String result = new String(bytes);
                MyLog.e(TAG, "locations result success result:" + result);
                if (bytes != null && bytes.length > 0) {

                    MyLog.e(TAG, "locations result success result:" + result);
                    if (!TextUtils.isEmpty(result)) {
                        List<PlasmaMachineEntity> plasmaMachineEntityList = JSON.parseArray(result, PlasmaMachineEntity.class);
                        if (plasmaMachineEntityList != null) {
                            MobileofficeApp.setPlasmaMachineEntityList(plasmaMachineEntityList);
                        } else {
                            getLocalTempPlasmaMachineList();
                        }
                    }

                } else {
                    getLocalTempPlasmaMachineList();
                }
                jumpActivity();
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                MyLog.e(TAG, "locations result fail reason:" + throwable.toString());
                getLocalTempPlasmaMachineList();
                ToastUtils.showToast(LaunchActivity.this, R.string.http_req_fail);
                jumpActivity();
            }
        });
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
//                LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
            }
        }
        finish();
    }
}