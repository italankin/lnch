package com.italankin.lnch.util.imageloader.resourceloader;

import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;

public interface ResourceLoader {

    boolean handles(Uri uri);

    @NonNull
    Drawable load(Uri uri);
}
