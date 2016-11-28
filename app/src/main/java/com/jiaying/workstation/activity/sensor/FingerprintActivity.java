package com.jiaying.workstation.activity.sensor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.MainActivity;
import com.jiaying.workstation.activity.physicalexamination.PhysicalExamActivity;
import com.jiaying.workstation.activity.physicalexamination.PhysicalExamResultActivity;
import com.jiaying.workstation.activity.plasmacollection.SelectPlasmaMachineActivity;
import com.jiaying.workstation.activity.plasmacollection.SelectPlasmaMachineResultActivity;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.constant.TypeConstant;
import com.jiaying.workstation.engine.LdFingerprintReader;
import com.jiaying.workstation.engine.LdIdReader;
import com.jiaying.workstation.engine.ProxyFingerprintReader;
import com.jiaying.workstation.engine.ProxyIdReader;
import com.jiaying.workstation.interfaces.IfingerprintReader;
import com.jiaying.workstation.interfaces.IidReader;
import com.jiaying.workstation.interfaces.OnCountDownTimerFinishCallback;
import com.jiaying.workstation.utils.CountDownTimerUtil;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;

import java.lang.reflect.Type;

/*
指纹认证模块
 */
public class FingerprintActivity extends BaseActivity implements IfingerprintReader.OnFingerprintReadCallback,IfingerprintReader.OnFingerprintOpenCallback,OnCountDownTimerFinishCallback{
    private static final String TAG = "FingerprintActivity";
    private Handler mHandler = new Handler();
    private Runnable mRunnable = null;


    private IfingerprintReader ifingerprintReader = null;
    private ProxyFingerprintReader proxyFingerprintReader = null;
    //    private CountDownTimerUtil countDownTimerUtil;

    private TextView result_txt;
    private TextView state_txt;
    private ImageView photo_image;

    private TextView nameTextView = null;
    private TextView idCardNoTextView = null;
    private ImageView avaterImageView = null;
    private String donorName = null;
    private Bitmap avatarBitmap = null;
    private String idCardNO = null;
    private int source;

    private CountDownTimerUtil countDownTimerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initVariables() {

        Intent donorInfoIntent = getIntent();
        source = donorInfoIntent.getIntExtra("source", 0);
        switch (source) {
            case TypeConstant.TYPE_LOG:
                break;
            case TypeConstant.TYPE_REG:
                donorName = donorInfoIntent.getStringExtra("donorName");
                Bitmap tempBitmap = donorInfoIntent.getParcelableExtra("avatar");
                Matrix matrix = new Matrix();
                matrix.postScale(1.0f, 1.0f);
                avatarBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(),
                        tempBitmap.getHeight(), matrix, true);
                idCardNO = donorInfoIntent.getStringExtra("idCardNO");
                break;
            case TypeConstant.TYPE_SELECT_MACHINE:

                break;

        }

        //指纹识别准备
        ifingerprintReader = LdFingerprintReader.getInstance(this);
        proxyFingerprintReader = ProxyFingerprintReader.getInstance(ifingerprintReader);
        proxyFingerprintReader.setOnFingerprintReadCallback(this);
        proxyFingerprintReader.setOnFingerprintOpenCallback(this);

        proxyFingerprintReader.open();
    }

    private void showOpenResult(int status) {
        if (status == 1) {
            Toast.makeText(this, "打开指纹设备：成功",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "打开指纹设备：失败",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void initView() {
        setContentView(R.layout.activity_fingerprint);
        new SetTopView(this, R.string.title_activity_fingerprint, true);
        if (source == TypeConstant.TYPE_SELECT_MACHINE) {
            new SetTopView(this, R.string.read_worker_fp, true);
        }
        ImageView back_img  = (ImageView) findViewById(R.id.back_img);
        back_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealBackClickEvent();
            }
        });
        result_txt = (TextView) findViewById(R.id.result_txt);
        state_txt = (TextView) findViewById(R.id.state_txt);
        photo_image = (ImageView) findViewById(R.id.photo_image);
        //开始倒计时
        startTimer();


        switch (source) {

            case TypeConstant.TYPE_LOG:
                break;
            case TypeConstant.TYPE_REG:
                nameTextView = (TextView) this.findViewById(R.id.name_txt);
                nameTextView.setText(donorName);
                avaterImageView = (ImageView) this.findViewById(R.id.head_image);
                avaterImageView.setImageBitmap(avatarBitmap);
                idCardNoTextView = (TextView) this.findViewById(R.id.id_txt);
                idCardNoTextView.setText(idCardNO);
                break;
        }
    }

    @Override
    public void loadData() {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MyLog.e(TAG,"onResume:" + this.toString());
    }

    @Override
    public void onFingerPrintInfo(final Bitmap bitmap) {
        //指纹识别结果
        if (bitmap != null) {
//            countDownTimerUtil.cancel();
            FingerprintActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    countDownTimerUtil.cancel();
                    photo_image.setImageBitmap(convert(bitmap));
                }
            });


            //认证通过后跳到
            mRunnable = new runnable();
            mHandler.postDelayed(mRunnable, 1000);
        } else {
            FingerprintActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FingerprintActivity.this, "指纹设备异常",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onFingerPrintOpenInfo(int status) {
        showOpenResult(status);
        if (1 != status) {
            proxyFingerprintReader.close();
//            this.finish();
        } else {
            proxyFingerprintReader.read();
        }
    }

    private Bitmap convert(Bitmap a) {

        int w = a.getWidth();
        int h = a.getHeight();
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();
//        m.postScale(1, -1);   //镜像垂直翻转
        m.postScale(-1, 1);   //镜像水平翻转
//        m.postRotate(-90);  //旋转-90度
        Bitmap new2 = Bitmap.createBitmap(a, 0, 0, w, h, m, true);
        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()), new Rect(0, 0, w, h), null);

