package com.italankin.lnch.util.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.italankin.lnch.BuildConfig;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.NoOpPreferences;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.icons.CircleDrawable;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

public class ColorPreference extends Preference {

    private static final float DISABLED_ALPHA = 0.35f;

    private final Preferences preferences;
    private final Preferences.Pref<Integer> pref;
    private final Integer defaultColor;
    private final boolean resettable;
    private final ColorPickerView.ColorModel colorModel;

    private OnPreferenceClickListener onPreferenceClickListener;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.pref_color_preference);
        preferences = getPreferences();
        pref = preferences.find(getKey());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPreference);
        defaultColor = a.getColor(R.styleable.ColorPreference_colorpref_defaultColor, Color.TRANSPARENT);
        int colorModelValue = a.getInteger(R.styleable.ColorPreference_colorpref_colorModel, 0);
        colorModel = colorModelValue == 0 ? ColorPickerView.ColorModel.RGB : ColorPickerView.ColorModel.ARGB;
        resettable = a.getBoolean(R.styleable.ColorPreference_colorpref_resettable, true);
        a.recycle();

        super.setOnPreferenceClickListener(this::onPreferenceClick);
    }

    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        this.onPreferenceClickListener = onPreferenceClickListener;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        Integer color = preferences.get(pref);
        int currentColor = color != null ? color : defaultColor;

        View colorPreview = holder.findViewById(R.id.pref_color_preview);
        Drawable background = colorPreview.getBackground();
        if (background instanceof CircleDrawable) {
            ((CircleDrawable) background).setColor(currentColor);
        } else {
            CircleDrawable cd = new CircleDrawable();
            cd.setColor(currentColor);
            colorPreview.setBackground(cd);
        }
        colorPreview.setAlpha(isEnabled() ? 1f : DISABLED_ALPHA);
    }

    private boolean onPreferenceClick(Preference preference) {
        if (onPreferenceClickListener == null || !onPreferenceClickListener.onPreferenceClick(preference)) {
            showColorSelectDialog();
        }
        return true;
    }

    private void showColorSelectDialog() {
        Integer color = preferences.get(pref);
        int currentColor = color != null ? color : defaultColor;

        ColorPickerDialog.Builder builder = ColorPickerDialog.builder(getContext())
                .setColorModel(colorModel)
                .setSelectedColor(currentColor)
                .setOnColorPickedListener(newColor -> {
                    preferences.set(pref, newColor);
                    notifyChanged();
                });
        if (resettable) {
            builder.setResetButton(getContext().getString(R.string.customize_action_reset), (dialog, which) -> {
                preferences.reset(pref);
                notifyChanged();
            });
        }
        builder.show();
    }

    private static Preferences getPreferences() {
        // a hack for get studio preview working
        if (BuildConfig.DEBUG && LauncherApp.daggerService == null) {
            return new NoOpPreferences();
        }
        return LauncherApp.daggerService.main().preferences();
    }
}
