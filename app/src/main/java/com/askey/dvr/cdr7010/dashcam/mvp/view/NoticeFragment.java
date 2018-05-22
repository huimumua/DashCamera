package com.askey.dvr.cdr7010.dashcam.mvp.view;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.logic.GlobalLogic;
import com.askey.dvr.cdr7010.dashcam.mvp.presenter.NoticePresenter;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;
import com.askey.dvr.cdr7010.dashcam.service.LedMananger;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

public class NoticeFragment extends BaseFragment<NoticeContract.View, NoticePresenter> implements NoticeContract.View{

    private static final String TAG = NoticeFragment.class.getSimpleName();
    private String param;
    private TextView mTitle;
    private TextView mDescription;
    private NoticeListener noticeListener;

    public interface NoticeListener{
        public void noticeJump();
    }


    public static NoticeFragment newInstance(String param) {
        NoticeFragment fragment = new NoticeFragment();
        Bundle args = new Bundle();
        args.putString("param", param);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            if(context!=null)
            {
                noticeListener= (NoticeListener) context;
            }
        } catch (ClassCastException e)
        { throw new ClassCastException(context.toString() + " must implement notice");
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
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logg.d(TAG, "NoticeFragment 和依附的Activity对象创建完成");
        initData();
        timer.start();
    }
    private void initView(View v) {

    }

    private void initData() {
        GlobalLogic.getInstance().putInt("MIC",1);
        mPresenter.start();
    }
    @Override
    public boolean isActive() {
        return isAdded();
    }
    @Override
    public  void showTitle(String title){
        mTitle.setText(title);
    }
    @Override
    public void onDestroy() {
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }
    @Override
    public  void showDescription(String description){
        mDescription.setText(description);
    }
    private CountDownTimer timer = new CountDownTimer(4000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
            if(getActivity() != null){
                Logg.d(TAG,"onTick millisUntilFinished="+millisUntilFinished);
            }
        }

        @Override
        public void onFinish() {
            noticeListener.noticeJump();
            Logg.d(TAG,"onFinish");
        }
    };

}
