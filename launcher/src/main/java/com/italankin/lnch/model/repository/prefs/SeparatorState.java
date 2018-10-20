package com.italankin.lnch.model.repository.prefs;

public interface SeparatorState {

    void setExanded(String id, boolean expanded);

    boolean isExpanded(String id);

    void remove(String id);
}
