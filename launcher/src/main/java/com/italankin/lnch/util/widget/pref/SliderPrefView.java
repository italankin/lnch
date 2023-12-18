package com.italankin.lnch.util.widget.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.slider.Slider;
import com.italankin.lnch.R;
import com.italankin.lnch.util.ViewUtils;

public class SliderPrefView extends RelativeLayout {

    private static final String STATE_SUPER = "super";
    private static final String STATE_VALUE = "value";

    private final ImageView icon;
    private final TextView title;
    private final Slider slider;

    public SliderPrefView(Context context) {
        this(context, null);
    }

    public SliderPrefView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.item_pref_slider, this);

        ViewUtils.setPaddingDimen(this, R.dimen.pref_view_padding);

        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        slider = findViewById(R.id.slider);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderPrefView);
        CharSequence text = a.getText(R.styleable.SliderPrefView_spv_title);
        title.setText(text);
        Drawable drawable = a.getDrawable(R.styleable.SliderPrefView_spv_icon);
        icon.setImageDrawable(drawable);
        a.recycle();
    }

    public void setIcon(@DrawableRes int drawable) {
        icon.setImageResource(drawable);
    }

    public void setTitle(@StringRes int text) {
        title.setText(text);
    }

    public void setMax(int max) {
        slider.setValueTo(max);
    }

    public void setProgress(int progress) {
        slider.setValue(progress);
    }

    public int getProgress() {
        return (int) slider.getValue();
    }

    public void addOnChangeListener(Slider.OnChangeListener listener) {
        slider.addOnChangeListener(listener);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        bundle.putInt(STATE_VALUE, (int) slider.getValue());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        setProgress(bundle.getInt(STATE_VALUE));
        super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));
    }
}
