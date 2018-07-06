package com.askey.dvr.cdr7010.dashcam.jvcmodule.local;

public class JvcUserListInfo {

    /**
     * user99 : {"userid":2,"name":"東野圭吾","selectdate":"20180607112000"}
     * user00 : {"userid":3,"name":"宫部美幸","selectdate":"20200607112000"}
     * num : 2
     * user01 : {"userid":1,"name":"松本清张","lastupdate":"20200607112000"}
     * user02 : {"userid":2,"name":"東野圭吾","lastupdate":"20200607112000"}
     * user03 : {"userid":3,"name":"宫部美幸","lastupdate":"20200607112000"}
     * user04 : {"userid":4,"name":"橫溝正史","lastupdate":"20200607112000"}
     * user05 : {"userid":5,"name":"京极夏彦","lastupdate":"20200607112000"}
     */
    public int status;
    public User99Bean user99;
    public User00Bean user00;
    public int num;
    public User01Bean user01;
    public User02Bean user02;
    public User03Bean user03;
    public User04Bean user04;
    public User05Bean user05;

    public static class User99Bean {
        /**
         * userid : 2
         * name : 東野圭吾
         * selectdate : 20180607112000
         */

        public int userid;
        public String name;
        public String selectdate;
    }

    public static class User00Bean {
        /**
         * userid : 3
         * name : 宫部美幸
         * selectdate : 20200607112000
         */

        public int userid;
        public String name;
        public String selectdate;
    }

    public static class User01Bean {
        /**
         * userid : 1
         * name : 松本清张
         * lastupdate : 20200607112000
         */

        public int userid;
        public String name;
        public String lastupdate;
    }

    public static class User02Bean {
        /**
         * userid : 2
         * name : 東野圭吾
         * lastupdate : 20200607112000
         */

        public int userid;
        public String name;
        public String lastupdate;
    }

    public static class User03Bean {
        /**
         * userid : 3
         * name : 宫部美幸
         * lastupdate : 20200607112000
         */

        public int userid;
        public String name;
        public String lastupdate;
    }

    public static class User04Bean {
        /**
         * userid : 4
         * name : 橫溝正史
         * lastupdate : 20200607112000
         */

        public int userid;
        public String name;
        public String lastupdate;
    }

    public static class User05Bean {
        /**
         * userid : 5
         * name : 京极夏彦
         * lastupdate : 20200607112000
         */

        public int userid;
        public String name;
        public String lastupdate;
    }
}
