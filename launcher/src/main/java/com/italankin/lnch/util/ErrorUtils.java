package com.italankin.lnch.util;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;

public final class ErrorUtils {

    public static void showErrorDialogOrToast(Context context,
            Throwable error,
            @StringRes int message,
            @Nullable LifecycleOwner lifecycleOwner) {
        if (LauncherApp.daggerService.main().preferences().get(Preferences.VERBOSE_ERRORS)) {
            AlertDialog alertDialog = DialogUtils.stacktraceDialog(context, error)
                    .show();
            if (lifecycleOwner != null) {
                DialogUtils.dismissOnDestroy(lifecycleOwner, alertDialog);
            }
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showErrorDialogOrToast(Context context, Throwable error, @Nullable LifecycleOwner lifecycleOwner) {
        showErrorDialogOrToast(context, error, R.string.error, lifecycleOwner);
    }

    public static void showErrorDialogOrToast(Context context, Throwable error) {
        showErrorDialogOrToast(context, error, null);
    }

    public static void showErrorDialogOrToast(Context context, Throwable error, @StringRes int message) {
        showErrorDialogOrToast(context, error, message, null);
    }

    private ErrorUtils() {
    }
}
