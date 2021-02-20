package com.italankin.lnch.feature.settings.apps.details.aliases;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetAliasesAction;

import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class AppAliasesPresenter extends AppPresenter<AppAliasesView> {

    private final DescriptorRepository descriptorRepository;

    private AppDescriptor descriptor;

    @Inject
    AppAliasesPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    void loadAliases(String descriptorId) {
        Single.fromCallable(() -> descriptorRepository.findById(AppDescriptor.class, descriptorId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<AppDescriptor>() {
                    @Override
                    protected void onSuccess(AppAliasesView viewState, AppDescriptor descriptor) {
                        AppAliasesPresenter.this.descriptor = descriptor;
                        viewState.onAliasesChanged(descriptor.aliases, descriptor.aliases.size() < AliasDescriptor.MAX_ALIASES);
                    }

                    @Override
                    protected void onError(AppAliasesView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    void addAlias(String alias) {
        String trimmed = alias.trim();
        if (trimmed.isEmpty()) {
            getViewState().onInvalidAlias();
            return;
        }
        descriptor.aliases.add(trimmed.toLowerCase(Locale.getDefault()));
        int size = descriptor.aliases.size();
        getViewState().notifyAliasAdded(size, size < AliasDescriptor.MAX_ALIASES);

        updateAliases();
    }

    void deleteAlias(int position) {
        descriptor.aliases.remove(position);
        int size = descriptor.aliases.size();
        getViewState().notifyAliasRemoved(size, size < AliasDescriptor.MAX_ALIASES);

        updateAliases();
    }

    private void updateAliases() {
        descriptorRepository.edit()
                .enqueue(new SetAliasesAction(descriptor, descriptor.aliases))
                .commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Updated descriptor=%s: aliases=%s", descriptor, descriptor.aliases);
                    }
                });
    }
}
