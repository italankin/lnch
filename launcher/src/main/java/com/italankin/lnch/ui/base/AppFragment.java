package com.italankin.lnch.ui.base;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.italankin.lnch.App;
import com.italankin.lnch.di.service.DaggerService;

public abstract class AppFragment extends MvpAppCompatFragment {
    protected DaggerService daggerService() {
        return ((App) getContext().getApplicationContext()).daggerService;
    }
}
