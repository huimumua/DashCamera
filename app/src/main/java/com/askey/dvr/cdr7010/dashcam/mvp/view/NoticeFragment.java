package com.askey.dvr.cdr7010.dashcam.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.adas.SystemPropertiesProxy;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.mvp.presenter.NoticePresenter;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class NoticeFragment extends BaseFragment<NoticeContract.View, NoticePresenter> implements NoticeContract.View {

    private static final String TAG = NoticeFragment.class.getSimpleName();
    private static final int DELAY_END_BOOT_ANIMATION = 1000;
    private static final int TIME_TO_SHOW_NOTICE = 4000;
    private String param;
    private TextView mTitle;
    private TextView mDescription;

    private NoticeListener noticeListener;

    public interface NoticeListener {
        void noticeJump();
        void noticeTimerFinish();
    }

    public static NoticeFragment newInstance(String param) {
        NoticeFragment fragment = new NoticeFragment();
        Bundle args = new Bundle();
        args.putString("param", param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context != null) {
                noticeListener = (NoticeListener) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement notice");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            param = getArguments().getString("param");
        }
        LedMananger.getInstance().setLedMicStatus(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected NoticePresenter createPresenter() {
        return new NoticePresenter(param, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_notice, container, false);
        mTitle = (TextView) root.findViewById(R.id.task_detail_title);
        mDescription = (TextView) root.findViewById(R.id.task_detail_description);
        initView(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logg.d(TAG, "NoticeFragment 和依附的Activity对象创建完成");
        initData();

        // PUCDR-1447: To cover the "Start Android..." screen, end boot animation until the notice is showed
        // Then start to count down the timerNotice
        new Handler().postDelayed(() -> {
            Log.v(TAG, "Set service.bootanim.exit=1 to end boot animation");
            SystemPropertiesProxy.set("service.bootanim.exit", "1");
            timerNotice.start();
        }, DELAY_END_BOOT_ANIMATION);
    }

    private void initView(View v) {
    }

    private void initData() {
        GlobalLogic.getInstance().putInt("MIC", 1);
        mPresenter.start();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void showDescription(String description) {
        mDescription.setText(description);
    }

    private CountDownTimer timerNotice = new CountDownTimer(TIME_TO_SHOW_NOTICE, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            Logg.i("NoticeFragment","=onFinish==");
            noticeListener.noticeJump();
            noticeListener.noticeTimerFinish();
        }
    };

    @Override
    public void onDestroy() {
        if (timerNotice != null) {
            timerNotice.cancel();
            timerNotice = null;
        }
        super.onDestroy();
    }
}
