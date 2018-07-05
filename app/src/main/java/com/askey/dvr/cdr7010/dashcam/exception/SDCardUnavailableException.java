package com.askey.dvr.cdr7010.dashcam.exception;

/**
 * sd卡不可用的异常
 * <p>
 * Created by Navas.li on 2018/7/5
 */
public class SDCardUnavailableException extends RuntimeException {

    public SDCardUnavailableException(String message) {
        super(message);
    }
}
