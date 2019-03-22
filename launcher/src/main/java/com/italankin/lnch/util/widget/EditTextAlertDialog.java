package com.italankin.lnch.util.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

public final class EditTextAlertDialog {

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private final AlertDialog.Builder builder;
        private final EditText editText;
        private List<Action<LinearLayout>> customizeRootActions;

        private Builder(Context context) {
            builder = new AlertDialog.Builder(context);
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
            int p = ResUtils.px2dp(builder.getContext(), 16);
            ViewUtils.setPadding(layout, p);
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
