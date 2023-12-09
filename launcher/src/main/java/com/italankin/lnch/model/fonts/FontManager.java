package com.italankin.lnch.model.fonts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.core.content.res.ResourcesCompat;
import com.italankin.lnch.R;
import com.italankin.lnch.util.IOUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
import timber.log.Timber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class FontManager {

    public static final String DEFAULT_FONT = "default";

    private static final String ANDROID_DEFAULT = "android_default";
    private static final String SANS_SERIF = "sans_serif";
    private static final String SERIF = "serif";
    private static final String MONOSPACE = "monospace";

    private static final String FONTS_DIR_NAME = "fonts";
    private static final String FONTS_PREFERENCES_NAME = "fonts";

    private static final int FONT_MAGIC_NUMBER = 0x00010000;

    private final Context context;
    private final SharedPreferences fontsData;
    private final File fontsDir;
    private final Map<String, Typeface> fonts = new LinkedHashMap<>(2);
    private final Typeface defaultTypeface;
    private final Map<String, Typeface> defaultFonts;

    public FontManager(Context context) {
        this.context = context;
        this.fontsDir = new File(context.getFilesDir(), FONTS_DIR_NAME);
        this.fontsData = context.getSharedPreferences(FONTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.defaultTypeface = ResourcesCompat.getFont(context, R.font.comfortaa);

        Map<String, Typeface> defaultFonts = new LinkedHashMap<>(4);
        defaultFonts.put(DEFAULT_FONT, defaultTypeface);
        defaultFonts.put(ANDROID_DEFAULT, Typeface.DEFAULT_BOLD);
        defaultFonts.put(SANS_SERIF, Typeface.SANS_SERIF);
        defaultFonts.put(SERIF, Typeface.SERIF);
        defaultFonts.put(MONOSPACE, Typeface.MONOSPACE);
        this.defaultFonts = Collections.unmodifiableMap(defaultFonts);
    }

    public Typeface getTypeface(String name) {
        Typeface typeface = fonts.get(name);
        if (typeface != null) {
            return typeface;
        }
        typeface = defaultFonts.get(name);
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
        return defaultTypeface;
    }

    public boolean exists(String name) {
        return defaultFonts.containsKey(name) || fonts.containsKey(name);
    }

    public Map<String, Typeface> getDefaultFonts() {
        return defaultFonts;
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
                        byte[] buffer = new byte[IOUtils.DEFAULT_BUFFER_SIZE];

                        // read magic number header
                        int read = inputStream.read(buffer, 0, 4);
                        if (read != 4) {
                            throw new IllegalStateException("File is too short");
                        }
                        ByteBuffer b = ByteBuffer.wrap(buffer, 0, 4);
                        int magicNumber = b.getInt();
                        if (magicNumber != FONT_MAGIC_NUMBER) {
                            throw new InvalidFontFormat();
                        }
                        os.write(buffer, 0, 4);

                        // read the rest of the file
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
            removeFontFile(filename);
        });
    }

    public Completable clear() {
        return Completable.fromRunnable(() -> {
            Collection<?> filenames = fontsData.getAll().values();
            for (Object filename : filenames) {
                removeFontFile((String) filename);
            }
            fontsData.edit().clear().apply();
            fonts.clear();
            Timber.d("deleted %d fonts", filenames.size());
        });
    }

    private void removeFontFile(String filename) {
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
    }
}
