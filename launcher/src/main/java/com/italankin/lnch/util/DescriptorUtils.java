package com.italankin.lnch.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

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

    private DescriptorUtils() {
        // no instance
    }
}
