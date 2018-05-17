package com.askey.dvr.cdr7010.dashcam.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.widget.SdCardDialog;
import com.askey.platform.AskeySettings;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int DIALOG_TYPE_SDCARD =1;
    private AudioManager audioManager;
    private int maxVolume,currentVolume;
    private Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraRecordFragment.newInstance())
                    .commit();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
        DialogManager.getIntance().registerContext(this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyAdapter.KEY_MENU:
                    ActivityUtils.startActivity(this, Const.PACKAGE_NAME, Const.CLASS_NAME, false);
                    return true;
                case KeyAdapter.KEY_VOLUME_UP:
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)+1;
                    if(currentVolume<=maxVolume){
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,currentVolume,
                                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
                case KeyAdapter.KEY_VOLUME_DOWN:
                    currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)-1;
                    if(currentVolume>=0){
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,currentVolume,
                                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args){
        Logg.d(TAG,"id="+id);
        switch(id){
            case DIALOG_TYPE_SDCARD:
                dialog = new SdCardDialog(this,R.style.dialogNoTitle);
                ((SdCardDialog)dialog).setMessage(args.getString("Message"));
                break;
        }
        return dialog;
    }
    @Override
    protected  void onPrepareDialog(int id,Dialog dialog,Bundle args){
        super.onPrepareDialog(id,dialog,args);
        if(id == DIALOG_TYPE_SDCARD){
            SdCardDialog sdCardDialog =(SdCardDialog)dialog;
            sdCardDialog.setMessage(args.getString("Message"));
        }
    }

    public boolean isDialogShowing(){
        if(!isFinishing() && null != dialog && dialog.isShowing()){
            return true;
        }
        return false;
    }
    public void dismissDialog(){
        if(!isFinishing() && null != dialog && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    @Override
    public void onDestroy(){
        DialogManager.getIntance().unregisterContext();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int micValue = GlobalLogic.getInstance().getInt(AskeySettings.Global.RECSET_VOICE_RECORD);
        int newVal = (micValue == 0) ? 1 : 0;
        boolean value = GlobalLogic.getInstance().putInt(AskeySettings.Global.RECSET_VOICE_RECORD, newVal);
        EventUtil.sendEvent(new MessageEvent<>(Event.EventCode.EVENT_MIC, value));
        return;
    }
}
