package com.italankin.lnch.feature.settings.fonts;

import android.graphics.Typeface;
import android.net.Uri;
import com.italankin.lnch.feature.base.AppViewModel;
import com.italankin.lnch.feature.settings.fonts.events.AddFontEvent;
import com.italankin.lnch.feature.settings.fonts.events.DeleteFontEvent;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.fonts.InvalidFontFormat;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FontsViewModel extends AppViewModel {

    private final FontManager fontManager;
    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    private final BehaviorSubject<List<FontItem>> fontItemsSubject = BehaviorSubject.create();
    private final PublishSubject<AddFontEvent> addFontSubject = PublishSubject.create();
    private final PublishSubject<DeleteFontEvent> deleteFontSubject = PublishSubject.create();

    private String previewText;

    @Inject
    public FontsViewModel(FontManager fontManager, DescriptorRepository descriptorRepository,
            Preferences preferences) {
        this.fontManager = fontManager;
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;

        reloadFonts();
    }

    Observable<List<FontItem>> fontItemsEvents() {
        return fontItemsSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<AddFontEvent> addFontEvents() {
        return addFontSubject.observeOn(AndroidSchedulers.mainThread());
    }

    Observable<DeleteFontEvent> deleteFontEvents() {
        return deleteFontSubject.observeOn(AndroidSchedulers.mainThread());
    }

    void deleteFont(FontItem item) {
        fontManager.delete(item.name)
                .andThen(Single.fromCallable(() -> {
                    String currentFont = preferences.get(Preferences.ITEM_FONT);
                    if (currentFont.equals(item.name)) {
                        Timber.d("Reset font to default");
                        preferences.reset(Preferences.ITEM_FONT);
                        return true;
                    }
                    return false;
                }))
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleState<>() {
                    @Override
                    public void onSuccess(Boolean reset) {
                        deleteFontSubject.onNext(new DeleteFontEvent(reset));
                        reloadFonts();
                    }
                });
    }

    void addFont(String name, Uri uri) {
        String fontName = name.trim();
        if (fontName.isEmpty()) {
            addFontSubject.onNext(new AddFontEvent.FontEmptyNameError());
            return;
        }
        if (fontManager.exists(fontName)) {
            addFontSubject.onNext(new AddFontEvent.FontExistsError(fontName));
            return;
        }
        fontManager.load(fontName, uri)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete() {
                        addFontSubject.onNext(new AddFontEvent.FontAdded());
                        reloadFonts();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof InvalidFontFormat) {
                            addFontSubject.onNext(new AddFontEvent.InvalidFormatError());
                        } else {
                            addFontSubject.onNext(new AddFontEvent.FontAddError(e));
                        }
                    }
                });
    }

    private void reloadFonts() {
        if (previewText == null) {
            List<CustomLabelDescriptor> descriptors = descriptorRepository.itemsOfType(CustomLabelDescriptor.class);
            previewText = descriptors
                    .get(new Random().nextInt(descriptors.size()))
                    .getVisibleLabel();
        }

        Map<String, Typeface> fonts = fontManager.getCustomFonts();
        Map<String, Typeface> defaultFonts = fontManager.getDefaultFonts();
        List<FontItem> items = new ArrayList<>(defaultFonts.size() + fonts.size());
        for (Map.Entry<String, Typeface> entry : defaultFonts.entrySet()) {
            String name = entry.getKey();
            Typeface typeface = entry.getValue();
            items.add(new FontItem(name, previewText, typeface, true));
        }
        for (Map.Entry<String, Typeface> entry : fonts.entrySet()) {
            String name = entry.getKey();
            Typeface typeface = entry.getValue();
            items.add(new FontItem(name, previewText, typeface, false));
        }

        fontItemsSubject.onNext(items);
    }
}
