package com.italankin.lnch.feature.settings.widgets;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.host.WidgetHostView;

public class PreviewWidgetHostView extends FrameLayout implements WidgetHostView {

    public PreviewWidgetHostView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.widget_grid_settings_preview, this);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public int resizeMode() {
        return 0;
    }

    @Override
    public boolean isReconfigurable() {
        return false;
    }
}
