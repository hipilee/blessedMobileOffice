package com.jiaying.workstation.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.plasmacollection.ManualDealIdCardActivity;
import com.jiaying.workstation.activity.plasmacollection.SelectPlasmaMachineActivity;
import com.jiaying.workstation.activity.sensor.FingerprintActivity;
import com.jiaying.workstation.activity.sensor.IdentityCardActivity;
import com.jiaying.workstation.utils.DealFlag;

/**
 * 采浆
 */
public class BloodPlasmaCollectionFragment extends Fragment {
    private Button nurse_login_btn;
    private Button pulp_btn;
    private DealFlag btn_collection_flag;

    private Button btn_idcard_forget;
    private Button btn_idcard_broken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plasma_collection, container, false);

        pulp_btn = (Button) view.findViewById(R.id.btn_collection);
        pulp_btn.setOnClickListener(new ClickListener());
        btn_collection_flag = new DealFlag();

        btn_idcard_forget = (Button) view.findViewById(R.id.btn_idcard_forget);
        btn_idcard_forget.setOnClickListener(new ClickListener());
        btn_idcard_broken = (Button) view.findViewById(R.id.btn_idcard_broken);
        btn_idcard_broken.setOnClickListener(new ClickListener());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        btn_collection_flag.reset();
    }

    //献浆
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (btn_collection_flag.isFirst()) {
                Intent it = null;
                switch (v.getId()) {
                    case R.id.btn_collection:
                        it = new Intent(getActivity(), IdentityCardActivity.class);
                        it.putExtra("type","normal");
                        break;
                    case R.id.btn_idcard_forget:
                        it = new Intent(getActivity(), ManualDealIdCardActivity.class);
                        it.putExtra("type","forgot");
                        break;
                    case R.id.btn_idcard_broken:
                        it = new Intent(getActivity(), ManualDealIdCardActivity.class);
                        it.putExtra("type","broken");
                        break;
                    default:
                        break;
                }
                if(it!=null){
                    startActivity(it);
                }

            }
        }
    }
}
