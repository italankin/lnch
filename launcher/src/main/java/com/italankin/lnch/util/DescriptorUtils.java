package com.italankin.lnch.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;

public final class DescriptorUtils {

    public static String getPackageName(Descriptor descriptor) {
        if (descriptor instanceof PackageDescriptor) {
            return ((PackageDescriptor) descriptor).getPackageName();
        }
        return null;
    }

    public static Intent getLaunchIntent(PackageManager packageManager, AppDescriptor descriptor) {
        Intent intent = packageManager.getLaunchIntentForPackage(descriptor.packageName);
        if (intent != null) {
            if (descriptor.componentName != null) {
                intent.setComponent(ComponentName.unflattenFromString(descriptor.componentName));
            }
            return intent;
        }
        return null;
    }

    private DescriptorUtils() {
        // no instance
    }
}
