package com.italankin.lnch.feature.home.search;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.DescriptorMatch;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.imageloader.cache.LruCache;
import com.italankin.lnch.util.widget.TextWatcherAdapter;
import me.italankin.adapterdelegates.CompositeAdapter;

import java.util.Collections;
import java.util.List;

public class SearchOverlay extends ConstraintLayout implements MatchAdapter.Listener, SearchResults.Callback {

    private final InputMethodManager inputMethodManager;
    private final ImageLoader imageLoader;

    private final EditText searchEditText;
    private final ImageView buttonGlobalSearch;
    private final ImageView buttonSettings;
    private final ImageView buttonCustomize;

    private final RecyclerView searchResultsList;
    private final CompositeAdapter<Match> searchAdapter;
    private final SearchResults searchResults;

    private SettingsState settingsState = SettingsState.SETTINGS;
    private Listener listener;

    public SearchOverlay(@NonNull Context context) {
        this(context, null);
    }

    public SearchOverlay(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        SearchRepository searchRepository;
        if (isInEditMode()) {
            imageLoader = null;
            searchRepository = null;
        } else {
            imageLoader = new ImageLoader.Builder(context)
                    .cache(new LruCache(32))
                    .build();
            searchRepository = LauncherApp.daggerService.main().searchRepository();
        }

        setFocusable(true);
        setFocusableInTouchMode(true);
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.search_bar_size));

        inflate(context, R.layout.widget_search_overlay, this);

        searchEditText = findViewById(R.id.search_edit_text);
        buttonGlobalSearch = findViewById(R.id.search_global);
        buttonSettings = findViewById(R.id.search_settings);
        buttonCustomize = findViewById(R.id.search_customize);
        searchResultsList = findViewById(R.id.search_results);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onFireSearch(null);
            }
            return true;
        });
        searchEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchResults.query(s);
                int length = s.length();
                setSettingsState(length == 0 ? SettingsState.SETTINGS : SettingsState.CLEAR_QUERY);
            }
        });

        searchResults = new SearchResults(searchRepository);

        searchAdapter = new CompositeAdapter.Builder<Match>(context)
                .add(new MatchAdapter(imageLoader, this))
                .recyclerView(searchResultsList)
                .setHasStableIds(true)
                .create();
        int margin = context.getResources().getDimensionPixelSize(R.dimen.search_result_margin);
        searchResultsList.addItemDecoration(new MarginItemDecoration(margin));

        setViewsEnabled(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        searchResults.subscribe(this);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        ViewUtils.setPaddingBottom(searchResultsList, insets.getSystemWindowInsetBottom());
        return super.onApplyWindowInsets(insets);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        searchResults.unsubscribe();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public int searchBarHeight() {
        return searchEditText.getHeight();
    }

    public void focusEditText() {
        if (!searchEditText.isEnabled()) {
            return;
        }
        searchEditText.requestFocus();
        inputMethodManager.showSoftInput(searchEditText, 0);
    }

    public void onSearchShown() {
        if (searchResultsList.getVisibility() == VISIBLE) {
            return;
        }
        boolean showMostUsed = LauncherApp.daggerService.main().preferences().get(Preferences.SEARCH_SHOW_MOST_USED);
        if (searchResultsList.getAdapter() == null) {
            if (!showMostUsed) {
                searchAdapter.setDataset(Collections.emptyList());
            }
            searchResultsList.setAdapter(searchAdapter);
        }
        searchResultsList.setVisibility(VISIBLE);
        setViewsEnabled(true);
        if (showMostUsed) {
            // fire initial search to show/hide recent items
            searchEditText.setText("");
        }
    }

    public void setupCustomizeButton(OnClickListener onClickListener) {
        if (onClickListener != null) {
            buttonCustomize.setOnClickListener(onClickListener);
            buttonCustomize.setVisibility(VISIBLE);
        } else {
            buttonCustomize.setVisibility(GONE);
        }
    }

    public void onSearchHidden() {
        // remove adapter to hide previous search results in next onSearchShown
        searchResultsList.setAdapter(null);
        searchResultsList.setVisibility(GONE);
        setViewsEnabled(false);
        searchEditText.setText("");
        hideSoftKeyboard();
        searchEditText.clearFocus();
    }

    public void setupGlobalSearch(Uri icon, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        buttonGlobalSearch.setOnClickListener(onClickListener);
        buttonGlobalSearch.setOnLongClickListener(onLongClickListener);
        imageLoader.load(icon)
                .errorPlaceholder(ContextCompat.getDrawable(getContext(), R.drawable.ic_action_search))
                .into(buttonGlobalSearch);
    }

    public void setupSettings(OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        buttonSettings.setOnClickListener(v -> {
            if (settingsState == SettingsState.SETTINGS) {
                onClickListener.onClick(v);
            } else {
                searchEditText.setText("");
            }
        });
        buttonSettings.setOnLongClickListener(v -> {
            if (settingsState == SettingsState.SETTINGS) {
                return onLongClickListener.onLongClick(v);
            } else if (listener != null) {
                listener.onSearchDismissed();
                return true;
            }
            return false;
        });
    }

    public void setSearchBarSizeDimen(@DimenRes int size) {
        setSearchBarSize(getResources().getDimensionPixelSize(size));
    }

    public void setSearchBarSize(int size) {
        searchEditText.setMinimumHeight(size);
    }

    public void setSearchBarTextSizeDimen(@DimenRes int size) {
        setSearchBarTextSize(getResources().getDimension(size));
    }

    public void setSearchBarTextSize(@Dimension float size) {
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    public void hideGlobalSearch() {
        buttonGlobalSearch.setImageResource(R.drawable.ic_action_search);
        buttonGlobalSearch.setOnClickListener(null);
        buttonGlobalSearch.setOnLongClickListener(null);
        buttonGlobalSearch.setEnabled(false);
    }

    public void hideSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    @Override
    public void onSearchItemClick(int position, Match match) {
        onFireSearch(match);
    }

    @Override
    public void onSearchItemPinClick(int position, Match match) {
        if (listener != null) {
            listener.onSearchItemPinClick(match);
        }
    }

    @Override
    public void onSearchItemInfoClick(int position, Match match) {
        if (listener != null) {
            listener.onSearchItemInfoClick(match);
        }
    }

    @Override
    public void onSearchResults(List<Match> results) {
        searchAdapter.setDataset(results);
        searchAdapter.notifyDataSetChanged();
    }

    private void setViewsEnabled(boolean enabled) {
        buttonGlobalSearch.setEnabled(enabled);
        buttonSettings.setEnabled(enabled);
        buttonCustomize.setEnabled(enabled);
        searchEditText.setEnabled(enabled);
    }

    private void setSettingsState(SettingsState state) {
        if (settingsState != state) {
            settingsState = state;
            if (settingsState == SettingsState.SETTINGS) {
                buttonSettings.setImageResource(R.drawable.ic_settings);
                if (buttonCustomize.getVisibility() != View.GONE) {
                    buttonCustomize.setVisibility(View.VISIBLE);
                }
            } else {
                buttonSettings.setImageResource(R.drawable.ic_close);
                if (buttonCustomize.getVisibility() != View.GONE) {
                    buttonCustomize.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void onFireSearch(Match match) {
        if (listener == null) {
            return;
        }
        boolean emptyQuery = searchEditText.getText().length() == 0;
        if (match == null && !emptyQuery) {
            int count = searchAdapter.getItemCount();
            if (count > 0) {
                match = searchAdapter.getItem(0);
            }
        }
        if (match != null) {
            if (match instanceof DescriptorMatch) {
                Descriptor descriptor = ((DescriptorMatch) match).getDescriptor();
                listener.handleDescriptorIntent(match.getIntent(getContext()), descriptor);
            } else {
                listener.handleIntent(match.getIntent(getContext()));
            }
        }
        searchEditText.setText("");
        listener.onSearchFired(emptyQuery);
    }

    private enum SettingsState {
        SETTINGS,
        CLEAR_QUERY
    }

    public interface Listener {
        void handleIntent(Intent intent);

        void handleDescriptorIntent(Intent intent, Descriptor descriptor);

        void onSearchFired(boolean emptyQuery);

        void onSearchDismissed();

        void onSearchItemPinClick(Match match);

        void onSearchItemInfoClick(Match match);
    }
}
