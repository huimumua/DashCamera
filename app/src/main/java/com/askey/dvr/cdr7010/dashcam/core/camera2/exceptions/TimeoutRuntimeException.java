package com.askey.dvr.cdr7010.dashcam.core.camera2.exceptions;

/**
 * Used instead of TimeoutException when something times out in a non-recoverable manner.
 *
 * <p>This typically happens due to a deadlock or bug in the camera service,
 * so please file a bug for this. This should never ever happen in normal operation, which is
 * why this exception is unchecked.</p>
 */
public class TimeoutRuntimeException extends RuntimeException {
    public TimeoutRuntimeException(String message) {
        super(message);
    }

    public TimeoutRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
