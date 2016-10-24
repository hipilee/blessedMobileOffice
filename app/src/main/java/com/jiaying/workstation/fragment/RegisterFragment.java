package com.jiaying.workstation.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.plasmacollection.ManualIdentityCardActivity;
import com.jiaying.workstation.activity.sensor.IdentityCardActivity;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.constant.TypeConstant;
import com.jiaying.workstation.utils.DealFlag;

/**
 * 等级
 */
public class RegisterFragment extends Fragment {
    private Button register_btn, idcard_forget_btn, idcard_broken_btn;
    private DealFlag register_btn_flag, idcard_forget_flag, idcard_broken_flag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register_btn_flag = new DealFlag();
        idcard_forget_flag = new DealFlag();
        idcard_broken_flag = new DealFlag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        register_btn = (Button) view.findViewById(R.id.btn_register);
        idcard_forget_btn = (Button) view.findViewById(R.id.btn_idcard_forget);
        idcard_broken_btn = (Button) view.findViewById(R.id.btn_idcard_broken);

        ClickListener clickListener = new ClickListener();

        register_btn.setOnClickListener(clickListener);
        idcard_forget_btn.setOnClickListener(clickListener);
        idcard_broken_btn.setOnClickListener(clickListener);
        return view;
    }

    //登记 1.身份证 2.指纹 3.头像
    private class ClickListener implements View.OnClickListener {


        @Override
        public void onClick(View v) {
            if (register_btn_flag.isFirst() || idcard_forget_flag.isFirst() || idcard_broken_flag.isFirst()) {

                switch (v.getId()) {
                    case R.id.btn_register:
                        Intent itIdentityCardAct = new Intent(getActivity(), IdentityCardActivity.class);
                        itIdentityCardAct.putExtra(IntentExtra.EXTRA_TYPE, TypeConstant.TYPE_REG);
                        startActivity(itIdentityCardAct);
                        break;

                    case R.id.btn_idcard_forget:
                        Intent itManualIdentityCardAct = new Intent(getActivity(), ManualIdentityCardActivity.class);
                        itManualIdentityCardAct.putExtra(IntentExtra.EXTRA_TYPE, TypeConstant.TYPE_REG);
                        startActivity(itManualIdentityCardAct);
                        break;

                    case R.id.btn_idcard_broken:
                        Intent itManualIdentityCardActCopy = new Intent(getActivity(), ManualIdentityCardActivity.class);
                        itManualIdentityCardActCopy.putExtra(IntentExtra.EXTRA_TYPE, TypeConstant.TYPE_REG);
                        startActivity(itManualIdentityCardActCopy);
                        break;
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        register_btn_flag.reset();
        idcard_forget_flag.reset();
        idcard_broken_flag.reset();
    }
}
