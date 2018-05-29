package com.askey.dvr.cdr7010.dashcam;

import com.askey.dvr.cdr7010.dashcam.IManualUploadCallback;

interface IManualUpload {
	void manualUpload(int camType, String filePath1, String startTime1, String endTime1, String filePath2, String startTime2, String endTime2);
	void manualUploadCancel(int cancel);
	void registerCallback(IManualUploadCallback callback);
	void unregisterCallback(IManualUploadCallback callback);
}
