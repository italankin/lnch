package com.italankin.lnch.util.widget.colorpicker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.italankin.lnch.R;

public class ColorPickerDialogFragment extends DialogFragment {
    private static final String ARG_SELECTED_COLOR = "selected_color";
    private static final String ARG_HEX_VISIBLE = "hex_visible";
    private static final String ARG_PREVIEW_VISIBLE = "preview_visible";
    private static final String ARG_COLOR_MODEL = "color_model";
    private static final String ARG_SHOW_RESET = "show_reset";

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new NullPointerException();
        }
        ColorPickerDialog.Builder builder = ColorPickerDialog.builder(requireContext());
        builder.setColorModel((ColorPickerView.ColorModel) arguments.getSerializable(ARG_COLOR_MODEL))
                .setHexVisible(arguments.getBoolean(ARG_HEX_VISIBLE, false))
                .setPreviewVisible(arguments.getBoolean(ARG_PREVIEW_VISIBLE, true))
                .setSelectedColor(arguments.getInt(ARG_SELECTED_COLOR))
                .setOnColorPickedListener(color -> {
                    if (listener != null) {
                        listener.onColorPicked(color);
                    }
                });
        if (arguments.getBoolean(ARG_SHOW_RESET, false)) {
            builder.setResetButton(getString(R.string.customize_action_reset), (dialog, which) -> {
                if (listener != null) {
                    listener.onColorReset();
                }
            });
        }
        builder.colorPicker.setColorChangedListener(newColor -> {
            getArguments().putInt(ARG_SELECTED_COLOR, newColor);
        });
        return builder.build();
    }

    public static class Builder {
        private final Bundle arguments = new Bundle();

        public Builder setSelectedColor(@ColorInt int color) {
            arguments.putInt(ARG_SELECTED_COLOR, color);
            return this;
        }

        public Builder setHexVisible(boolean visible) {
            arguments.putBoolean(ARG_HEX_VISIBLE, visible);
            return this;
        }

        public Builder setPreviewVisible(boolean visible) {
            arguments.putBoolean(ARG_PREVIEW_VISIBLE, visible);
            return this;
        }

        public Builder setColorModel(ColorPickerView.ColorModel model) {
            arguments.putSerializable(ARG_COLOR_MODEL, model);
            return this;
        }

        public Builder showResetButton(boolean show) {
            arguments.putBoolean(ARG_SHOW_RESET, show);
            return this;
        }

        public ColorPickerDialogFragment build() {
            ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
            fragment.setArguments(arguments);
            return fragment;
        }
    }

    public interface Listener {
        void onColorPicked(@ColorInt int newColor);

        default void onColorReset() {
        }
    }
}
