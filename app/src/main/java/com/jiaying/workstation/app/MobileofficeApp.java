package com.jiaying.workstation.app;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.jiaying.workstation.entity.PlasmaMachineEntity;
import com.jiaying.workstation.utils.CrashException;
import com.jiaying.workstation.utils.MyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：lenovo on 2016/6/23 21:41
 * 邮箱：353510746@qq.com
 * 功能：application
 */
public class MobileofficeApp extends Application {
    private static final String TAG = "MobileofficeApp";
    private static List<PlasmaMachineEntity> plasmaMachineEntityList = new ArrayList<PlasmaMachineEntity>();
    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.e(TAG,"Application onCreate");
        Fresco.initialize(this);
    }
    public void initCrash() {
        // 设置该CrashHandler为程序的默认处理器
//        CrashException catchExcep = new CrashException(this);
//        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
        CrashReport.initCrashReport(getApplicationContext(), "066679fa7d", true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MyLog.e(TAG,"Application onTerminate");
    }

    public static void setPlasmaMachineEntityList(List<PlasmaMachineEntity> list) {
        plasmaMachineEntityList.addAll(list);
    }

    public static List<PlasmaMachineEntity> getPlasmaMachineEntityList() {
        return plasmaMachineEntityList;
    }
    public static void updatePlasmaMachineEntityList(PlasmaMachineEntity plasmaMachineEntity) {
        if(plasmaMachineEntity==null){
            return;
        }
        String locationId = plasmaMachineEntity.getLocationID();
        boolean hasSameLocationId = false;
        for(int i = 0;i<plasmaMachineEntityList.size();i++){
            String location_id = plasmaMachineEntityList.get(i).getLocationID();
            if(locationId.equals(location_id)){
                hasSameLocationId = true;
                plasmaMachineEntityList.remove(i);
                plasmaMachineEntityList.add(plasmaMachineEntity);
                break;
            }
        }
        if(!hasSameLocationId){
            plasmaMachineEntityList.add(plasmaMachineEntity);
        }
    }
    public static void clearPlasmaMachineEntityList() {
        plasmaMachineEntityList.clear();
    }
}
