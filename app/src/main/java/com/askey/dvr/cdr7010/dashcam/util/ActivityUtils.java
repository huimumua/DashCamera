

package com.askey.dvr.cdr7010.dashcam.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commitAllowingStateLoss();
    }
    public static void hideFragment(@NonNull FragmentManager fragmentManager,@NonNull Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment != null) {
            transaction.hide(fragment);
        }
    }
    public static void displayFragmentToActivtity(@NonNull FragmentManager fragmentManager,@NonNull Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment != null) {
            transaction.show(fragment);
        }
    }

    public static void startActivity(Context context, String packageName, String className, boolean isFinish) {
        startActivity(context, packageName, className, null, isFinish);
    }

    public static void startActivity(Context context, String packageName, String className, Bundle extraInfo, boolean isFinish) {
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className) || context == null) {
            return;
        }
        ComponentName componetName = new ComponentName(packageName, className);
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            if (extraInfo != null) {
                intent.putExtra(Const.EXTRA_INFO, extraInfo);
            }
            context.startActivity(intent);
            if (isFinish && (context instanceof Activity)) {
                ((Activity) context).finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutDown(Context context) {
        try {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            //不弹出是否关机的确认窗口
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
