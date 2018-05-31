package com.askey.dvr.cdr7010.dashcam;

import android.os.ParcelFileDescriptor;
import com.askey.dvr.cdr7010.dashcam.ICommunicationCallback;

interface ICommunication {
    //by broadcast
	void changeUserID(int userId);
	void alertComplite(int eventType);
	void weatherAlertRequest();
	void setEventData(int eventNo, long timeStamp, out List<String> picturePath, out List<String> moviePath);
	//IMainApp
	void startInitialSetup();
	void endInitialSetup();
	void settingsUpdateRequest(String setings);
	void FWUpdateRequest();

	//IEventDetection
    ParcelFileDescriptor get1HzDataPipe();
	void sendTripData(String filePath);

	//IEventSending
	void EventSending_SetEventData(int eventNo,long timeStamp,out List<String> picturePath,out List<String> moviePath);

	//IVersionUp
    void getVersionUpInformation(int fileType,String currentVersion);
	void getVersionUpData(int fileType,String version,int range);

	void registerCallback(ICommunicationCallback callback);
	void unregisterCallback(ICommunicationCallback callback);
}
