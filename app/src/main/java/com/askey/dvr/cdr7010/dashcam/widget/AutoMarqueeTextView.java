package com.askey.dvr.cdr7010.dashcam.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AutoMarqueeTextView extends TextView {
    public AutoMarqueeTextView(Context context) {
        super(context);
        setFocusable(true);//在每个构造方法中，将TextView设置为可获取焦点
    }

    public AutoMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AutoMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}