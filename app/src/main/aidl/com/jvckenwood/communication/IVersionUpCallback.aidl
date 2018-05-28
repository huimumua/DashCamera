package com.jvckenwood.communication;

import android.os.ParcelFileDescriptor;


interface IVersionUpCallback {
	void reportVersionUpInformation(int oos,int fileType,String response);
}