package com.jvckenwood.versionup;

import android.os.ParcelFileDescriptor;
import com.jvckenwood.versionup.IMainAppCallback;

interface IMainApp {
	void FWUpdateRequest();
	void registerCallback(IMainAppCallback callback);
	void unregisterCallback(IMainAppCallback callback);
	
}