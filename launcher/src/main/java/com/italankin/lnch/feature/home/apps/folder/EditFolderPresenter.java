package com.italankin.lnch.feature.home.apps.folder;

import android.content.Intent;

import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.model.descriptor.DescriptorArg;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.EditIntentAction;
import com.italankin.lnch.model.repository.descriptor.actions.FolderMoveAction;
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

import java.util.Iterator;

import javax.inject.Inject;

@InjectViewState
public class EditFolderPresenter extends BaseFolderPresenter<EditFolderView> {

    @Inject
    EditFolderPresenter(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences) {
        super(homeDescriptorsState, descriptorRepository, preferences);
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
        homeDescriptorsState.updateItem(item);
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
        homeDescriptorsState.updateItem(item);
        getViewState().onItemChanged(position);
    }

    void editIntent(String descriptorId, Intent intent, String label) {
        descriptorRepository.edit()
                .enqueue(new EditIntentAction(descriptorId, intent, label));
        for (int i = 0; i < items.size(); i++) {
            DescriptorUi item = items.get(i);
            if (item.getDescriptor().getId().equals(descriptorId)) {
                IntentDescriptorUi ui = (IntentDescriptorUi) item;
                ui.setCustomLabel(label);
                homeDescriptorsState.updateItem(ui);
                getViewState().onItemChanged(i);
                break;
            }
        }
    }

    void moveItem(int from, int to) {
        descriptorRepository.edit()
                .enqueue(new FolderMoveAction(folder.getDescriptor(), from, to));
        ListUtils.move(folder.items, from, to);
        ListUtils.move(items, from, to);
        getViewState().onFolderItemMove(from, to);
    }

    void removeFromFolder(String descriptorId, String folderId) {
        if (!folder.getDescriptor().getId().equals(folderId)) {
            return;
        }
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folderId, descriptorId));
        removeFromFolder(descriptorId);
        if (items.isEmpty()) {
            items.add(new EmptyFolderDescriptorUi());
        }
        getViewState().onFolderUpdated(items);
    }

    void removeItem(DescriptorArg arg) {
        int position = findDescriptorIndex(arg);
        if (position == -1) {
            return;
        }
        descriptorRepository.edit()
                .enqueue(new RemoveAction(arg.id));
        homeDescriptorsState.removeByArg(arg);
        removeFromFolder(arg.id);
        getViewState().onFolderUpdated(items);
    }

    private void removeFromFolder(String descriptorId) {
        for (Iterator<DescriptorUi> i = items.iterator(); i.hasNext(); ) {
            if (i.next().getDescriptor().getId().equals(descriptorId)) {
                i.remove();
                folder.items.remove(descriptorId);
                break;
            }
        }
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
}
