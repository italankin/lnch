package com.italankin.lnch.feature.home.apps.folder;

import android.content.Intent;
import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.descriptor.actions.*;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.util.ListUtils;

import javax.inject.Inject;
import java.util.Iterator;

@InjectViewState
public class EditFolderPresenter extends BaseFolderPresenter<EditFolderView> {

    @Inject
    EditFolderPresenter(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences, FontManager fontManager) {
        super(homeDescriptorsState, descriptorRepository, preferences, fontManager);
    }

    void startEditIntent(String descriptorId) {
        HomeEntry<IntentDescriptorUi> entry = homeDescriptorsState.find(IntentDescriptorUi.class, descriptorId);
        if (entry == null) {
            return;
        }
        getViewState().onEditIntent(entry.item);
    }

    void showRenameDialog(String descriptorId) {
        int position = findDescriptorIndex(descriptorId);
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

    void showSetColorDialog(String descriptorId) {
        int position = findDescriptorIndex(descriptorId);
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

    void editIntent(String descriptorId, Intent intent) {
        descriptorRepository.edit()
                .enqueue(new EditIntentAction(descriptorId, intent));
        for (int i = 0; i < items.size(); i++) {
            DescriptorUi item = items.get(i);
            if (item.getDescriptor().getId().equals(descriptorId)) {
                IntentDescriptorUi intentItem = (IntentDescriptorUi) item;
                intentItem.intent = intent;
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

    void removeFromFolder(String descriptorId, String folderId, boolean moveToDesktop) {
        if (!folder.getDescriptor().getId().equals(folderId)) {
            return;
        }
        descriptorRepository.edit()
                .enqueue(new RemoveFromFolderAction(folderId, descriptorId, moveToDesktop));
        removeFromFolder(descriptorId);
        if (moveToDesktop) {
            HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, descriptorId);
            if (entry != null) {
                entry.item.setIgnored(false);
                homeDescriptorsState.updateItem(entry.item);
            }
        }
        if (items.isEmpty()) {
            items.add(new EmptyFolderDescriptorUi());
        }
        getViewState().onFolderUpdated(items);
    }

    void removeItem(String descriptorId) {
        int position = findDescriptorIndex(descriptorId);
        if (position == -1) {
            return;
        }
        descriptorRepository.edit()
                .enqueue(new RemoveAction(descriptorId));
        homeDescriptorsState.removeById(descriptorId);
        removeFromFolder(descriptorId);
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

    private int findDescriptorIndex(String descriptorId) {
        for (int i = 0, s = items.size(); i < s; i++) {
            DescriptorUi item = items.get(i);
            if (item.getDescriptor().getId().equals(descriptorId)) {
                return i;
            }
        }
        return -1;
    }
}
