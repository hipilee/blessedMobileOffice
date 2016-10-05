package com.jiaying.workstation.activity.plasmacollection;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.entity.IdentityCardEntity;
import com.jiaying.workstation.utils.SetTopView;
import com.jiaying.workstation.utils.ToastUtils;

/**
 * 作者：lenovo on 2016/10/3 11:42
 * 邮箱：353510746@qq.com
 * 功能：手动操作身份证(损坏或者破坏)
 */
public class ManualDealIdCardActivity extends BaseActivity {
    private EditText et_idcard;
    private Button btn_submit;
    private String idCardNO;
    private String type;

    @Override
    public void initVariables() {
        type = getIntent().getStringExtra("type");
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_maual_deal_idcard);
        new SetTopView(this, R.string.input_idcard, true);
        et_idcard = (EditText) findViewById(R.id.et_idcard);
//        et_idcard.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_CLASS_NUMBER);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idCardNO = et_idcard.getText().toString();
                if (TextUtils.isEmpty(idCardNO)) {
                    ToastUtils.showToast(ManualDealIdCardActivity.this, R.string.input_idcard_tip);
                    return;
                }
//                if (idCardNO.length() != 18) {
//                    ToastUtils.showToast(ManualDealIdCardActivity.this, R.string.input_idcard_wrong_tip);
//                    return;
//                }
                goToSelectionMachine();
            }
        });
    }

    @Override
    public void loadData() {

    }

    private void goToSelectionMachine() {

        //读取到了身份证信息
        IdentityCardEntity card = IdentityCardEntity.getIntance();
        card.setName(null);
        card.setSex(null);
        card.setAddr(null);
        card.setNation(null);
        card.setYear(null);
        card.setMonth(null);
        card.setDay(null);
        card.setIdcardno(idCardNO);
        card.setPhotoBmp(null);
        card.setType(type);
        Intent it = new Intent(ManualDealIdCardActivity.this, ShowDonorInfoActivity.class);
        startActivity(it);
        finish();
    }
}
