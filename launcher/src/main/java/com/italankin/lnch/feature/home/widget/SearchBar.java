package com.italankin.lnch.feature.home.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.adapter.SearchAdapter;
import com.italankin.lnch.model.repository.search.SearchRepository;
import com.italankin.lnch.model.repository.search.match.Match;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.squareup.picasso.Picasso;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SearchBar extends FrameLayout {

    private static final float TEXT_SIZE_FACTOR = 3.11f;

    private final InputMethodManager inputMethodManager;
    private final Picasso picasso;

    private final AutoCompleteTextView searchEditText;
    private final ImageView buttonGlobalSearch;
    private final View buttonSettings;

    private final SearchAdapter searchAdapter;

    private Listener listener;

    public SearchBar(@NonNull Context context) {
        this(context, null);
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        picasso = LauncherApp.daggerService.main().picassoFactory().create(context);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(ResUtils.resolveColor(context, R.attr.colorSearchBarBackground));
        setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.search_bar_size));

        inflate(context, R.layout.widget_search_bar, this);

        searchEditText = findViewById(R.id.search_edit_text);
        buttonGlobalSearch = findViewById(R.id.search_global);
        buttonSettings = findViewById(R.id.search_settings);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                onFireSearch(null);
            }
            return true;
        });

        SearchRepository searchRepository = LauncherApp.daggerService.main().searchRepository();
        searchAdapter = new SearchAdapter(picasso, searchRepository, new SearchAdapter.Listener() {
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
        });
        searchEditText.setAdapter(searchAdapter);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void focusEditText() {
        searchEditText.requestFocus();
        inputMethodManager.showSoftInput(searchEditText, 0);
    }

    public void reset() {
        hidePopup();
        searchEditText.setText("");
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        searchEditText.clearFocus();
    }

    public void setupGlobalSearch(Uri icon, OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        buttonGlobalSearch.setVisibility(VISIBLE);
        buttonGlobalSearch.setOnClickListener(onClickListener);
        buttonGlobalSearch.setOnLongClickListener(onLongClickListener);
        picasso.load(icon)
                .resizeDimen(R.dimen.search_icon_size, R.dimen.search_icon_size)
                .centerInside()
                .error(R.drawable.ic_action_search)
                .into(buttonGlobalSearch);
        ViewUtils.setPaddingStartDimen(searchEditText, R.dimen.search_bar_size);
    }

    public void setupSettings(OnClickListener onClickListener, OnLongClickListener onLongClickListener) {
        buttonSettings.setOnClickListener(onClickListener);
        buttonSettings.setOnLongClickListener(onLongClickListener);
    }

    public void setSearchBarSizeDimen(@DimenRes int size) {
        setSearchBarSize(getResources().getDimensionPixelSize(size));
    }

    public void setSearchBarSize(int size) {
        if (getMinimumHeight() != size) {
            setMinimumHeight(size);
            float density = getResources().getDisplayMetrics().density;
            float textSize = (size / density) / TEXT_SIZE_FACTOR;
            searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }
    }

    public void hidePopup() {
        if (searchEditText.isPopupShowing()) {
            searchEditText.dismissDropDown();
        }
    }

    public void hideGlobalSearch() {
        buttonGlobalSearch.setVisibility(GONE);
        buttonGlobalSearch.setOnClickListener(null);
        buttonGlobalSearch.setOnLongClickListener(null);
        ViewUtils.setPaddingStartDimen(searchEditText, R.dimen.search_padding_start);
    }

    public boolean isGlobalSearchVisible() {
        return buttonGlobalSearch.getVisibility() == VISIBLE;
    }

    public void hideSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void onFireSearch(Match match) {
        if (listener == null) {
            return;
        }
        if (searchEditText.getText().length() > 0) {
            if (!searchEditText.isPopupShowing()) {
                searchEditText.showDropDown();
                return;
            }
            if (match == null) {
                int count = searchAdapter.getCount();
                if (count > 0) {
                    match = searchAdapter.getItem(0);
                }
            }
            if (match != null) {
                listener.handleIntent(match.getIntent());
            }
            searchEditText.setText("");
        }
        listener.onSearchFired();
    }

    public interface Listener {
        void handleIntent(Intent intent);

        void onSearchFired();

        void onSearchItemPinClick(Match match);

        void onSearchItemInfoClick(Match match);
    }
}
