package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Intent;

import com.italankin.lnch.model.descriptor.Descriptor;

public interface SearchIntentStarterDelegate {

    void handleSearchIntent(Intent intent);

    void handleSearchIntent(Intent intent, Descriptor descriptor);
}
