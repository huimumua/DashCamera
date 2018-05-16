package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;
import com.jvckenwood.communication.ICommunicationCallback;

interface ICommunication {
      void startInitialSetup(ICommunicationCallback callback);
	  void endInitialSetup();
	  ParcelFileDescriptor get1HzDataPipe();
	  void sendTripData(String filePath);
	  void setEventNo(int eventNo, long timeStamp);
	  void setEventData(int eventNo, String recordPath);
	  void getVersionUpInformation();
	  void getVersionUpData();
}