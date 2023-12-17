package com.italankin.lnch.di.service;

import android.content.Context;
import com.italankin.lnch.di.component.DaggerMainComponent;
import com.italankin.lnch.di.component.DaggerViewModelComponent;
import com.italankin.lnch.di.component.MainComponent;
import com.italankin.lnch.di.component.ViewModelComponent;

public class DaggerService {
    private final ViewModelComponent viewModels;
    private final MainComponent main;

    public DaggerService(Context context) {
        main = DaggerMainComponent.builder()
                .context(context)
                .build();
        viewModels = DaggerViewModelComponent.builder()
                .mainComponent(main)
                .build();
    }

    public ViewModelComponent presenters() {
        return viewModels;
    }

    public MainComponent main() {
        return main;
    }
}
