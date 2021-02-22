package com.italankin.lnch.model.repository.prefs;

/**
 * State of expanded/collapsed {@link com.italankin.lnch.model.descriptor.impl.GroupDescriptor}s
 */
public interface SeparatorState {

    void setExpanded(String id, boolean expanded);

    boolean isExpanded(String id);

    void remove(String id);
}
