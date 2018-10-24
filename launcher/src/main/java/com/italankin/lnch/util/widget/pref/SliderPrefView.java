package com.italankin.lnch.util.widget.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.italankin.lnch.R;

/**
 * @author Igor Talankin
 */
public class SliderPrefView extends RelativeLayout {

    private final ImageView icon;
    private final TextView title;
    private final SeekBar seekbar;

    public SliderPrefView(Context context) {
        this(context, null);
    }

    public SliderPrefView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.item_pref_slider, this);

        int p = context.getResources().getDimensionPixelSize(R.dimen.pref_view_padding);
        setPadding(p, p, p, p);

        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        seekbar = findViewById(R.id.seekbar);

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
        seekbar.setMax(max);
    }

    public void setProgress(int progress) {
        seekbar.setProgress(progress);
    }

    public int getProgress() {
        return seekbar.getProgress();
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        seekbar.setOnSeekBarChangeListener(listener);
    }
}
