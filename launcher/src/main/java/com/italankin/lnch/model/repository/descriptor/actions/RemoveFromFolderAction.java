package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.IgnorableDescriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;

import java.util.List;

public class RemoveFromFolderAction extends BaseAction {
    private final String folderId;
    private final String itemId;
    private final boolean moveToDesktop;

    public RemoveFromFolderAction(String folderId, String itemId, boolean moveToDesktop) {
        this.folderId = folderId;
        this.itemId = itemId;
        this.moveToDesktop = moveToDesktop;
    }

    public RemoveFromFolderAction(String folderId, String itemId) {
        this(folderId, itemId, false);
    }

    @Override
    public void apply(List<Descriptor> items) {
        FolderDescriptor descriptor = findById(items, folderId);
        if (descriptor != null) {
            descriptor.items.remove(itemId);
            if (moveToDesktop) {
                IgnorableDescriptor item = findById(items, itemId);
                if (item != null) {
                    item.setIgnored(false);
                }
            }
        }
    }
}
