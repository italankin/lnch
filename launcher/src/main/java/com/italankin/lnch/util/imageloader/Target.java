package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public interface Target {

    default void onPrepareLoad(@Nullable Drawable placeholder) {
    }

    default void onImageLoaded(Drawable drawable) {
    }

    default void onImageFailed(Exception e, @Nullable Drawable placeholder) {
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
        if (placeholder != null) {
            target.setImageDrawable(placeholder);
        }
    }

    @Override
    public void onPrepareLoad(@Nullable Drawable placeholder) {
        if (placeholder != null) {
            target.setImageDrawable(placeholder);
        }
    }
}
