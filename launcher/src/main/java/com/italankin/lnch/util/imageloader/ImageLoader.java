package com.italankin.lnch.util.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.italankin.lnch.util.imageloader.cache.Cache;
import com.italankin.lnch.util.imageloader.resourceloader.ActivityIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.PackageIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.PackageResourceLoader;
import com.italankin.lnch.util.imageloader.resourceloader.ResourceLoader;
import com.italankin.lnch.util.imageloader.resourceloader.ShortcutIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.WidgetPreviewLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class ImageLoader {

    private final List<ResourceLoader> resourceLoaders;
    private final Handler callbackHandler;
    private final Cache cache;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Object, LoadTask> tasks = new WeakHashMap<>(32);

    private final Object lock = new Object();

    private ImageLoader(Builder builder) {
        this.resourceLoaders = builder.resourceLoaders;
        this.callbackHandler = builder.callbackHandler;
        this.cache = builder.cache;
    }

    public void cancel(@NonNull ImageView target) {
        LoadTask loadTask;
        synchronized (lock) {
            loadTask = tasks.remove(target);
        }
        if (loadTask != null) {
            loadTask.cancel();
        }
    }

    public RequestBuilder load(@NonNull Uri uri) {
        for (ResourceLoader resourceLoader : resourceLoaders) {
            if (resourceLoader.handles(uri)) {
                return new RequestBuilder(uri, resourceLoader);
            }
        }
        throw new IllegalArgumentException("No ResourceLoader for uri=" + uri);
    }

    public static class Builder {
        private Handler callbackHandler;
        private Cache cache = Cache.NO_OP;
        private final List<ResourceLoader> resourceLoaders = new ArrayList<>(2);

        public Builder(Context context) {
            resourceLoaders.add(new PackageIconLoader(context));
            resourceLoaders.add(new ActivityIconLoader(context));
            resourceLoaders.add(new PackageResourceLoader(context));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                resourceLoaders.add(new ShortcutIconLoader(context));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resourceLoaders.add(new WidgetPreviewLoader(context));
            }
        }

        public Builder cache(@NonNull Cache cache) {
            this.cache = cache;
            return this;
        }

        public Builder register(@NonNull ResourceLoader resourceLoader) {
            resourceLoaders.add(resourceLoader);
            return this;
        }

        public Builder callbackHandler(Handler handler) {
            this.callbackHandler = handler;
            return this;
        }

        public ImageLoader build() {
            if (callbackHandler == null) {
                callbackHandler = new Handler(Looper.getMainLooper());
            }
            return new ImageLoader(this);
        }
    }

    public class RequestBuilder {
        private final Uri uri;
        private final ResourceLoader resourceLoader;
        private Drawable placeholder;
        private Drawable errorPlaceholder;
        private boolean noCache = false;

        RequestBuilder(Uri uri, ResourceLoader resourceLoader) {
            this.uri = uri;
            this.resourceLoader = resourceLoader;
        }

        public RequestBuilder placeholder(Drawable placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public RequestBuilder errorPlaceholder(Drawable errorPlaceholder) {
            this.errorPlaceholder = errorPlaceholder;
            return this;
        }

        public RequestBuilder noCache() {
            this.noCache = true;
            return this;
        }

        public void into(@NonNull ImageView target) {
            into(target, Callback.EMPTY);
        }

        public void into(@NonNull ImageView target, @NonNull Callback callback) {
            cancel(target);
            Request request = new Request(
                    uri,
                    new ImageViewTarget(target),
                    errorPlaceholder,
                    callback,
                    noCache);
            request.target.onPrepareLoad(placeholder);
            LoadTask task = new LoadTask(resourceLoader, request, callbackHandler);
            synchronized (lock) {
                tasks.put(target, task);
            }
            executor.execute(task);
        }
    }

    private void removeTask(LoadTask task) {
        synchronized (lock) {
            Iterator<Map.Entry<Object, LoadTask>> iterator = tasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, LoadTask> entry = iterator.next();
                if (entry.getValue() == task) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private class LoadTask implements Runnable {
        private final ResourceLoader resourceLoader;
        private final Request request;
        private final Handler handler;
        private volatile boolean cancelled = false;

        LoadTask(ResourceLoader resourceLoader, Request request, Handler handler) {
            this.request = request;
            this.resourceLoader = resourceLoader;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                if (isCancelled()) {
                    return;
                }
                if (!request.noCache) {
                    Drawable cached = cache.get(request.uri);
                    if (cached != null) {
                        handler.post(() -> {
                            request.target.onImageLoaded(cached);
                            request.callback.onSuccess();
                        });
                        return;
                    }
                }
                Drawable drawable = resourceLoader.load(request.uri);
                if (drawable == null) {
                    Timber.tag("ImageLoader").w("%s.load(%s) returned null", resourceLoader, request.uri);
                    return;
                }
                if (!request.noCache) {
                    cache.put(request.uri, drawable);
                }
                if (isCancelled()) {
                    return;
                }
                handler.post(() -> {
                    request.target.onImageLoaded(drawable);
                    request.callback.onSuccess();
                });
            } catch (Exception e) {
                Timber.tag("ImageLoader").e(e, "LoadTask failed: %s", e.getMessage());
                if (isCancelled()) {
                    return;
                }
                handler.post(() -> {
                    request.target.onImageFailed(e, request.errorPlaceholder);
                    request.callback.onError(e);
                });
            } finally {
                removeTask(this);
            }
        }

        void cancel() {
            cancelled = true;
        }

        private boolean isCancelled() {
            return request.target.isDead() || cancelled;
        }
    }
}
