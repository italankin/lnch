package com.italankin.lnch.model.repository.descriptor.apps.interactors;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageManager;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.prefs.Preferences;

import static com.italankin.lnch.model.repository.descriptor.apps.interactors.LauncherActivityInfoUtils.*;

public class AppDescriptorInteractor {

    private final PackageManager packageManager;
    private final Preferences preferences;
    private final NameNormalizer nameNormalizer;

    public AppDescriptorInteractor(PackageManager packageManager, Preferences preferences,
            NameNormalizer nameNormalizer) {
        this.packageManager = packageManager;
        this.preferences = preferences;
        this.nameNormalizer = nameNormalizer;
    }

    public AppDescriptor.Mutable createItem(LauncherActivityInfo info) {
        return createItem(info, false);
    }

    AppDescriptor.Mutable createItem(LauncherActivityInfo info, boolean withComponentName) {
        String packageName = info.getApplicationInfo().packageName;
        CharSequence label = info.getLabel();
        AppDescriptor.Mutable app = new AppDescriptor.Mutable(
                packageName,
                withComponentName ? getComponentName(info) : null,
                getVersionCode(packageManager, packageName),
                label.toString());
        app.setLabel(nameNormalizer.normalize(label));
        app.setColor(getDominantIconColor(info, isDarkTheme()));
        return app;
    }

    boolean updateItem(AppDescriptor.Mutable app, LauncherActivityInfo info) {
        boolean updated = false;
        long versionCode = getVersionCode(packageManager, app.getPackageName());
        if (app.getVersionCode() != versionCode) {
            app.setVersionCode(versionCode);
            app.setColor(getDominantIconColor(info, isDarkTheme()));
            updated = true;
        }
        if (app.getComponentName() != null) {
            String newComponentName = getComponentName(info);
            if (!newComponentName.equals(app.getComponentName())) {
                app.setComponentName(newComponentName);
                updated = true;
            }
        }
        String newLabel = info.getLabel().toString();
        if (!newLabel.equals(app.getOriginalLabel())) {
            app.setOriginalLabel(newLabel);
            ;
            updated = true;
        }
        app.setLabel(nameNormalizer.normalize(info.getLabel()));
        return updated;
    }

    private boolean isDarkTheme() {
        Preferences.ColorTheme colorTheme = preferences.get(Preferences.COLOR_THEME);
        return colorTheme != Preferences.ColorTheme.LIGHT;
    }
}
