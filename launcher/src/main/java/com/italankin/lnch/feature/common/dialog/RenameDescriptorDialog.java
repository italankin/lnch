package com.italankin.lnch.feature.common.dialog;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import com.italankin.lnch.R;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

import androidx.annotation.Nullable;

public class RenameDescriptorDialog {

    private final Context context;
    private final String visibleLabel;
    private final OnRename onRename;

    public RenameDescriptorDialog(Context context, String visibleLabel, OnRename onRename) {
        this.context = context;
        this.visibleLabel = visibleLabel;
        this.onRename = onRename;
    }

    public void show() {
        EditTextAlertDialog.builder(context)
                .setTitle(visibleLabel)
                .customizeEditText(editText -> {
                    editText.setText(visibleLabel);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    if (visibleLabel != null) {
                        editText.setSelection(visibleLabel.length());
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String newLabel = editText.getText().toString().trim();
                    if (!newLabel.equals(visibleLabel)) {
                        onRename.onRename(newLabel);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.customize_action_reset, (dialog, which) -> {
                    onRename.onRename(null);
                })
                .setCancellable(false)
                .show();
    }

    public interface OnRename {
        void onRename(@Nullable String newLabel);
    }
}
