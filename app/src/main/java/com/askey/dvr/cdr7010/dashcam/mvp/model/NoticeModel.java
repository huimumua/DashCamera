package com.askey.dvr.cdr7010.dashcam.mvp.model;

import com.askey.dvr.cdr7010.dashcam.domain.NoticeItem;
import com.askey.dvr.cdr7010.dashcam.notice.NoticeContract;

public class NoticeModel implements NoticeContract.Model {

    private NoticeContract.InteractionListener<NoticeItem> mListener;

    private String param;

    public NoticeModel(String param, NoticeContract.InteractionListener<NoticeItem> listener) {
        this.param = param;
        this.mListener = listener;
    }

    @Override
    public void loadContent() {
        //request data
        if(true){
            mListener.onSuccess(null);
        }else{
            mListener.onFail(0,"错误信息");
        }
    }
}