package com.jiaying.workstation.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.softfan.dataCenter.DataCenterClientService;
import android.softfan.dataCenter.DataCenterException;
import android.softfan.dataCenter.DataCenterRun;
import android.softfan.dataCenter.IDataCenterProcess;
import android.softfan.dataCenter.config.DataCenterClientConfig;
import android.softfan.dataCenter.task.DataCenterTaskCmd;
import android.softfan.dataCenter.task.IDataCenterNotify;
import android.softfan.util.textUnit;
import android.util.Log;

import com.jiaying.workstation.activity.plasmacollection.Res;
import com.jiaying.workstation.utils.MyLog;

//import com.cylinder.www.env.Signal;
//import com.cylinder.www.env.net.FilterSignal;
//import com.cylinder.www.env.net.RecordState;
//import com.cylinder.www.env.person.businessobject.Donor;

/**
 * Created by hipilee on 2014/11/19.
 */
// Consider using AsyncTask or HandlerThread
public class ObservableZXDCSignalListenerThread extends Thread implements IDataCenterNotify, IDataCenterProcess {
    private static final String TAG = "ObservableZXDCSignalListenerThread";
    private static ObservableHint observableHint;

    private Boolean isContinue = true;
    private String ap = "libo2";
    private String org = "*";
//	private RecordState recordState;
//	private RecoverState recoverState;
//	private FilterSignal filterSignal;
//	private CheckSignal checkSignal;

    private static DataCenterClientService clientService;

    //	public ObservableZXDCSignalListenerThread(RecordState recordState, FilterSignal filterSignal) {
//		Log.e("camera", "ObservableZXDCSignalListenerThread constructor" + "construct");
//
//		this.observableHint = new ObservableHint();
//
//		this.recordState = recordState;
//		this.recoverState = new RecoverState();
//		this.filterSignal = filterSignal;
//		this.checkSignal = new CheckSignal(this.filterSignal);
//	}
    public ObservableZXDCSignalListenerThread() {
        Log.e("camera", "ObservableZXDCSignalListenerThread constructor" + "construct");

        this.observableHint = new ObservableHint();

//	this.recoverState = new RecoverState();
    }

    public static void addObserver(Observer observer) {
        observableHint.addObserver(observer);
    }

    public static void deleteObserver(Observer observer) {
        observableHint.deleteObserver(observer);
    }

    public static void notifyObservers(Res res) {
        observableHint.notifyObservers(res);
    }


    @Override
    public void run() {
        super.run();

        // there must be a pause if without there will be something wrong.
//		recoverState.recover(recordState, observableHint);
        MyLog.e(TAG, TAG + " is run");
        clientService = DataCenterClientService.get(ap, org);
        if (clientService == null) {
            DataCenterClientConfig config = new DataCenterClientConfig();
            config.setAddr("111.9.45.67");
            config.setPort(10014);
            config.setAp(ap);
            config.setOrg(org);
            config.setPassword("123456");
            config.setServerAp("JzDataCenter");
            config.setServerOrg("*");
            config.setProcess(this);
            // config.setPushThreadClass(DataCenterClientTestService.class);

            DataCenterClientService.startup(config);

            clientService = DataCenterClientService.get(ap, org);

            if (clientService == null) {
                MyLog.e(TAG, "clientService == null");
            }
        }

        while (isContinue) {

            synchronized (this) {
                try {
                    this.wait(5000);
                } catch (InterruptedException e) {
                }
            }
        }

        finishReceivingSignal();
    }

    public synchronized void finishReceivingSignal() {
        Log.e("camera", " finish");
        notify();
    }

    public synchronized void commitSignal(Boolean isInitiative) {
        try {
            Log.e("camera", "waitToCommitSignal " + 1);

            wait();

            Log.e("camera", "waitToCommitSignal " + 2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }

        // If we close the APP initiative,then reset the states.
//		if (isInitiative) {
//			recordState.reset();
//		}
//		recordState.commit();
    }

    private class ObservableHint extends Observable {
        private ArrayList<Observer> arrayListObserver;

        private ObservableHint() {
            arrayListObserver = new ArrayList<Observer>();
        }

        @Override
        public void addObserver(Observer observer) {
            super.addObserver(observer);
            arrayListObserver.add(observer);
        }

        @Override
        public synchronized void deleteObserver(Observer observer) {
            super.deleteObserver(observer);
            arrayListObserver.remove(observer);
        }

        @Override
        public void notifyObservers(Object data) {
            super.notifyObservers(data);
            for (Observer observer : arrayListObserver) {
                observer.update(observableHint, data);
            }
        }
    }


    public void selfSleep(long m) {
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void onSend(DataCenterTaskCmd selfCmd) throws DataCenterException {
    }

    public void onResponse(DataCenterTaskCmd selfCmd, DataCenterTaskCmd responseCmd) throws DataCenterException {
    }

    public void onFree(DataCenterTaskCmd selfCmd) {
    }

    public void onTimeout(DataCenterTaskCmd selfCmd) {
    }

    public void processMsg(DataCenterRun dataCenterRun, DataCenterTaskCmd cmd) throws DataCenterException {
        Log.e("processMsg", "cmd:" + cmd.getCmd());
        if ("zxdc_rev_confirm_donor".equals(cmd.getCmd())) {
            notifyObservers(Res.ZXDCRES);
        } else if ("tablet_rev_confirm_donor".equals(cmd.getCmd())) {
            notifyObservers(Res.TABLETRES);
        }

    }

    @Override
    public void processResponseMsg(DataCenterRun dataCenterRun, DataCenterTaskCmd dataCenterTaskCmd, DataCenterTaskCmd dataCenterTaskCmd1) throws DataCenterException {
        Log.e("processResponseMsg", "dataCenterTaskCmd: " + dataCenterTaskCmd.getCmd() + " " + "dataCenterTaskCmd1: " + dataCenterTaskCmd1.getCmd());
        DataCenterTaskCmd c = dataCenterTaskCmd;
        String s = textUnit.ObjToString(dataCenterTaskCmd.getValue("result"));
        if ("confirm_donor".equals(dataCenterTaskCmd1.getCmd())) {
            if (!"failure".equals(textUnit.ObjToString(dataCenterTaskCmd.getValue("result")))) {
                notifyObservers(Res.SERVERRES_PASS);
            } else {
                notifyObservers(Res.SERVERRES_NOT_PASS);
            }
        }
    }

    @Override
    public void onSended(DataCenterTaskCmd selfCmd) throws DataCenterException {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSendTimeout(DataCenterTaskCmd selfCmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResponseTimeout(DataCenterTaskCmd selfCmd) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAdd(DataCenterTaskCmd dataCenterTaskCmd, List<DataCenterTaskCmd> list) {

    }

    public void startMsgProcess() {
    }

    public void stopMsgProcess() {
    }

    public static DataCenterClientService getClientService() {

        return clientService;
    }

}
