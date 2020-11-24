package com.italankin.lnch.feature.settings.apps.list;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetIgnoreAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetSearchVisibilityAction;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class AppsListPresenter extends AppPresenter<AppsListView> {

    private final DescriptorRepository descriptorRepository;
    private final DescriptorRepository.Editor editor;

    private List<AppDescriptorUi> items = Collections.emptyList();

    @Inject
    AppsListPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
        this.editor = descriptorRepository.edit();
    }

    @Override
    protected void onFirstViewAttach() {
        loadApps();
    }

    void toggleAppVisibility(int position, AppDescriptorUi item) {
        boolean ignored = !item.isIgnored();
        item.setIgnored(ignored);
        editor.enqueue(new SetIgnoreAction(item.getDescriptor().getId(), !ignored));
        getViewState().onItemChanged(position);
    }

    void setAppSettings(String id, boolean searchVisible, boolean shortcutsSearchVisible) {
        for (AppDescriptorUi item : items) {
            AppDescriptor descriptor = item.getDescriptor();
            if (descriptor.getId().equals(id)) {
                item.setSearchVisible(searchVisible);
                item.setShortcutsSearchVisible(shortcutsSearchVisible);
                editor.enqueue(new SetSearchVisibilityAction(descriptor.getId(), searchVisible, shortcutsSearchVisible));
                break;
            }
        }
    }

    void resetAppSettings(String id) {
        setAppSettings(id, true, true);
    }

    void saveChanges() {
        editor.commit()
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        Timber.d("Changes saved");
                    }
                });
    }

    void loadApps() {
        getViewState().showLoading();
        Single
                .fromCallable(() -> {
                    List<AppDescriptor> appDescriptors = descriptorRepository.itemsOfType(AppDescriptor.class);
                    ArrayList<AppDescriptorUi> result = new ArrayList<>(appDescriptors.size());
                    for (AppDescriptor appDescriptor : appDescriptors) {
                        result.add(new AppDescriptorUi(appDescriptor));
                    }
                    Collections.sort(result, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER
                            .compare(lhs.getVisibleLabel(), rhs.getVisibleLabel()));
                    return result;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<AppDescriptorUi>>() {
                    @Override
                    protected void onSuccess(AppsListView viewState, List<AppDescriptorUi> apps) {
                        items = apps;
                        viewState.onAppsLoaded(apps);
                    }

                    @Override
                    protected void onError(AppsListView viewState, Throwable e) {
                        viewState.showError(e);
                    }
                });
    }
}
