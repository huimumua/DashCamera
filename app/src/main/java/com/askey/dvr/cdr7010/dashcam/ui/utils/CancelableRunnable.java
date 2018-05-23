package com.askey.dvr.cdr7010.dashcam.ui.utils;
public abstract class CancelableRunnable implements Runnable,Cancelable{
    private volatile boolean _isCancelled = false;

    @Override
    public void _cancel() {
        _isCancelled = true;
    }

    @Override
    public void run() {
        if (_isCancelled) {
            return;
        }
        doRun();
    }

    protected abstract void doRun();
}