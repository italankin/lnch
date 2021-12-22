package com.italankin.lnch.feature.common.dialog;

import android.content.Context;

import com.italankin.lnch.R;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;

import androidx.annotation.Nullable;

public class SetColorDescriptorDialog {

    private final Context context;
    private final int visibleColor;
    private final OnSetColor onSetColor;

    public SetColorDescriptorDialog(Context context, int visibleColor, OnSetColor onSetColor) {
        this.context = context;
        this.visibleColor = visibleColor;
        this.onSetColor = onSetColor;
    }

    public void show() {
        ColorPickerDialog.builder(context)
                .setHexVisible(false)
                .setSelectedColor(visibleColor)
                .setOnColorPickedListener(color -> {
                    if (color != visibleColor) {
                        onSetColor.onSetColor(color);
                    }
                })
                .setResetButton(context.getString(R.string.customize_action_reset), (dialog, which) -> {
                    onSetColor.onSetColor(null);
                })
                .setCancellable(false)
                .show();
    }

    public interface OnSetColor {
        void onSetColor(@Nullable Integer newColor);
    }
}
