package com.italankin.lnch.feature.home.apps.folder;

import android.content.Intent;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorArg;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.BaseAction;
import com.italankin.lnch.model.repository.descriptor.actions.EditIntentAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveAction;
import com.italankin.lnch.model.repository.descriptor.actions.RemoveFromFolderAction;
import com.italankin.lnch.model.repository.descriptor.actions.RenameAction;
import com.italankin.lnch.model.repository.descriptor.actions.SetColorAction;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.util.ListUtils;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@InjectViewState
public class EditFolderPresenter extends BaseFolderPresenter<EditFolderView> {

    @Inject
    EditFolderPresenter(DescriptorRepository descriptorRepository, Preferences preferences) {
        super(descriptorRepository, preferences);
    }

    void showRenameDialog(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        CustomLabelDescriptorUi item = (CustomLabelDescriptorUi) items.get(position);
        getViewState().onShowRenameDialog(position, item);
    }

    void renameItem(int position, CustomLabelDescriptorUi item, String newLabel) {
        descriptorRepository.edit()
                .enqueue(new RenameAction(item.getDescriptor(), newLabel));
        item.setCustomLabel(newLabel);
        getViewState().onItemChanged(position);
    }

    void showSetColorDialog(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        CustomColorDescriptorUi item = (CustomColorDescriptorUi) items.get(position);
        getViewState().onShowSetColorDialog(position, item);
    }

    void changeItemCustomColor(int position, CustomColorDescriptorUi item, Integer color) {
        descriptorRepository.edit()
                .enqueue(new SetColorAction(item.getDescriptor(), color));
        item.setCustomColor(color);
        getViewState().onItemChanged(position);
    }

    void removeFromFolder(String descriptorId, String folderId) {
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folderId, descriptorId));
        removeItemFromFolder(descriptorId);
    }

    void editIntent(String descriptorId, Intent intent, String label) {
        descriptorRepository.edit()
                .enqueue(new EditIntentAction(descriptorId, intent, label));
        for (int i = 0; i < items.size(); i++) {
            DescriptorUi item = items.get(i);
            if (item.getDescriptor().getId().equals(descriptorId)) {
                IntentDescriptorUi ui = (IntentDescriptorUi) item;
                ui.setCustomLabel(label);
                getViewState().onItemChanged(i);
                break;
            }
        }
    }

    void removeItem(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        descriptorRepository.edit()
                .enqueue(new RemoveAction(arg.id));
        removeItemFromFolder(arg.id);
    }

    void swapApps(int from, int to) {
        descriptorRepository.edit()
                .enqueue(new FolderSwapAction(folder, from, to));
        ListUtils.swap(items, from, to);
        getViewState().onItemsSwap(from, to);
    }

    private int findDescriptorIndex(DescriptorArg arg) {
        for (int i = 0, s = items.size(); i < s; i++) {
            DescriptorUi item = items.get(i);
            if (arg.is(item.getDescriptor())) {
                return i;
            }
        }
        return -1;
    }

    private static class FolderSwapAction extends BaseAction {

        private final String folderId;
        private final int from;
        private final int to;

        private FolderSwapAction(FolderDescriptor descriptor, int from, int to) {
            this.folderId = descriptor.getId();
            this.from = from;
            this.to = to;
        }

        @Override
        public void apply(List<Descriptor> items) {
            FolderDescriptor folder = findById(items, folderId);
            if (folder != null) {
                ListUtils.swap(folder.items, from, to);
            }
        }
    }
}
