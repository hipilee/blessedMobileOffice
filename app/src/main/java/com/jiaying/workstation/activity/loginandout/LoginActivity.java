package com.jiaying.workstation.activity.loginandout;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.MainActivity;
import com.jiaying.workstation.activity.ServerSettingActivity;
import com.jiaying.workstation.adapter.NurseAdapter;
import com.jiaying.workstation.db.DataPreference;
import com.jiaying.workstation.entity.NurseEntity;
import com.jiaying.workstation.net.http.ApiClient;
import com.jiaying.workstation.utils.DealFlag;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;
import com.jiaying.workstation.utils.ToastUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;


import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * 护士登陆界面
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    //nurseGridView 这三个对象形成了适配器模式
    private GridView nurseGridView;
    private NurseAdapter nurseAdapter;
    private List<NurseEntity> nurseList;
    //nurseGridView 这三个对象形成了适配器模式

    private DealFlag login_deal_flag;
    private ImageView iv_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {
        //该按钮用于防双击
        login_deal_flag = new DealFlag();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_login);
        new SetTopView(this, R.string.title_activity_pulp_machine_for_nurse, false);

        //使logo图片长按，可以进入服务器信息配置界面。
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.setEnabled(true);
        setIv_logoOnLongClickListener();

        nurseGridView = (GridView) findViewById(R.id.gridview);
        nurseList = new ArrayList<>();
        nurseAdapter = new NurseAdapter(nurseList, this);
        nurseGridView.setAdapter(nurseAdapter);
        nurseGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //选择护士后就指纹认证
                if (login_deal_flag.isFirst()) {
                    DataPreference preference = new DataPreference(LoginActivity.this);
                    preference.writeStr("nurse_id", nurseList.get(position).getId());
                    preference.writeLong("login_time", System.currentTimeMillis());
                    preference.commit();
//                    Intent it = new Intent(LoginActivity.this, FingerprintActivity.class);
                    Intent it = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(it);

                }
            }
        });
    }

    private void setIv_logoOnLongClickListener() {
        iv_logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent it = new Intent(LoginActivity.this, ServerSettingActivity.class);
                startActivity(it);
                return false;
            }
        });
    }

    @Override
    public void loadData() {
        ApiClient.get("users", new LoadNurseInfoHandler());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        login_deal_flag.reset();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class LoadNurseInfoHandler extends AsyncHttpResponseHandler {
        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            if (bytes != null && bytes.length > 0) {
                String result = new String(bytes);
                Log.e(TAG, "访问护士信息成功，信息为：" + result);
                if (!TextUtils.isEmpty(result)) {
                    List<NurseEntity> nurseEntityList = JSON.parseArray(result, NurseEntity.class);
                    if (nurseEntityList != null) {
                        nurseList.addAll(nurseEntityList);
                        nurseAdapter.notifyDataSetChanged();
                    }
                }
            } else {
                Log.e(TAG, "访问护士信息成功，无信息");
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            MyLog.e(TAG, "访问护士信息失败" + throwable.toString());
            ToastUtils.showToast(LoginActivity.this, R.string.http_req_fail);
        }
    }
}