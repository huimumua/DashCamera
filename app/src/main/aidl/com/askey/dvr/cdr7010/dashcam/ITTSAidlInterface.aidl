// ITTSAidlInterface.aidl
package com.askey.dvr.cdr7010.dashcam;

interface ITTSAidlInterface {

    void ttsStop(int requestId);

    void ttsEventStart(int requestId,int priority,int voiceId);

    void ttsDeleteFile();

    void ttsMenuCursorFocused();

    void ttsMenuItemClick();

    void ttsMenuItemBack();

}
