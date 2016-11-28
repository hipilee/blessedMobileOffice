package com.jiaying.workstation.activity.sensor;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.engine.FaceCollector;
import com.jiaying.workstation.engine.ProxyFaceCollector;
import com.jiaying.workstation.interfaces.IfaceCollector;
import com.jiaying.workstation.utils.SetTopView;
import com.jiaying.workstation.utils.ToastUtils;

/**
 * 作者：lenovo on 2016/3/12 10:07
 * 邮箱：353510746@qq.com
 * 功能：
 */
public class FaceCollectionActivity extends BaseActivity implements IfaceCollector.OnFaceCollectCallback {
    SurfaceView sfvBottom;

    SurfaceView sfvTop;
    ImageView ivFinger;
    SurfaceViewTopCallback sfvTopCallback;
    Button btnOk, btnAgain, btnFlash;
    Bitmap bitmapUI;
    String src, name, path, loginPath, succName, nameTemp;

    private IfaceCollector ifaceCollector;
    private ProxyFaceCollector proxyFaceCollector;

    @Override
    public void initVariables() {

    }

    @Override
    public void initView() {

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera_preview);
        new SetTopView(this, R.string.face_collect, true);

        //底层的sfvBottom用于显示实时的预览画面，ivUI在第二层显示矩形框，sfvTop在最顶层显示动画效果，ivFinger，用于显示获取到的指纹
        sfvBottom = (SurfaceView) findViewById(R.id.sfvFrame);
        ivFinger = (ImageView) findViewById(R.id.ivFinger);
        sfvTop = (SurfaceView) findViewById(R.id.sfvAnimation);

        sfvTopCallback = new SurfaceViewTopCallback();
        sfvTop.getHolder().addCallback(sfvTopCallback);


        //将最顶层的SurfaeView设置为透明,在该surface上可以画一些动画效果
        sfvTop.setZOrderOnTop(true);
        sfvTop.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        //read in the UI bitmap of collection interface
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;


        //采浆人脸
        ifaceCollector = FaceCollector.getInstance(this, sfvBottom);
        proxyFaceCollector = ProxyFaceCollector.getInstance(ifaceCollector);
        proxyFaceCollector.setOnCollectCallback(this);
        proxyFaceCollector.open();
        proxyFaceCollector.collect();

    }

    @Override
    public void loadData() {

    }



    private final class SurfaceViewTopCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

    }


    @Override
    public void onCollect(Bitmap bitmap, int originX, int originY, int width, int height) {
        if (bitmap != null) {
            ivFinger.setVisibility(View.VISIBLE);
            ivFinger.setImageBitmap(bitmap);
        }
    }
}
