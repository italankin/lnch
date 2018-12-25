package com.italankin.lnch.feature.settings.backup;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;
import com.italankin.lnch.util.dialogfragment.ListenerFragment;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;

public class BackupFragment extends AppPreferenceFragment implements BackupView {

    private static final String TAG_BACKUP_DIALOG = "backup";
    private static final String TAG_RESET_DIALOG = "reset_dialog";

    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    private static final int REQUEST_CODE_WRITE_STORAGE = 2;

    @InjectPresenter
    BackupPresenter presenter;

    @ProvidePresenter
    BackupPresenter providePresenter() {
        return daggerService().presenters().backup();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_backup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.key_backup).setOnPreferenceClickListener(preference -> {
            if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
            } else {
                backupSettings();
            }
            return true;
        });
        findPreference(R.string.key_restore).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*");
            try {
                startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
            }
            return true;
        });
        findPreference(R.string.key_reset).setOnPreferenceClickListener(preference -> {
            showResetDialog();
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            presenter.onRestoreFromSource(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE_WRITE_STORAGE) {
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            backupSettings();
        } else {
            Toast.makeText(requireContext(), R.string.error_write_storage_permission, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showProgress() {
        // TODO
    }

    @Override
    public void onRestoreSuccess() {
        Toast.makeText(requireContext(), R.string.settings_backups_message_restore_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRestoreError(Throwable error) {
        showError(error);
    }

    @Override
    public void onBackupSuccess(String path) {
        Toast.makeText(requireContext(), getString(R.string.settings_backups_message_backup_success, path), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackupError(Throwable error) {
        showError(error);
    }

    @Override
    public void onResetSuccess() {
        Toast.makeText(requireContext(), R.string.settings_backups_message_reset_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResetError(Throwable error) {
        showError(error);
    }

    private void showError(Throwable error) {
        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void backupSettings() {
        class BackupDialogListenerProvider implements ListenerFragment<SimpleDialogFragment.Listener> {
            @Override
            public SimpleDialogFragment.Listener get(Fragment parentFragment) {
                return ((BackupFragment) parentFragment).presenter::onBackupSettings;
            }
        }
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.title_settings_backups_backup_apps)
                .setMessage(R.string.settings_backups_dialog_backup_message)
                .setPositiveButton(R.string.settings_backups_dialog_backup_message_action)
                .setNegativeButton(R.string.cancel)
                .setListenerProvider(new BackupDialogListenerProvider())
                .build()
                .show(getChildFragmentManager(), TAG_BACKUP_DIALOG);
    }

    private void showResetDialog() {
        class ResetDialogListenerProvider implements ListenerFragment<SimpleDialogFragment.Listener> {
            @Override
            public SimpleDialogFragment.Listener get(Fragment parentFragment) {
                return ((BackupFragment) parentFragment).presenter::resetAppsSettings;
            }
        }
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.settings_backups_dialog_reset_title)
                .setMessage(R.string.settings_backups_dialog_reset_message)
                .setPositiveButton(R.string.settings_backups_dialog_reset_action)
                .setNegativeButton(R.string.cancel)
                .setListenerProvider(new ResetDialogListenerProvider())
                .build()
                .show(getChildFragmentManager(), TAG_RESET_DIALOG);
    }
}
