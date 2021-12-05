package com.italankin.lnch.feature.home.apps.delegate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ListUtils;
import com.italankin.lnch.util.NumberUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public abstract class AppClickDelegateImpl implements AppClickDelegate {

    private final Context context;
    private final Picasso picasso;
    private final ErrorDelegate errorDelegate;
    private final PopupDelegate popupDelegate;
    private final ShortcutStarterDelegate shortcutStarterDelegate;
    private final Preferences preferences;
    private final ShortcutsRepository shortcutsRepository;

    public AppClickDelegateImpl(Context context,
            Picasso picasso,
            ErrorDelegate errorDelegate,
            PopupDelegate popupDelegate,
            ShortcutStarterDelegate shortcutStarterDelegate,
            Preferences preferences,
            ShortcutsRepository shortcutsRepository) {
        this.context = context;
        this.picasso = picasso;
        this.errorDelegate = errorDelegate;
        this.popupDelegate = popupDelegate;
        this.shortcutStarterDelegate = shortcutStarterDelegate;
        this.preferences = preferences;
        this.shortcutsRepository = shortcutsRepository;
    }

    protected abstract void pinShortcut(Shortcut shortcut);

    @Override
    public void onAppClick(AppDescriptorUi item, @Nullable View itemView) {
        ComponentName componentName = DescriptorUtils.getComponentName(context, item.getDescriptor());
        if (componentName != null) {
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
                List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(item.getDescriptor());
                showAppPopup(item, processShortcuts(shortcuts), itemView);
                break;
        }
    }

    private void showAppPopup(AppDescriptorUi item, List<Shortcut> shortcuts, View view) {
        boolean uninstallAvailable = !PackageUtils.isSystem(context.getPackageManager(), item.packageName);
        ActionPopupWindow.ItemBuilder infoItem = new ActionPopupWindow.ItemBuilder(context)
                .setLabel(R.string.popup_app_info)
                .setIcon(R.drawable.ic_app_info)
                .setOnClickListener(v -> IntentUtils.safeStartAppSettings(context, item.packageName, v));
        ActionPopupWindow.ItemBuilder uninstallItem = new ActionPopupWindow.ItemBuilder(context)
                .setLabel(R.string.popup_app_uninstall)
                .setIcon(R.drawable.ic_action_delete)
                .setOnClickListener(v -> {
                    Intent intent = PackageUtils.getUninstallIntent(item.packageName);
                    if (!IntentUtils.safeStartActivity(context, intent)) {
                        errorDelegate.showError(R.string.error);
                    }
                });

        ActionPopupWindow popup = new ActionPopupWindow(context, picasso);
        if (shortcuts.isEmpty()) {
            popup.addShortcut(infoItem.setIconDrawableTintAttr(R.attr.colorAccent));
            if (uninstallAvailable) {
                popup.addShortcut(uninstallItem.setIconDrawableTintAttr(R.attr.colorAccent));
            }
        } else {
            popup.addAction(infoItem);
            if (uninstallAvailable) {
                popup.addAction(uninstallItem);
            }
            for (Shortcut shortcut : shortcuts) {
                popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(shortcut.getShortLabel())
                        .setIcon(shortcut.getIconUri())
                        .setEnabled(shortcut.isEnabled())
                        .setOnClickListener(v -> {
                            shortcutStarterDelegate.startShortcut(shortcut, v);
                        })
                        .setOnPinClickListener(v -> pinShortcut(shortcut))
                );
            }
        }
        popupDelegate.showPopupWindow(popup, view);
    }

    private List<Shortcut> processShortcuts(List<Shortcut> shortcuts) {
        if (preferences.get(Preferences.SHORTCUTS_SORT_MODE) == Preferences.ShortcutsSortMode.REVERSED) {
            shortcuts = ListUtils.reversedCopy(shortcuts);
        }
        int max = NumberUtils.parseInt(preferences.get(Preferences.MAX_DYNAMIC_SHORTCUTS), -1);
        if (max < 0 || shortcuts.size() <= max) {
            return shortcuts;
        }
        List<Shortcut> result = new ArrayList<>(shortcuts.size());
        for (Shortcut shortcut : shortcuts) {
            if (!shortcut.isDynamic() || max-- > 0) {
                result.add(shortcut);
            }
        }
        return result;
    }
}
