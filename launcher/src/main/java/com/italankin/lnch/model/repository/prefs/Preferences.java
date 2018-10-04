package com.italankin.lnch.model.repository.prefs;

public interface Preferences {

    boolean searchShowSoftKeyboard();

    String homeLayout();

    void setOverlayColor(int color);

    int overlayColor();

    boolean useCustomTabs();

    boolean showScrollbar();
}
