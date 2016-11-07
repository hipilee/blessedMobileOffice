package com.jiaying.workstation.activity.plasmacollection;

;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.register.RegisterResultActivity;
import com.jiaying.workstation.activity.sensor.FaceCollectionActivity;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.constant.TypeConstant;
import com.jiaying.workstation.entity.IdentityCardEntity;
import com.jiaying.workstation.utils.SetTopView;

public class ShowDonorInfoActivity extends BaseActivity {
    private IdentityCardEntity identityCardEntity;
    private Button btn_sure;
    private int source;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {
        identityCardEntity = IdentityCardEntity.getIntance();
        source = getIntent().getIntExtra(IntentExtra.EXTRA_TYPE, 0);
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_show_donor_info);
        new SetTopView(this, R.string.title_activity_show_donor_info, true);

        //点击确定按钮后，界面跳转到选择浆机界面。
        setBtnSureClickEvent();

        //显示浆员信息
        setDonorInfoUi();
    }

    @Override
    public void loadData() {

    }

    private void setBtnSureClickEvent() {
        btn_sure = (Button) this.findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (source) {
                    case TypeConstant.TYPE_REG:
                        goToFaceCollectionAct();
                        break;

                    case TypeConstant.TYPE_BLOODPLASMACOLLECTION:
                        goToSelectMachineAct();
                        break;
                }
            }

            private void goToSelectMachineAct() {
                Intent intent = new Intent(ShowDonorInfoActivity.this, SelectPlasmaMachineActivity.class);
                finish();
                startActivity(intent);
            }

            private void goToFaceCollectionAct() {
                Intent intent = new Intent(ShowDonorInfoActivity.this, RegisterResultActivity.class);
                finish();
                startActivity(intent);
            }
        });
    }


    private void setDonorInfoUi() {
        TextView tv_name = (TextView) this.findViewById(R.id.tv_name);
        tv_name.setText(identityCardEntity.getName());

        TextView tv_sex = (TextView) this.findViewById(R.id.tv_sex);
        tv_sex.setText(identityCardEntity.getSex());

        TextView tv_nation = (TextView) this.findViewById(R.id.tv_nation);
        tv_nation.setText(identityCardEntity.getNation());

        TextView tv_birthday = (TextView) this.findViewById(R.id.tv_birthday);
        String strBirthday = identityCardEntity.getYear() + " 年 " + identityCardEntity.getMonth() + " 月 " + identityCardEntity.getDay() + " 日 ";
        tv_birthday.setText(strBirthday);

        TextView tv_address = (TextView) this.findViewById(R.id.tv_address);
        tv_address.setText(identityCardEntity.getAddr());

        TextView tv_idcard = (TextView) this.findViewById(R.id.tv_idcard);
        tv_idcard.setText(identityCardEntity.getIdcardno());

        ImageView imageView = (ImageView) this.findViewById(R.id.iv_head);
        imageView.setImageBitmap(identityCardEntity.getPhotoBmp());
    }

    private void showAuthResultDialog() {

    }


}
