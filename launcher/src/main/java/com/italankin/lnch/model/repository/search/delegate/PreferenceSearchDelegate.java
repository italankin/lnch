package com.italankin.lnch.model.repository.search.delegate;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.settings.SettingsActivity;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;
import com.italankin.lnch.feature.settings.searchstore.SettingsStore;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchDelegate;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.icons.BadgedIconDrawable;

import java.util.*;

public class PreferenceSearchDelegate implements SearchDelegate {

    private static final int MAX_RESULTS = 3;

    private final SettingsStore settingsStore;

    public PreferenceSearchDelegate(SettingsStore settingsStore) {
        this.settingsStore = settingsStore;
    }

    @Override
    public List<Match> search(String query, EnumSet<Preferences.SearchTarget> searchTargets) {
        if (!searchTargets.contains(Preferences.SearchTarget.PREFERENCE)) {
            return Collections.emptyList();
        }
        List<SettingsEntry> entries = settingsStore.search(query, MAX_RESULTS);
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<Match> result = new ArrayList<>(entries.size());
        for (SettingsEntry entry : entries) {
            result.add(new PreferenceMatch(entry));
        }
        return result;
    }
}

class PreferenceMatch implements Match {
    private final SettingsEntry entry;

    PreferenceMatch(SettingsEntry entry) {
        this.entry = entry;
    }

    @Override
    public Uri getIcon() {
        return null;
    }

    @Override
    public Drawable getDrawableIcon(Context context) {
        return new BadgedIconDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_settings),
                ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
        );
    }

    @Override
    public CharSequence getLabel(Context context) {
        return context.getString(entry.title());
    }

    @Nullable
    @Override
    public CharSequence getSubtext(Context context) {
        if (entry.category() == 0) {
            return null;
        }
        return context.getString(entry.category());
    }

    @Override
    public int getColor(Context context) {
        return ResUtils.resolveColor(context, R.attr.colorText);
    }

    @Override
    public Intent getIntent(Context context) {
        return SettingsActivity.createIntent(context, entry.key());
    }

    @Override
    public Kind getKind() {
        return Kind.PREFERENCE;
    }

    @Override
    public Set<Action> availableActions() {
        return Collections.emptySet();
    }

    @Override
    public int hashCode() {
        return entry.key().hashCode();
    }
}
