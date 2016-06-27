package com.jiaying.workstation.activity.launch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.softfan.dataCenter.DataCenterClientService;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.loginandout.LoginActivity;
import com.jiaying.workstation.activity.sensor.FaceCollectionActivity;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.DeviceEntity;
import com.jiaying.workstation.net.serveraddress.LogServer;
import com.jiaying.workstation.net.serveraddress.SignalServer;
import com.jiaying.workstation.net.serveraddress.VideoServer;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.utils.WifiAdmin;

/**
 * 启动页面，自动连接网络，连接上网络后，连接服务器，得到时间同步信号后跳转到护士登录界面
 */
public class LaunchActivity extends Activity {
    private Handler mHandler = new Handler();

    public static DataCenterClientService clientService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        //初始化网络
        LogServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        SignalServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));
        VideoServer.getInstance().setIdataPreference(new DataPreference(getApplicationContext()));

        //初始化设备
        DeviceEntity.getInstance().setDataPreference(new DataPreference(getApplicationContext()));
        connectTcpIpServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoWifiConnect();
    }

    //自动连接wifi
    private void autoWifiConnect() {
        ConnectWifiThread connectWifiThread = new ConnectWifiThread("JiaYing_ZXDC", "jyzxdcarm", 3, this);
        connectWifiThread.start();
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
                    if (wifiIsOk) {
                        //界面跳转
                        connectTcpIpServer();
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

        private void jumpToLoginActivity() {
            LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, LoginActivity.class));
        }
    }


}
