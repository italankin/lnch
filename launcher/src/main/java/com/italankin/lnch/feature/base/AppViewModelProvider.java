package com.italankin.lnch.feature.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.component.PresenterComponent;

public final class AppViewModelProvider {

    public static <VM extends ViewModel> VM get(ViewModelStoreOwner owner, Class<? extends VM> modelClass, Factory<VM> factory) {
        return new ViewModelProvider(owner, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                //noinspection unchecked
                return (T) factory.get(LauncherApp.daggerService.presenters());
            }
        }).get(modelClass);
    }

    public interface Factory<T> {
        T get(PresenterComponent component);
    }

    private AppViewModelProvider() {
    }
}
