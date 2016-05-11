package com.jiaying.workstation.activity.plasmacollection;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
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
    private String donorName = null;
    private Bitmap avatarBitmap = null;
    private String idCardNO = null;
    private TextView result_txt;
    private TextView state_txt;
    private ImageView photo_image;
    private TextView number_txt;
    private TextView nameTextView = null;
    private TextView idCardNoTextView = null;
    private ImageView avaterImageView = null;
    private ProgressDialog allocDevDialog;
    private AlertDialog.Builder failAllocDialogBuilder, succAllocDialogBuilder;
    private AlertDialog failAllocDialog, succAllocDialog;

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

        responseHandler = new ResponseHandler();
        ObservableZXDCSignalListenerThread.addObserver(responseHandler);

        nullRes = new NullRes();
        serRes = new SerRes();
        zxdcRes = new ZxdcRes();
        tabletRes = new TabletRes();
        serAndZxdcRes = new SerAndZxdcRes();
        serAndTablet = new SerAndTablet();
        tabletAndZxdc = new TabletAndZxdc();
        resContext = new ResContext();

        resContext.setCurState(nullRes);

        sendToTcpIpServer();
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_pulp_machine_select_result);
        new SetTopView(this, R.string.title_activity_pulp_machine_select_result, false);
        time_txt = (TextView) findViewById(R.id.time_txt);
        number_txt = (TextView) findViewById(R.id.number_txt);
        number_txt.setText("请到" + plasmaMachineEntity.getNumber() + "号浆机");
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
        countDownTimerUtil.start(Constants.COUNT_DOWN_TIME_10S);
        countDownTimerUtil.setOnCountDownTimerFinishCallback(this);
    }

    @Override
    public void loadData() {

    }


    @Override
    public void onFinish() {
        MyLog.e(TAG, "超时开始");
        MyLog.e(TAG, "" + allocDevDialog.toString());
        allocDevDialog.dismiss();
        allocDevDialog.cancel();
        showFailDialog("设备分配失败", "分配超时");
        MyLog.e(TAG, "超时结束");
    }

    //将浆员信息发送到服务器
    private void sendToTcpIpServer() {
        DataCenterClientService clientService = ObservableZXDCSignalListenerThread.getClientService();
        if (clientService != null) {
            DataCenterTaskCmd retcmd = new DataCenterTaskCmd();

            constructConfirm_donorCmd(retcmd, identityCardEntity, plasmaMachineEntity.getId());

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
        MyLog.e(TAG, "locationId:" + plasmaMachineEntity.getId());
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

    /*分配设备超时后的对话框*/
    private void showFailDialog(String title, String msg) {
        //AlertDialog.Builder normalDialog=new AlertDialog.Builder(getApplicationContext());
        failAllocDialogBuilder = new AlertDialog.Builder(this);
        failAllocDialogBuilder.setIcon(R.mipmap.ic_launcher);
        failAllocDialogBuilder.setTitle(title);
        failAllocDialogBuilder.setMessage(msg);


        failAllocDialogBuilder.setPositiveButton("重发", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //让失败对话框消失
                failAllocDialog.dismiss();

                //重新开始倒计时
                countDownTimerUtil = CountDownTimerUtil.getInstance(time_txt, SelectPlasmaMachineResultActivity.this);
                countDownTimerUtil.setOnCountDownTimerFinishCallback(SelectPlasmaMachineResultActivity.this);
                countDownTimerUtil.start(Constants.COUNT_DOWN_TIME_10S);

                //重新设置当前状态为空
                resContext.setCurState(nullRes);

                //再次发送讲员信息命令
                sendToTcpIpServer();

                //显示分配进度对话框
                showProgress("设备分配中", "服务器（**）\n多媒体平板（**）\n单采机（**）");

            }
        });
        failAllocDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                Log.e("showFailDialog", "放弃");
            }
        });
        failAllocDialog = failAllocDialogBuilder.create();
        failAllocDialog.setCanceledOnTouchOutside(false);
        failAllocDialog.show();
    }

    /*分配设备成功后的对话框*/
    private void showSuccesslDialog(String title, String msg) {
        //AlertDialog.Builder normalDialog=new AlertDialog.Builder(getApplicationContext());
        succAllocDialogBuilder = new AlertDialog.Builder(this);
        succAllocDialogBuilder.setIcon(R.mipmap.ic_launcher);
        succAllocDialogBuilder.setTitle(title);
        succAllocDialogBuilder.setMessage(msg);

        succAllocDialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
            }
        });

        succAllocDialog = succAllocDialogBuilder.create();
        succAllocDialog.setCanceledOnTouchOutside(false);
        succAllocDialog.setCancelable(false);
        succAllocDialog.show();
    }

    private void showProgress(String title, String msg) {
        allocDevDialog = new ProgressDialog(this);
        //实例化
        allocDevDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //设置进度条风格，风格为圆形，旋转的
        allocDevDialog.setTitle(title);
        //设置ProgressDialog 标题
        allocDevDialog.setMessage(msg);
        //设置ProgressDialog 提示信息
        allocDevDialog.setIcon(R.mipmap.ic_launcher);
        //设置ProgressDialog 标题图标
        allocDevDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                countDownTimerUtil.cancel();
                finish();
                Log.e("showFailDialog", "放弃");
            }
        });

