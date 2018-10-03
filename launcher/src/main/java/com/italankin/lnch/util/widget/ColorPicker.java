package com.italankin.lnch.util.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.italankin.lnch.R;

public class ColorPicker extends LinearLayout {

    public static final ColorModel RGB = new RGB();
    public static final ColorModel ARGB = new ARGB();

    private final View preview;
    private ColorModel colorModel;
    private ColorModel.ColorChangedListener listener;

    public ColorPicker(Context context) {
        this(context, null);
    }

    public ColorPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        preview = new View(getContext());
        preview.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                context.getResources().getDimensionPixelSize(R.dimen.color_picker_preview_size)));
        addView(preview);
        setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
        if (isInEditMode()) {
            preview.setBackgroundColor(Color.RED);
        }
    }

    public void setColorModel(ColorModel model) {
        if (colorModel != null) {
            colorModel.setListener(null);
        }
        for (int i = 1, count = getChildCount(); i < count; i++) {
            removeViewAt(i);
        }
        colorModel = model;
        colorModel.setListener(color -> {
            preview.setBackgroundColor(color);
            if (listener != null) {
                listener.onChanged(color);
            }
        });
        colorModel.init(this, LayoutInflater.from(getContext()));
    }

    public void setSelectedColor(@ColorInt int color) {
        colorModel.setColor(color);
    }

    @ColorInt
    public int getSelectedColor() {
        return colorModel.getColor();
    }

    public void setPreviewVisible(boolean visible) {
        preview.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setColorChangedListener(ColorModel.ColorChangedListener listener) {
        this.listener = listener;
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

        public void setValue(int value) {
            seekbar.setProgress(value);
        }

        public int getValue() {
            return seekbar.getProgress();
        }
    }

    public interface ColorModel {
        void init(ViewGroup root, LayoutInflater inflater);

        void setColor(@ColorInt int color);

        @ColorInt
        int getColor();

        void setListener(ColorChangedListener listener);

        interface ColorChangedListener {
            void onChanged(@ColorInt int color);
        }
    }

    public static abstract class AbstractColorModel implements ColorModel,
            SeekBar.OnSeekBarChangeListener {
        protected ColorChangedListener listener;

        @Override
        public void setListener(ColorChangedListener listener) {
            this.listener = listener;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (listener != null) {
                listener.onChanged(getColor());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // empty
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // empty
        }

        protected Row addRow(ViewGroup root, LayoutInflater inflater) {
            View row = inflater.inflate(R.layout.partial_color_picker_row, root, false);
            TextView labelView = row.findViewById(R.id.label);
            SeekBar seekBarView = row.findViewById(R.id.seekbar);
            seekBarView.setOnSeekBarChangeListener(this);
            TextView valueView = row.findViewById(R.id.value);
            root.addView(row);
            return new Row(row, labelView, seekBarView, valueView);
        }
    }

    private static class RGB extends AbstractColorModel {
        private Row red;
        private Row green;
        private Row blue;

        @Override
        public void init(ViewGroup root, LayoutInflater inflater) {
            red = addRow(root, inflater);
            red.label.setText("R");
            red.label.setTextColor(Color.RED);
            green = addRow(root, inflater);
            green.label.setText("G");
            green.label.setTextColor(Color.GREEN);
            blue = addRow(root, inflater);
            blue.label.setText("B");
            blue.label.setTextColor(Color.BLUE);
        }

        @Override
        public void setColor(int color) {
            red.setValue((color >> 16) & 0xff);
            green.setValue((color >> 8) & 0xff);
            blue.setValue(color & 0xff);
            updateValues();
            if (listener != null) {
                listener.onChanged(getColor());
            }
        }

        protected void updateValues() {
            red.value.setText(String.valueOf(red.getValue()));
            green.value.setText(String.valueOf(green.getValue()));
            blue.value.setText(String.valueOf(blue.getValue()));
        }

        @Override
        public int getColor() {
            return Color.rgb(red.getValue(), green.getValue(), blue.getValue());
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateValues();
            super.onProgressChanged(seekBar, progress, fromUser);
        }
    }

    private static class ARGB extends RGB {
        private Row alpha;

        @Override
        public void init(ViewGroup root, LayoutInflater inflater) {
            alpha = addRow(root, inflater);
            alpha.label.setText("A");
            alpha.label.setTextColor(Color.WHITE);
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

        @Override
        protected void updateValues() {
            alpha.value.setText(String.valueOf(alpha.getValue()));
            super.updateValues();
        }
    }
}

