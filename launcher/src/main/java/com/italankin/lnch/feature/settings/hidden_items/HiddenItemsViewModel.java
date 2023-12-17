package com.italankin.lnch.feature.settings.hidden_items;

import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HiddenItemsViewModel extends AppViewModel {

    private final DescriptorRepository descriptorRepository;
    private final BehaviorSubject<List<HiddenItem>> hiddenItemsSubject = BehaviorSubject.create();

    @Inject
    public HiddenItemsViewModel(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;

        observeApps();
    }

    Observable<List<HiddenItem>> hiddenItemsEvents() {
        return hiddenItemsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void observeApps() {
        descriptorRepository.observe(true)
                .map(descriptors -> {
                    ArrayList<HiddenItem> list = new ArrayList<>();
                    for (Descriptor descriptor : descriptors) {
                        if (descriptor instanceof IgnorableDescriptor) {
                            IgnorableDescriptor ignorableDescriptor = (IgnorableDescriptor) descriptor;
                            if (ignorableDescriptor.isIgnored()) {
                                list.add(new HiddenItem(ignorableDescriptor));
                            }
                        }
                    }
                    Collections.sort(list, (lhs, rhs) -> {
                        return lhs.originalLabel.compareTo(rhs.originalLabel);
                    });
                    return list;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(new State<List<HiddenItem>>() {
                    @Override
                    public void onNext(List<HiddenItem> items) {
                        hiddenItemsSubject.onNext(items);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hiddenItemsSubject.onError(e);
                    }
                });
    }

    void showItem(IgnorableDescriptor descriptor) {
        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(descriptor, false))
                .commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }
}
