package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;
//import com.jvckenwood.communication.IEventSendingCallback;

interface IEventSending {
	void setEventData(int eventNo,long timeStamp,out List<String> picturePath,out List<String> moviePath);
	//void registerCB(IEventSendingCallback callback);

}