package com.italankin.lnch.util;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

public final class IntentUtils {

    public static Intent getPackageSystemSettings(String packageName) {
        Uri uri = Uri.fromParts("package", packageName, null);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private IntentUtils() {
        // no instance
    }
}
