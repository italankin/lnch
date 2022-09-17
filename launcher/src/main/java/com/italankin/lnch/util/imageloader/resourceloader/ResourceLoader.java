package com.italankin.lnch.util.imageloader.resourceloader;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ResourceLoader {

    boolean handles(Uri uri);

    @Nullable
    Drawable load(Uri uri);
}
