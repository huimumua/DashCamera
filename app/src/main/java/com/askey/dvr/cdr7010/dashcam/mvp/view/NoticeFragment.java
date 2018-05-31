package com.askey.dvr.cdr7010.dashcam.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.mvp.presenter.NoticePresenter;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.util.ActivityUtils;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class NoticeFragment extends BaseFragment<NoticeContract.View, NoticePresenter> implements NoticeContract.View {

    private static final String TAG = NoticeFragment.class.getSimpleName();
    private String param;
    private TextView mTitle;
    private TextView mDescription;

    private LinearLayout llTaskDetail;
    private TextView tvNoticeContent;

    private NoticeListener noticeListener;

    public interface NoticeListener {
        /**
         * jump to anotherPage.
         * <p>
         * Modified by Navas.li on 2018.5.31.add param
         *
         * @param isStartRecord whether to start record.false for do not start record otherwise for will start record
         */
        void noticeJump(boolean isStartRecord);
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
        timerNotice.start();
    }

    private void initView(View v) {
        llTaskDetail = (LinearLayout) v.findViewById(R.id.ll_task_detail);
        tvNoticeContent = (TextView) v.findViewById(R.id.tv_notice_content);
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

    @Override
    public void beforeContractDayStart(String content) {
        tvNoticeContent.setText(content);
        llTaskDetail.setVisibility(View.GONE);
        tvNoticeContent.setVisibility(View.VISIBLE);
        timerBefore.start();
    }

    @Override
    public void afterContractDayEnd(String content) {
        tvNoticeContent.setText(content);
        llTaskDetail.setVisibility(View.GONE);
        tvNoticeContent.setVisibility(View.VISIBLE);
        timeFinishApp.start();//1分钟后关闭电源
    }

    @Override
    public void inContractDay() {
        noticeListener.noticeJump(true);
    }

    private CountDownTimer timerNotice = new CountDownTimer(4000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            mPresenter.checkContractDay();//倒计时结束，检查合约日期
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

    private CountDownTimer timerBefore = new CountDownTimer(60000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            //进入到主页面，但不开启录制
            noticeListener.noticeJump(false);
        }
    };

    @Override
    public void onDestroy() {
        if (timerNotice != null) {
            timerNotice.cancel();
            timerNotice = null;
        }
        if (timeFinishApp != null) {
            timeFinishApp.cancel();
            timeFinishApp = null;
        }
        if (timerBefore != null) {
            timerBefore.cancel();
            timerBefore = null;
        }
        super.onDestroy();
    }
}
