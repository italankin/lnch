package com.italankin.lnch.model.repository.shortcuts;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

public interface Shortcut extends Comparable<Shortcut> {

    CharSequence getShortLabel();

    CharSequence getLongLabel();

    Uri getIconUri();

    String getPackageName();

    String getId();

    boolean isDynamic();

    boolean isEnabled();

    CharSequence getDisabledMessage();

    int getRank();

    boolean start(Rect bounds, Bundle options);

    @Override
    default int compareTo(@NonNull Shortcut that) {
        if (this.isDynamic() == that.isDynamic()) {
            return Integer.compare(this.getRank(), that.getRank());
        }
        return this.isDynamic() ? 1 : -1;
    }
}
