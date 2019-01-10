package com.italankin.lnch.feature.base;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.service.DaggerService;

public abstract class AppFragment extends MvpAppCompatFragment {

    protected DaggerService daggerService() {
        return LauncherApp.daggerService;
    }
}
