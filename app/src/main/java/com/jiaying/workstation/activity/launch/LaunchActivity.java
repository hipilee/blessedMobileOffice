package com.jiaying.workstation.activity.launch;

import android.app.Activity;
import android.content.Intent;
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

/**
 * 启动页面，三秒后跳转到选择护士界面
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

        // 服务器联通过后才跳转到登陆界面
        mHandler.postDelayed(new runnable(), 3000);
    }

    private class runnable implements Runnable {
        @Override
        public void run() {
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent it = new Intent(LaunchActivity.this, LoginActivity.class);
        startActivity(it);
        finish();
    }

    //连服务器
    private void connectTcpIpServer() {
        ObservableZXDCSignalListenerThread observableZXDCSignalListenerThread = new ObservableZXDCSignalListenerThread();
        observableZXDCSignalListenerThread.start();
    }

}
