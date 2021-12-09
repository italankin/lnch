package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.api.LauncherShortcuts;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ViewUtils;

import androidx.annotation.Nullable;

public class ShortcutStarterDelegateImpl implements ShortcutStarterDelegate {

    private final Context context;
    private final ErrorDelegate errorDelegate;
    private final CustomizeDelegate customizeDelegate;

    public ShortcutStarterDelegateImpl(Context context, ErrorDelegate errorDelegate,
            CustomizeDelegate customizeDelegate) {
        this.context = context;
        this.errorDelegate = errorDelegate;
        this.customizeDelegate = customizeDelegate;
    }

    @Override
    public void startShortcut(@Nullable Shortcut shortcut, @Nullable View view) {
        if (shortcut == null) {
            errorDelegate.showError(R.string.error_shortcut_not_found);
            return;
        }
        if (handleCustomizeShortcut(shortcut.getPackageName(), shortcut.getId())) {
            return;
        }
        if (!shortcut.isEnabled()) {
            onShortcutDisabled(shortcut.getDisabledMessage());
            return;
        }
        Rect bounds = ViewUtils.getViewBounds(view);
        Bundle opts = IntentUtils.getActivityLaunchOptions(view, bounds);
        if (!shortcut.start(bounds, opts)) {
            errorDelegate.showError(R.string.error);
        }
    }

    private void onShortcutDisabled(CharSequence disabledMessage) {
        CharSequence message = TextUtils.isEmpty(disabledMessage)
                ? context.getText(R.string.error_shortcut_disabled)
                : disabledMessage;
        errorDelegate.showError(message);
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
