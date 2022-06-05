package com.italankin.lnch.feature.settings.backup;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BackupFragment extends AppPreferenceFragment implements BackupView, SimpleDialogFragment.Listener,
        SettingsToolbarTitle {

    private static final String MIME_TYPE_ANY = "*/*";

    private static final String TAG_RESET_DIALOG_APPS = "reset_dialog_apps";
    private static final String TAG_RESET_DIALOG_LNCH = "reset_dialog_lnch";

    @InjectPresenter
    BackupPresenter presenter;

    private final ActivityResultLauncher<Void> restoreLauncher = registerForActivityResult(
            new OpenDocumentContract(), result -> {
                if (result != null) {
                    presenter.onRestoreSettings(result);
                }
            });

    private final ActivityResultLauncher<Void> backupLauncher = registerForActivityResult(
            new CreateDocumentContract(), result -> {
                if (result != null) {
                    presenter.onBackupSettings(result);
                }
            });

    @ProvidePresenter
    BackupPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().backup();
    }

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_other_bar);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_backup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_backup).setOnPreferenceClickListener(preference -> {
            backupSettings();
            return true;
        });
        findPreference(R.string.pref_key_restore).setOnPreferenceClickListener(preference -> {
            restoreSettings();
            return true;
        });
        findPreference(R.string.pref_key_reset_apps).setOnPreferenceClickListener(preference -> {
            showResetAppsDialog();
            return true;
        });
        findPreference(R.string.pref_key_reset_lnch).setOnPreferenceClickListener(preference -> {
            showResetLnchDialog();
            return true;
        });
        scrollToTarget();
    }

    @Override
    public void onRestoreSuccess() {
        Toast.makeText(requireContext(), R.string.settings_other_bar_restore_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRestoreError(Throwable error) {
        showError();
    }

    @Override
    public void onBackupSuccess() {
        Toast.makeText(requireContext(), R.string.settings_other_bar_backup_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackupError(Throwable error) {
        showError();
    }

    @Override
    public void onResetSuccess() {
        Toast.makeText(requireContext(), R.string.settings_other_bar_reset_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResetError(Throwable error) {
        showError();
    }

    @Override
    public void onPositiveButtonClick(@Nullable String tag) {
        if (TAG_RESET_DIALOG_APPS.equals(tag)) {
            presenter.resetAppsSettings();
        } else if (TAG_RESET_DIALOG_LNCH.equals(tag)) {
            presenter.resetLnchSettings();
        }
    }

    private void showError() {
        Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
    }

    private void restoreSettings() {
        try {
            restoreLauncher.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void backupSettings() {
        try {
            backupLauncher.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void showResetAppsDialog() {
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.settings_other_bar_reset_dialog_title)
                .setMessage(R.string.settings_other_bar_reset_dialog_apps_message)
                .setPositiveButton(R.string.settings_other_bar_reset_dialog_action)
                .setNegativeButton(R.string.cancel)
                .build()
                .show(getChildFragmentManager(), TAG_RESET_DIALOG_APPS);
    }

    private void showResetLnchDialog() {
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.settings_other_bar_reset_dialog_title)
                .setMessage(R.string.settings_other_bar_reset_dialog_lnch_message)
                .setPositiveButton(R.string.settings_other_bar_reset_dialog_action)
                .setNegativeButton(R.string.cancel)
                .build()
                .show(getChildFragmentManager(), TAG_RESET_DIALOG_LNCH);
    }

    private static class OpenDocumentContract extends ActivityResultContract<Void, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(MIME_TYPE_ANY);
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK && intent != null ? intent.getData() : null;
        }
    }

    private static class CreateDocumentContract extends ActivityResultContract<Void, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(MIME_TYPE_ANY)
                    .putExtra(Intent.EXTRA_TITLE, BackupFileNameProvider.generateFileName());
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK && intent != null ? intent.getData() : null;
        }
    }
}
