package com.italankin.lnch.util.picasso;

import android.content.Context;
import android.net.Uri;

import com.italankin.lnch.BuildConfig;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class PicassoFactory implements Picasso.Listener {

    private final PackageManagerRequestHandler requestHandler;
    private final LruCache memoryCache;

    public PicassoFactory(Context context) {
        requestHandler = new PackageManagerRequestHandler(context);
        memoryCache = new LruCache(context);
    }

    public Picasso create(Context context) {
        if (context == context.getApplicationContext()) {
            throw new IllegalArgumentException();
        }
        Picasso.Builder builder = new Picasso.Builder(context);
        if (BuildConfig.DEBUG) {
            builder.listener(this);
        }
        builder.addRequestHandler(requestHandler);
        builder.memoryCache(memoryCache);
        return builder.build();
    }

    @Override
    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
        Timber.tag("Picasso").e(exception, "Error while loading '%s'", uri);
    }
}
