package com.italankin.lnch.feature.home.util;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class IntentQueue {

    private final List<OnIntentAction> listeners = new ArrayList<>(2);
    private Intent lastUnhandledIntent;

    public void registerOnIntentAction(OnIntentAction action) {
        listeners.add(listeners.size(), action);
        if (lastUnhandledIntent != null && action.onIntent(lastUnhandledIntent)) {
            lastUnhandledIntent = null;
        }
    }

    public void unregisterOnIntentAction(OnIntentAction action) {
        listeners.remove(action);
    }

    public void post(Intent intent) {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            OnIntentAction action = listeners.get(i);
            if (action.onIntent(intent)) {
                return;
            }
        }
        lastUnhandledIntent = intent;
    }

    public interface OnIntentAction {

        boolean onIntent(Intent intent);
    }
}
