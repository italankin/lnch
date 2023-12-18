package com.italankin.lnch.util;

import android.app.Dialog;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

public final class DialogUtils {

    public static void dismissOnDestroy(LifecycleOwner lifecycleOwner, Dialog dialog) {
        WeakReference<Dialog> dialogRef = new WeakReference<>(dialog);
        LifecycleUtils.doOnDestroyOnce(lifecycleOwner, () -> {
            Dialog d = dialogRef.get();
            dialogRef.clear();
            if (d != null && d.isShowing()) {
                d.dismiss();
            }
        });
    }

    private DialogUtils() {
    }
}
