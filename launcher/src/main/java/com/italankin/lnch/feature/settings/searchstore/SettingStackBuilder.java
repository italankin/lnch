package com.italankin.lnch.feature.settings.searchstore;

import java.util.List;

import androidx.fragment.app.Fragment;

public interface SettingStackBuilder {

    List<Fragment> createStack(String requestKey);
}
