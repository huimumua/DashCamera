package com.askey.dvr.cdr7010.dashcam.mvp.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.askey.dvr.cdr7010.dashcam.basemvp.BasePresenter;

public abstract class BaseFragment<V,P extends BasePresenter<V>> extends Fragment {
    private static final String TAG = "BaseFragment";
    protected P mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();//创建Presenter
        mPresenter.attachView((V)this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    protected abstract P createPresenter();
}