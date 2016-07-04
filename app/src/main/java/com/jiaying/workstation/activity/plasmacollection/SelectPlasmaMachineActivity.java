package com.jiaying.workstation.activity.plasmacollection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiaying.workstation.R;
import com.jiaying.workstation.activity.BaseActivity;
import com.jiaying.workstation.activity.sensor.FingerprintActivity;
import com.jiaying.workstation.adapter.PlasmaMachineSelectAdapter;
import com.jiaying.workstation.app.MobileofficeApp;
import com.jiaying.workstation.constant.IntentExtra;
import com.jiaying.workstation.constant.TypeConstant;
import com.jiaying.workstation.entity.PlasmaMachineEntity;
import com.jiaying.workstation.thread.ObservableZXDCSignalListenerThread;
import com.jiaying.workstation.utils.MyLog;
import com.jiaying.workstation.utils.SetTopView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SelectPlasmaMachineActivity extends BaseActivity {
    private static final String TAG = "SelectPlasmaMachineActivity";
    private GridView mGridView;
    private List<PlasmaMachineEntity> mList;
    private PlasmaMachineSelectAdapter mAdapter;
    private TextView sure_txt;//确定分配


    private PlasmaMachineStateHandlerObserver plasmaMachineStateHandlerObserver;
    private ResContext resContext;
    private PlasmaMachineStateRes plasmaMachineStateRes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void initView() {
        setContentView(R.layout.activity_pulp_machine_select);
        new SetTopView(this, R.string.title_activity_pulp_machine_select, true);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = new Intent(SelectPlasmaMachineActivity.this, FingerprintActivity.class);
                it.putExtra("source", TypeConstant.TYPE_SELECT_MACHINE);
                it.putExtra(IntentExtra.EXTRA_PLASMAMACHINE, mList.get(position));

//                Intent it = new Intent(SelectPlasmaMachineActivity.this, SelectPlasmaMachineResultActivity.class);
//                it.putExtra(IntentExtra.EXTRA_PLASMAMACHINE, mList.get(position));
                startActivity(it);
                SelectPlasmaMachineActivity.this.finish();
            }
        });
        mList = new ArrayList<PlasmaMachineEntity>();
        mAdapter = new PlasmaMachineSelectAdapter(mList, this);
        mGridView.setAdapter(mAdapter);
        mList.addAll(MobileofficeApp.getPlasmaMachineEntityList());
        mAdapter.notifyDataSetChanged();

        sure_txt = (TextView) findViewById(R.id.sure_txt);
        sure_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectSize = 0;
                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).isCheck()) {
                        selectSize++;
                    }
                }

            }
        });

        resContext = new ResContext();
        resContext.open();
        plasmaMachineStateHandlerObserver = new PlasmaMachineStateHandlerObserver();
        ObservableZXDCSignalListenerThread.addObserver(plasmaMachineStateHandlerObserver);
        resContext.setCurState(plasmaMachineStateRes);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObservableZXDCSignalListenerThread.deleteObserver(plasmaMachineStateHandlerObserver);
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initVariables() {

    }

    private class PlasmaMachineStateHandlerObserver extends Handler implements Observer {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            msg.obj = (msg.obj == null) ? (Res.NOTHING) : (msg.obj);
            switch ((Res) msg.obj) {
                case ZXDC_STATE_CHANGE:
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

    private class PlasmaMachineStateRes extends State {

        @Override
        void handle(Res res) {
            switch (res) {
                case ZXDC_STATE_CHANGE:
                    mList.clear();
                    mList.addAll(MobileofficeApp.getPlasmaMachineEntityList());
                    mAdapter.notifyDataSetChanged();
                    resContext.setCurState(plasmaMachineStateRes);
                    break;

            }

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

}
