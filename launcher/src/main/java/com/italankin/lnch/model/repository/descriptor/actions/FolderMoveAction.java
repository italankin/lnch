package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.List;

public class FolderMoveAction extends BaseAction {
    private final String folderId;
    private final int from;
    private final int to;

    public FolderMoveAction(FolderDescriptor descriptor, int from, int to) {
        this.folderId = descriptor.getId();
        this.from = from;
        this.to = to;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        FolderDescriptor.Mutable folder = findById(items, folderId);
        if (folder != null) {
            folder.move(from, to);
        }
    }
}
