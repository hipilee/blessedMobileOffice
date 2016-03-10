package com.jiaying.workstation.engine;

import com.jiaying.workstation.interfaces.IfingerprintReader;

/**
 * 作者：lenovo on 2016/3/4 13:27
 * 邮箱：353510746@qq.com
 * 功能：指纹代理
 */
public class ProxyFingerprintReader implements IfingerprintReader {

    private IfingerprintReader ifingerprintReader;

    public ProxyFingerprintReader(IfingerprintReader ifingerprintReader) {
        this.ifingerprintReader = ifingerprintReader;
    }

    @Override
    public void read() {
        ifingerprintReader.read();
    }

    @Override
    public void close() {
        ifingerprintReader.close();
    }


}
