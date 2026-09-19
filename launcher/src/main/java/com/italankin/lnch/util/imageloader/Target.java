package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public interface Target {

    default void onPrepareLoad(@Nullable Drawable placeholder) {
    }

    default void onImageLoaded(Drawable drawable) {
    }

    default void onImageFailed(Exception e, @Nullable Drawable placeholder) {
    }

    default boolean isDead() {
        return false;
    }
}

class ImageViewTarget implements Target {

    private final WeakReference<ImageView> targetRef;

    ImageViewTarget(ImageView target) {
        targetRef = new WeakReference<>(target);
    }

    @Override
    public void onImageLoaded(Drawable drawable) {
        ImageView target = targetRef.get();
        if (target != null) {
            target.setImageDrawable(drawable);
        }
    }

    @Override
    public void onImageFailed(Exception e, @Nullable Drawable placeholder) {
        ImageView target = targetRef.get();
        if (target != null) {
            target.setImageDrawable(placeholder);
        }
    }

    @Override
    public void onPrepareLoad(@Nullable Drawable placeholder) {
        ImageView target = targetRef.get();
        if (target != null && placeholder != null) {
            target.setImageDrawable(placeholder);
        }
    }

    @Override
    public boolean isDead() {
        return targetRef.get() == null;
    }
}
