package com.italankin.lnch.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class Debouncer {

    private static final int MSG_TRIGGER = 1;

    private final long delayMillis;
    private final Handler handler;

    public Debouncer(long delayMillis) {
        this.delayMillis = delayMillis;
        this.handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.what == MSG_TRIGGER) {
                ((Runnable) msg.obj).run();
                return true;
            }
            return false;
        });
    }

    public void send(Runnable r) {
        handler.removeMessages(MSG_TRIGGER);
        Message msg = handler.obtainMessage(MSG_TRIGGER, r);
        handler.sendMessageDelayed(msg, delayMillis);
    }
}
