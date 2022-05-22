package com.italankin.lnch.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.descriptor.NameNormalizer;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

public final class DescriptorUtils {

    @Nullable
    public static String getPackageName(Descriptor descriptor) {
        if (descriptor instanceof PackageDescriptor) {
            return ((PackageDescriptor) descriptor).getPackageName();
        }
        return null;
    }

    @Nullable
    public static ComponentName getComponentName(Context context, AppDescriptor descriptor) {
        if (descriptor.componentName != null) {
            ComponentName componentName = descriptor.getComponentName();
            if (componentName != null) {
                return componentName;
            }
        }
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(descriptor.packageName);
        return intent != null ? intent.getComponent() : null;
    }

    public static DeepShortcutDescriptor makeDeepShortcut(Shortcut shortcut, AppDescriptor app,
            NameNormalizer nameNormalizer) {
        DeepShortcutDescriptor descriptor = new DeepShortcutDescriptor(
                shortcut.getPackageName(), shortcut.getId());
        descriptor.color = app.color;
        CharSequence label = shortcut.getShortLabel();
        if (TextUtils.isEmpty(label)) {
            descriptor.originalLabel = descriptor.label = app.getVisibleLabel();
        } else {
            descriptor.originalLabel = label.toString();
            descriptor.label = nameNormalizer.normalize(label);
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

    private DescriptorUtils() {
        // no instance
    }
}
