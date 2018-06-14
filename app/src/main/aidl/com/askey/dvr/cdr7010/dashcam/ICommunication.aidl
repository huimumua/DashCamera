package com.askey.dvr.cdr7010.dashcam;

import android.os.ParcelFileDescriptor;
import com.askey.dvr.cdr7010.dashcam.ICommunicationCallback;

interface ICommunication {

	//IMainApp
	void settingsUpdateRequest(String setings);


	void registerCallback(ICommunicationCallback callback);
	void unregisterCallback(ICommunicationCallback callback);
}
