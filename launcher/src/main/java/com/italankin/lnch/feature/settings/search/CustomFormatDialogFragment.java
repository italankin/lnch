package com.italankin.lnch.feature.settings.search;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.TextWatcherAdapter;

import java.util.regex.Matcher;

public class CustomFormatDialogFragment extends DialogFragment {
    private static final String KEY_STATE_CUSTOM_FORMAT = "custom_format";

    private Preferences preferences;

    private Button positiveButton;
    private String customFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        if (savedInstanceState != null) {
            customFormat = savedInstanceState.getString(KEY_STATE_CUSTOM_FORMAT);
        } else {
            customFormat = preferences.get(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return EditTextAlertDialog.builder(requireContext())
                .setTitle(R.string.settings_search_engine_custom_format_title)
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String url = editText.getText().toString();
                    preferences.set(Preferences.CUSTOM_SEARCH_ENGINE_FORMAT, url);
                })
                .setNegativeButton(R.string.cancel, null)
                .customizeEditText(editText -> {
                    editText.setText(customFormat);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
                    editText.setHint(R.string.settings_search_engine_custom_format_edit_hint);
                    editText.addTextChangedListener(new TextWatcherAdapter() {
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            customFormat = s.toString();
                            updateButtonState();
                        }
                    });
                })
                .customizeRoot(view -> {
                    TextView textView = new TextView(view.getContext());
                    textView.setTextColor(ResUtils.resolveColor(view.getContext(), android.R.attr.textColorSecondary));
                    textView.setText(R.string.settings_search_engine_custom_format_hint);
                    view.addView(textView);
                })
                .build();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_STATE_CUSTOM_FORMAT, customFormat);
    }

    @Override
    public void onResume() {
        super.onResume();
        positiveButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        updateButtonState();
    }

    private void updateButtonState() {
        positiveButton.setEnabled(validate(customFormat));
    }

    private static boolean validate(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        if (!input.contains("%s") && !input.contains("%S")) {
            return false;
        }
        String sanitized = input.replaceAll("%([sS])", "0");
        Matcher matcher = Patterns.WEB_URL.matcher(sanitized);
        return matcher.matches();
    }
}
