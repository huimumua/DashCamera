package com.askey.dvr.cdr7010.dashcam.mvp.view;


import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.util.Const;

public class UpdateFragment extends Fragment {
    private ImageView btn_ok;
    private int updateType ;
    private UpdateListener updateListener;

    public interface UpdateListener {
        void updateCompleteJump();
        void displayTipInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            updateType = getArguments().getInt("updateType");
        }
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context != null) {
                updateListener = (UpdateFragment.UpdateListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement notice");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_upgrade_succ, null);
        if(updateType == Const.SDCARD_UPDATE){
            btn_ok =(ImageView) root.findViewById(R.id.btn_ok);
            btn_ok.setVisibility(View.VISIBLE);
        }
        return root;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(updateListener != null) {
            updateListener.displayTipInfo();
        }
        updateTimer.start();
    }
    private CountDownTimer updateTimer = new CountDownTimer(5000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            if(updateListener !=null) {
                updateListener.updateCompleteJump();
            }
        }
    };

    @Override
    public void onDestroy() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        super.onDestroy();
    }
    public void stopTimer(){
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
}