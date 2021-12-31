package com.italankin.lnch.feature.home.apps.folder.empty;

import com.italankin.lnch.model.descriptor.Descriptor;

import androidx.annotation.Nullable;

/**
 * Fake descriptor for showing empty folder state
 */
class EmptyFolderDescriptor implements Descriptor {

    static final EmptyFolderDescriptor INSTANCE = new EmptyFolderDescriptor();

    private EmptyFolderDescriptor() {
    }

    @Override
    public String getId() {
        return "empty-folder/";
    }

    @Override
    public String getOriginalLabel() {
        return "";
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj == this;
    }

    @Override
    public Descriptor copy() {
        return EmptyFolderDescriptor.INSTANCE;
    }
}
