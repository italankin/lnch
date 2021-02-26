package com.italankin.lnch.feature.settings.notifications;

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
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;

public class NotificationsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CheckBoxPreference notificationDot = findPreference(Preferences.NOTIFICATION_DOT);
        notificationDot.setOnPreferenceClickListener(preference -> {
            if (NotificationUtils.isNotificationAccessGranted(requireContext())) {
                return false;
            } else {
                notificationDot.setChecked(false);
                showRequestNotificationAccessDialog();
                return true;
            }
        });
        if (!NotificationUtils.isNotificationAccessGranted(requireContext())) {
            notificationDot.setChecked(false);
        }
    }

    private void showRequestNotificationAccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_home_misc_notifications_dot_dialog_title)
                .setMessage(R.string.settings_home_misc_notifications_dot_dialog_message)
                .setPositiveButton(R.string.settings_home_misc_notifications_dot_dialog_allow, (dialog, which) -> {
                    Intent notificationSettings = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    if (!IntentUtils.safeStartActivity(requireContext(), notificationSettings)) {
                        Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
