package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.net.Uri;

class Request {
    final Uri uri;
    final WeakTarget target;
    final Drawable errorPlaceholder;
    final Callback callback;
    final boolean noCache;

    Request(Uri uri, Target target, Drawable errorPlaceholder, Callback callback, boolean noCache) {
        this.uri = uri;
        this.target = (target instanceof WeakTarget) ? (WeakTarget) target : new WeakTarget(target);
        this.errorPlaceholder = errorPlaceholder;
        this.callback = callback;
        this.noCache = noCache;
    }
}

