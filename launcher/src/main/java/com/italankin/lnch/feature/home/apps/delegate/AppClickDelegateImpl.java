package com.italankin.lnch.feature.home.apps.delegate;

import android.content.ComponentName;
import android.content.Context;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.IntentUtils;

import androidx.annotation.Nullable;

public class AppClickDelegateImpl implements AppClickDelegate {

    private final Context context;
    private final ErrorDelegate errorDelegate;
    private final ItemPopupDelegate itemPopupDelegate;
    private final Preferences preferences;
    private final UsageTracker usageTracker;

    public AppClickDelegateImpl(Context context,
            Preferences preferences,
            ErrorDelegate errorDelegate,
            ItemPopupDelegate itemPopupDelegate,
            UsageTracker usageTracker) {
        this.context = context;
        this.errorDelegate = errorDelegate;
        this.itemPopupDelegate = itemPopupDelegate;
        this.preferences = preferences;
        this.usageTracker = usageTracker;
    }

    @Override
    public void onAppClick(AppDescriptorUi item, @Nullable View itemView) {
        ComponentName componentName = DescriptorUtils.getLauncherComponentName(context, item.getDescriptor());
        if (componentName != null) {
            usageTracker.trackLaunch(item.getDescriptor());
            if (IntentUtils.safeStartMainActivity(context, componentName, itemView)) {
                return;
            }
        }
        errorDelegate.showError(R.string.error);
    }

    @Override
    public void onAppLongClick(AppDescriptorUi item, @Nullable View itemView) {
        switch (preferences.get(Preferences.APP_LONG_CLICK_ACTION)) {
            case INFO:
                IntentUtils.safeStartAppSettings(context, item.packageName, itemView);
                break;
            case POPUP:
            default:
                itemPopupDelegate.showItemPopup(item, itemView);
                break;
        }
    }
}
