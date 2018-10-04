package com.italankin.lnch.util.widget.colorpicker;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.italankin.lnch.R;

abstract class AbstractColorModelController implements ColorModelController {
    @Nullable
    protected ColorPickerView.OnColorChangedListener listener;

    @Override
    public void setListener(@Nullable ColorPickerView.OnColorChangedListener listener) {
        this.listener = listener;
    }

    protected Row addRow(ViewGroup root, LayoutInflater inflater, CharSequence label,
            @ColorInt int textColor, int max) {
        View rowView = inflater.inflate(R.layout.partial_color_picker_row, root, false);
        TextView labelView = rowView.findViewById(R.id.label);
        labelView.setTextColor(textColor);
        labelView.setText(label);
        SeekBar seekBarView = rowView.findViewById(R.id.seekbar);
        seekBarView.setMax(max);
        TextView valueView = rowView.findViewById(R.id.hex_value);
        root.addView(rowView);
        Row row = new Row(rowView, labelView, seekBarView, valueView);
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                row.update();
                if (listener != null) {
                    listener.onColorChanged(getColor());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return row;
    }

    public static class Row {
        public final View root;
        public final TextView label;
        public final SeekBar seekbar;
        public final TextView value;

        public Row(View root, TextView label, SeekBar seekbar, TextView value) {
            this.root = root;
            this.label = label;
            this.seekbar = seekbar;
            this.value = value;
        }

        public void setValue(int newValue) {
            seekbar.setProgress(newValue);
            update();
        }

        public int getValue() {
            return seekbar.getProgress();
        }

        public void update() {
            value.setText(String.valueOf(getValue()));
        }
    }
}
