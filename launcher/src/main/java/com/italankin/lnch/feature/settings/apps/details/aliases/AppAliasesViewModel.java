package com.italankin.lnch.feature.settings.apps.details.aliases;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetAliasesAction;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppAliasesViewModel extends AppViewModel {

    private final DescriptorRepository descriptorRepository;
    private final BehaviorSubject<List<String>> aliasesSubject = BehaviorSubject.create();

    private List<String> aliases = new ArrayList<>(1);
    private AppDescriptor descriptor;

    @Inject
    AppAliasesViewModel(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }

    Observable<List<String>> aliasesEvents() {
        return aliasesSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void loadAliases(String descriptorId) {
        Single.fromCallable(() -> descriptorRepository.findById(AppDescriptor.class, descriptorId))
                .subscribeOn(Schedulers.computation())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(AppDescriptor descriptor) {
                        AppAliasesViewModel.this.descriptor = descriptor;
                        aliases = new ArrayList<>(descriptor.aliases);
                        aliasesSubject.onNext(aliases);
                    }

                    @Override
                    public void onError(Throwable e) {
                        aliasesSubject.onError(e);
                    }
                });
    }

    void addAlias(String alias) {
        String trimmed = alias.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        aliases.add(trimmed.toLowerCase(Locale.getDefault()));
        aliasesSubject.onNext(aliases);

        updateAliases();
    }

    void deleteAlias(int position) {
        aliases.remove(position);
        aliasesSubject.onNext(aliases);

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
