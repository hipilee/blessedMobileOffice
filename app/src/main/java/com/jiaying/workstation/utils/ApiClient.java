package com.jiaying.workstation.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 作者：lenovo on 2016/6/24 09:47
 * 邮箱：353510746@qq.com
 * 功能：Http请求
 */
public class ApiClient {
    private static AsyncHttpClient client = new AsyncHttpClient();    //实例话对象
    private static final String API_SERVER = "http://jiaying.picp.net:8989/api/";
    static {
        client.setTimeout(10000);   //设置链接超时，如果不设置，默认为10s
    }

    public static void get(String method, AsyncHttpResponseHandler res)    //用一个完整url获取一个string对象
    {
        client.get(API_SERVER + method, res);
    }

    public static void get(String method, RequestParams params, AsyncHttpResponseHandler res)   //url里面带参数
    {
        client.get(API_SERVER + method, params, res);
    }

    public static void get(String method, JsonHttpResponseHandler res)   //不带参数，获取json对象或者数组
    {
        client.get(API_SERVER + method, res);
    }

    public static void get(String method, RequestParams params, JsonHttpResponseHandler res)   //带参数，获取json对象或者数组
    {
        client.get(API_SERVER + method, params, res);
    }

    public static void get(String method, BinaryHttpResponseHandler bHandler)   //下载数据使用，会返回byte数据
    {
        client.get(API_SERVER + method, bHandler);
    }

    public static AsyncHttpClient getClient() {
        return client;
    }
}