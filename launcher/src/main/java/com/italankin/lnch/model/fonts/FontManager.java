package com.italankin.lnch.model.fonts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;

import com.italankin.lnch.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import timber.log.Timber;

public class FontManager {

    public static final String DEFAULT_FONT = "default";
    public static final Map<String, Typeface> DEFAULT_FONTS;

    private static final Typeface DEFAULT_TYPEFACE = Typeface.DEFAULT_BOLD;
    private static final String SANS_SERIF = "sans_serif";
    private static final String SERIF = "serif";
    private static final String MONOSPACE = "monospace";

    private static final String FONTS_DIR_NAME = "fonts";
    private static final String FONTS_PREFERENCES_NAME = "fonts";

    static {
        HashMap<String, Typeface> defaults = new LinkedHashMap<>(4);
        defaults.put(DEFAULT_FONT, DEFAULT_TYPEFACE);
        defaults.put(SANS_SERIF, Typeface.SANS_SERIF);
        defaults.put(SERIF, Typeface.SERIF);
        defaults.put(MONOSPACE, Typeface.MONOSPACE);
        DEFAULT_FONTS = Collections.unmodifiableMap(defaults);
    }

    private final Context context;
    private final SharedPreferences fontsData;
    private final File fontsDir;
    private final Map<String, Typeface> fonts = new LinkedHashMap<>(2);

    public FontManager(Context context) {
        this.context = context;
        this.fontsDir = new File(context.getFilesDir(), FONTS_DIR_NAME);
        this.fontsData = context.getSharedPreferences(FONTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public Typeface getTypeface(String name) {
        Typeface typeface = fonts.get(name);
        if (typeface != null) {
            return typeface;
        }
        typeface = DEFAULT_FONTS.get(name);
        if (typeface != null) {
            return typeface;
        }
        if (fontsData.contains(name)) {
            String filename = fontsData.getString(name, null);
            if (filename != null) {
                typeface = Typeface.createFromFile(new File(fontsDir, filename));
                Timber.d("getTypeface: '%s' loaded", name);
                fonts.put(name, typeface);
                return typeface;
            }
        }
        Timber.w("getTypeface: typeface '%s' not found", name);
        return DEFAULT_TYPEFACE;
    }

    public boolean exists(String name) {
        return DEFAULT_FONTS.containsKey(name) || fonts.containsKey(name);
    }

    public Map<String, Typeface> getCustomFonts() {
        return Collections.unmodifiableMap(fonts);
    }

    public Completable loadUserFonts() {
        return Completable
                .fromRunnable(() -> {
                    fonts.clear();
                    for (Map.Entry<String, ?> entry : fontsData.getAll().entrySet()) {
                        String name = entry.getKey();
                        String filename = (String) entry.getValue();
                        Typeface typeface = Typeface.createFromFile(new File(fontsDir, filename));
                        fonts.put(name, typeface);
                    }
                    Timber.i("loadUserFonts: %d loaded", fonts.size());
                })
                .onErrorComplete(throwable -> {
                    Timber.e(throwable, "loadUserFonts:");
                    return true;
                });
    }

    public Single<Typeface> load(String name, Uri uri) {
        return Single.using(() -> context.getContentResolver().openInputStream(uri),
                inputStream -> Single.fromCallable(() -> {
                    if (!fontsDir.exists()) {
                        if (fontsDir.mkdir()) {
                            Timber.i("load: created dir: %s", fontsDir);
                        } else {
                            Timber.e("load: cannot create dir: %s", fontsDir);
                            throw new IllegalStateException("cannot create dir: " + fontsDir);
                        }
                    }
                    Timber.d("load: name=%s, uri=%s", name, uri);
                    String filename = UUID.randomUUID().toString();
                    File fontFile = new File(fontsDir, filename);
                    try (FileOutputStream os = new FileOutputStream(fontFile)) {
                        int read;
                        byte[] buffer = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
                        while ((read = inputStream.read(buffer)) != -1) {
                            os.write(buffer, 0, read);
                        }
                    } catch (IOException e) {
                        Timber.e(e, "load:");
                        throw e;
                    }
                    Typeface typeface = Typeface.createFromFile(fontFile);
                    fontsData.edit().putString(name, filename).apply();
                    fonts.put(name, typeface);
                    return typeface;
                }),
                IOUtils::closeQuietly);
    }

    public Completable delete(String name) {
        return Completable.fromRunnable(() -> {
            fonts.remove(name);
            String filename = fontsData.getString(name, null);
            fontsData.edit().remove(name).apply();
            if (filename == null) {
                Timber.w("delete: no filename under key: %s", name);
                return;
            }
            File file = new File(fontsDir, filename);
            if (file.exists()) {
                if (file.delete()) {
                    Timber.i("delete: deleted file: %s", file);
                } else {
                    Timber.e("delete: cannot file: %s", file);
                }
            } else {
                Timber.w("delete: file does not exist: %s", file);
            }
        });
    }
}
