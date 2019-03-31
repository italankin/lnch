package com.italankin.lnch.util;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;

public final class DescriptorUtils {

    public static String getPackageName(Descriptor descriptor) {
        if (descriptor instanceof PackageDescriptor) {
            return ((PackageDescriptor) descriptor).getPackageName();
        }
        return null;
    }

    private DescriptorUtils() {
        // no instance
    }
}
