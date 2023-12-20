package com.italankin.lnch.feature.settings.search;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;
import com.italankin.lnch.R;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class SearchEnginePreference extends ListPreference {

    private View.OnClickListener onCustomFormatClickListener;

    public SearchEnginePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.widget_preference_custom_search);
        setEntries(R.array.pref_desc_search_engines);
        setEntryValues(R.array.pref_values_search_engines);
        setDefaultValue(Preferences.SEARCH_ENGINE.defaultValue().toString());
        setKey(Preferences.SEARCH_ENGINE.key());
    }

    public void setOnCustomFormatClickListener(View.OnClickListener listener) {
        onCustomFormatClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View customFormat = holder.findViewById(R.id.custom_format);
        boolean isCustom = Preferences.SearchEngine.CUSTOM.toString().equals(getValue());
        customFormat.setVisibility(isCustom ? View.VISIBLE : View.GONE);
        customFormat.setOnClickListener(v -> {
            if (onCustomFormatClickListener != null) {
                onCustomFormatClickListener.onClick(v);
            }
        });
    }
}
