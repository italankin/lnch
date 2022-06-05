package com.italankin.lnch.feature.settings.preferencesearch;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

interface PreferenceSearchView extends MvpView {

    @StateStrategyType(SingleStateStrategy.class)
    void onSearchResults(List<PreferenceSearchItem> items);
}
