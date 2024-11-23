package com.italankin.lnch.feature.home.apps.folder;

import android.content.Intent;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUi;
import com.italankin.lnch.feature.home.repository.EditModeState;
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

public class EditFolderViewModel extends BaseFolderViewModel {

    private final EditModeState editModeState;

    @Inject
    EditFolderViewModel(HomeDescriptorsState homeDescriptorsState, DescriptorRepository descriptorRepository,
            Preferences preferences, FontManager fontManager, EditModeState editModeState) {
        super(homeDescriptorsState, descriptorRepository, preferences, fontManager);
        this.editModeState = editModeState;
    }

    void editIntent(String descriptorId, Intent intent) {
        editModeState.addAction(new EditIntentAction(descriptorId, intent));
        for (DescriptorUi item : items) {
            if (item.getDescriptor().getId().equals(descriptorId)) {
                IntentDescriptorUi intentItem = (IntentDescriptorUi) item;
                intentItem.intent = intent;
                break;
            }
        }
    }

    void moveItem(int from, int to) {
        editModeState.addAction(new FolderMoveAction(folder.getDescriptor(), from, to));
        ListUtils.move(folder.items, from, to);
        ListUtils.move(items, from, to);
    }

    void removeFromFolder(String descriptorId, String folderId, boolean moveToDesktop) {
        if (!folder.getDescriptor().getId().equals(folderId)) {
            return;
        }
        editModeState.addAction(new RemoveFromFolderAction(folderId, descriptorId, moveToDesktop));
        removeFromFolder(descriptorId);
        if (moveToDesktop) {
            HomeEntry<IgnorableDescriptorUi> entry = homeDescriptorsState.find(IgnorableDescriptorUi.class, descriptorId);
            if (entry != null) {
                entry.item.setIgnored(false);
                homeDescriptorsState.updateItem(entry.item);

                int last = homeDescriptorsState.items().size() - 1;
                editModeState.addAction(new MoveAction(entry.position, last));
                homeDescriptorsState.moveItem(entry.position, last);
            }
        }
        if (items.isEmpty()) {
            items.add(new EmptyFolderDescriptorUi());
        }
        folderUpdateEvents.onNext(items);
    }

    void renameItem(CustomLabelDescriptorUi item, String newLabel) {
        editModeState.addAction(new RenameAction(item.getDescriptor(), newLabel));
        item.setCustomLabel(newLabel);
        homeDescriptorsState.updateItem(item);
        folderUpdateEvents.onNext(items);
    }

    void setCustomColor(CustomColorDescriptorUi item, Integer newColor) {
        editModeState.addAction(new SetColorAction(item.getDescriptor(), newColor));
        item.setCustomColor(newColor);
        homeDescriptorsState.updateItem(item);
        folderUpdateEvents.onNext(items);
    }

    void removeItem(String descriptorId) {
        int position = findDescriptorIndex(descriptorId);
        if (position == -1) {
            return;
        }
        editModeState.addAction(new RemoveAction(descriptorId));
        homeDescriptorsState.remove(descriptorId);
        removeFromFolder(descriptorId);
        folderUpdateEvents.onNext(items);
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
