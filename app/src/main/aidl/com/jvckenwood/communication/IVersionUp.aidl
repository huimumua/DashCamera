package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;
import com.jvckenwood.communication.IVersionUpCallback;

interface IVersionUp {
    void getVersionUpInformation(int fileType,String currentVersion);
	void getVersionUpData(int fileType,String version,int range);
	void registerCallback(IVersionUpCallback callback);
	void unregisterCallback(IVersionUpCallback callback);
	
}