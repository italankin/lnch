package com.italankin.lnch.feature.settings.apps.list;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.SetSearchVisibilityAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetVisibilityAction;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;

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

    private List<AppViewModel> items = Collections.emptyList();

    @Inject
    AppsListPresenter(DescriptorRepository descriptorRepository) {
        this.descriptorRepository = descriptorRepository;
        this.editor = descriptorRepository.edit();
    }

    @Override
    protected void onFirstViewAttach() {
        loadApps();
    }

    void toggleAppVisibility(int position, AppViewModel item) {
        boolean hidden = !item.isHidden();
        item.setHidden(hidden);
        editor.enqueue(new SetVisibilityAction(item.getDescriptor(), !hidden));
        getViewState().onItemChanged(position);
    }

    void setAppSettings(String id, boolean searchVisible, boolean shortcutsSearchVisible) {
        for (AppViewModel item : items) {
            AppDescriptor descriptor = item.getDescriptor();
            if (descriptor.getId().equals(id)) {
                item.setSearchVisible(searchVisible);
                item.setShortcutsSearchVisible(shortcutsSearchVisible);
                editor.enqueue(new SetSearchVisibilityAction(descriptor, searchVisible, shortcutsSearchVisible));
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
                    ArrayList<AppViewModel> result = new ArrayList<>(appDescriptors.size());
                    for (AppDescriptor appDescriptor : appDescriptors) {
                        result.add(new AppViewModel(appDescriptor));
                    }
                    Collections.sort(result, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER
                            .compare(lhs.getVisibleLabel(), rhs.getVisibleLabel()));
                    return result;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<List<AppViewModel>>() {
                    @Override
                    protected void onSuccess(AppsListView viewState, List<AppViewModel> apps) {
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
