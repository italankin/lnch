package com.italankin.lnch.util.imageloader.resourceloader;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public interface ResourceLoader {

    boolean handles(Uri uri);

    Drawable load(Uri uri);
}
