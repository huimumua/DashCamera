package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

import com.askey.dvr.cdr7010.dashcam.jvcmodule.local.JvcStatusParams.JvcStatusParam;

import java.io.Serializable;
import java.util.EnumMap;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/6/1.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class LocalJvcStatusMoudle implements Serializable{

    private static final long serialVersionUID = 5220225839573759446L;

    private EnumMap<JvcStatusParam,Object> mJvcStatusMap;

    public EnumMap<JvcStatusParam, Object> getJvcStatusMap() {
        return mJvcStatusMap;
    }

    public void setJvcStatusMap(EnumMap<JvcStatusParam, Object> jvcStatusMap) {
        this.mJvcStatusMap = jvcStatusMap;
    }
}
