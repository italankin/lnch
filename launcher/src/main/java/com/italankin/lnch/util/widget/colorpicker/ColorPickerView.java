package com.italankin.lnch.util.widget.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.italankin.lnch.R;

import timber.log.Timber;

public class ColorPickerView extends LinearLayout {

    private static final String KEY_BASE_STATE = "BASE_STATE";
    private static final String KEY_PREVIEW_VISIBLE = "PREVIEW_VISIBLE";
    private static final String KEY_SELECTED_COLOR = "SELECTED_COLOR";
    private static final String KEY_MODEL = "MODEL";

    private final View preview;
    private final TextView hex;
    private final ViewGroup container;
    private ColorModelController colorModelController;
    @Nullable
    private OnColorChangedListener colorChangedListener;
    @Nullable
    private OnHexValueClickListener onHexValueClickListener;

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.widget_color_picker, this);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);

        preview = findViewById(R.id.preview);
        int gridSize = context.getResources().getDimensionPixelSize(R.dimen.backdrop_grid_size);
        preview.setBackground(new BackdropDrawable(gridSize));
        hex = findViewById(R.id.hex_value);
        hex.setOnClickListener(v -> {
            if (onHexValueClickListener != null) {
                onHexValueClickListener.onHexValueClick(getSelectedColor());
            }
        });
        container = findViewById(R.id.row_container);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);
        int model = a.getInt(R.styleable.ColorPickerView_cp_model, -1);
        setColorModel(model != -1 ? ColorModel.values()[model] : ColorModel.RGB);
        int selectedColor = a.getColor(R.styleable.ColorPickerView_cp_selectedColor, Color.BLACK);
        setSelectedColor(selectedColor);
        boolean previewVisible = a.getBoolean(R.styleable.ColorPickerView_cp_previewVisible, true);
        if (!previewVisible) {
            setPreviewVisible(false);
        }
        boolean hexVisible = a.getBoolean(R.styleable.ColorPickerView_cp_hexVisible, false);
        if (hexVisible) {
            setHexVisible(true);
        }
        int previewHeight = a.getDimensionPixelSize(R.styleable.ColorPickerView_cp_previewHeight, -1);
        if (previewHeight != -1) {
            setPreviewHeight(previewHeight);
        }
        a.recycle();
    }

    public void setColorModel(ColorModel model) {
        int selectedColor = Color.BLACK;
        if (colorModelController != null) {
            selectedColor = colorModelController.getColor();
            colorModelController.setListener(null);
        }
        container.removeAllViews();
        colorModelController = getModelController(model);
        colorModelController.setListener(newColor -> {
            ColorDrawable background = (ColorDrawable) preview.getBackground();
            background.setColor(newColor);
            hex.setText(String.format("#%08x", newColor));
            if (colorChangedListener != null) {
                colorChangedListener.onColorChanged(newColor);
            }
        });
        colorModelController.init(container, LayoutInflater.from(getContext()));
        colorModelController.setColor(selectedColor);
    }

    public void setSelectedColor(@ColorInt int color) {
        colorModelController.setColor(color);
    }

    @ColorInt
    public int getSelectedColor() {
        return colorModelController.getColor();
    }

    public void setPreviewVisible(boolean visible) {
        preview.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setPreviewHeight(@Px int height) {
        ViewGroup.LayoutParams layoutParams = preview.getLayoutParams();
        layoutParams.height = height;
        preview.requestLayout();
        preview.invalidate();
    }

    public void setHexVisible(boolean visible) {
        hex.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setColorChangedListener(@Nullable OnColorChangedListener listener) {
        colorChangedListener = listener;
    }

    public void setOnHexValueClickListener(@Nullable OnHexValueClickListener listener) {
        onHexValueClickListener = listener;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable base = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable(KEY_BASE_STATE, base);
        state.putBoolean(KEY_PREVIEW_VISIBLE, preview.getVisibility() == View.VISIBLE);
        state.putInt(KEY_SELECTED_COLOR, colorModelController.getColor());
        state.putSerializable(KEY_MODEL, colorModelController.getClass());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle myState = (Bundle) state;
        super.onRestoreInstanceState(myState.getParcelable(KEY_BASE_STATE));
        boolean previewVisible = myState.getBoolean(KEY_PREVIEW_VISIBLE, true);
        setPreviewVisible(previewVisible);
        //noinspection unchecked
        Class<? extends ColorModel> model = (Class<? extends ColorModel>) myState.getSerializable(KEY_MODEL);
        try {
            //noinspection ConstantConditions
            setColorModel(model.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            Timber.e(e, "onRestoreInstanceState:");
        }
        int selectedColor = myState.getInt(KEY_SELECTED_COLOR, Color.BLACK);
        setSelectedColor(selectedColor);
    }

    private ColorModelController getModelController(ColorModel model) {
        switch (model) {
            case ARGB:
                return new ARGB();
            default:
            case RGB:
                return new RGB();
        }
    }

    public interface OnColorChangedListener {
        void onColorChanged(@ColorInt int newColor);
    }

    public interface OnHexValueClickListener {
        void onHexValueClick(@ColorInt int currentColor);
    }

    public enum ColorModel {
        RGB,
        ARGB
    }

    /**
     * Draws transparency grid and color on top of it
     */
    private static class BackdropDrawable extends ColorDrawable {
        private final int gridSize;
        private final Paint paint = new Paint();

        public BackdropDrawable(@Px int gridSize) {
            if (gridSize <= 0) {
                throw new IllegalArgumentException("size must be > 0");
            }
            this.gridSize = gridSize;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int bw = getBounds().width();
            int width = (bw + gridSize - bw % gridSize) / gridSize;
            int bh = getBounds().height();
            int height = (bh + gridSize - bh % gridSize) / gridSize;
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    paint.setColor(w % 2 == h % 2 ? Color.WHITE : Color.LTGRAY);
                    canvas.drawRect(gridSize * w, gridSize * h,
                            gridSize * w + gridSize, gridSize * h + gridSize,
                            paint);
                }
            }
            super.draw(canvas);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }
}

