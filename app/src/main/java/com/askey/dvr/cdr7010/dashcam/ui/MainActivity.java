package com.askey.dvr.cdr7010.dashcam.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.domain.KeyAdapter;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;

public class MainActivity extends AppCompatActivity {
    private static final String PACKAGE_NAME = "com.askey.dvr.cdr7010.setting";
    private static final String CLASS_NAME ="com.askey.dvr.cdr7010.setting.SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

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
                    ActivityUtils.startActivity(this,PACKAGE_NAME,CLASS_NAME,false);
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    @Override
    public void onBackPressed() {
        return;
    }
}
