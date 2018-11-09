package com.italankin.lnch.util.widget.colorpicker;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;

import com.italankin.lnch.R;

public final class ColorPickerDialog {

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        final ColorPickerView colorPicker;
        final AlertDialog.Builder alertDialogBuilder;
        OnColorPickedListener onColorPickedListener;

        private Builder(Context context) {
            colorPicker = new ColorPickerView(context);
            alertDialogBuilder = new AlertDialog.Builder(context)
                    .setView(colorPicker)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        if (onColorPickedListener != null) {
                            onColorPickedListener.onColorPicked(colorPicker.getSelectedColor());
                        }
                    });
        }

        public Builder setSelectedColor(@ColorInt int color) {
            colorPicker.setSelectedColor(color);
            return this;
        }

        public Builder setHexVisible(boolean visible) {
            colorPicker.setHexVisible(visible);
            return this;
        }

        public Builder setPreviewVisible(boolean visible) {
            colorPicker.setPreviewVisible(visible);
            return this;
        }

        public Builder setColorModel(ColorPickerView.ColorModel model) {
            colorPicker.setColorModel(model);
            return this;
        }

        public Builder setOnColorPickedListener(OnColorPickedListener onColorPickedListener) {
            this.onColorPickedListener = onColorPickedListener;
            return this;
        }

        public Builder setResetButton(CharSequence text, DialogInterface.OnClickListener listener) {
            alertDialogBuilder.setNeutralButton(text, listener);
            return this;
        }

        public AlertDialog build() {
            return alertDialogBuilder.create();
        }

        public AlertDialog show() {
            AlertDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }

    public interface OnColorPickedListener {
        void onColorPicked(@ColorInt int color);
    }
}
