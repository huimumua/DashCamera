package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;
import com.jvckenwood.communication.IEventDetectionCallback;

interface IEventDetection {
    ParcelFileDescriptor get1HzDataPipe();
	void sendTripData(String filePath);
	void sendImpactLocation(double lat,double lon,double gpsAcc,float detectVal,long occurDate);
	void registerCallback(IEventDetectionCallback callback);
	void unregisterCallback(IEventDetectionCallback callback);

	
/* 
	ParcelFileDescriptor get1HzDataPipe();
	void sendTripData(String filePath);
	void setEventNo(int eventNo, long timeStamp);
	void setEventData(int eventNo, String recordPath);
	void getVersionUpInformation();
	void getVersionUpData();
*/	
}