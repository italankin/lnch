package com.italankin.lnch.util.widget.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;

import java.io.Serializable;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/**
 * @author Igor Talankin
 */
@SuppressWarnings("unchecked")
public class ValuePrefView extends RelativeLayout {

    private static final String STATE_VALUE = "value";
    private static final String STATE_SUPER = "super";

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

        ViewUtils.setPaddingDimen(this, R.dimen.pref_view_padding);

        icon = findViewById(R.id.icon);
        title = findViewById(R.id.title);
        value = findViewById(R.id.value);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ValuePrefView);
        CharSequence text = a.getText(R.styleable.ValuePrefView_tpv_title);
        title.setText(text);
        Drawable drawable = a.getDrawable(R.styleable.ValuePrefView_tpv_icon);
        icon.setImageDrawable(drawable);
        a.recycle();

        TypedValue attribute = ResUtils.resolveAttribute(context, R.attr.selectableItemBackground);
        if (attribute != null) {
            setBackgroundResource(attribute.resourceId);
        }
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

    public void setValue(Serializable rawValue) {
        valueHolder.set(rawValue);
        value.setText(valueHolder.getDescription());
    }

    public <T> T getValue() {
        return (T) valueHolder.get();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(STATE_SUPER, super.onSaveInstanceState());
        state.putSerializable(STATE_VALUE, valueHolder.get());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Serializable value = bundle.getSerializable(STATE_VALUE);
        setValue(value);
        super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));
    }

    public interface ValueHolder<T extends Serializable> {
        void set(T value);

        T get();

        CharSequence getDescription();
    }

    public static class ObjectValueHolder implements ValueHolder<Serializable> {
        private Serializable value;

        @Override
        public void set(Serializable value) {
            this.value = value;
        }

        @Override
        public Serializable get() {
            return value;
        }

        @Override
        public CharSequence getDescription() {
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
        public CharSequence getDescription() {
            return String.format(withAlpha ? FORMAT_ARGB : FORMAT_RGB, value);
        }
    }
}
