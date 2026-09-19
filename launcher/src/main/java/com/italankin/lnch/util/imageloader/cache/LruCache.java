package com.italankin.lnch.util.imageloader.cache;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LruCache implements Cache {

    private final androidx.collection.LruCache<Uri, Drawable.ConstantState> cache;

    public LruCache(int maxSize) {
        cache = new androidx.collection.LruCache<>(maxSize);
    }

    @Override
    public void put(Uri uri, @NonNull Drawable drawable) {
        Drawable.ConstantState constantState = drawable.getConstantState();
        if (constantState != null) {
            cache.put(uri, constantState);
        }
    }

    @Nullable
    @Override
    public Drawable get(Uri uri) {
        Drawable.ConstantState constantState = cache.get(uri);
        return constantState != null ? constantState.newDrawable().mutate() : null;
    }
}
