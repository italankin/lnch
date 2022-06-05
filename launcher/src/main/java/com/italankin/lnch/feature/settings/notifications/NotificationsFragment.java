package com.italankin.lnch.feature.settings.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.BasePreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.NotificationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;

public class NotificationsFragment extends BasePreferenceFragment implements SettingsToolbarTitle {

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_misc_notifications);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CheckBoxPreference notificationDot = findPreference(Preferences.NOTIFICATION_DOT);
        setClickListener(notificationDot);
        CheckBoxPreference notificationPopup = findPreference(Preferences.NOTIFICATION_POPUP);
        setClickListener(notificationPopup);
        if (!NotificationUtils.isNotificationAccessGranted(requireContext())) {
            notificationDot.setChecked(false);
            notificationPopup.setChecked(false);
        }
        scrollToTarget();
    }

    private void setClickListener(CheckBoxPreference pref) {
        pref.setOnPreferenceClickListener(preference -> {
            if (NotificationUtils.isNotificationAccessGranted(requireContext())) {
                return false;
            } else {
                pref.setChecked(false);
                showRequestNotificationAccessDialog();
                return true;
            }
        });
    }

    private void showRequestNotificationAccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_home_misc_notifications_permissions_dialog_title)
                .setMessage(R.string.settings_home_misc_notifications_permission_dialog_message)
                .setPositiveButton(R.string.settings_home_misc_notifications_permissions_dialog_allow, (dialog, which) -> {
                    Intent notificationSettings = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                    if (!IntentUtils.safeStartActivity(requireContext(), notificationSettings)) {
                        Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
