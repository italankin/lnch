package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import com.italankin.lnch.api.LauncherIntents;
import com.italankin.lnch.api.LauncherShortcuts;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ResUtils;

public class SearchIntentStarterDelegateImpl implements SearchIntentStarterDelegate {

    private final Context context;
    private final Preferences preferences;
    private final ErrorDelegate errorDelegate;
    private final CustomizeDelegate customizeDelegate;
    private final UsageTracker usageTracker;

    public SearchIntentStarterDelegateImpl(Context context, Preferences preferences, ErrorDelegate errorDelegate,
            CustomizeDelegate customizeDelegate, UsageTracker usageTracker) {
        this.context = context;
        this.preferences = preferences;
        this.errorDelegate = errorDelegate;
        this.customizeDelegate = customizeDelegate;
        this.usageTracker = usageTracker;
    }

    @Override
    public void handleSearchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean isCustom = intent.getBooleanExtra(IntentDescriptor.EXTRA_CUSTOM_INTENT, false);
        if (preferences.get(Preferences.SEARCH_USE_CUSTOM_TABS)
                && Intent.ACTION_VIEW.equals(intent.getAction())
                && intent.getData() != null
                && !isCustom) {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(new CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(ResUtils.resolveColor(context, android.R.attr.colorPrimary))
                            .build())
                    .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                    .setShowTitle(true)
                    .build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (IntentUtils.canHandleIntent(context, customTabsIntent.intent)) {
                customTabsIntent.launchUrl(context, intent.getData());
            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && !isCustom &&
                LauncherIntents.ACTION_START_SHORTCUT.equals(intent.getAction())) {
            String packageName = intent.getStringExtra(LauncherIntents.EXTRA_PACKAGE_NAME);
            String shortcutId = intent.getStringExtra(LauncherIntents.EXTRA_SHORTCUT_ID);
            if (!handleCustomizeShortcut(packageName, shortcutId)) {
                // start deep shortcut
                context.sendBroadcast(intent);
            }
            return;
        }
        IntentUtils.safeStartActivity(context, intent, (unused, e) -> errorDelegate.showError(e));
    }

    @Override
    public void handleSearchIntent(Intent intent, Descriptor descriptor) {
        usageTracker.trackLaunch(descriptor);
        handleSearchIntent(intent);
    }

    private boolean handleCustomizeShortcut(String packageName, String shortcutId) {
        if (context.getPackageName().equals(packageName)
                && LauncherShortcuts.ID_SHORTCUT_CUSTOMIZE.equals(shortcutId)) {
            customizeDelegate.startCustomize();
            return true;
        }
        return false;
    }
}
