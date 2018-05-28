package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;
import com.jvckenwood.communication.IMainAppCallback;

interface IMainApp {
    void startInitialSetup();
	void endInitialSetup();
	void settingsUpdateRequest(String setings);
	void manualUploadCancel(int cancel);
	void voipInfomationRequest(int userId,int isUserCall);
	void FWUpdateRequest();
	void registerCallback(IMainAppCallback callback);
	void unregisterCallback(IMainAppCallback callback);
	
/* 
	ParcelFileDescriptor get1HzDataPipe();
	void sendTripData(String filePath);
	void setEventNo(int eventNo, long timeStamp);
	void setEventData(int eventNo, String recordPath);
	void getVersionUpInformation();
	void getVersionUpData();
*/	
}