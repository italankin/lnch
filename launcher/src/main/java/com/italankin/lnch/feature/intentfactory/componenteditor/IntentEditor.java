package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.Intent;

public interface IntentEditor {

    void bind(Intent result);

    void update();

    interface Host {

        void requestUpdate();
    }
}
