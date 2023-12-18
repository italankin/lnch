package com.italankin.lnch.util.widget.colorpicker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import com.italankin.lnch.R;


class RGB extends AbstractColorModelController {
    private Row red;
    private Row green;
    private Row blue;

    @Override
    public void init(ViewGroup root, LayoutInflater inflater) {
        red = addRow(root, inflater, "R", ContextCompat.getColor(root.getContext(), R.color.color_picker_text_red), 255);
        green = addRow(root, inflater, "G", ContextCompat.getColor(root.getContext(), R.color.color_picker_text_green), 255);
        blue = addRow(root, inflater, "B", ContextCompat.getColor(root.getContext(), R.color.color_picker_text_blue), 255);
    }

    @Override
    public void setColor(int color) {
        red.setValue((color >> 16) & 0xff);
        green.setValue((color >> 8) & 0xff);
        blue.setValue(color & 0xff);
        if (listener != null) {
            listener.onColorChanged(getColor());
        }
    }

    @Override
    public int getColor() {
        return Color.rgb(red.getValue(), green.getValue(), blue.getValue());
    }
}
