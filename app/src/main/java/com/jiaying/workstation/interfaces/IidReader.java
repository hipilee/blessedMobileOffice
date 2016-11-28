package com.jiaying.workstation.interfaces;

import com.jiaying.workstation.entity.IdentityCardEntity;

/**
 * Created by Administrator on 2016/3/9 0009.
 */
public interface IidReader {
    void open();

    void read();

    int close();

    void setOnIdReadCallback(OnIdReadCallback onIdReadCallback);

    void setOnIdOpenCallback(OnIdopenCallback onIdOpenCallback);

    interface OnIdopenCallback {
        void onOpen(int status);
    }

    /**
     * Created by Administrator on 2016/3/9 0009.
     */
    interface OnIdReadCallback {
        public void onRead(IdentityCardEntity identityCardEntity);
    }
}
