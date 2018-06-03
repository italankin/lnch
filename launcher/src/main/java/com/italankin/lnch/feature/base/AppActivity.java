package com.italankin.lnch.feature.base;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.italankin.lnch.App;
import com.italankin.lnch.di.service.DaggerService;

public abstract class AppActivity extends MvpAppCompatActivity {
    protected DaggerService daggerService() {
        return ((App) getApplicationContext()).daggerService;
    }
}
