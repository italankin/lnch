package com.italankin.lnch.feature.home.apps.folder;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

@InjectViewState
public class FolderPresenter extends AppPresenter<FolderView> {

    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    @Inject
    FolderPresenter(DescriptorRepository descriptorRepository, Preferences preferences) {
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    void loadFolder(String descriptorId) {
        Single
                .fromCallable(() -> {
                    return descriptorRepository.findById(GroupDescriptor.class, descriptorId);
                })
                .zipWith(descriptorsById(), (group, descriptorsById) -> {
                    List<DescriptorUi> items = new ArrayList<>(4);
                    for (String id : group.items) {
                        Descriptor item = descriptorsById.get(id);
                        if (item != null) {
                            items.add(DescriptorUiFactory.createItem(item));
                        }
                    }
                    Collections.sort(items, new AscLabelComparator());
                    return new FolderState(group, items, new UserPrefs(preferences));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<FolderState>() {
                    @Override
                    protected void onSuccess(FolderView viewState, FolderState folderState) {
                        viewState.onShowFolder(folderState.descriptor, folderState.items, folderState.userPrefs);
                    }

                    @Override
                    protected void onError(FolderView viewState, Throwable e) {
                        viewState.onError(e);
                    }
                });
    }

    private Single<Map<String, Descriptor>> descriptorsById() {
        return Single.fromCallable(() -> {
            List<Descriptor> items = descriptorRepository.items();
            HashMap<String, Descriptor> result = new HashMap<>(items.size());
            for (Descriptor item : items) {
                result.put(item.getId(), item);
            }
            return result;
        });
    }

    private static class FolderState {
        final GroupDescriptor descriptor;
        final List<DescriptorUi> items;
        final UserPrefs userPrefs;

        FolderState(GroupDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
            this.descriptor = descriptor;
            this.items = items;
            this.userPrefs = userPrefs;
        }
    }

    private static class AscLabelComparator implements Comparator<DescriptorUi> {
        @Override
        public int compare(DescriptorUi lhs, DescriptorUi rhs) {
            if (lhs instanceof CustomLabelDescriptorUi && rhs instanceof CustomLabelDescriptorUi) {
                String lhsLabel = ((CustomLabelDescriptorUi) lhs).getVisibleLabel();
                String rhsLabel = ((CustomLabelDescriptorUi) rhs).getVisibleLabel();
                return String.CASE_INSENSITIVE_ORDER.compare(lhsLabel, rhsLabel);
            } else {
                // should not happen
                return lhs.getDescriptor().getId().compareTo(rhs.getDescriptor().getId());
            }
        }
    }
}
