package com.askey.dvr.cdr7010.dashcam;

interface IEcallCallback {
    void onVoipInformationResult(int requestID, int status, long impactId, String policyNo, int policyBranchNo,
        String authUserName, String displayName, String outboundProxy, String password,
        int port, String protocol, boolean isSendKeepAlive, String profileName, boolean isAutoRegistration);
}
