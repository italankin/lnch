package com.italankin.lnch.feature.settings.backup;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;
import com.italankin.lnch.util.dialogfragment.ListenerFragment;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BackupFragment extends AppPreferenceFragment implements BackupView {

    private static final String MIME_TYPE_ANY = "*/*";

    private static final String TAG_RESET_DIALOG = "reset_dialog";

    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    private static final int REQUEST_CODE_CREATE_DOCUMENT = 2;

    @InjectPresenter
    BackupPresenter presenter;

    @ProvidePresenter
    BackupPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().backup();
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
        findPreference(R.string.pref_key_reset).setOnPreferenceClickListener(preference -> {
            showResetDialog();
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uri = data != null ? data.getData() : null;
        if (uri == null) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_CREATE_DOCUMENT: {
                if (resultCode == Activity.RESULT_OK) {
                    presenter.onBackupSettings(uri);
                }
                break;
            }
            case REQUEST_CODE_OPEN_DOCUMENT: {
                if (resultCode == Activity.RESULT_OK) {
                    presenter.onRestoreSettings(uri);
                }
                break;
            }
        }
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

    private void showError() {
        Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
    }

    private void restoreSettings() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType(MIME_TYPE_ANY);
        try {
            startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void backupSettings() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(MIME_TYPE_ANY);
        intent.putExtra(Intent.EXTRA_TITLE, BackupFileNameProvider.generateFileName());
        startActivityForResult(intent, REQUEST_CODE_CREATE_DOCUMENT);
    }

    private void showResetDialog() {
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.settings_other_bar_reset_dialog_title)
                .setMessage(R.string.settings_other_bar_reset_dialog_message)
                .setPositiveButton(R.string.settings_other_bar_reset_dialog_action)
                .setNegativeButton(R.string.cancel)
                .setListenerProvider(new ResetDialogListenerProvider())
                .build()
                .show(getChildFragmentManager(), TAG_RESET_DIALOG);
    }

    private static class ResetDialogListenerProvider implements ListenerFragment<SimpleDialogFragment.Listener> {
        @Override
        public SimpleDialogFragment.Listener get(Fragment parentFragment) {
            return (String tag) -> ((BackupFragment) parentFragment).presenter.resetAppsSettings();
        }
    }
}
