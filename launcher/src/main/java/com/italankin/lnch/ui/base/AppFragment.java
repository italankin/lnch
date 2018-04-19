package com.italankin.lnch.ui.base;

import android.support.v4.app.Fragment;

import com.italankin.lnch.App;
import com.italankin.lnch.di.service.DaggerService;

public abstract class AppFragment extends Fragment {
    protected DaggerService daggerService() {
        return ((App) getContext().getApplicationContext()).daggerService;
    }
}
