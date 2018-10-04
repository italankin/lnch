package com.italankin.lnch.util.widget.colorpicker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

class ARGB extends RGB {
    private Row alpha;

    @Override
    public void init(ViewGroup root, LayoutInflater inflater) {
        alpha = addRow(root, inflater, "A", Color.WHITE, 255);
        super.init(root, inflater);
    }

    @Override
    public void setColor(int color) {
        alpha.setValue((color >> 24) & 0xff);
        super.setColor(color);
    }

    @Override
    public int getColor() {
        return ((alpha.getValue() << 24) | 0xffffff) & super.getColor();
    }
}
