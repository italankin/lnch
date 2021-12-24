package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.util.ListUtils;

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
    public void apply(List<Descriptor> items) {
        FolderDescriptor folder = findById(items, folderId);
        if (folder != null) {
            ListUtils.move(folder.items, from, to);
        }
    }
}
