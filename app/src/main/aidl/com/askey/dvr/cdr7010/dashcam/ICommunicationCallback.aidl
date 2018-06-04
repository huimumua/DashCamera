package com.askey.dvr.cdr7010.dashcam;

interface ICommunicationCallback {
    //form broadcast
    void tripIdUpdateNotification();
    void weatherAlertResponse(String response);
    void tripIdVersionUpResponse(int fairmware, int voice);
    void tripIdLogUploadUpResponse(int logupload);

    //IMainAppCallback
    void reportInsuranceTerm(int oos, String response);
    void reportUserList(int oos, String response);
    void reportSystemSettings(int oos, String response);
    void reportUserSettings(int oos, String response);
    void reportSettingsUpdate(int oos, String response);
    void reportTxEventProgress(int eventNo,int progress,int total);
    void onFWUpdateRequest(int result);

    //IVersionUpCallback


	//local
	void onGetUserList(int oos, String response, boolean received);

}
