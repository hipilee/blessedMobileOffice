package com.jiaying.workstation.activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.softfan.dataCenter.DataCenterClientService;
import android.softfan.dataCenter.config.DataCenterClientConfig;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.jiaying.workstation.R;
import com.jiaying.workstation.app.MobileofficeApp;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.DeviceEntity;
import com.jiaying.workstation.fragment.BloodPlasmaCollectionFragment;
import com.jiaying.workstation.fragment.DispatchFragment;
import com.jiaying.workstation.fragment.PhysicalExamFragment;
import com.jiaying.workstation.fragment.RegisterFragment;
import com.jiaying.workstation.fragment.SearchFragment;
import com.jiaying.workstation.net.serveraddress.LogServer;
import com.jiaying.workstation.net.serveraddress.SignalServer;
import com.jiaying.workstation.net.serveraddress.VideoServer;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.utils.ToastUtils;

/**
 * 主界面包括（建档，登记，体检，采浆，调度四大部分；以及一个查询）
 */
public class MainActivity extends BaseActivity {
    private FragmentManager fragmentManager;

    private RadioGroup mGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new BloodPlasmaCollectionFragment()).commit();

        ((RadioButton)findViewById(R.id.btn_1)).setVisibility(View.GONE);
        ((RadioButton)findViewById(R.id.btn_2)).setVisibility(View.GONE);
        ((RadioButton)findViewById(R.id.btn_3)).setVisibility(View.GONE);
        ((RadioButton)findViewById(R.id.btn_5)).setVisibility(View.GONE);
        ((RadioButton)findViewById(R.id.btn_6)).setVisibility(View.GONE);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);

        mGroup = (RadioGroup) findViewById(R.id.group);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_1:

                        break;
                    case R.id.btn_2:
                        //登记
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new RegisterFragment()).commit();
                        break;
                    case R.id.btn_3:
                        //体检
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new PhysicalExamFragment()).commit();
                        break;
                    case R.id.btn_4:
                        //采集血浆
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new BloodPlasmaCollectionFragment()).commit();
                        break;
                    case R.id.btn_5:
                        //调度
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new DispatchFragment()).commit();
                        break;
                    case R.id.btn_6:
                        //查询
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
                        break;
                }


            }
        });
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initVariables() {
        //连服务器
//        connectTcpIpServer();
    }
    //退出
    private void loginOut() {
        MobileofficeApp.clearPlasmaMachineEntityList();
        DataPreference preference = new DataPreference(MainActivity.this);
        preference.writeStr("nurse_id","wrong");
        preference.commit();
    }

}
