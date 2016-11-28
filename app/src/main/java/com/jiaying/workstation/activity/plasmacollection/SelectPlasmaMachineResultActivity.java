package com.jiaying.workstation.activity.plasmacollection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.softfan.dataCenter.DataCenterClientService;
import android.softfan.dataCenter.task.DataCenterTaskCmd;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.constant.Constants;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.entity.IdentityCardEntity;
import com.jiaying.workstation.entity.PlasmaMachineEntity;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.interfaces.OnCountDownTimerFinishCallback;
import com.jiaying.workstation.utils.BitmapUtils;
import com.jiaying.workstation.utils.CountDownTimerUtil;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

//浆机分配给浆员的结果
public class SelectPlasmaMachineResultActivity extends BaseActivity implements OnCountDownTimerFinishCallback {
    private static final String TAG = "SelectPlasmaMachineResultActivity";
    private CountDownTimerUtil countDownTimerUtil;
    private TextView time_txt;
    private TextView number_txt;
    private TextView nameTextView = null;
    private TextView idCardNoTextView = null;
    private ImageView avaterImageView = null;
    private ProgressDialog allocDevDialog = null;

    //    private AlertDialog.Builder failAllocDialogBuilder, succAllocDialogBuilder;
    private AlertDialog failAllocDialog = null;
    private AlertDialog succAllocDialog = null;
    private IdentityCardEntity identityCardEntity;
    private PlasmaMachineEntity plasmaMachineEntity;
    private ResponseHandler responseHandler;
    private ResContext resContext;
    private NullRes nullRes;
    private SerRes serRes;
    private ZxdcRes zxdcRes;
    private TabletRes tabletRes;
    private SerAndZxdcRes serAndZxdcRes;
    private SerAndTablet serAndTablet;
    private TabletAndZxdc tabletAndZxdc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initVariables() {
        identityCardEntity = IdentityCardEntity.getIntance();
        plasmaMachineEntity = (PlasmaMachineEntity) getIntent().getSerializableExtra(IntentExtra.EXTRA_PLASMAMACHINE);


        resContext = new ResContext();
        resContext.open();

        responseHandler = new ResponseHandler();
        ObservableZXDCSignalListenerThread.addObserver(responseHandler);

        //收到信息的各种状态
        nullRes = new NullRes();
        serRes = new SerRes();
        zxdcRes = new ZxdcRes();
        tabletRes = new TabletRes();
        serAndZxdcRes = new SerAndZxdcRes();
        serAndTablet = new SerAndTablet();
        tabletAndZxdc = new TabletAndZxdc();

        resContext.setCurState(nullRes);

        allocateDev();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_pulp_machine_select_result);
        new SetTopView(this, R.string.title_activity_pulp_machine_select_result, false);
        time_txt = (TextView) findViewById(R.id.time_txt);
        number_txt = (TextView) findViewById(R.id.number_txt);
        number_txt.setText("请到" + plasmaMachineEntity.getLocationID() + "号浆机");
//        result_txt = (TextView) findViewById(R.id.result_txt);
//        state_txt = (TextView) findViewById(R.id.state_txt);
//        photo_image = (ImageView) findViewById(R.id.photo_image);

        nameTextView = (TextView) this.findViewById(R.id.name_txt);
        nameTextView.setText(identityCardEntity.getName());

        avaterImageView = (ImageView) this.findViewById(R.id.head_image);
        avaterImageView.setImageBitmap(identityCardEntity.getPhotoBmp());

        idCardNoTextView = (TextView) this.findViewById(R.id.id_txt);
        idCardNoTextView.setText(identityCardEntity.getIdcardno());