//        设置点击进度对话框外的区域对话框不消失
        allocDevDialog.setCanceledOnTouchOutside(false);
        //设置ProgressDialog 的一个Button
        allocDevDialog.setIndeterminate(false);
        //设置ProgressDialog 的进度条是否不明确
        allocDevDialog.setCancelable(false);
        //设置ProgressDialog 是否可以按退回按键取消
        allocDevDialog.show();
    }

    private class ResponseHandler extends Handler implements Observer {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //有些消息的msg.obj是null需要处理
            msg.obj = (msg.obj == null) ? (Res.NOTHING) : (msg.obj);
            switch ((Res) msg.obj) {
                case SERVERRES:
                    resContext.handle((Res) msg.obj);
                    break;

                case TABLETRES:
                    resContext.handle((Res) msg.obj);
                    break;

                case ZXDCRES:
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

        public void setCurState(State state) {
            this.state = state;
        }

        private synchronized void handle(Res res) {
            state.handle(res);
        }
    }

    private abstract class State {
        abstract void handle(Res res);
    }

    private class NullRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {

                case SERVERRES:
                    resContext.setCurState(serRes);
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（**）\n单采机（**）");
                    break;
                case TABLETRES:
                    resContext.setCurState(tabletRes);
                    allocDevDialog.setMessage("服务器（**）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ZXDCRES:
                    resContext.setCurState(zxdcRes);
                    allocDevDialog.setMessage("服务器（**）\n多媒体平板（**）\n单采机（应答）");
                    break;
            }
        }
    }

    private class SerRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {

                case TABLETRES:
                    resContext.setCurState(serAndTablet);
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ZXDCRES:
                    resContext.setCurState(serAndZxdcRes);
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（**）\n单采机（应答）");
                    break;
            }

        }
    }

    private class ZxdcRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case SERVERRES:
                    resContext.setCurState(serAndZxdcRes);
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（**）\n单采机（应答）");
                    break;
                case TABLETRES:
                    resContext.setCurState(tabletAndZxdc);
                    allocDevDialog.setMessage("服务器（**）\n多媒体平板（应答）\n单采机（应答）");
                    break;
            }
        }
    }

    private class TabletRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case SERVERRES:
                    resContext.setCurState(serAndTablet);
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（应答）\n单采机（**）");
                    break;
                case ZXDCRES:
                    resContext.setCurState(tabletAndZxdc);
                    allocDevDialog.setMessage("服务器（**）\n多媒体平板（应答）\n单采机（应答）");
                    break;
            }
        }
    }

    private class SerAndZxdcRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case TABLETRES:
                    countDownTimerUtil.cancel();
                    allocDevDialog.dismiss();
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getId() + "号浆机！");
                    break;

            }

        }
    }

    private class SerAndTablet extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ZXDCRES:
                    countDownTimerUtil.cancel();
                    allocDevDialog.dismiss();
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getId() + "号浆机！");
                    break;

            }

        }
    }

    private class TabletAndZxdc extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case SERVERRES:
                    countDownTimerUtil.cancel();
                    allocDevDialog.dismiss();
                    allocDevDialog.setMessage("服务器（应答）\n多媒体平板（应答）\n单采机（应答）");
                    showSuccesslDialog("设备分配成功", "请到" + plasmaMachineEntity.getId() + "号浆机！");
                    break;

            }
        }
    }

}
