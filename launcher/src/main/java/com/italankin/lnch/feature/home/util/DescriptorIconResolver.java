package com.italankin.lnch.feature.home.util;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.DrawableRes;
import com.italankin.lnch.R;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.imageloader.resourceloader.ActivityIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.PackageIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.ShortcutIconLoader;

public class DescriptorIconResolver {

    public static void resolve(DescriptorUi descriptorUi, ResolveResult resolveResult) {
        resolve(descriptorUi.getDescriptor(), resolveResult);
    }

    public static void resolve(Descriptor descriptor, ResolveResult resolveResult) {
        if (descriptor instanceof AppDescriptor) {
            AppDescriptor d = (AppDescriptor) descriptor;
            ComponentName componentName = d.getComponentName();
            if (componentName != null) {
                resolveResult.uriIcon(ActivityIconLoader.uriFrom(componentName));
            } else {
                resolveResult.uriIcon(PackageIconLoader.uriFrom(d.packageName));
            }
        } else if (descriptor instanceof DeepShortcutDescriptor) {
            DeepShortcutDescriptor d = (DeepShortcutDescriptor) descriptor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                resolveResult.uriIcon(ShortcutIconLoader.uriFrom(d.packageName, d.shortcutId, true));
            } else {
                resolveResult.resourceIcon(0);
            }
        } else if (descriptor instanceof FolderDescriptor) {
            resolveResult.resourceIcon(R.drawable.ic_folder);
        } else {
            resolveResult.resourceIcon(R.drawable.ic_shortcut);
        }
    }

    public interface ResolveResult {

        void uriIcon(Uri icon);

        void resourceIcon(@DrawableRes int drawableId);
    }
}
