package com.italankin.lnch.feature.settings.backup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class BackupFileNameProvider {
    private static final String BACKUP_FILE_FORMAT = "lnch-backup-%s.json";
    private static final String DATE_FORMAT = "yyyy_MM_dd-hh_mm_ss";

    static String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String date = dateFormat.format(new Date());
        return String.format(Locale.getDefault(), BACKUP_FILE_FORMAT, date);
    }

    private BackupFileNameProvider() {
        // no instance
    }
}
