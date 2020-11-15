package com.italankin.lnch.feature.settings.misc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.NotificationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;

public class MiscFragment extends BasePreferenceFragment {

    private Callbacks callbacks;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_misc);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(Preferences.NOTIFICATION_BADGE).setOnPreferenceClickListener(preference -> {
            Context context = requireContext();
            if (NotificationUtils.isNotificationAccessGranted(context)) {
                return false;
            } else {
                ((CheckBoxPreference) preference).setChecked(false);
                Intent notificationSettings = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                if (!IntentUtils.safeStartActivity(context, notificationSettings)) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        findPreference(R.string.pref_key_misc_shortcuts).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showShortcutsPreferences();
            }
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    public interface Callbacks {
        void showShortcutsPreferences();
    }
}
