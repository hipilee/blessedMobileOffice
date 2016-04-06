package com.jiaying.workstation.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.softfan.dataCenter.DataCenterClientService;
import android.softfan.dataCenter.config.DataCenterClientConfig;
import android.widget.RadioGroup;

import com.jiaying.workstation.R;
import com.jiaying.workstation.fragment.BloodPlasmaCollectionFragment;
import com.jiaying.workstation.fragment.DispatchFragment;
import com.jiaying.workstation.fragment.PhysicalExamFragment;
import com.jiaying.workstation.fragment.RegisterFragment;
import com.jiaying.workstation.fragment.SearchFragment;

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
        fragmentManager.beginTransaction().replace(R.id.fragment_container, new SearchFragment()).commit();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null) {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setTitle(R.string.app_name);
//        }
        mGroup = (RadioGroup) findViewById(R.id.group);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
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
    }

}
