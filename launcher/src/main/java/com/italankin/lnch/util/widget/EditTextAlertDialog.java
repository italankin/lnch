package com.italankin.lnch.util.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.util.ViewUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class EditTextAlertDialog {

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private final MaterialAlertDialogBuilder builder;
        private final EditText editText;
        private List<Action<LinearLayout>> customizeRootActions;

        private Builder(Context context) {
            builder = new MaterialAlertDialogBuilder(context);
            editText = new AppCompatEditText(context);
        }

        public Builder setTitle(CharSequence title) {
            builder.setTitle(title);
            return this;
        }

        public Builder setTitle(@StringRes int title) {
            return setTitle(builder.getContext().getText(title));
        }

        public Builder setNegativeButton(CharSequence title, DialogInterface.OnClickListener listener) {
            builder.setNegativeButton(title, listener);
            return this;
        }

        public Builder setNegativeButton(@StringRes int title, DialogInterface.OnClickListener listener) {
            return setNegativeButton(builder.getContext().getText(title), listener);
        }

        public Builder setPositiveButton(CharSequence title, OnClickListener listener) {
            builder.setPositiveButton(title, ((dialog, which) -> listener.onClick(dialog, editText)));
            return this;
        }

        public Builder setPositiveButton(@StringRes int title, OnClickListener listener) {
            return setPositiveButton(builder.getContext().getText(title), listener);
        }

        public Builder setNeutralButton(@StringRes int title, DialogInterface.OnClickListener listener) {
            builder.setNeutralButton(title, listener);
            return this;
        }

        public Builder setNeutralButton(CharSequence title, DialogInterface.OnClickListener listener) {
            builder.setNeutralButton(title, listener);
            return this;
        }

        public Builder setCancellable(boolean cancellable) {
            builder.setCancelable(cancellable);
            return this;
        }

        public Builder customizeEditText(Action<EditText> action) {
            action.customize(editText);
            return this;
        }

        public Builder customizeRoot(Action<LinearLayout> action) {
            if (customizeRootActions == null) {
                customizeRootActions = new ArrayList<>(1);
            }
            customizeRootActions.add(action);
            return this;
        }

        public AlertDialog build() {
            LinearLayout layout = new LinearLayout(builder.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            ViewUtils.setPaddingDp(layout, 16);
            layout.addView(editText, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (customizeRootActions != null) {
                for (Action<LinearLayout> action : customizeRootActions) {
                    action.customize(layout);
                }
            }
            builder.setView(layout);
            return builder.create();
        }

        public AlertDialog show() {
            AlertDialog dialog = build();
            try {
                return dialog;
            } finally {
                dialog.show();
            }
        }

        public AlertDialog show(LifecycleOwner lifecycleOwner) {
            AlertDialog alertDialog = show();
            WeakReference<AlertDialog> ref = new WeakReference<>(alertDialog);
            lifecycleOwner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onDestroy(@NonNull LifecycleOwner owner) {
                    owner.getLifecycle().removeObserver(this);
                    AlertDialog dialog = ref.get();
                    if (dialog != null) {
                        dialog.dismiss();
                        ref.clear();
                    }
                }
            });
            return alertDialog;
        }
    }

    public interface Action<T extends View> {
        void customize(T t);
    }

    public interface OnClickListener {
        void onClick(DialogInterface dialog, EditText editText);
    }

    private EditTextAlertDialog() {
        // no instance
    }
}