//        return newb;
//        Bitmap roundBitmap = BitmapUtils.makeRoundCorner(newb);
//        MyLog.e("ERROR",roundBitmap.getWidth() + ",height:" + roundBitmap.getHeight());
        return newb;

    }

    @Override
    public void onFinish() {
        dealBackClickEvent();
    }


    private class runnable implements Runnable {
        @Override
        public void run() {
            Intent it = null;
            int type = getIntent().getIntExtra("source", 0);
            if (type == TypeConstant.TYPE_REG) {
                //登记的话就到采集人脸
//                it = new Intent(FingerprintActivity.this, FaceCollectionActivity.class);
                it = new Intent(FingerprintActivity.this, FaceCollectionActivity.class);
            } else if (type == TypeConstant.TYPE_BLOODPLASMACOLLECTION) {
                //献浆的，去选择浆机
                it = new Intent(FingerprintActivity.this, SelectPlasmaMachineActivity.class);
            } else if (type == TypeConstant.TYPE_PHYSICAL_EXAM) {
                //体检，去体检
                it = new Intent(FingerprintActivity.this, PhysicalExamActivity.class);
            } else if (type == TypeConstant.TYPE_PHYSICAL_EXAM_SUBMIT_XJ) {
                //体检完成后提交体检，献浆员打指纹-》医生打指纹

                new SetTopView(FingerprintActivity.this, R.string.title_activity_fingerprint_xj, false);
                it = new Intent(FingerprintActivity.this, FingerprintActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                it.putExtra(IntentExtra.EXTRA_TYPE, TypeConstant.TYPE_PHYSICAL_EXAM_SUBMIT_DOC);
            } else if (type == TypeConstant.TYPE_PHYSICAL_EXAM_SUBMIT_DOC) {
                //体检完成后提交体检，医生打指纹后，显示体检结果
                new SetTopView(FingerprintActivity.this, R.string.title_activity_fingerprint_doc, false);
                it = new Intent(FingerprintActivity.this, PhysicalExamResultActivity.class);
            } else if (type == TypeConstant.TYPE_SELECT_MACHINE) {
                it = new Intent(FingerprintActivity.this, SelectPlasmaMachineResultActivity.class);
                it.putExtra(IntentExtra.EXTRA_PLASMAMACHINE, getIntent().getSerializableExtra(IntentExtra.EXTRA_PLASMAMACHINE));
            } else if (type == TypeConstant.TYPE_SELECT_MACHINE) {
                it = new Intent(FingerprintActivity.this, SelectPlasmaMachineResultActivity.class);
                it.putExtra(IntentExtra.EXTRA_PLASMAMACHINE, getIntent().getExtras());
                startActivity(it);
            } else {
                //其他的情况
                it = new Intent(FingerprintActivity.this, MainActivity.class);
            }
            if (it != null) {
                startActivity(it);
//                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        closeFingerReader();
        proxyFingerprintReader.close();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.e(TAG,"onDestroy:" + this.toString());
//        proxyFingerprintReader.close();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyLog.e(TAG,"onNewIntent=" + this.toString());
        startTimer();
        photo_image.setImageResource(R.mipmap.finger_print);
        proxyFingerprintReader.read();
    }

    /**
     * 处理返回按钮
     */
    private void dealBackClickEvent() {
        finishTimer();
        startActivity(new Intent(FingerprintActivity.this, MainActivity.class));
    }


    private void startTimer(){
        countDownTimerUtil = CountDownTimerUtil.getInstance(result_txt, this);
        countDownTimerUtil.setOnCountDownTimerFinishCallback(this);
        countDownTimerUtil.start();
    }
    private void finishTimer(){
        if (countDownTimerUtil != null) {
            countDownTimerUtil.cancel();
            countDownTimerUtil = null;
        }
    }
}
