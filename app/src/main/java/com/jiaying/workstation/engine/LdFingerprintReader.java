package com.jiaying.workstation.engine;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.jiaying.workstation.constant.Constants;
import com.jiaying.workstation.interfaces.IfingerprintReader;
import com.jiaying.workstation.utils.ZA_finger;
import com.za.android060;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 作者：lenovo on 2016/3/8 13:44
 * 邮箱：353510746@qq.com
 * 功能：龙盾指纹识别
 */
public class LdFingerprintReader implements IfingerprintReader {

    private OnFingerprintReadCallback onFingerprintReadCallback;
    private Activity mActivity;
    private ZA_finger za_finger;
    private boolean fpflag = false;
    private boolean fpcharflag = false;
    private boolean fpmatchflag = false;
    private int fpcharbuf = 0;
    long ssart = System.currentTimeMillis();
    long ssend = System.currentTimeMillis();
    private Handler objHandler_fp;
    private HandlerThread thread;
    private static LdFingerprintReader ldFingerprintReader = null;

    android060 a6 = new android060();
    String TAG = "060";
    int DEV_ADDR = 0xffffffff;
    private Handler objHandler_3;
    String sdCardRoot = Environment
            .getExternalStorageDirectory()
            .getAbsolutePath();

    private int usborcomtype;///0 noroot  1root
    private int defDeviceType;
    private int defiCom;
    private int defiBaud;
    private String tag = "LdFingerprintReader";

    public LdFingerprintReader(Activity mActivity) {
        this.mActivity = mActivity;
        usborcomtype = 1;
        defDeviceType = 2;
        defiCom = 3;
        defiBaud = 12;
//******在主线程中执行读取指纹操作
        thread = new HandlerThread("MyHandlerThread");
        thread.start();
        objHandler_fp = new Handler(thread.getLooper());
//******在主线程中执行读取指纹操作

//********在主线程中执行读取指纹操作
//        objHandler_fp = new Handler();
//********在主线程中执行读取指纹操作

        za_finger = new ZA_finger();
    }

    public synchronized static LdFingerprintReader getInstance(Activity activity) {
        ldFingerprintReader = new LdFingerprintReader(activity);

        return ldFingerprintReader;
    }

    //打开设备
    @Override
    public int open() {

        za_finger.finger_power_on();

        int status = 0;
        if (1 == usborcomtype) {
            LongDunD8800_CheckEuq();
            status = a6.ZAZOpenDeviceEx(-1, defDeviceType, defiCom, defiBaud, 0, 0);
        } else {
            int fd = getrwusbdevices();
            status = a6.ZAZOpenDeviceEx(fd, defDeviceType, defiCom, defiBaud, 0, 0);
        }
        Log.e(tag, "open()");
        return status;
    }

    @Override
    public void read() {
        Log.e(tag, "read()");
        fpflag = true;
        fpcharflag = true;
        fpmatchflag = true;

        fpflag = false;
        readsfpimg();

    }

    public void readsfpimg() {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        objHandler_fp.postDelayed(fpTasks, 0);
    }

    private Runnable fpTasks = new Runnable() {
        public void run()// 运行该服务执行此函数
        {
            String temp = "";
            long timecount = 0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);
            if (fpflag) return;
            if (timecount > Constants.COUNT_DOWN_TIME_20S) {
                return;
            }
            int nRet = 0;
            Log.e("error", " " + DEV_ADDR);
            nRet = a6.ZAZGetImage(DEV_ADDR);
            if (nRet == 0) {
                int[] len = {0, 0};
                char[] Image = new char[256 * 288];
                a6.ZAZUpImage(DEV_ADDR, Image, len);
                String str = "/mnt/sdcard/test.bmp";
                a6.ZAZImgData2BMP(Image, str);

                Bitmap bmpDefaultPic;
                bmpDefaultPic = BitmapFactory.decodeFile(str, null);

                onFingerprintReadCallback.onFingerPrintInfo(bmpDefaultPic);
            } else if (nRet == a6.PS_NO_FINGER) {

                objHandler_fp.postDelayed(fpTasks, 100);

            } else if (nRet == a6.PS_GET_IMG_ERR) {

                objHandler_fp.postDelayed(fpTasks, 100);
                return;
            } else {
                onFingerprintReadCallback.onFingerPrintInfo(null);
                return;
            }
        }
    };

    public int LongDunD8800_CheckEuq() {
        Process process = null;
        DataOutputStream os = null;


        String path = "/dev/bus/usb/00*/*";
        String path1 = "/dev/bus/usb/00*/*";
        File fpath = new File(path);
        Log.d("*** LongDun D8800 ***", " check path:" + path);

        String command = "chmod 777 " + path;
        String command1 = "chmod 777 " + path1;
        Log.d("*** LongDun D8800 ***", " exec command:" + command);
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return 1;
        } catch (Exception e) {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: " + e.getMessage());
        }
        //  }
        //  }
        return 0;
    }

    /**
     *
     */
    public int getrwusbdevices() {

        // get FileDescriptor by Android USB Host API
        UsbManager mUsbManager = (UsbManager) mActivity
                .getSystemService(Context.USB_SERVICE);

        final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mActivity, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        BroadcastReceiver mUsbReceiver = null;
        mActivity.registerReceiver(mUsbReceiver, filter);
        Log.i(TAG, "zhw 060");
        int fd = -1;
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.i(TAG,
                    device.getDeviceName() + " "
                            + Integer.toHexString(device.getVendorId()) + " "
                            + Integer.toHexString(device.getProductId()));

            if ((device.getVendorId() == 0x2109)
                    && (0x7638 == device.getProductId())) {
                Log.d(TAG, " get FileDescriptor ");
                mUsbManager.requestPermission(device, mPermissionIntent);
                while (!mUsbManager.hasPermission(device)) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (mUsbManager.hasPermission(device)) {
                    if (mUsbManager
                            .openDevice(device) != null) {
                        fd = mUsbManager
                                .openDevice(device).getFileDescriptor();
                        Log.d(TAG, " get FileDescriptor fd " + fd);
                        return fd;
                    } else
                        Log.e(TAG, "UsbManager openDevice failed");

                    mUsbManager.openDevice(device).close();
                }
                break;
            }

        }

        return 0;
    }

    @Override
    public int close() {
        Log.e(tag, "close()-1");
        byte[] tmp = {5, 6, 7};
        //a6.ZAZBT_rev(tmp, tmp.length);
        objHandler_fp.removeCallbacks(fpTasks);
        int status = a6.ZAZCloseDeviceEx();
        za_finger.finger_power_off();
        return status;
    }

    @Override
    public void setOnFingerprintReadCallback(OnFingerprintReadCallback onFingerprintReadCallback) {
        this.onFingerprintReadCallback = onFingerprintReadCallback;
    }

}
