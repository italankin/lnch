package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;

import java.util.List;

public class RemoveFromFolderAction extends BaseAction {
    private final String folderId;
    private final String itemId;

    public RemoveFromFolderAction(String folderId, String itemId) {
        this.folderId = folderId;
        this.itemId = itemId;
    }

    @Override
    public void apply(List<Descriptor> items) {
        FolderDescriptor descriptor = findById(items, folderId);
        if (descriptor != null) {
            descriptor.items.remove(itemId);
        }
    }
}
