package com.italankin.lnch.di.service;

import android.content.Context;

import com.italankin.lnch.di.component.DaggerMainComponent;
import com.italankin.lnch.di.component.DaggerPresenterComponent;
import com.italankin.lnch.di.component.MainComponent;
import com.italankin.lnch.di.component.PresenterComponent;

public class DaggerService {
    private final PresenterComponent presenters;
    private final MainComponent main;

    public DaggerService(Context context) {
        main = DaggerMainComponent.builder()
                .context(context)
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
