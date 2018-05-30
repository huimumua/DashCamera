package com.jvckenwood.versionup;

import android.os.ParcelFileDescriptor;


interface IMainAppCallback {
	void onFWUpdateResponse(int result);
}