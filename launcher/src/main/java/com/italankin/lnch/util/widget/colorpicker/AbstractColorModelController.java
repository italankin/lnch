package com.italankin.lnch.util.widget.colorpicker;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.adapter.SeekBarChangeListener;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

abstract class AbstractColorModelController implements ColorModelController {
    @Nullable
    protected ColorPickerView.OnColorChangedListener listener;

    @CallSuper
    @Override
    public void destroy() {
        this.listener = null;
    }

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
        TextView valueView = rowView.findViewById(R.id.value);
        valueView.setOnClickListener(v -> {
            EditTextAlertDialog.builder(root.getContext())
                    .setTitle(R.string.color_picker_edit_value_title)
                    .customizeEditText(editText -> {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                        String text = String.valueOf(seekBarView.getProgress());
                        editText.setText(text);
                        editText.setSelection(text.length());
                        editText.setHint(editText.getContext()
                                .getString(R.string.color_picker_edit_value_hint, 0, max));
                    })
                    .setPositiveButton(R.string.ok, (dialog, editText) -> {
                        try {
                            String s = editText.getText().toString().trim();
                            int value = Integer.parseInt(s);
                            seekBarView.setProgress(Math.min(max, Math.max(0, value)));
                        } catch (NumberFormatException ignored) {
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        root.addView(rowView);
        Row row = new Row(rowView, labelView, seekBarView, valueView);
        seekBarView.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            row.update();
            if (listener != null) {
                listener.onColorChanged(getColor());
            }
        }));
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
