package com.askey.dvr.cdr7010.dashcam.util;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class AppUtils {
    private static final String LOG_TAG = AppUtils.class.getSimpleName();

    /**
     * 返回app运行状态
     * 1:程序在前台运行
     * 2:程序在后台运行
     * 3:程序未启动
     * 注意：需要配置权限<uses-permission android:name="android.permission.GET_TASKS" />
     */
    @SuppressWarnings("deprecation")
    public static int getAppStatus(Context context, String pageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(200);

        //判断程序是否在栈顶
        if (list.size() > 0 && list.get(0).topActivity.getPackageName().equals(pageName)) {
            return 1;
        } else {
            //判断程序是否在栈里
            for (ActivityManager.RunningTaskInfo info : list) {
                if (info.topActivity.getPackageName().equals(pageName)) {
                    return 2;
                }
            }
            return 3;//栈里找不到，返回3
        }
    }

    /**
     * 返回Activity是否在顶端
     */
    @SuppressWarnings("deprecation")
    public static boolean isActivityTop(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(2);

        //判断程序是否在栈顶
        if (list != null && list.size() > 0 && list.get(0).topActivity.getClassName().equals(className)) {
            return true;
        }
        return false;
    }

    /**
     * 返回App是否在顶端
     */
    @SuppressWarnings("deprecation")
    public static boolean isAppTop(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(2);

        //判断程序是否在栈顶
        if (list != null && list.size() > 0 && list.get(0).topActivity.getPackageName().equals(context.getPackageName())) {
            return true;
        }

        return false;
    }


}
