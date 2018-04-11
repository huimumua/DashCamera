package com.askey.dvr.cdr7010.dashcam.notice;


public interface NoticeContract {

     interface Model {
        //请求数据
        void loadContent();
    }
    interface View  {

        void showTitle(String title);
        void showDescription(String description);
        boolean isActive();
    }

    interface Presenter {
        void requestLoadData();
    }

    public interface InteractionListener<T> {
        //请求成功
        void onSuccess(T t);
        //请求失败
        void onFail(int errorCode, String errorMsg);
    }
}