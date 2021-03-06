package com.italankin.lnch.feature.settings.lookfeel;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.base.AppPreferenceFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

public class LookAndFeelFragment extends AppPreferenceFragment implements MvpView, ColorPickerDialogFragment.Listener {

    private static final String TAG_APPS_COLOR_OVERLAY = "apps_color_overlay";
    private static final String TAG_STATUS_COLOR = "status_color";
    private static final String TAG_DOT_COLOR = "dot_color";

    @InjectPresenter
    LookAndFeelPresenter presenter;

    private Callbacks callbacks;
    private Preferences preferences;

    @ProvidePresenter
    LookAndFeelPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().lookAndFeel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.saveData();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_look_and_feel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference(R.string.pref_key_appearance).setOnPreferenceClickListener(preference -> {
            if (callbacks != null) {
                callbacks.showItemLookPreferences();
            }
            return true;
        });
        findPreference(Preferences.APPS_COLOR_OVERLAY_SHOW).setOnPreferenceChangeListener((preference, newValue) -> {
            updateColorOverlay((Boolean) newValue);
            return true;
        });
        findPreference(Preferences.APPS_COLOR_OVERLAY).setOnPreferenceClickListener(preference -> {
            onColorOverlayClick();
            return true;
        });
        findPreference(Preferences.STATUS_BAR_COLOR).setOnPreferenceClickListener(preference -> {
            onStatusBarColorClick();
            return true;
        });
        findPreference(Preferences.NOTIFICATION_DOT_COLOR).setOnPreferenceClickListener(preference -> {
            onNotificationDotColorClick();
            return true;
        });
        findPreference(Preferences.APPS_LIST_ANIMATE).setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        updateColorOverlay(preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW));
        updateStatusBarColor();
        updateNotificationDotColor();
    }

    @Override
    public void onColorPicked(@Nullable String tag, int newColor) {
        if (tag == null) {
            return;
        }
        switch (tag) {
            case TAG_APPS_COLOR_OVERLAY: {
                preferences.set(Preferences.APPS_COLOR_OVERLAY, newColor);
                updateColorOverlay(true);
                break;
            }
            case TAG_STATUS_COLOR: {
                preferences.set(Preferences.STATUS_BAR_COLOR, newColor);
                updateStatusBarColor();
                break;
            }
            case TAG_DOT_COLOR: {
                preferences.set(Preferences.NOTIFICATION_DOT_COLOR, newColor);
                updateNotificationDotColor();
                break;
            }
        }
    }

    @Override
    public void onColorReset(@Nullable String tag) {
        if (tag == null) {
            return;
        }
        switch (tag) {
            case TAG_APPS_COLOR_OVERLAY: {
                preferences.reset(Preferences.APPS_COLOR_OVERLAY);
                updateColorOverlay(true);
                break;
            }
            case TAG_STATUS_COLOR: {
                preferences.reset(Preferences.STATUS_BAR_COLOR);
                updateStatusBarColor();
                break;
            }
            case TAG_DOT_COLOR: {
                preferences.reset(Preferences.NOTIFICATION_DOT_COLOR);
                updateNotificationDotColor();
                break;
            }
        }
    }

    private void updateColorOverlay(Boolean newValue) {
        Preference preference = findPreference(Preferences.APPS_COLOR_OVERLAY);
        if (newValue) {
            int color = preferences.get(Preferences.APPS_COLOR_OVERLAY);
            preference.setSummary(String.format("#%06x", color & 0x00ffffff));
        } else {
            preference.setSummary(null);
        }
    }

    private void onColorOverlayClick() {
        new ColorPickerDialogFragment.Builder()
                .setColorModel(ColorPickerView.ColorModel.RGB)
                .setHexVisible(false)
                .setPreviewVisible(true)
                .setSelectedColor(preferences.get(Preferences.APPS_COLOR_OVERLAY))
                .showResetButton(true)
                .build()
                .show(getChildFragmentManager(), TAG_APPS_COLOR_OVERLAY);
    }

    private void updateStatusBarColor() {
        Preference preference = findPreference(Preferences.STATUS_BAR_COLOR);
        Integer statusBarColor = preferences.get(Preferences.STATUS_BAR_COLOR);
        if (statusBarColor != null) {
            preference.setSummary(String.format("#%06x", statusBarColor));
        } else {
            preference.setSummary(null);
        }
    }

    private void onStatusBarColorClick() {
        Integer color = preferences.get(Preferences.STATUS_BAR_COLOR);
        new ColorPickerDialogFragment.Builder()
                .setColorModel(ColorPickerView.ColorModel.ARGB)
                .setHexVisible(false)
                .setPreviewVisible(true)
                .setSelectedColor(color != null ? color : Color.TRANSPARENT)
                .showResetButton(true)
                .build()
                .show(getChildFragmentManager(), TAG_STATUS_COLOR);
    }

    private void onNotificationDotColorClick() {
        Integer color = preferences.get(Preferences.NOTIFICATION_DOT_COLOR);
        int selectedColor = color != null ? color : ContextCompat.getColor(requireContext(), R.color.notification_dot);
        new ColorPickerDialogFragment.Builder()
                .setColorModel(ColorPickerView.ColorModel.RGB)
                .setHexVisible(false)
                .setPreviewVisible(true)
                .setSelectedColor(selectedColor)
                .showResetButton(true)
                .build()
                .show(getChildFragmentManager(), TAG_DOT_COLOR);
    }

    private void updateNotificationDotColor() {
        Preference preference = findPreference(Preferences.NOTIFICATION_DOT_COLOR);
        boolean dotEnabled = preferences.get(Preferences.NOTIFICATION_DOT);
        preference.setEnabled(dotEnabled);
        Integer color = preferences.get(Preferences.NOTIFICATION_DOT_COLOR);
        if (dotEnabled && color != null) {
            preference.setSummary(String.format("#%06x", color));
        } else {
            preference.setSummary(null);
        }
    }

    public interface Callbacks {
        void showItemLookPreferences();
    }
}
