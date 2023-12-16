package com.italankin.lnch.model.backup;

import java.util.Map;

public interface PreferencesBackup {

    /**
     * @return a dump of current preferences' values
     */
    Map<String, Object> read();

    /**
     * Override preferences' values with a values from the {@code map}
     *
     * @param map a map of preference keys and their values
     */
    void write(Map<String, ?> map);
}
