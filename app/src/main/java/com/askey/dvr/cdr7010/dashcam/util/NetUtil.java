package com.askey.dvr.cdr7010.dashcam.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;

/**
 * whether the net is connect
 * <p>
 * Created by Navas.li on 2018/6/25
 */
public class NetUtil {

    /**
     * 检查当前网络是否可用
     *
     * @return 是否连接到网络
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) DashCamApplication.getAppContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return info.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }
}
