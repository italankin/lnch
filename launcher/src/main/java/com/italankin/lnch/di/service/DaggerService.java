package com.italankin.lnch.di.service;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.component.DaggerMainComponent;
import com.italankin.lnch.di.component.DaggerPresenterComponent;
import com.italankin.lnch.di.component.MainComponent;
import com.italankin.lnch.di.component.PresenterComponent;
import com.italankin.lnch.di.module.AppModule;
import com.italankin.lnch.di.module.MainModule;

public class DaggerService {
    private final PresenterComponent presenters;
    private final MainComponent main;

    public DaggerService(LauncherApp launcherApp) {
        main = DaggerMainComponent.builder()
                .mainModule(new MainModule())
                .appModule(new AppModule(launcherApp))
                .build();
        presenters = DaggerPresenterComponent.builder()
                .mainComponent(main)
                .build();
    }

    public PresenterComponent presenters() {
        return presenters;
    }

    public MainComponent main() {
        return main;
    }
}
