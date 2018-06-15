package com.askey.dvr.cdr7010.dashcam;

import com.askey.dvr.cdr7010.dashcam.IEcallCallback;

interface IEcall {

    /**
     * ************communication by broadcast************
     * @param status
     * 	    0:不明
     *      1:正常通話完了
     *      2:キャンセル(接続NG)
     *      3.キャンセル(事故発生画面にてキャンセル押下)
     *      4.キャンセル(その他)
     */
	void discEmergencyCall(int status);

    /**
     * ************communication by broadcast************
     * @param requestID
     *      VoIP情報取得依頼ID(Uniqueであること.同じ値を含んだRESULTが結果となる)
     * @param isUserCall
     *      1.User Button Press / 2.Realtime.
     */
    void voipInformationRequest(int requestID, int isUserCall);

    void registerCallback(IEcallCallback callback);
    void unregisterCallback(IEcallCallback callback);

}
