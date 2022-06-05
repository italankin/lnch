package com.italankin.lnch.feature.settings.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.italankin.lnch.feature.home.fragmentresult.FragmentResultSender;
import com.italankin.lnch.feature.settings.util.TargetPreference;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ViewUtils;

import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements FragmentResultSender {

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(@StringRes int key) {
        return (T) findPreference(getString(key));
    }

    @SuppressWarnings("unchecked")
    protected <T extends Preference> T findPreference(Preferences.Pref<?> pref) {
        return (T) findPreference(pref.key());
    }

    @Override
    public void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    protected void scrollToTarget() {
        String preference = TargetPreference.get(this);
        if (preference != null) {
            scrollToPreference(preference);
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> highlightTargetPreference(preference),
                    TargetPreference.HIGHLIGHT_DELAY);
        }
    }

    private void highlightTargetPreference(String target) {
        RecyclerView preferencesList = getListView();
        if (preferencesList == null) {
            return;
        }
        RecyclerView.Adapter<?> adapter = preferencesList.getAdapter();
        if (!(adapter instanceof PreferenceGroup.PreferencePositionCallback)) {
            return;
        }
        PreferenceGroup.PreferencePositionCallback callback = (PreferenceGroup.PreferencePositionCallback) adapter;
        int position = callback.getPreferenceAdapterPosition(target);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        RecyclerView.ViewHolder holder = preferencesList.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            ViewUtils.setTemporaryPressedState(holder.itemView, TargetPreference.HIGHLIGHT_DURATION);
        }
    }
}
