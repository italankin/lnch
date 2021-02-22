package com.italankin.lnch.model.descriptor;

import com.google.gson.annotations.JsonAdapter;
import com.italankin.lnch.model.repository.store.json.DescriptorJsonTypeAdapter;

/**
 * Base interface for all items which are displayed on home page
 */
@JsonAdapter(DescriptorJsonTypeAdapter.class)
public interface Descriptor {

    /**
     * @return unique identifier for this descriptor
     */
    String getId();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    /**
     * @return a deep copy of this descriptor
     */
    Descriptor copy();
}
