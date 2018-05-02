package com.askey.dvr.cdr7010.dashcam.core.camera;

import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.askey.dvr.cdr7010.dashcam.util.Logg;

@SuppressWarnings("deprecation")
public class Camera1Controller implements android.hardware.Camera.PreviewCallback, android.hardware.Camera.ErrorCallback {
    private static final String LOG_TAG = "Camera1Controller";
    private android.hardware.Camera _camera;
    private SurfaceHolder _surfaceHolder;
    /**
     * Boolean indicating whether the app is running in continuous autofocus
     * mode. In which case, the sensor events are simply ignored.
     */
    private boolean _isContinuousAutofocusModeRunning = false;

    /**
     * This flag is required for devices on which Continous_Picture_Mode is
     * supported (found in supported mode list) but is not actually functional.
     * (Samsung galaxy nexus)
     */
    private boolean _doNotUseContinuosPictureMode;

    /**
     * The callback buffer to be given to camera.
     */
    private byte[] _cameraPreviewBuffer;

    private Timer _checkContinuosPictureModeSupportedTimer;
    private CheckContinuosPictureModeSupportedTask _checkContinuosPictureModeSupportedTask;
    private final long CHECK_CONTINUOS_PICTURE_MODE_SUPPORTED_TIMEOUT = 5 * 1000;
    private final long CANCEL_AUTOFOCUS_TIMEOUT = 3 * 1000;

    private android.hardware.Camera.AutoFocusMoveCallback _autofocusMoveCallback;
    private CameraPreviewCallback _cameraPreviewCallback;
    private boolean _previewing = false;
    private boolean autofocusing = false;
    private boolean isFocused = false;
    private Timer _cancelAutofocusTimer;
    private CancelAutoFocusTask _cancelAutofocusTask;
    private ErrorCallback _errorCallback;

    public interface ErrorCallback {
        void onError(int error);
    }

    public boolean hasCamera() {
        return true;
    }

