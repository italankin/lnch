package com.italankin.lnch.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.R;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    public static MaterialAlertDialogBuilder stacktraceDialog(Context context, Throwable e) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
        }
        TextView tv = new TextView(context);
        ViewUtils.setPaddingDp(tv, 8);
        tv.setTextIsSelectable(true);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.setTextSize(11);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setText(sw.toString());
        return new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.error)
                .setView(tv);
    }

    private DialogUtils() {
    }
}
