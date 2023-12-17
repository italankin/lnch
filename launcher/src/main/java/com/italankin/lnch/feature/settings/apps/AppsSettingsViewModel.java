package com.italankin.lnch.feature.settings.apps;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppsSettingsViewModel extends AppViewModel {

    private final DescriptorRepository descriptorRepository;
    private final BehaviorSubject<List<AppDescriptorUi>> appsSubject = BehaviorSubject.create();

    @Inject
    AppsSettingsViewModel(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;

        observeApps();
    }

    Observable<List<AppDescriptorUi>> appsEvents() {
        return appsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void toggleAppVisibility(AppDescriptorUi item) {
        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(item.getDescriptor(), !item.isIgnored()))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }

    private void observeApps() {
        descriptorRepository.observe(true)
                .map(descriptors -> {
                    ArrayList<AppDescriptorUi> list = new ArrayList<>();
                    for (Descriptor descriptor : descriptors) {
                        if (descriptor instanceof AppDescriptor) {
                            list.add(new AppDescriptorUi((AppDescriptor) descriptor));
                        }
                    }
                    Collections.sort(list, (lhs, rhs) -> {
                        return String.CASE_INSENSITIVE_ORDER.compare(lhs.getVisibleLabel(), rhs.getVisibleLabel());
                    });
                    return list;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(new State<List<AppDescriptorUi>>() {
                    @Override
                    public void onNext(List<AppDescriptorUi> apps) {
                        appsSubject.onNext(apps);
                    }

                    @Override
                    public void onError(Throwable e) {
                        appsSubject.onError(e);
                    }
                });
    }
}
