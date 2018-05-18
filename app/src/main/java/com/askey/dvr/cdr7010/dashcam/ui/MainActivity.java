package com.askey.dvr.cdr7010.dashcam.ui;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.DialogManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.dvr.cdr7010.dashcam.widget.CommDialog;
import com.askey.dvr.cdr7010.dashcam.widget.WarningDialog;
import com.askey.platform.AskeySettings;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int DIALOG_TYPE_SDCARD =1;
    public static final int DIALOG_TYPE_WARNING = 2;
    public static final int DIALOG_TYPE_COMM_TEXT =3;
    public static final int DIALOG_TYPE_COMM_CONFIRM =4;
    private AudioManager audioManager;
    private int maxVolume,currentVolume;
    private Dialog dialog = null;
    private int dialogType = 0;

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
        Logg.d(TAG,"onCreateDialog id="+id);
        switch(id){
            case DIALOG_TYPE_SDCARD:
                dialog = new CommDialog(this,R.style.dialogNoTitle);
                ((CommDialog)dialog).setMessage(args.getString("Message"));
                break;
            case DIALOG_TYPE_WARNING:
                dialog = new WarningDialog(this,R.style.dialogNoTitle);
                ((WarningDialog)dialog).setMessage(args.getString("Message"));
                break;
            case DIALOG_TYPE_COMM_TEXT:
                dialog = new CommDialog(this,R.style.dialogNoTitle);
                ((CommDialog)dialog).setMessage(args.getString("Message"));
                break;
            case DIALOG_TYPE_COMM_CONFIRM:
                break;
            default:
        }
        return dialog;
    }
    @Override
    protected  void onPrepareDialog(int id,Dialog dialog,Bundle args){
        super.onPrepareDialog(id,dialog,args);
        dialogType = id;
        Logg.d(TAG,"onPrepareDialog id="+id);
        if(id == DIALOG_TYPE_SDCARD){
            CommDialog sdCardDialog =(CommDialog)dialog;
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
    public int getDialogType(){
        return dialogType;
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
