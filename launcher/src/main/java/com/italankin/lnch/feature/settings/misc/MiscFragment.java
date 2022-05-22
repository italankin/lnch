package com.italankin.lnch.feature.settings.misc;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;

public class MiscFragment extends BasePreferenceFragment {

    public static MiscFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        MiscFragment fragment = new MiscFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_misc);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_misc_experimental).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowExperimentalPreferencesContract().result());
            return true;
        });
        findPreference(R.string.pref_key_home_hidden_items).setOnPreferenceClickListener(preference -> {
            sendResult(new ShowHiddenItems().result());
            return true;
        });
    }

    public static class ShowExperimentalPreferencesContract extends SignalFragmentResultContract {
        public ShowExperimentalPreferencesContract() {
            super("show_experimental_preferences");
        }
    }

    public static class ShowHiddenItems extends SignalFragmentResultContract {
        public ShowHiddenItems() {
            super("show_hidden_items");
        }
    }
}
