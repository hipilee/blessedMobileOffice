package com.jiaying.workstation.activity.loginandout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.ServerSettingActivity;
import com.jiaying.workstation.activity.sensor.FingerprintActivity;
import com.jiaying.workstation.adapter.NurseAdapter;
import com.jiaying.workstation.entity.NurseEntity;
import com.jiaying.workstation.utils.ApiClient;
import com.jiaying.workstation.utils.DealFlag;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;
import com.jiaying.workstation.utils.ToastUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 护士分配浆机
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private GridView mGridView;
    private NurseAdapter mAdapter;
    private List<NurseEntity> mList;
    private DealFlag login_deal_flag;
    private ImageView iv_logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        login_deal_flag = new DealFlag();
    }

    @Override
    public void initVariables() {

    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_login);
        new SetTopView(this, R.string.title_activity_pulp_machine_for_nurse, false);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.setEnabled(true);
        iv_logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent it = new Intent(LoginActivity.this, ServerSettingActivity.class);
                startActivity(it);
                return false;
            }
        });
        mGridView = (GridView) findViewById(R.id.gridview);
        mList = new ArrayList<NurseEntity>();
        mAdapter = new NurseAdapter(mList, this);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //选择护士后就指纹认证
                if (login_deal_flag.isFirst()) {
                    Intent it = new Intent(LoginActivity.this, FingerprintActivity.class);

                    startActivity(it);
                }
            }
        });
        for (int i = 0; i < 10; i++) {
            NurseEntity nurseEntity = new NurseEntity();
            nurseEntity.setName("护士" + i);
            nurseEntity.setId("1232" + i);
           if(i%2==1){
               nurseEntity.setPhoto("http://img2.imgtn.bdimg.com/it/u=4240359214,139934804&fm=21&gp=0.jpg");
           }else{
               nurseEntity.setPhoto("http://img2.imgtn.bdimg.com/it/u=918496586,990160806&fm=21&gp=0.jpg");
           }
            mList.add(nurseEntity);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void loadData() {
        ApiClient.get("users/", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, org.apache.http.Header[] headers, byte[] bytes) {
                if(bytes!=null && bytes.length > 0){
                    String result = new String(bytes);
                    MyLog.e(TAG,"users result:"+ result);
                    if(!TextUtils.isEmpty(result)){
                        List<NurseEntity> nurseEntityList = JSON.parseArray(result,NurseEntity.class);
                        if(nurseEntityList !=null){
                            mList.addAll(nurseEntityList);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onFailure(int i, org.apache.http.Header[] headers, byte[] bytes, Throwable throwable) {
                ToastUtils.showToast(LoginActivity.this,R.string.http_req_fail);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        login_deal_flag.reset();
    }
}