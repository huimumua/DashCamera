package com.askey.dvr.cdr7010.dashcam;

import com.askey.dvr.cdr7010.dashcam.IManualUploadCallback;

interface IEcall {
	void cancelEmergencyCall();
	void discEmergencyCall(int status);
}
