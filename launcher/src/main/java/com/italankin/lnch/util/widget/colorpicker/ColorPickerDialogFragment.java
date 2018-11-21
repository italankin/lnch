package com.italankin.lnch.util.widget.colorpicker;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.italankin.lnch.R;

import java.io.Serializable;

public class ColorPickerDialogFragment extends DialogFragment {
    private static final String ARG_SELECTED_COLOR = "selected_color";
    private static final String ARG_HEX_VISIBLE = "hex_visible";
    private static final String ARG_PREVIEW_VISIBLE = "preview_visible";
    private static final String ARG_COLOR_MODEL = "color_model";
    private static final String ARG_SHOW_RESET = "show_reset";
    private static final String ARG_PROVIDER = "provider";

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

    private Listener getListener() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        ListenerProvider provider = (ListenerProvider) arguments.getSerializable(ARG_PROVIDER);
        if (provider == null) {
            return null;
        }
        return provider.get(getParentFragment());
    }

    public static class Builder {
        private final Bundle arguments = new Bundle(6);

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

        public Builder setListenerProvider(ListenerProvider provider) {
            arguments.putSerializable(ARG_PROVIDER, provider);
            return this;
        }

        public ColorPickerDialogFragment build() {
            if (!arguments.containsKey(ARG_PROVIDER)) {
                throw new IllegalArgumentException(ARG_PROVIDER + " is required");
            }
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

    public interface ListenerProvider extends Serializable {
        Listener get(Fragment parentFragment);
    }
}
