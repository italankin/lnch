package com.italankin.lnch.feature.widgets.host;

import android.view.View;

public interface WidgetHostView {

    int getAppWidgetId();

    View getView();

    int resizeMode();

    boolean isReconfigurable();
}
