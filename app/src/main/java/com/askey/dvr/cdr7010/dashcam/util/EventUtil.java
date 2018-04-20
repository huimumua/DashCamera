package com.askey.dvr.cdr7010.dashcam.util;

import org.greenrobot.eventbus.EventBus;

public class EventUtil{
    //注册事件
    public static void register(Object context) {
        if (!EventBus.getDefault().isRegistered(context)) {
            EventBus.getDefault().register(context);
        }
    }
    //解除
    public static void unregister(Object context) {
        if (EventBus.getDefault().isRegistered(context)) {
            EventBus.getDefault().unregister(context);
        }
    }
    //发送消息
    public static void sendEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    public static void sendStickyEvent(Object event) {
        EventBus.getDefault().postSticky(event);
    }
}