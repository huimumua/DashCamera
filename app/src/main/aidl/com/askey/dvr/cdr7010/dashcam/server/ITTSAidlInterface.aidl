// ITTSAidlInterface.aidl
package com.askey.dvr.cdr7010.dashcam.server;

interface ITTSAidlInterface {

    void ttsNormalStart(String message);

    void ttsResume();

    void ttsPause();

    void ttsStop();

    void ttsRelease();

    void changeLanguage(String language);

    void ttsEventStart(String message,int eventType,int priority);

    void setTtsStreamVolume();
}
