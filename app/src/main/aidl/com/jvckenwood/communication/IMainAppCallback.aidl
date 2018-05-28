package com.jvckenwood.communication;

interface IMainAppCallback {
    void reportInsuranceTerm(int oos, String response);
    void reportUserList(int oos, String response);
    void reportSystemSettings(int oos, String response);
    void reportUserSettings(int oos, String response);
    void reportSettingsUpdate(int oos, String response);
    void reportDrivingReport(int oos, String response);
    void reportManthlyDrivingReport(int oos, String response);
    void reportServerNotifocation(int oos, String response);
    void reportDrivingAdvice(int oos, String response);
    void reportTxEventProgress(int eventNo,int progress,int total);
    void reportTxManualProgress(int progress1,int total1,int progress2,int total2);
    void voipInfomationResponse(int oos, String response);
    void onFWUpdateRequest(int result);
    
/*    
      void reportInsuranceTerm(int status, String start, String end, int flag); 
	  void reportDrivingReport();
	  void reportManthlyDrivingReport();
	  void reportServerNotifocation();
	  void reportDrivingAdvice();
	  void reportRecorderSettings(int status, int volume, long settings);
	  void reportTxEventProgress(int progress ,int total);
	  void reportRealTimeAlert(int alertID);
	  void reportTripDataResult(int status);
	  void reportEventDataResult(int status);
	  void reportVersionUpInformation();
	  void reportVersionUpData();
*/	  
}