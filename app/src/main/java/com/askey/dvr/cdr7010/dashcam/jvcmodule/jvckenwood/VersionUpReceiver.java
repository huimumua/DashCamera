package com.askey.dvr.cdr7010.dashcam.jvcmodule.jvckenwood;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.askey.dvr.cdr7010.dashcam.domain.EventInfo;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.util.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class VersionUpReceiver extends BroadcastReceiver{
    public static final String PREFERENCE_KEY_UPDATE_COMPLETED = "updateCompleted";

    private static final String LOG_TAG = "VersionUpReceiver";
    private static final String ACTION_EVENT_DOWNLOAD_RESULT = "com.jvckenwood.versionup.DOWNLOAD_RESULT";
    private static final String ACTION_EVENT_UPDATE_READY = "com.jvckenwood.versionup.UPDATE_READY";
    private static final String ACTION_EVENT_UPDATE_COMPLETED = "com.jvckenwood.versionup.UPDATE_COMPLETED";
    private static final String ACTION_EVENT_STARTUP = "com.jvckenwood.versionup.STARTUP";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Logg.i(LOG_TAG, "onReceive: action=" + action);
        if(action.equals(ACTION_EVENT_DOWNLOAD_RESULT)){
            int status = intent.getIntExtra("status", -1);
            int http = intent.getIntExtra("http", -1);
            int length = intent.getIntExtra("length", -1);

//            EventInfo eventInfo = EventManager.getInstance().getEventInfoByEventType(eventType);
//            if(checkEventInfo(eventInfo, eventType)) EventManager.getInstance().handOutEventInfo(eventInfo, timeStamp);

        }else if(action.equals(ACTION_EVENT_UPDATE_READY)){ //null params


        }else if(action.equals(ACTION_EVENT_UPDATE_COMPLETED)){
            int type = intent.getIntExtra("type", -1);
            int result = intent.getIntExtra("result", -10); //-1被正常使用

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", type); //0：OTA、2：SDカード
                jsonObject.put("result", result); //0：成功、-1：アップデート失敗
                String saveJson = jsonObject.toString();
                Logg.d(LOG_TAG, "Save UPDATE_COMPLETED information : " + saveJson);
                SPUtils.put(context, PREFERENCE_KEY_UPDATE_COMPLETED, saveJson);
            } catch (JSONException e) {
                Logg.e(LOG_TAG, "Save UPDATE_COMPLETED information error: " + e.getMessage());
            }
        }else if(action.equals(ACTION_EVENT_STARTUP)){
            int bootinfo = intent.getIntExtra("bootinfo", -1);
            int updateInfo = intent.getIntExtra("updateInfo", -10);
            String farmver = intent.getStringExtra("farmver");
            String soundver = intent.getStringExtra("soundver");
            StartUpInfo startUpInfo = new StartUpInfo(bootinfo, updateInfo, farmver, soundver);
            EventUtil.sendEvent(startUpInfo);
        }
    }

    private boolean checkEventInfo(EventInfo eventInfo, int eventType){
        if(eventInfo == null){
            Logg.e(LOG_TAG, "checkEventInfo: can't find EventInfo, eventType=" + eventType);
            return false;
        }
        return true;
    }

    public class StartUpInfo{
        public int bootinfo;
        public int updateInfo;
        public String farmver;
        public String soundver;

        public StartUpInfo(int bootinfo, int updateInfo, String farmver, String soundver) {
            this.bootinfo = bootinfo;
            this.updateInfo = updateInfo;
            this.farmver = farmver;
            this.soundver = soundver;
        }
    }

}