package com.italankin.lnch.util.imageloader;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.italankin.lnch.R;

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
    private final Object cacheKey;

    ImageViewTarget(ImageView target, @Nullable Object cacheKey) {
        this.target = target;
        this.cacheKey = cacheKey;
    }

    @Override
    public void onImageLoaded(Drawable drawable) {
        target.setImageDrawable(drawable);
        target.setTag(R.id.image_loader_cache_key, cacheKey);
    }

    @Override
    public void onImageFailed(Exception e, @Nullable Drawable placeholder) {
        target.setImageDrawable(placeholder);
        target.setTag(R.id.image_loader_cache_key, null);
    }

    @Override
    public void onPrepareLoad(@Nullable Drawable placeholder) {
        if (placeholder != null) {
            target.setImageDrawable(placeholder);
        }
    }
}
