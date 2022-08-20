package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

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

    static class WeakTarget implements Target {
        private final WeakReference<Target> ref;

        private WeakTarget(Target target) {
            this.ref = new WeakReference<>(target);
        }

        @Override
        public void onImageLoaded(Drawable drawable) {
            Target target = ref.get();
            if (target != null) {
                target.onImageLoaded(drawable);
            }
        }

        @Override
        public void onImageFailed(Exception e, @Nullable Drawable placeholder) {
            Target target = ref.get();
            if (target != null) {
                target.onImageFailed(e, placeholder);
            }
        }

        @Override
        public void onPrepareLoad(@Nullable Drawable placeholder) {
            Target target = ref.get();
            if (target != null) {
                target.onPrepareLoad(placeholder);
            }
        }

        public boolean isDead() {
            return ref.get() == null;
        }
    }
}