    @SuppressWarnings("deprecation")
    public synchronized int openCamera() {
        try {
            _camera = android.hardware.Camera.open(); // attempt to get a Camera instance
            return 1;
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized void closeCamera() {
        if (_camera == null) {
            return;
        }
        try {
            /**
             * Cancel any pending autofocus cycles
             */
            _isContinuousAutofocusModeRunning = false;
            _camera.cancelAutoFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            /**
             * Reset the preview callback so that no new frames will be
             * received.
             */
            _camera.setPreviewCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            /**
             * Stop the preview
             */
            _camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            /**
             * Release the camera
             */
            _camera.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        _cancelCheckContinuosPictureModeSupportedTimer();
        _camera = null;
        _previewing = false;
    }

    public void configure() {
        if (_camera != null) {
            setPreviewFps();
            setExposureCompensation();
            setExposureParams();
            //setWhiteBalance();
            //setAutofocusMode();
            setDisplayOrientation();
            //setISO();
            _camera.setErrorCallback(this);
        }
    }

    public void setPreviewDisplay(Object holder) {
        try {
            _surfaceHolder = (SurfaceHolder) holder;
            _camera.setPreviewDisplay(_surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (_camera != null) {
            try {
                _camera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void startPreview(int width, int height) {
        if (_camera != null) {
            try {
                setOptimalPreviewSize(width, height);
                if (_cameraPreviewCallback != null) {
                    _cameraPreviewCallback.setPreviewSize(getPreviewWidth(), getPreviewHeight());
                }
                _camera.setPreviewCallbackWithBuffer(Camera1Controller.this);
                _cameraPreviewBuffer = new byte[(width * height * 3 / 2)];
                _camera.addCallbackBuffer(_cameraPreviewBuffer);
                _camera.startPreview();
                _previewing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stopPreview() {
        if (_camera != null) {
            try {
                _camera.stopPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        _previewing = false;
    }

    public void setErrorCallback(ErrorCallback callback) {
        _errorCallback = callback;
    }

    @SuppressWarnings("deprecation")
    private void setOptimalPreviewSize(int width, int height) {
        android.hardware.Camera.Parameters params = _camera.getParameters();
        final android.hardware.Camera.Size sizes = getClosestSupportedSize(
                params.getSupportedPreviewSizes(), width, height);
        params.setPreviewSize(sizes.width, sizes.height);
        params.setPictureSize(sizes.width, sizes.height);
        _camera.setParameters(params);
    }

    public void setPreviewFps() {
        if (_camera != null) {
            try {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                final List<int[]> supportedFpsRange = params.getSupportedPreviewFpsRange();
                final int[] max_fps = supportedFpsRange.get(supportedFpsRange.size() - 1);
                params.setPreviewFpsRange(max_fps[0], max_fps[1]);
                _camera.setParameters(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setExposureParams() {
        if (_camera != null) {
            try {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                if (params.isAutoExposureLockSupported()) {
                    params.setAutoExposureLock(false);
                }
                if (params.isVideoStabilizationSupported()) {
                    params.setVideoStabilization(true);
                }
                _camera.setParameters(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setExposureCompensation() {
        if (_camera != null) {
            try {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                int minExp= params.getMinExposureCompensation();
                int maxExp = params.getMaxExposureCompensation();
                int curExp = params.getExposureCompensation();
                Logg.i(LOG_TAG, " minExp:" + minExp + " maxExp:" + maxExp + " curExp:" + curExp);
                if (minExp !=0 || maxExp != 0) {
                    params.setExposureCompensation(4);
                }
                _camera.setParameters(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setWhiteBalance() {
        if (_camera != null) {
            try {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                List<String> strWhiteBalanceList= params.getSupportedWhiteBalance();
                String strWhiteBalance  = params.getWhiteBalance();
                String strAntibanding = params.getAntibanding();
                Logg.e(LOG_TAG, " strWhiteBalanceList:" + strWhiteBalanceList  + " strWhiteBalance:" + strWhiteBalance + " strAntibanding:" + strAntibanding);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setAutofocusMode() {
        if (_camera != null) {
            try {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                List<String> strFocusModes = params.getSupportedFocusModes();
                Logg.e(LOG_TAG, "====strFocusModes=====" + strFocusModes);
                params.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                if (strFocusModes.contains(
                        android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    Logg.e(LOG_TAG, "====FOCUS_MODE_CONTINUOUS_VIDEO=====");
                }
                _camera.setParameters(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setDisplayOrientation() {
        if (_camera != null) {
            _camera.setDisplayOrientation(0);
        }
    }

    public void setISO(){
        if(_camera!=null) {
            android.hardware.Camera.Parameters params = _camera.getParameters();
            String isoValues = params.get("iso-values");
            Logg.e(LOG_TAG, "====isoValues=====" + isoValues);
            params.set("iso", "ISO800");
            _camera.setParameters(params);
            String newValue = params.get("iso");
            Logg.i(LOG_TAG, "=====newValue===" + newValue);
        }
    }

    @SuppressWarnings("deprecation")
    public synchronized void autofocus() {
        if (_isContinuousAutofocusModeRunning) {
            /**
             * Ignore if continuous autofocus is running.
             */
            return;
        }
        if (!autofocusing && _camera != null) {
            try {
                /**
                 * This call is required to remove any pending autofocus
                 * callback events.
                 */
                _camera.cancelAutoFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                autofocusing = true;
                isFocused = false;
                startCancelAutofocusTimer();
                try {
                    /**
                     * Callback to be received when running in continuous
                     * autofocus mode.
                     */
                    _autofocusMoveCallback = new android.hardware.Camera.AutoFocusMoveCallback() {
                        @Override
                        public void onAutoFocusMoving(boolean start,
                                                      android.hardware.Camera camera) {
                            _cancelCheckContinuosPictureModeSupportedTimer();
                        }
                    };

                    _camera.setAutoFocusMoveCallback(_autofocusMoveCallback);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                _camera.autoFocus(_autoFocusCallback);
            } catch (Exception e) {
                e.printStackTrace();
                autofocusing = false;
            }
        }
    }

    public void setCameraPreviewCallback(CameraPreviewCallback callback) {
        _cameraPreviewCallback = callback;
    }

    public boolean isPreviewing() {
        return _previewing;
    }

    /**
     * Callback to be received when autofocus is complete
     */
    @SuppressWarnings("deprecation")
    private android.hardware.Camera.AutoFocusCallback _autoFocusCallback = new android.hardware.Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean autoFocusSuccess, android.hardware.Camera arg1) {
            autofocusing = false;
            isFocused = autoFocusSuccess;
            _cancelCheckContinuosPictureModeSupportedTimer();

            if (autoFocusSuccess && !_doNotUseContinuosPictureMode) {
                android.hardware.Camera.Parameters params = _camera.getParameters();
                if (params.getSupportedFocusModes().contains(
                        android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    params.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                _camera.setParameters(params);
                _isContinuousAutofocusModeRunning = true;
                try {
                    _autofocusMoveCallback = new android.hardware.Camera.AutoFocusMoveCallback() {
                        @Override
                        public void onAutoFocusMoving(boolean start,
                                                      android.hardware.Camera camera) {
                            _cancelCheckContinuosPictureModeSupportedTimer();
                        }
                    };
                    _camera.setAutoFocusMoveCallback(_autofocusMoveCallback);
                } catch (Throwable e) {
                    _doNotUseContinuosPictureMode = true;
                    _isContinuousAutofocusModeRunning = false;
                }
                _startCheckContinuosPictureModeSupportedTimer();
            }
        }
    };

    @Override
    @SuppressWarnings("deprecation")
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        if (_cameraPreviewCallback != null) {
            _cameraPreviewCallback.onPreviewFrame(data, camera);
        }
    }

    @SuppressWarnings("deprecation")
    @Override //android.hardware.Camera.ErrorCallback
    public void onError(int error, android.hardware.Camera camera) {
        if (_errorCallback != null) {
            _errorCallback.onError(error);
        }
    }

    @SuppressWarnings("deprecation")
    public int getPreviewWidth() {
        android.hardware.Camera.Parameters parameters = _camera.getParameters();
        return parameters.getPreviewSize().width;
    }

    @SuppressWarnings("deprecation")
    public int getPreviewHeight() {
        android.hardware.Camera.Parameters parameters = _camera.getParameters();
        return parameters.getPreviewSize().height;
    }

    @SuppressWarnings("deprecation")
    public int getPreviewFormat() {
        android.hardware.Camera.Parameters parameters = _camera.getParameters();
        return parameters.getPreviewFormat();
    }

    /**
     * This class extends {@link TimerTask} and is used to check if continuous
     * autofocus mode is actually supported on device. Since on some devices
     * even if we get the continuous autofocus mode in list of supported modes,
     * it is not functional.
     *
     * @author Sayyad.abid
     */
    private class CheckContinuosPictureModeSupportedTask extends TimerTask {
        @Override
        public void run() {
            _doNotUseContinuosPictureMode = true;
            _isContinuousAutofocusModeRunning = false;
            setAutofocusMode();
        }
    }

    /**
     * Method to start a timer to check if continuous autofocus mode is actually
     * supported.
     */
    private void _startCheckContinuosPictureModeSupportedTimer() {
        _cancelCheckContinuosPictureModeSupportedTimer();
        try {
            _checkContinuosPictureModeSupportedTimer = new Timer();
            _checkContinuosPictureModeSupportedTask = new CheckContinuosPictureModeSupportedTask();
            _checkContinuosPictureModeSupportedTimer.schedule(
                    _checkContinuosPictureModeSupportedTask,
                    CHECK_CONTINUOS_PICTURE_MODE_SUPPORTED_TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to cancel a previously set timer to check if continuous autofocus
     * mode is actually supported.
     */
    private void _cancelCheckContinuosPictureModeSupportedTimer() {
        if (_checkContinuosPictureModeSupportedTimer != null) {
            try {
                _checkContinuosPictureModeSupportedTimer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            _checkContinuosPictureModeSupportedTimer = null;
        }

        if (_checkContinuosPictureModeSupportedTask != null) {
            try {
                _checkContinuosPictureModeSupportedTask.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            _checkContinuosPictureModeSupportedTask = null;
        }
    }

    /**
     * This class extends {@link TimerTask} and is used to cancel the pending autofocus cycle.
     *
     * @author Sayyad.abid
     */
    private class CancelAutoFocusTask extends TimerTask {
        @Override
        public void run() {
            autofocusing = false;
        }
    }

    /**
     * This method is used to schedule a timer to cancel pending autofocus cycle.
     */
    protected void startCancelAutofocusTimer() {
        cancelAutofocusTimer( );
        try {
            _cancelAutofocusTimer = new Timer( );
            _cancelAutofocusTask = new CancelAutoFocusTask( );
            _cancelAutofocusTimer.schedule( _cancelAutofocusTask, CANCEL_AUTOFOCUS_TIMEOUT );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
    }

    /**
     * This method is used to cancel the previously set timer.
     */
    protected void cancelAutofocusTimer() {
        if ( _cancelAutofocusTimer != null ) {
            try {
                _cancelAutofocusTimer.cancel( );
            } catch ( Exception e ) {
                e.printStackTrace( );
            }
            _cancelAutofocusTimer = null;
        }

        if ( _cancelAutofocusTask != null ) {
            try {
                _cancelAutofocusTask.cancel( );
            } catch ( Exception e ) {
                e.printStackTrace( );
            }
            _cancelAutofocusTask = null;
        }
    }

    @SuppressWarnings("deprecation")
    private static android.hardware.Camera.Size getClosestSupportedSize(
            List<android.hardware.Camera.Size> supportedSizes,
            final int requestedWidth, final int requestedHeight) {
        return Collections.min(supportedSizes, new Comparator<android.hardware.Camera.Size>() {

            private int diff(final android.hardware.Camera.Size size) {
                return Math.abs(requestedWidth - size.width) + Math.abs(requestedHeight - size.height);
            }

            @Override
            public int compare(final android.hardware.Camera.Size lhs, final android.hardware.Camera.Size rhs) {
                return diff(lhs) - diff(rhs);
            }
        });

    }
}

