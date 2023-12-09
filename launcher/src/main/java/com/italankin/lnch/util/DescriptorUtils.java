package com.italankin.lnch.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.os.Process;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.*;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;

import java.util.*;

public final class DescriptorUtils {

    @Nullable
    public static String getPackageName(Descriptor descriptor) {
        if (descriptor instanceof PackageDescriptor) {
            return ((PackageDescriptor) descriptor).getPackageName();
        }
        return null;
    }

    @Nullable
    public static ComponentName getLauncherComponentName(Context context, AppDescriptor descriptor) {
        if (descriptor.componentName != null) {
            ComponentName componentName = descriptor.getComponentName();
            if (componentName != null) {
                return componentName;
            }
        }
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        List<LauncherActivityInfo> activityList = launcherApps.getActivityList(descriptor.packageName, Process.myUserHandle());
        if (activityList.isEmpty()) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(descriptor.packageName);
            return intent != null ? intent.getComponent() : null;
        }
        LauncherActivityInfo info = activityList.get(0);
        return info != null ? info.getComponentName() : null;
    }

    public static DeepShortcutDescriptor.Mutable makeDeepShortcut(Shortcut shortcut, AppDescriptor app,
            NameNormalizer nameNormalizer) {
        DeepShortcutDescriptor.Mutable descriptor = new DeepShortcutDescriptor.Mutable(
                shortcut.getPackageName(), shortcut.getId());
        descriptor.setColor(app.color);
        CharSequence label = shortcut.getShortLabel();
        if (TextUtils.isEmpty(label)) {
            descriptor.setOriginalLabel(app.getVisibleLabel());
            descriptor.setLabel(app.getVisibleLabel());
        } else {
            descriptor.setOriginalLabel(label.toString());
            descriptor.setLabel(nameNormalizer.normalize(label));
        }
        return descriptor;
    }

    public static String getVisibleLabel(Descriptor descriptor) {
        if (descriptor instanceof CustomLabelDescriptor) {
            String visibleLabel = ((CustomLabelDescriptor) descriptor).getVisibleLabel();
            return visibleLabel != null ? visibleLabel : descriptor.getOriginalLabel();
        }
        if (descriptor instanceof LabelDescriptor) {
            String label = ((LabelDescriptor) descriptor).getLabel();
            return label != null ? label : descriptor.getOriginalLabel();
        }
        return descriptor.getOriginalLabel();
    }

    @Nullable
    public static AppDescriptor findAppByPackageName(List<? extends Descriptor> descriptors, String packageName) {
        for (Descriptor item : descriptors) {
            if (item instanceof AppDescriptor && packageName.equals(((AppDescriptor) item).packageName)) {
                return (AppDescriptor) item;
            }
        }
        return null;
    }

    public static Map<String, Descriptor> associateById(List<? extends Descriptor> descriptors) {
        Map<String, Descriptor> map = new HashMap<>(descriptors.size());
        for (Descriptor descriptor : descriptors) {
            map.put(descriptor.getId(), descriptor);
        }
        return map;
    }

    public static Set<String> createSearchTokens(Descriptor descriptor) {
        Set<String> result = new HashSet<>(4);
        result.add(descriptor.getOriginalLabel());
        if (descriptor instanceof CustomLabelDescriptor) {
            String customLabel = ((CustomLabelDescriptor) descriptor).getCustomLabel();
            if (customLabel != null) {
                result.add(customLabel);
            }
        }
        if (descriptor instanceof LabelDescriptor) {
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (label != null) {
                result.add(label);
            }
        }
        if (descriptor instanceof AliasDescriptor) {
            result.addAll(((AliasDescriptor) descriptor).getAliases());
        }
        return result;
    }

    private DescriptorUtils() {
        // no instance
    }
}
