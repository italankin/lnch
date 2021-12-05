package com.italankin.lnch.feature.home.apps.delegate;

import androidx.annotation.StringRes;

public interface ErrorDelegate {

    void showError(@StringRes int message);

    void showError(CharSequence message);
}
