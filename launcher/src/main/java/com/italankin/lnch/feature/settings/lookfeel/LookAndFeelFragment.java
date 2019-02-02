package com.italankin.lnch.feature.settings.lookfeel;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LookAndFeelFragment extends BasePreferenceFragment {

    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_look_and_feel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.key_item_appearance).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showItemLookPreferences();
            }
            return true;
        });
    }

    public interface Callbacks {
        void showItemLookPreferences();
    }
}