        //倒计时开始
        countDownTimerUtil = CountDownTimerUtil.getInstance(time_txt, this);
        countDownTimerUtil.start(Constants.COUNT_DOWN_TIME_10S);
        countDownTimerUtil.setOnCountDownTimerFinishCallback(this);
    }

    @Override
    public void loadData() {

    }

    @Override
    public void onFinish() {
        disAllocDevDialog();
        resContext.close();
        showFailDialog("设备分配失败", "分配超时");
    }

    //将浆员信息发送到服务器
    private void allocateDev() {
        DataCenterClientService clientService = ObservableZXDCSignalListenerThread.getClientService();
        if (clientService != null) {
            DataCenterTaskCmd retcmd = new DataCenterTaskCmd();

            constructConfirm_donorCmd(retcmd, identityCardEntity, plasmaMachineEntity.getLocationID());

            clientService.getApDataCenter().addSendCmd(retcmd);
            showProgress("设备分配中", "服务器（**）\n多媒体平板（**）\n单采机（**）");
        } else {
            MyLog.e(TAG, "clientService==null");
        }
    }

    private void constructConfirm_donorCmd(DataCenterTaskCmd retcmd, IdentityCardEntity identityCardEntity, String locationId) {
        //       retcmd.setSelfNotify(this);
        retcmd.setCmd("confirm_donor");
        retcmd.setHasResponse(true);
        retcmd.setLevel(2);
        HashMap<String, Object> values = new HashMap<>();
        values.put("donorId", identityCardEntity.getIdcardno());
        values.put("locationId", locationId);
        values.put("name", identityCardEntity.getName());
        values.put("gender", identityCardEntity.getSex());
        values.put("nationality", identityCardEntity.getNation());
        values.put("year", identityCardEntity.getYear());
        values.put("month", identityCardEntity.getMonth());
        values.put("day", identityCardEntity.getDay());
        values.put("address", identityCardEntity.getAddr());
        values.put("face", BitmapUtils.bitmapToBase64(identityCardEntity.getPhotoBmp()));
//        values.put("type",identityCardEntity.getType());
        retcmd.setValues(values);
    }

    /*分配设备超时后的对话框*/
    private void showFailDialog(String title, String msg) {

        if (isFinishing()) {
            return;
        }
        //AlertDialog.Builder normalDialog=new AlertDialog.Builder(getApplicationContext());
        AlertDialog.Builder failAllocDialogBuilder = new AlertDialog.Builder(this);
        failAllocDialogBuilder.setIcon(R.mipmap.ic_launcher);
        failAllocDialogBuilder.setTitle(title);
        failAllocDialogBuilder.setMessage(msg);


        failAllocDialogBuilder.setPositiveButton("重发", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //让失败对话框消失
                dialog.dismiss();

                //重新开始倒计时
                countDownTimerUtil = CountDownTimerUtil.getInstance(time_txt, SelectPlasmaMachineResultActivity.this);
                countDownTimerUtil.setOnCountDownTimerFinishCallback(SelectPlasmaMachineResultActivity.this);
                countDownTimerUtil.start(Constants.COUNT_DOWN_TIME_20S);

                resContext.open();
                //重新设置当前状态为空
                resContext.setCurState(nullRes);

                //再次发送讲员信息命令
                allocateDev();

                //显示分配进度对话框
                showProgress("设备分配中", "服务器（**）\n多媒体平板（**）\n单采机（**）");

            }
        });
        failAllocDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                countDownTimerUtil.cancel();
                finish();
                Log.e("showFailDialog", "放弃");
            }
        });
        if (failAllocDialog == null) {
            failAllocDialog = failAllocDialogBuilder.create();
        }
        failAllocDialog.setCancelable(false);
        failAllocDialog.setCanceledOnTouchOutside(false);
        failAllocDialog.show();
    }

    /*分配设备成功后的对话框*/
    private void showSuccesslDialog(String title, String msg) {
        if (isFinishing()) {
            return;
        }
        //AlertDialog.Builder normalDialog=new AlertDialog.Builder(getApplicationContext());
        AlertDialog.Builder succAllocDialogBuilder = new AlertDialog.Builder(this);
        succAllocDialogBuilder.setIcon(R.mipmap.ic_launcher);
        succAllocDialogBuilder.setTitle(title);
        succAllocDialogBuilder.setMessage(msg);

        succAllocDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        if (succAllocDialog == null) {
            succAllocDialog = succAllocDialogBuilder.create();
        }

        succAllocDialog.setCanceledOnTouchOutside(false);
        succAllocDialog.setCancelable(false);
        succAllocDialog.show();
    }

    private void showProgress(String title, String msg) {
        if (isFinishing()) {
            return;
        }
        if (allocDevDialog == null) {
            allocDevDialog = new ProgressDialog(this);
        }
        allocDevDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        allocDevDialog.setTitle(title);
        allocDevDialog.setMessage(msg);
        allocDevDialog.setIcon(R.mipmap.ic_launcher);
        allocDevDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                countDownTimerUtil.cancel();
                finish();
            }
        });

