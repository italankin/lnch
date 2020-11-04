package com.italankin.lnch.feature.widgets.host;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.ContextWrapper;

public class LauncherAppWidgetHost extends AppWidgetHost {

    private final Context fixedContext;

    public LauncherAppWidgetHost(Context context, int hostId) {
        super(context, hostId);
        fixedContext = new FixedContextWrapper(context);
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context, int appWidgetId, AppWidgetProviderInfo appWidget) {
        return new LauncherAppWidgetHostView(context);
    }

    public final AppWidgetHostView createView(int appWidgetId, AppWidgetProviderInfo appWidget) {
        return createView(fixedContext, appWidgetId, appWidget);
    }

    private static class FixedContextWrapper extends ContextWrapper {

        FixedContextWrapper(Context base) {
            super(base);
        }

        @Override
        public Object getSystemService(String name) {
            if (LAYOUT_INFLATER_SERVICE.equals(name)) {
                // use application context's inflater to bypass appcompat inflater
                return getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            }
            return getBaseContext().getSystemService(name);
        }
    }
}
