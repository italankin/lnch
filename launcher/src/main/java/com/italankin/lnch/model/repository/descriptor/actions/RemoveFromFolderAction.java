package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.mutable.IgnorableMutableDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

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
    public void apply(List<MutableDescriptor<?>> items) {
        FolderDescriptor.Mutable descriptor = findById(items, folderId);
        if (descriptor != null) {
            descriptor.removeItem(itemId);
            if (moveToDesktop) {
                IgnorableMutableDescriptor<?> item = findById(items, itemId);
                if (item != null) {
                    item.setIgnored(false);
                }
            }
        }
    }
}
