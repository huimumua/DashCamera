package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;


interface IEventDetectionCallback {
	void reportRealTimeAlert(int alertID);
	void requestLocationData(int oos,String response);
}