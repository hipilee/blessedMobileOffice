package com.jiaying.workstation.engine;

import com.jiaying.workstation.interfaces.IfaceCollector;

/**
 * 作者：lenovo on 2016/3/15 07:21
 * 邮箱：353510746@qq.com
 * 功能：人脸识别
 */
public class ProxyFaceCollector implements IfaceCollector {
    private IfaceCollector ifaceCollector;
    private static ProxyFaceCollector proxyFaceCollector = null;

    private ProxyFaceCollector(IfaceCollector ifaceCollector) {
        this.ifaceCollector = ifaceCollector;
    }

    public synchronized static ProxyFaceCollector getInstance(IfaceCollector ifaceCollector) {

        proxyFaceCollector = new ProxyFaceCollector(ifaceCollector);

        return proxyFaceCollector;
    }

    @Override
    public int close() {
        return ifaceCollector.close();
    }

    @Override
    public void collect() {
        ifaceCollector.collect();
    }

    @Override
    public void setOnCollectCallback(OnFaceCollectCallback onFaceCollectCallback) {
        ifaceCollector.setOnCollectCallback(onFaceCollectCallback);
    }

    @Override
    public int open() {
        return ifaceCollector.open();
    }


}
