package com.askey.dvr.cdr7010.dashcam;

interface IManualUploadCallback {
    void reportTxManualProgress(int progress1,int total1,int progress2,int total2);
    void manualUploadComplete(int result, String response);
}
