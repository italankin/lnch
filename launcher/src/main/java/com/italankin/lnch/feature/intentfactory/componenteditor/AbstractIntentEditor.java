package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.Intent;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

abstract class AbstractIntentEditor implements IntentEditor {

    protected final AppCompatActivity activity;
    protected final Host host;
    protected Intent result;

    public AbstractIntentEditor(AppCompatActivity activity) {
        this.activity = activity;
        this.host = (Host) activity;
    }

    @Override
    public final void bind(Intent result) {
        this.result = result;
        bind();
        update();
    }

    protected abstract void bind();

    protected static void showEdit(TextView textView, @StringRes int title, OnSet callback) {
        String text = getTrimmed(textView);
        EditTextAlertDialog.builder(textView.getContext())
                .setTitle(title)
                .customizeEditText(editText -> {
                    editText.setText(text);
                    editText.setHint(title);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String label = getTrimmed(editText);
                    if (label.isEmpty()) {
                        textView.setText(null);
                        callback.onSet(null);
                    } else if (!label.equals(text)) {
                        textView.setText(label);
                        callback.onSet(label);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.intent_factory_clear, (dialog, which) -> {
                    textView.setText(null);
                    callback.onSet(null);
                })
                .show();
    }

    protected static String getTrimmed(TextView textView) {
        return textView.getText().toString().trim();
    }

    interface OnSet {
        void onSet(@Nullable String newValue);
    }
}
