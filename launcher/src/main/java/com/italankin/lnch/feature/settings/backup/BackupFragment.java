package com.italankin.lnch.feature.settings.backup;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;

public class BackupFragment extends AppPreferenceFragment implements BackupView {

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
            // TODO
            return true;
        });
        findPreference(R.string.key_restore).setOnPreferenceClickListener(preference -> {
            // TODO
            return true;
        });
    }
}
