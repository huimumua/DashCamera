package com.jvckenwood.communication;

interface ICommunicationCallback {
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
}