//        设置点击进度对话框外的区域对话框不消失
        allocDevDialog.setCanceledOnTouchOutside(false);
        allocDevDialog.setIndeterminate(false);
        allocDevDialog.setCancelable(false);
        allocDevDialog.show();
    }

    private class ResponseHandler extends Handler implements Observer {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //有些消息的msg.obj是null需要处理
            msg.obj = (msg.obj == null) ? (Res.NOTHING) : (msg.obj);
            switch ((Res) msg.obj) {
                case ALOC_SERVERRES_PASS:
                    resContext.handle((Res) msg.obj);
                    break;

                case ALOC_SERVERRES_NOT_PASS:
                    resContext.handle((Res) msg.obj);
                    break;

                case ALOC_TABLETRES:
                    resContext.handle((Res) msg.obj);
                    break;

                case ALOC_ZXDCRES:
                    resContext.handle((Res) msg.obj);
                    break;
            }

        }

        @Override
        public void update(Observable observable, Object data) {

            Message msg = Message.obtain();
            msg.obj = data;
            this.sendMessage(msg);

        }
    }

    private class ResContext {
        private State state;

        private Boolean isOpen = true;

        public synchronized void open() {
            this.isOpen = true;
        }

        public synchronized void close() {
            this.isOpen = false;
        }

        public void setCurState(State state) {
            this.state = state;
        }

        private synchronized void handle(Res res) {
            if (isOpen) {
                state.handle(res);
            }
        }
    }

    private abstract class State {
        abstract void handle(Res res);
    }

    private class NullRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {

                case ALOC_SERVERRES_PASS:
                    resContext.setCurState(serRes);
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（**）\n单采机（**）");
                    break;
                case ALOC_SERVERRES_NOT_PASS:
                    showFailDialog("设备分配失败", "今日不可献浆");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;
                case ALOC_TABLETRES:
                    resContext.setCurState(tabletRes);
                    showProgress("设备分配中", "服务器（**）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ALOC_ZXDCRES:
                    resContext.setCurState(zxdcRes);
                    showProgress("", "服务器（**）\n多媒体平板（**）\n单采机（应答）");
                    break;
            }
        }
    }

    private class SerRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {

                case ALOC_TABLETRES:
                    resContext.setCurState(serAndTablet);
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ALOC_ZXDCRES:
                    resContext.setCurState(serAndZxdcRes);
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（**）\n单采机（应答）");
                    break;
            }

        }
    }

    private class ZxdcRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ALOC_SERVERRES_PASS:
                    resContext.setCurState(serAndZxdcRes);
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（**）\n单采机（应答）");
                    break;
                case ALOC_SERVERRES_NOT_PASS:
                    showFailDialog("设备分配失败", "今日不可献浆");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;
                case ALOC_TABLETRES:
                    resContext.setCurState(tabletAndZxdc);
                    showProgress("设备分配中", "服务器（**）\n多媒体平板（应答）\n单采机（应答）");
                    break;
            }
        }
    }

    private class TabletRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ALOC_SERVERRES_PASS:
                    resContext.setCurState(serAndTablet);
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ALOC_SERVERRES_NOT_PASS:
                    showFailDialog("设备分配失败", "今日不可献浆");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;
                case ALOC_ZXDCRES:
                    resContext.setCurState(tabletAndZxdc);
                    showProgress("设备分配中", "服务器（**）\n多媒体平板（应答）\n单采机（应答）");
                    break;
            }
        }
    }

    private class SerAndZxdcRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ALOC_TABLETRES:
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getLocationID() + "号浆机！");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;

            }

        }
    }

    private class SerAndTablet extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ALOC_ZXDCRES:
                    showProgress("设备分配中", "服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getLocationID() + "号浆机！");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;

            }

        }
    }

    private class TabletAndZxdc extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ALOC_SERVERRES_PASS:
                    showProgress("设备分配", "服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getLocationID() + "号浆机！");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;

                case ALOC_SERVERRES_NOT_PASS:
                    showFailDialog("设备分配失败", "今日不可献浆");
                    countDownTimerUtil.cancel();
                    disAllocDevDialog();
                    break;

            }
        }
    }

    //关闭分配中对话框
    private void disAllocDevDialog() {
        if (!isFinishing()) {
            if (allocDevDialog != null) {
                allocDevDialog.dismiss();
                allocDevDialog = null;
            }
        }
    }
}
