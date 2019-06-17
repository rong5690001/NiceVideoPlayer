package com.rong.videoplayer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by lenovo on 2016/3/9.
 *
 * @author Liuliwei
 * @description 判断网络状态
 */
public class NetUtil {

    // 判断网络连接状态
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = connectivityManager.getActiveNetworkInfo();
            if (net != null) {
                return net.isAvailable();
            }
        }
        return false;
    }

    // 获取连接类型
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = connectivityManager.getActiveNetworkInfo();
            if (net != null && net.isAvailable()) {
                return net.getType();
            }
        }
        return -1;
    }

}
