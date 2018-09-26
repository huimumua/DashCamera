package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import android.content.ContentResolver;
import android.content.Context;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.AskySettingManager;
import com.askey.dvr.cdr7010.dashcam.ui.utils.UIElementStatusEnum;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.platform.AskeySettings;
import com.google.gson.Gson;

import java.util.EnumMap;
import java.util.concurrent.CountDownLatch;

/**
 * 项目名称：mainapp
 * 类描述：
 * 创建人：skysoft  charles.bai
 * 创建时间：2018/6/26 10:34
 * 修改人：skysoft
 * 修改时间：2018/6/26 10:34
 * 修改备注：
 */
public class UserSettingManager {

    private static final String LOG_TAG = "AskySettingManager";
    private static EnumMap<JvcStatusParams.JvcStatusParam, Object> mReportMap;
    public static UserInfoSaveCallback userInfoSaveCallback;
    private static int localUserId, seletedUserId;
    private static boolean isFirstUserChange;

    public static void getUserList(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap, CountDownLatch countDownLatch) {

        mReportMap = enumMap;
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        if (enumMap != null) {
            int oos = (int) enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if (oos == 0) { // 0:圈内

                String response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);

                Gson gson = new Gson();

                JvcUserListInfo userListInfo = gson.fromJson(response, JvcUserListInfo.class);
                int status = userListInfo.status;
                if (status == 0) {
                    if (userListInfo == null || userListInfo.user99 == null) {
                        return;
                    }
                    String localSeletedata;
                    localSeletedata = Settings.Global.getString(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER_DAYS);
                    localUserId = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, 01);
                    seletedUserId = userListInfo.user99.userid;
                    if (localUserId != seletedUserId) {
                        EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SwitchUserEvent>(Event.EventCode.EVENT_SWITCH_USER, UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_PREPARE));
                        String suffix = "_user"+seletedUserId;
                        /**
                         * 几组用户设置信息保存成功再刷新默认用户的设置信息
                         */
                        GlobalLogic.getInstance().setFirstUserChange(isFirstUserChange);
                        if (isFirstUserChange){
                            UserSettingManager.setUserInfoSaveCallBack(new UserInfoSaveCallback() {
                                @Override
                                public void notifyUserInfo(boolean isOk) {
                                    if (isOk){
                                        initAskySetting(appContext, suffix);
                                    }
                                }
                            });
                        }else {
                            initAskySetting(appContext, suffix);
                        }
                        isFirstUserChange = false;
                        EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SwitchUserEvent>(Event.EventCode.EVENT_SWITCH_USER, UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_START));
                    }
                    //保存五組用戶數據信息
                    setUserInfoLists(userListInfo);
                    if (countDownLatch!=null){
                        countDownLatch.countDown();
                        Log.d(LOG_TAG,"abby countDown~~01~!");
                    }
                }
            }
        }

    }

    private static void initAskySetting(Context appContext, String suffix) {
        try {
            AskySettingManager.getInstance(appContext).initAskySetting(AskeySettings.Global.SYSSET_USER_ID+suffix);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void userSettings(EnumMap<JvcStatusParams.JvcStatusParam, Object> enumMap, CountDownLatch countDownLatch) {

        if (enumMap != null) {
            int oos = (int) enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
            //圏外の場合は取得できなかったことを通知するのでMainAPPで保持している設定値(前回値)を使用すること
            if (oos == 0) { // 0:圈内\

                String response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                Gson gson = new Gson();
                JvcUserSettingInfo info = gson.fromJson(response, JvcUserSettingInfo.class);
                int status = info.status;
                Context appContext = DashCamApplication.getAppContext();
                ContentResolver contentResolver = appContext.getContentResolver();
                int SelctedUserId = Settings.Global.getInt(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER, 01);
                if (status == 0) {
                    if (info != null) {
                        switch (SelctedUserId) {
                            case 01:
                                setUserSettings(info.user01, SelctedUserId, countDownLatch);
                                break;
                            case 02:
                                setUserSettings(info.user02, SelctedUserId, countDownLatch);
                                break;
                            case 03:
                                setUserSettings(info.user03, SelctedUserId, countDownLatch);
                                break;
                            case 04:
                                setUserSettings(info.user04, SelctedUserId, countDownLatch);
                                break;
                            case 05:
                                setUserSettings(info.user05, SelctedUserId, countDownLatch);
                                break;
                        }
                    }
                }
            }
        }
    }

    public static void setUserSettings(JvcUserSettingInfo.UserSettinginfo userListInfo, int num, CountDownLatch countDownLatch) {
        if (userListInfo == null) {
            return;
        }
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        switch (num){
            case 01:
            case 02:
            case 03:
            case 04:
            case 05:
                String suffix = "_user"+num;
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID+suffix, userListInfo.userid);
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME+suffix, userListInfo.user_name);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_FCWS+suffix, userListInfo.warn_coll);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_LDS+suffix, userListInfo.warn_dev);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_DELAY_START+suffix, userListInfo.warn_delay);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.ADAS_PEDESTRIAN_COLLISION+suffix, userListInfo.warn_pades);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_REVERSE_RUN+suffix, userListInfo.reverse);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_SPEED_LIMIT_AREA+suffix, userListInfo.zone30);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_STOP+suffix, userListInfo.pause);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FREQ_ACCIDENT_AREA+suffix, userListInfo.accident);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_TIME+suffix, userListInfo.runtime);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_INTENSE_DRIVING+suffix, userListInfo.rapid);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ABNORMAL_HANDING+suffix, userListInfo.handle);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_FLUCTUATION_DETECTION+suffix, userListInfo.wobble);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_OUT_OF_AREA+suffix, userListInfo.outside);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_DRIVING_REPORT+suffix, userListInfo.report);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ADVICE+suffix, userListInfo.advice);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_NOTIFICATION+suffix, userListInfo.notice);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_WEATHER_INFO+suffix, userListInfo.weather);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_ROAD_KILL+suffix, userListInfo.animal);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.NOTIFY_LOCATION_INFO+suffix, userListInfo.location);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_NOTIFY_VOL+suffix, userListInfo.volume_n);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_PLAYBACK_VOL+suffix, userListInfo.volume_p);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_MONITOR_BRIGHTNESS+suffix, userListInfo.bright);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_ACTION+suffix, userListInfo.psave_e);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_POWERSAVE_TIME+suffix, userListInfo.psave_s);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_LANGUAGE+suffix, userListInfo.lang);
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS+suffix, userListInfo.set_update_day);
                Settings.Global.putInt(contentResolver, AskeySettings.Global.COMM_EMERGENCY_AUTO+suffix, userListInfo.outbound_call);
                if (countDownLatch!=null){
                    countDownLatch.countDown();
                    Log.d(LOG_TAG,"abby countDown~~02~!");
                }
                //回调通知到usersetting已保存完成用户设置信息
                if (userInfoSaveCallback!=null){
                    userInfoSaveCallback.notifyUserInfo(true);
                }
                break;
        }

    }

    public static void setUserInfoLists(JvcUserListInfo infoLists) {
        Log.d(LOG_TAG,"setUserInfoLists~~~~");
        if (infoLists == null) {
            return;
        }
        Context appContext = DashCamApplication.getAppContext();
        ContentResolver contentResolver = appContext.getContentResolver();
        if (infoLists.user99!=null ) {
            if (!TextUtils.isEmpty(infoLists.user99.name)) {
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME, infoLists.user99.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID, infoLists.user99.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SELECT_USER_DAYS, String.valueOf(infoLists.user99.selectdate));
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS, String.valueOf(infoLists.user99.lastupdate));
            Log.d(LOG_TAG,"setUserInfoLists~~~~"+String.valueOf(infoLists.user99.selectdate));
        }
        if (infoLists.user01!=null) {
            if (!TextUtils.isEmpty(infoLists.user01.name)){
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME_USER1, infoLists.user01.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID_USER1, infoLists.user01.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS_USER1, String.valueOf(infoLists.user01.lastupdate));
        }
        if (infoLists.user02!=null) {
            if (!TextUtils.isEmpty(infoLists.user02.name)) {
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME_USER2, infoLists.user02.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID_USER2, infoLists.user02.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS_USER2, String.valueOf(infoLists.user02.lastupdate));
        }
        if (infoLists.user03!=null) {
            if (!TextUtils.isEmpty(infoLists.user03.name)) {
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME_USER3, infoLists.user03.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID_USER3, infoLists.user03.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS_USER3, String.valueOf(infoLists.user03.lastupdate));
        }
        if (infoLists.user04!=null) {
            if (!TextUtils.isEmpty(infoLists.user04.name)) {
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME_USER4, infoLists.user04.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID_USER4, infoLists.user04.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS_USER4, String.valueOf(infoLists.user04.lastupdate));
        }
        if (infoLists.user05!=null) {
            if (!TextUtils.isEmpty(infoLists.user05.name)) {
                Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_USER_NAME_USER5, infoLists.user05.name);
            }
            Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_ID_USER5, infoLists.user05.userid);
            Settings.Global.putString(contentResolver, AskeySettings.Global.SYSSET_SET_LASTUPDATE_DAYS_USER5, String.valueOf(infoLists.user05.lastupdate));
        }
        Settings.Global.putInt(contentResolver, AskeySettings.Global.SYSSET_USER_NUM, infoLists.num);
        if (localUserId != seletedUserId) {
            EventUtil.sendEvent(new MessageEvent<UIElementStatusEnum.SwitchUserEvent>(Event.EventCode.EVENT_SWITCH_USER, UIElementStatusEnum.SwitchUserEvent.SWITCH_USER_COMPLETE));
        }
    }


    public interface UserInfoSaveCallback {
        void notifyUserInfo(boolean isOk);

    }

    public static void setUserInfoSaveCallBack(UserInfoSaveCallback callBack) {
        userInfoSaveCallback = callBack;
    }
}
