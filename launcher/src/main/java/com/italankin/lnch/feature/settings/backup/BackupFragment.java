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
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;

public class BackupFragment extends AppPreferenceFragment implements BackupView {

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
                presenter.onBackupSettings();
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
        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            presenter.onBackupSettings();
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
        Toast.makeText(requireContext(), R.string.restore_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRestoreError(Throwable error) {
        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackupSuccess(String path) {
        Toast.makeText(requireContext(), getString(R.string.backup_success, path), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackupError(Throwable error) {
        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    }
}
