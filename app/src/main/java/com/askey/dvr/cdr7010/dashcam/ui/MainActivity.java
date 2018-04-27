package com.askey.dvr.cdr7010.dashcam.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.Event;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.domain.MessageEvent;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.service.FileManager;
import com.askey.dvr.cdr7010.dashcam.service.GPSStatusManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Const;
import com.askey.dvr.cdr7010.dashcam.util.EventUtil;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        GPSStatusManager.getInstance().recordLocation(true);
        FileManager.getInstance(this); // bindService
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraRecordFragment.newInstance())
                    .commit();
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if( event.getAction() == KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyAdapter.KEY_MENU:
                    ActivityUtils.startActivity(this, Const.PACKAGE_NAME,Const.CLASS_NAME,false);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public void onBackPressed() {
        int micValue = GlobalLogic.getInstance().getInt("MIC");
        boolean value = (micValue ==0) ? GlobalLogic.getInstance().putInt("MIC",1) : GlobalLogic.getInstance().putInt("MIC",0);
        EventUtil.sendEvent(new MessageEvent<Boolean>(Event.EventCode.EVENT_MIC,value));
        return;
    }
    @Override
    public void onDestroy() {
        GPSStatusManager.getInstance().recordLocation(false);
        super.onDestroy();
    }
}
