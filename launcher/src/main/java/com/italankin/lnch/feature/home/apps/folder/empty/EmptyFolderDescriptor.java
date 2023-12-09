package com.italankin.lnch.feature.home.apps.folder.empty;

import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;

import java.util.Collections;
import java.util.Set;

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
    public Set<String> getSearchTokens() {
        return Collections.emptySet();
    }

    @Override
    public MutableDescriptor<?> toMutable() {
        throw new UnsupportedOperationException();
    }
}
