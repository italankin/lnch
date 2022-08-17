package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

public interface Target {

    default void onPrepareLoad(@Nullable Drawable placeholder) {
    }

    default void onImageLoaded(Drawable drawable) {
    }

    default void onImageFailed(Exception e, @Nullable Drawable placeholder) {
    }
}

class WeakTarget implements Target {
    private final WeakReference<Target> ref;

    WeakTarget(Target target) {
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

class ImageViewTarget implements Target {

    private final ImageView target;

    ImageViewTarget(ImageView target) {
        this.target = target;
    }

    @Override
    public void onImageLoaded(Drawable drawable) {
        target.setImageDrawable(drawable);
    }

    @Override
    public void onImageFailed(Exception e, @Nullable Drawable placeholder) {
        target.setImageDrawable(placeholder);
    }

    @Override
    public void onPrepareLoad(@Nullable Drawable placeholder) {
        target.setImageDrawable(placeholder);
    }
}
