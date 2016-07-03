package com.jiaying.workstation.activity;

import android.app.Activity;
import android.os.Bundle;


import com.jiaying.workstation.R;

/**
 * activity基类
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables();
        initView();
        loadData();
    }

    //初始化变量，包括Intent带的数据和Activity内的变量
    public abstract void initVariables();

    //加载layout布局文件，初始化控件，为控件挂上事件方法
    public abstract void initView();

    // 调用服务器API加载数据
    public abstract void loadData();

    @Override
    public void onBackPressed() {
    }
}
