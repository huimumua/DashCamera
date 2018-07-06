package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

public class JvcUserSettingInfo {
    public int status;
    public int num;
    public UserSettinginfo user01;
    public UserSettinginfo user02;
    public UserSettinginfo user03;
    public UserSettinginfo user04;
    public UserSettinginfo user05;

    public class UserSettinginfo {
        public int userid;                        //数値
        public String user_name;                            //文字
        public int warn_coll;                        //0,1
        public int warn_dev;                        //0,1
        public int warn_delay;                            //0,1
        public int warn_pades;                    //0,1
        public int reverse;                        //0,1
        public int zone30;                        //0,1
        public int pause;                        //0,1
        public int accident;                    //0,1
        public int runtime;                //0,1
        public int rapid;                    //0,1
        public int handle;                    //0,1
        public int wobble;                            //0,1
        public int outside;                            //0,1
        public int report;                            //0,1
        public int advice;                            //0,1
        public int notice;                            //0,1
        public int weather;                            //0,1
        public int animal;                        //0,1
        public int location;                            //0,1
        public int volume_n;                            //0,1,2,3,4,5
        public int volume_p;                            //0,1,2,3,4,5
        public int bright;                            //0～10
        public int psave_s;                            //10,60,180
        public int psave_e;                            //0,1,2
        public int lang;                            //0
        public String set_update_day;                            //YYYYMMDDhhmmss
        public int outbound_call;                            //0,1
        public String lastupdate;                            //YYYYMMDDhhmmss
    }
}
