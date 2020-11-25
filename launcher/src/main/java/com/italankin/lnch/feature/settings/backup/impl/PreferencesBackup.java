package com.italankin.lnch.feature.settings.backup.impl;

import java.util.Map;

public interface PreferencesBackup {

    Map<String, Object> read();

    void write(Map<String, ?> map);
}
