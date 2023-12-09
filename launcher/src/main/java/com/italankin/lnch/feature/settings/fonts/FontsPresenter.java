package com.italankin.lnch.feature.settings.fonts;

import android.graphics.Typeface;
import android.net.Uri;
import com.arellomobile.mvp.InjectViewState;
import com.italankin.lnch.feature.base.AppPresenter;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.fonts.InvalidFontFormat;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.model.repository.prefs.Preferences;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@InjectViewState
public class FontsPresenter extends AppPresenter<FontsView> {

    private final FontManager fontManager;
    private final DescriptorRepository descriptorRepository;
    private final Preferences preferences;

    private String previewText;

    @Inject
    public FontsPresenter(FontManager fontManager, DescriptorRepository descriptorRepository,
            Preferences preferences) {
        this.fontManager = fontManager;
        this.descriptorRepository = descriptorRepository;
        this.preferences = preferences;
    }

    @Override
    protected void onFirstViewAttach() {
        loadFonts();
    }

    void loadFonts() {
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

        getViewState().onItemsUpdated(items);
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleState<Boolean>() {
                    @Override
                    protected void onSuccess(FontsView viewState, Boolean reset) {
                        viewState.onFontDeleted(reset);
                        loadFonts();
                    }
                });
    }

    void addFont(String name, Uri uri) {
        String fontName = name.trim();
        if (fontName.isEmpty()) {
            getViewState().onAddFontEmptyNameError();
            return;
        }
        if (fontManager.exists(fontName)) {
            getViewState().onAddFontExistsError(fontName);
            return;
        }
        fontManager.load(fontName, uri)
                .ignoreElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableState() {
                    @Override
                    public void onComplete(FontsView viewState) {
                        viewState.onFontAdded();
                        loadFonts();
                    }

                    @Override
                    protected void onError(FontsView viewState, Throwable e) {
                        if (e instanceof InvalidFontFormat) {
                            viewState.showErrorInvalidFormat();
                        } else {
                            viewState.showError(e);
                        }
                    }
                });
    }
}
