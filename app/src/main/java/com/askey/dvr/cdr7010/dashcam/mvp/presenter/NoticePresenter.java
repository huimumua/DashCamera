package com.askey.dvr.cdr7010.dashcam.mvp.presenter;

import android.text.TextUtils;

import com.askey.dvr.cdr7010.dashcam.basemvp.BasePresenter;
import com.askey.dvr.cdr7010.dashcam.domain.NoticeItem;
import com.askey.dvr.cdr7010.dashcam.mvp.model.NoticeModel;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;

public class NoticePresenter extends BasePresenter<NoticeContract.View> implements NoticeContract .InteractionListener<NoticeItem>,NoticeContract .Presenter{
    private static final String DESCRIPTION ="本网站在美国维护受美国法律保护这样的提示你是不是经常看到，"+"\r\n"+"不光是看到，可能很多小伙伴还搜索过！因为根据本站监测，该关键词的搜索有上升趋势！";
    private NoticeContract .View mView;
    private NoticeContract .Model mModel;
    private String param;
    private NoticeItem noticeInfo;

    public NoticePresenter (String param, NoticeContract .View view){
        this.param= param;
        this.mView = view;
        mModel = new NoticeModel(param,this);
    }

    @Override
    public void onSuccess(NoticeItem noticeInfo) {
        showNoticeInfo(noticeInfo);
    }

    @Override
    public void onFail(int errorCode, String errorMsg) {
        showNoticeInfo(noticeInfo);
    }



    @Override
    public void start() {
        mModel.loadContent();
    }
    private void showNoticeInfo(NoticeItem noticeInfo){
        String title = "";
        String description ="";
        if(noticeInfo != null){
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
    @Override
    public  void requestLoadData(){
        return;
    }
}