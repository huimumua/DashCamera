package com.askey.dvr.cdr7010.dashcam.ui;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams;
import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.LocalJvcStatusManager;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;
import com.askey.platform.AskeySettings;

import org.json.JSONObject;

public class InsuranceFragment extends Fragment {

    private static final String TAG = "InsuranceFragment";
    private TextView tvContent;

    public static Fragment newInstance() {
        return new InsuranceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insurance, container, false);
        tvContent = (TextView) view.findViewById(R.id.tv_notice_content);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocalJvcStatusManager.getInsuranceTerm(jvcStatusCallback);
    }


    private LocalJvcStatusManager.LocalJvcStatusCallback jvcStatusCallback = enumMap -> {
        Logg.d(TAG, "onDataArriving...");
        int oos = -1;
        String response = null;
        if (enumMap != null) {
            if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.OOS)) {
                oos = (int) enumMap.get(JvcStatusParams.JvcStatusParam.OOS);
                Logg.d(TAG, "oos..." + oos);
            }
            if (enumMap.containsKey(JvcStatusParams.JvcStatusParam.RESPONSE)) {
                response = (String) enumMap.get(JvcStatusParams.JvcStatusParam.RESPONSE);
                Logg.d(TAG, "response.." + response);
            }
            switch (oos) {
                case 0://成功
                    if (response != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int status = jsonObject.optInt("status");
                            switch (status) {
                                case 0://正常
                                    int flg = jsonObject.optInt("flg");
                                    dealFlg(flg);
                                    break;
                                case -1://想定外の例外
                                    break;
                                case -100://IMEIが未入力
                                    break;
                                case -700://IMEIがDBに未登録
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default://圏外
                    int flg = Settings.Global.getInt(getActivity().getContentResolver(), AskeySettings.Global.SYSSET_STARTUP_INFO, -1);
                    Logg.d(TAG, "flg FROM SETTINGS==" + flg);
                    dealFlg(flg);
                    break;
            }
        }
    };

    private void dealFlg(int flg) {
        switch (flg) {
            case 1://始期日以前
                beforeContractDayStart();
                break;
            case 0://対象の証券無し
            case 2://証券期間中 do nothing
                intContractDay();
                break;
            case 3://"満期日+14日"以降
                afterContractDayEnd();
                break;
        }
    }

    public void afterContractDayEnd() {
        getActivity().runOnUiThread(() -> {
            timeFinishApp.start();
            tvContent.setText(getString(R.string.after_contract_day_stop));
        });
    }

    /**
     * 合约开始日之前，停止界面更新
     */
    public void beforeContractDayStart() {
        getActivity().runOnUiThread(() -> {
            timeFinishShow.start();
            tvContent.setText(getString(R.string.before_contract_day_start));
        });
    }

    private void intContractDay() {
        getActivity().runOnUiThread(() -> startRecordFragment(true));
    }

    private void startRecordFragment(boolean recordEvent) {
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).startRecordFragment(recordEvent);
        }
    }

    private CountDownTimer timeFinishShow = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            //进入到录制页面，开始录制，但不开启event,ECall不能响应
            startRecordFragment(false);
        }
    };

    private CountDownTimer timeFinishApp = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            Logg.d(TAG, "关闭电源");
            ActivityUtils.shutDown(DashCamApplication.getAppContext());
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        if (timeFinishShow != null) {
            timeFinishShow.cancel();
            timeFinishShow = null;
        }
    }
}
