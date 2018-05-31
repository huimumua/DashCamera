package com.askey.dvr.cdr7010.dashcam.notice;


public interface NoticeContract {

    interface Model {
        //请求数据
        void loadContent();
    }

    interface View {

        void showTitle(String title);

        void showDescription(String description);

        /**
         * 合约日期开始之前
         * <p>
         * add by Navas.li on 2018.5.29
         *
         * @param content string to show
         */
        void beforeContractDayStart(String content);

        /**
         * 合约日期结束之后
         * <p>
         * add by Navas.li on 2018.5.29
         *
         * @param content string to show
         */
        void afterContractDayEnd(String content);

        /**
         * 在合约期内
         * <p>
         * add by Navas.li on 2018.5.29
         */
        void inContractDay();

        boolean isActive();
    }

    interface Presenter {
        void requestLoadData();
    }

    interface InteractionListener<T> {
        //请求成功
        void onSuccess(T t);

        //请求失败
        void onFail(int errorCode, String errorMsg);
    }
}