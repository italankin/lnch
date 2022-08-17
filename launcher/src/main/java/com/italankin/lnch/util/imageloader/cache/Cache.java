package com.italankin.lnch.util.imageloader.cache;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Cache {

    void put(Uri uri, @NonNull Drawable drawable);

    @Nullable
    Drawable get(Uri uri);

    Cache NO_OP = new Cache() {
        @Override
        public void put(Uri uri, @NonNull Drawable drawable) {
        }

        @Nullable
        @Override
        public Drawable get(Uri uri) {
            return null;
        }
    };
}
