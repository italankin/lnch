package com.italankin.lnch.model.descriptor;

import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.search.Searchable;

import java.util.Set;

/**
 * Base interface for all items which are displayed on home page
 */
public interface Descriptor extends Searchable {

    /**
     * @return unique identifier for this descriptor
     */
    String getId();

    String getOriginalLabel();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    MutableDescriptor<?> toMutable();

    @Override
    default Set<String> getSearchTokens() {
        return DescriptorUtils.createSearchTokens(this);
    }
}
