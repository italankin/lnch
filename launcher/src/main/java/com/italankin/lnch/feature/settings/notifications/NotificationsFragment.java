package com.italankin.lnch.feature.settings.notifications;

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

public class NotificationsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(Preferences.NOTIFICATION_DOT).setOnPreferenceClickListener(preference -> {
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
    }
}
