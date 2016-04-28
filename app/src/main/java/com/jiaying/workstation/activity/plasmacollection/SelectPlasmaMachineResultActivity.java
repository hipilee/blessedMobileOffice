package com.jiaying.workstation.activity.plasmacollection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.softfan.dataCenter.DataCenterClientService;
import android.softfan.dataCenter.task.DataCenterTaskCmd;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.DispatchStateListActivity;
import com.jiaying.workstation.constant.Constants;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.constant.TypeConstant;
import com.jiaying.workstation.entity.IdentityCardEntity;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.interfaces.OnCountDownTimerFinishCallback;
import com.jiaying.workstation.utils.BitmapUtils;
import com.jiaying.workstation.utils.CountDownTimerUtil;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;
import com.jiaying.workstation.utils.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

//浆机分配给浆员的结果
public class SelectPlasmaMachineResultActivity extends BaseActivity implements OnCountDownTimerFinishCallback {
    private static final String TAG = "SelectPlasmaMachineResultActivity";
    private CountDownTimerUtil countDownTimerUtil;
    private TextView time_txt;
    private String donorName = null;
    private Bitmap avatarBitmap = null;
    private String idCardNO = null;
    private TextView result_txt;
    private TextView state_txt;
    private ImageView photo_image;

    private TextView nameTextView = null;
    private TextView idCardNoTextView = null;
    private ImageView avaterImageView = null;
    private IdentityCardEntity identityCardEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initVariables() {
        identityCardEntity = IdentityCardEntity.getIntance();
        sendToTcpIpServer();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_pulp_machine_select_result);
        new SetTopView(this, R.string.title_activity_pulp_machine_select_result, false);
        time_txt = (TextView) findViewById(R.id.time_txt);
        result_txt = (TextView) findViewById(R.id.result_txt);
        state_txt = (TextView) findViewById(R.id.state_txt);
        photo_image = (ImageView) findViewById(R.id.photo_image);

        nameTextView = (TextView) this.findViewById(R.id.name_txt);
        nameTextView.setText(identityCardEntity.getName());

        avaterImageView = (ImageView) this.findViewById(R.id.head_image);
        avaterImageView.setImageBitmap(identityCardEntity.getPhotoBmp());

        idCardNoTextView = (TextView) this.findViewById(R.id.id_txt);
        idCardNoTextView.setText(identityCardEntity.getIdcardno());

        //倒计时开始
        countDownTimerUtil = CountDownTimerUtil.getInstance(time_txt, this);
        countDownTimerUtil.start(Constants.COUNT_DOWN_TIME_5S);
        countDownTimerUtil.setOnCountDownTimerFinishCallback(this);
    }

    @Override
    public void loadData() {

    }


    @Override
    public void onFinish() {
        ToastUtils.showToast(this, R.string.identify_time_out);
        finish();
    }

    //将浆员信息发送到服务器
    private void sendToTcpIpServer() {
        DataCenterClientService clientService = ObservableZXDCSignalListenerThread.getClientService();
        if (clientService != null) {
            DataCenterTaskCmd retcmd = new DataCenterTaskCmd();

            constructConfirm_donorCmd(retcmd,identityCardEntity,"10002");

            clientService.getApDataCenter().addSendCmd(retcmd);
        } else {
            MyLog.e(TAG, "clientService==null");
        }
    }

    private void constructConfirm_donorCmd(DataCenterTaskCmd retcmd,IdentityCardEntity identityCardEntity,String locationId){
        //       retcmd.setSelfNotify(this);
        retcmd.setCmd("confirm_donor");
        retcmd.setHasResponse(true);
        retcmd.setLevel(2);
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("donorId", identityCardEntity.getIdcardno());
        values.put("locationId", "10002");
        values.put("name", identityCardEntity.getName());
        values.put("gender", identityCardEntity.getSex());
        values.put("nationality", identityCardEntity.getNation());
        values.put("year", identityCardEntity.getYear());
        values.put("month", identityCardEntity.getMonth());
        values.put("day", identityCardEntity.getDay());
        values.put("address", identityCardEntity.getAddr());
        values.put("face", BitmapUtils.bitmapToBase64(identityCardEntity.getPhotoBmp()));
        retcmd.setValues(values);
    }
}
