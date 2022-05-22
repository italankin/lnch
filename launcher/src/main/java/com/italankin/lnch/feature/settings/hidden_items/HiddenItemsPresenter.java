package com.italankin.lnch.feature.settings.hidden_items;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import com.italankin.lnch.util.DescriptorUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class HiddenItemsPresenter extends AppPresenter<HiddenItemsView> {

    private final DescriptorRepository descriptorRepository;

    @Inject
    public HiddenItemsPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
    }


    @Override
    protected void onFirstViewAttach() {
        observeApps();
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

    void observeApps() {
        getViewState().showLoading();
        descriptorRepository.observe()
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
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<HiddenItem>>() {
                    @Override
                    protected void onNext(HiddenItemsView viewState, List<HiddenItem> items) {
                        viewState.onItemsUpdated(items);
                    }

                    @Override
                    protected void onError(HiddenItemsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
