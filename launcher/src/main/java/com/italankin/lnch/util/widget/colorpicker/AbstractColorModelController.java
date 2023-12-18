package com.italankin.lnch.util.widget.colorpicker;

import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import com.google.android.material.slider.Slider;
import com.italankin.lnch.R;
import com.italankin.lnch.util.NumberUtils;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

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
            @ColorInt Integer textColor, int max) {
        View rowView = inflater.inflate(R.layout.partial_color_picker_row, root, false);
        TextView labelView = rowView.findViewById(R.id.label);
        if (textColor != null) {
            labelView.setTextColor(textColor);
        }
        labelView.setText(label);
        Slider slider = rowView.findViewById(R.id.slider);
        slider.setValueTo(max);
        TextView valueView = rowView.findViewById(R.id.value);
        valueView.setOnClickListener(v -> {
            EditTextAlertDialog.builder(root.getContext())
                    .setTitle(R.string.color_picker_edit_value_title)
                    .customizeEditText(editText -> {
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                        String text = String.valueOf((int) slider.getValue());
                        editText.setText(text);
                        editText.setSelection(text.length());
                        editText.setHint(editText.getContext()
                                .getString(R.string.color_picker_edit_value_hint, 0, max));
                    })
                    .setPositiveButton(R.string.ok, (dialog, editText) -> {
                        String s = editText.getText().toString().trim();
                        Integer value = NumberUtils.parseInt(s);
                        if (value != null) {
                            slider.setValue(Math.min(max, Math.max(0, value)));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        root.addView(rowView);
        Row row = new Row(rowView, labelView, slider, valueView);
        slider.addOnChangeListener((aSlider, value, fromUser) -> {
            row.update();
            if (listener != null) {
                listener.onColorChanged(getColor());
            }
        });
        return row;
    }

    public static class Row {
        public final View root;
        public final TextView label;
        public final Slider slider;
        public final TextView value;

        public Row(View root, TextView label, Slider slider, TextView value) {
            this.root = root;
            this.label = label;
            this.slider = slider;
            this.value = value;
        }

        public void setValue(int newValue) {
            slider.setValue(newValue);
            update();
        }

        public int getValue() {
            return (int) slider.getValue();
        }

        public void update() {
            value.setText(String.valueOf(getValue()));
        }
    }
}
