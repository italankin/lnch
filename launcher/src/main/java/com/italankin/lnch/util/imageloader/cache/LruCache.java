package com.italankin.lnch.util.imageloader.cache;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LruCache implements Cache {

    private final androidx.collection.LruCache<Uri, Drawable> cache;

    public LruCache(int maxSize) {
        cache = new androidx.collection.LruCache<>(maxSize);
    }

    @Override
    public void put(Uri uri, @NonNull Drawable drawable) {
        cache.put(uri, drawable);
    }

    @Nullable
    @Override
    public Drawable get(Uri uri) {
        return cache.get(uri);
    }
}
