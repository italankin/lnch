package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.os.Build;

import com.italankin.lnch.BuildConfig;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class PicassoFactory {
    private final LruCache memoryCache;

    public PicassoFactory(Context context) {
        memoryCache = new LruCache(context);
    }

    public Picasso create(Context context) {
        if (context == context.getApplicationContext()) {
            throw new IllegalArgumentException();
        }
        Picasso.Builder builder = new Picasso.Builder(context);
        if (BuildConfig.DEBUG) {
            builder.listener((picasso, uri, exception) -> {
                Timber.tag("Picasso").e(exception, "Error while loading '%s'", uri);
            });
        }
        builder.addRequestHandler(new PackageIconHandler(context));
        builder.addRequestHandler(new PackageResourceHandler(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            builder.addRequestHandler(new ShortcutIconHandler(context));
        }
        builder.memoryCache(memoryCache);
        return builder.build();
    }
}
