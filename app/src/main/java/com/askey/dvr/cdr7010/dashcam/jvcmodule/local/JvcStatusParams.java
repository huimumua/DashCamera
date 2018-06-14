package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

/***
 * Company: Chengdu Skysoft Info&Tech Co., Ltd.
 * Copyright ©2014-2018 Chengdu Skysoft Info&Tech Co., Ltd.
 * Created by Mark on 2018/6/1.

 * @since:JDK1.6
 * @version:1.0
 * @see
 ***/
public class JvcStatusParams {

    public enum JvcStatusParam {
        OOS("oos"), RESPONSE("response");

        private String name;

        JvcStatusParam(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
