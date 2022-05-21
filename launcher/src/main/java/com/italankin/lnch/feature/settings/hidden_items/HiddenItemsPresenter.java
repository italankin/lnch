package com.italankin.lnch.feature.settings.hidden_items;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

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

    void showItem(IgnorableDescriptorUi item) {
        descriptorRepository.edit()
                .enqueue(new SetIgnoreAction(item.getDescriptor(), false))
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
                    ArrayList<IgnorableDescriptorUi> list = new ArrayList<>();
                    for (Descriptor descriptor : descriptors) {
                        if (descriptor instanceof IgnorableDescriptor &&
                                ((IgnorableDescriptor) descriptor).isIgnored()) {
                            list.add((IgnorableDescriptorUi) DescriptorUiFactory.createItem(descriptor));
                        }
                    }
                    return list;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new State<List<IgnorableDescriptorUi>>() {
                    @Override
                    protected void onNext(HiddenItemsView viewState, List<IgnorableDescriptorUi> items) {
                        viewState.onItemsUpdated(items);
                    }

                    @Override
                    protected void onError(HiddenItemsView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
