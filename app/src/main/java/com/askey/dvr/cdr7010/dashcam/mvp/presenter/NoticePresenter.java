package com.askey.dvr.cdr7010.dashcam.mvp.presenter;

import android.text.TextUtils;

import com.askey.dvr.cdr7010.dashcam.R;
import com.askey.dvr.cdr7010.dashcam.application.DashCamApplication;
import com.askey.dvr.cdr7010.dashcam.basemvp.BasePresenter;
import com.askey.dvr.cdr7010.dashcam.domain.NoticeItem;
import com.askey.dvr.cdr7010.dashcam.mvp.model.NoticeModel;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;

public class NoticePresenter extends BasePresenter<NoticeContract.View> implements NoticeContract.InteractionListener<NoticeItem>, NoticeContract.Presenter {
    private static final String DESCRIPTION = "本製品の操作は、必ず安全場所に停車してからおこなってください。" + "\r\n" + "運転中のに操作をおこなうと、思わめ事故の原因になります。";
    private NoticeContract.View mView;
    private NoticeContract.Model mModel;

    public NoticePresenter(String param, NoticeContract.View view) {
        this.mView = view;
        mModel = new NoticeModel(param, this);
    }

    @Override
    public void onSuccess(NoticeItem noticeInfo) {
        showNoticeInfo(noticeInfo);
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        showNoticeInfo(null);
    }


    @Override
    public void start() {
        mModel.loadContent();
    }

    private void showNoticeInfo(NoticeItem noticeInfo) {
        String title = "";
        String description = "";
        if (noticeInfo != null) {
            title = noticeInfo.getTitle();
            description = noticeInfo.getDescription();
        }

        if (TextUtils.isEmpty(title)) {
            mView.showTitle("警告");
        } else {
            mView.showTitle(title);
        }

        if (TextUtils.isEmpty(description)) {
            mView.showDescription(DESCRIPTION);
        } else {
            mView.showDescription(description);
        }
    }

    /**
     * 判断契约日
     */
    public void checkContractDay() {//暂时是写在这个方法里，有接口了之后移到接口里处理
//        int result = ((int) (Math.random() * 3)) - 2;
        int result = 1;
        String content;
        if (result < 0) {
            content = DashCamApplication.getAppContext().getString(R.string.after_contract_day_stop);
            mView.afterContractDayEnd(content);
        } else if (result == 0) {
            content = DashCamApplication.getAppContext().getString(R.string.before_contract_day_start);
            mView.beforeContractDayStart(content);
        } else {
            mView.inContractDay();
        }
    }

    @Override
    public void requestLoadData() {
    }
}