package com.italankin.lnch.util.widget.colorpicker;

import android.app.Dialog;
import android.os.Bundle;

import com.italankin.lnch.R;
import com.italankin.lnch.util.dialogfragment.BaseDialogFragment;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ColorPickerDialogFragment extends BaseDialogFragment<ColorPickerDialogFragment.Listener> {
    private static final String ARG_SELECTED_COLOR = "selected_color";
    private static final String ARG_HEX_VISIBLE = "hex_visible";
    private static final String ARG_PREVIEW_VISIBLE = "preview_visible";
    private static final String ARG_COLOR_MODEL = "color_model";
    private static final String ARG_SHOW_RESET = "show_reset";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            throw new NullPointerException();
        }
        ColorPickerDialog.Builder builder = ColorPickerDialog.builder(requireContext());
        if (arguments.containsKey(ARG_COLOR_MODEL)) {
            builder.setColorModel((ColorPickerView.ColorModel) arguments.getSerializable(ARG_COLOR_MODEL));
        }
        builder.setHexVisible(arguments.getBoolean(ARG_HEX_VISIBLE, false))
                .setPreviewVisible(arguments.getBoolean(ARG_PREVIEW_VISIBLE, true))
                .setSelectedColor(arguments.getInt(ARG_SELECTED_COLOR))
                .setOnColorPickedListener(color -> {
                    Listener listener = getListener();
                    if (listener != null) {
                        listener.onColorPicked(color);
                    }
                });
        if (arguments.getBoolean(ARG_SHOW_RESET, false)) {
            builder.setResetButton(getString(R.string.customize_action_reset), (dialog, which) -> {
                Listener listener = getListener();
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

    public static class Builder extends BaseBuilder<ColorPickerDialogFragment, Listener, Builder> {

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

        @Override
        protected ColorPickerDialogFragment createInstance() {
            return new ColorPickerDialogFragment();
        }
    }

    public interface Listener {
        void onColorPicked(@ColorInt int newColor);

        default void onColorReset() {
        }
    }
}
