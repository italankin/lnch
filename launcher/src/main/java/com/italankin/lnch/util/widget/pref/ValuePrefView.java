package com.italankin.lnch.util.widget.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.italankin.lnch.R;

/**
 * @author Igor Talankin
 */
@SuppressWarnings("unchecked")
public class ValuePrefView extends RelativeLayout {

    private final ImageView icon;
    private final TextView title;
    private final TextView value;
    private ValueHolder valueHolder = new ObjectValueHolder();

    public ValuePrefView(Context context) {
        this(context, null);
    }

    public ValuePrefView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.item_pref_text, this);

        int p = context.getResources().getDimensionPixelSize(R.dimen.pref_view_padding);
        setPadding(p, p, p, p);

        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        value = findViewById(R.id.value);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValuePrefView);
        CharSequence text = a.getText(R.styleable.ValuePrefView_tpv_title);
        title.setText(text);
        Drawable drawable = a.getDrawable(R.styleable.ValuePrefView_tpv_icon);
        icon.setImageDrawable(drawable);
        a.recycle();

        TypedValue out = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, out, true);
        setBackgroundResource(out.resourceId);
    }

    public void setValueHolder(ValueHolder<?> provider) {
        valueHolder = provider;
    }

    public void setIcon(@DrawableRes int drawable) {
        icon.setImageResource(drawable);
    }

    public void setTitle(@StringRes int text) {
        title.setText(text);
    }

    public void setValue(Object rawValue) {
        valueHolder.set(rawValue);
        value.setText(valueHolder.getTitle());
    }

    public <T> T getValue() {
        return (T) valueHolder.get();
    }

    public interface ValueHolder<T> {
        void set(T value);

        T get();

        CharSequence getTitle();
    }

    public static class ObjectValueHolder implements ValueHolder<Object> {
        private Object value;

        @Override
        public void set(Object value) {
            this.value = value;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public CharSequence getTitle() {
            return String.valueOf(value);
        }
    }

    public static class ColorValueHolder implements ValueHolder<Integer> {
        private static final String FORMAT_ARGB = "#%08x";
        private static final String FORMAT_RGB = "#%06x";

        private final boolean withAlpha;
        private Integer value;

        public ColorValueHolder() {
            this(false);
        }

        public ColorValueHolder(boolean withAlpha) {
            this.withAlpha = withAlpha;
        }

        @Override
        public void set(Integer value) {
            this.value = value;
        }

        @Override
        public Integer get() {
            return value;
        }

        @Override
        public CharSequence getTitle() {
            return String.format(withAlpha ? FORMAT_ARGB : FORMAT_RGB, value);
        }
    }
}
