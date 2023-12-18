package com.italankin.lnch.feature.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.component.ViewModelComponent;

public final class AppViewModelProvider {

    @SuppressWarnings("unchecked")
    public static <VM extends ViewModel> VM get(ViewModelStoreOwner owner, Class<? extends VM> modelClass, Factory<VM> factory) {
        return new ViewModelProvider(owner, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) factory.get(LauncherApp.daggerService.presenters());
            }
        }).get(modelClass);
    }

    public interface Factory<T> {
        T get(ViewModelComponent component);
    }

    private AppViewModelProvider() {
    }
}
