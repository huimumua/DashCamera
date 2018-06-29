package com.askey.dvr.cdr7010.dashcam;

interface ICommunicationCallback {

    //IMainAppCallback
    void reportTxEventProgress(int eventNo,int progress,int total);
    void reportSettingsUpdate(int oos, String response);


}
