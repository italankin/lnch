package com.italankin.lnch.feature.settings.apps.details.aliases;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetAliasesAction;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@InjectViewState
public class AppAliasesPresenter extends AppPresenter<AppAliasesView> {

    private final DescriptorRepository descriptorRepository;

    private List<String> aliases = new ArrayList<>(1);
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
                        aliases = new ArrayList<>(descriptor.aliases);
                        viewState.onAliasesChanged(aliases, aliases.size() < AliasDescriptor.MAX_ALIASES);
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
        aliases.add(trimmed.toLowerCase(Locale.getDefault()));
        int size = aliases.size();
        getViewState().notifyAliasAdded(size, size < AliasDescriptor.MAX_ALIASES);

        updateAliases();
    }

    void deleteAlias(int position) {
        aliases.remove(position);
        int size = aliases.size();
        getViewState().notifyAliasRemoved(size, size < AliasDescriptor.MAX_ALIASES);

        updateAliases();
    }

    private void updateAliases() {
        List<String> newAliases = new ArrayList<>(aliases);
        descriptorRepository.edit()
                .enqueue(new SetAliasesAction(descriptor.getId(), newAliases))
                .commit()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Updated descriptor=%s: aliases=%s", descriptor, newAliases);
                    }
                });
    }
}
