package com.italankin.lnch.feature.settings.lookfeel;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.slider.Slider;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.fonts.FontsActivity;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;
import com.italankin.lnch.util.widget.pref.ValuePrefView;

public class AppearanceActivity extends AppCompatActivity implements
        ColorPickerDialogFragment.Listener,
        SimpleDialogFragment.Listener {

    private static final String TAG_WALLPAPER_DIM_COLOR = "wallpaper_dim_color";
    private static final String TAG_SHADOW_COLOR = "shadow_color";
    private static final String TAG_PREVIEW_OVERLAY = "preview_overlay";
    private static final String TAG_DISCARD_CHANGES = "discard_changes";

    private Preferences preferences;
    private FontManager fontManager;

    private OnBackPressedCallback onBackPressedCallback;

    private ImageView wallpaper;
    private TextView preview;
    private View wallpaperDim;

    private Slider itemTextSize;
    private ValuePrefView itemFont;
    private Slider itemPadding;
    private Slider itemShadowRadius;
    private ValuePrefView itemShadowColor;

    private PreviewBackground previewBackground = PreviewBackground.WALLPAPER;

    private final ActivityResultLauncher<Void> selectFontLauncher = registerForActivityResult(
            new FontsActivity.Contract(),
            result -> {
                if (result == null) {
                    return;
                }
                if (result.selected != null) {
                    itemFont.setValue(result.selected);
                    updatePreview();
                    onBackPressedCallback.setEnabled(true);
                } else if (result.selectedDeleted) {
                    preferences.reset(Preferences.ITEM_FONT);
                    itemFont.setValue(preferences.get(Preferences.ITEM_FONT));
                    updatePreview();
                    onBackPressedCallback.setEnabled(true);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        fontManager = LauncherApp.daggerService.main().fontManager();

        setContentView(R.layout.activity_settings_appearance);
        initView();

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                new SimpleDialogFragment.Builder()
                        .setMessage(R.string.settings_home_laf_appearance_discard_message)
                        .setPositiveButton(R.string.settings_home_laf_appearance_discard_button)
                        .setNegativeButton(R.string.cancel)
                        .build()
                        .show(getSupportFragmentManager(), TAG_DISCARD_CHANGES);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void initView() {
        wallpaper = findViewById(R.id.wallpaper);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initOverlay();
        initPreview();

        initTextSize();
        initFont();
        initPadding();
        initShadowRadius();
        initShadowColor();

        updatePreview();
        updatePreviewBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_appearance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_save) {
            save();
            finish();
            return true;
        } else if (itemId == R.id.action_reset) {
            resetPreferences();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorPicked(@Nullable String tag, int newColor) {
        if (tag == null) {
            return;
        }
        switch (tag) {
            case TAG_WALLPAPER_DIM_COLOR: {
                wallpaperDim.setBackgroundColor(newColor);
                break;
            }
            case TAG_PREVIEW_OVERLAY: {
                preview.setTextColor(newColor);
                break;
            }
            case TAG_SHADOW_COLOR: {
                itemShadowColor.setValue(newColor);
                updatePreview();
                onBackPressedCallback.setEnabled(true);
                break;
            }
        }
    }

    @Override
    public void onColorReset(@Nullable String tag) {
        if (TAG_SHADOW_COLOR.equals(tag)) {
            int color = ResUtils.resolveColor(this, R.attr.colorItemShadowDefault);
            itemShadowColor.setValue(color);
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        }
    }

    @Override
    public void onPositiveButtonClick(String tag) {
        if (TAG_DISCARD_CHANGES.equals(tag)) {
            finish();
        }
    }

    private void initOverlay() {
        int dimColor = preferences.get(Preferences.WALLPAPER_DIM_COLOR);
        wallpaperDim = findViewById(R.id.wallpaper_dim);
        wallpaperDim.setBackgroundColor(dimColor);
        wallpaperDim.setOnClickListener(v -> {
            Drawable background = v.getBackground();
            int selectedColor;
            if (background instanceof ColorDrawable) {
                selectedColor = ((ColorDrawable) background).getColor();
            } else {
                selectedColor = dimColor;
            }
            new ColorPickerDialogFragment.Builder()
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(selectedColor)
                    .build()
                    .show(getSupportFragmentManager(), TAG_WALLPAPER_DIM_COLOR);
        });
    }

    private void initPreview() {
        preview = findViewById(R.id.item_preview).findViewById(R.id.label);
        preview.setBackgroundResource(R.drawable.selector_item_appearance);
        String previewText = LauncherApp.daggerService.main()
                .nameNormalizer()
                .normalize(getString(R.string.preview));
        preview.setText(previewText);
        preview.setTextColor(ContextCompat.getColor(this, R.color.seed));
        preview.setOnClickListener(v -> {
            new ColorPickerDialogFragment.Builder()
                    .setSelectedColor(preview.getCurrentTextColor())
                    .build()
                    .show(getSupportFragmentManager(), TAG_PREVIEW_OVERLAY);
        });
        View previewBackgroundSwitcher = findViewById(R.id.preview_background_switcher);
        previewBackgroundSwitcher.setOnClickListener(v -> {
            previewBackground = PreviewBackground.values()[
                    (previewBackground.ordinal() + 1) % PreviewBackground.values().length];
            updatePreviewBackground();
        });
    }

    private void updatePreviewBackground() {
        switch (previewBackground) {
            case WALLPAPER:
                wallpaper.setImageDrawable(null);
                wallpaperDim.setVisibility(View.VISIBLE);
                break;
            case WHITE:
                wallpaper.setImageDrawable(new ColorDrawable(Color.WHITE));
                wallpaperDim.setVisibility(View.INVISIBLE);
                break;
            case BLACK:
                wallpaper.setImageDrawable(new ColorDrawable(Color.BLACK));
                wallpaperDim.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void resetPreferences() {
        preferences.reset(
                Preferences.ITEM_TEXT_SIZE,
                Preferences.ITEM_PADDING,
                Preferences.ITEM_SHADOW_RADIUS,
                Preferences.ITEM_FONT,
                Preferences.ITEM_SHADOW_COLOR
        );
        itemTextSize.setValue(preferences.get(Preferences.ITEM_TEXT_SIZE));
        itemPadding.setValue(preferences.get(Preferences.ITEM_PADDING));
        itemShadowRadius.setValue(preferences.get(Preferences.ITEM_SHADOW_RADIUS));
        itemFont.setValue(preferences.get(Preferences.ITEM_FONT));
        itemShadowColor.setValue(ResUtils.resolveColor(this, R.attr.colorItemShadowDefault));
        updatePreview();
        onBackPressedCallback.setEnabled(false);
    }

    private void initTextSize() {
        itemTextSize = findViewById(R.id.item_text_size);
        setParams(itemTextSize, Preferences.ITEM_TEXT_SIZE);
        Slider.OnChangeListener listener = (slider, value, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        };
        itemTextSize.addOnChangeListener(listener);
    }

    private void initFont() {
        itemFont = findViewById(R.id.item_font);
        String font = preferences.get(Preferences.ITEM_FONT);
        itemFont.setValueHolder(new ValuePrefView.ValueHolder<String>() {
            private String value;

            @Override
            public void set(String value) {
                this.value = value;
            }

            @Override
            public String get() {
                return value;
            }

            @Override
            public CharSequence getDescription() {
                return value;
            }
        });
        itemFont.setValue(font);
        itemFont.setOnClickListener(v -> selectFontLauncher.launch(null));
    }

    private void initPadding() {
        itemPadding = findViewById(R.id.item_padding);
        setParams(itemPadding, Preferences.ITEM_PADDING);
        Slider.OnChangeListener listener = (slider, value, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        };
        itemPadding.addOnChangeListener(listener);
    }

    private void initShadowRadius() {
        itemShadowRadius = findViewById(R.id.item_shadow_radius);
        setParams(itemShadowRadius, Preferences.ITEM_SHADOW_RADIUS);
        Slider.OnChangeListener listener = (slider, value, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        };
        itemShadowRadius.addOnChangeListener(listener);
    }

    private void initShadowColor() {
        itemShadowColor = findViewById(R.id.item_shadow_color);
        Integer shadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
        itemShadowColor.setValueHolder(new ValuePrefView.ColorValueHolder());
        itemShadowColor.setValue(shadowColor != null
                ? shadowColor
                : ResUtils.resolveColor(this, R.attr.colorItemShadowDefault));
        itemShadowColor.setOnClickListener(v -> {
            new ColorPickerDialogFragment.Builder()
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(preview.getShadowColor())
                    .showResetButton(true)
                    .build()
                    .show(getSupportFragmentManager(), TAG_SHADOW_COLOR);
        });
    }

    private void updatePreview() {
        int textSize = (int) itemTextSize.getValue();
        int padding = (int) itemPadding.getValue();
        int shadowRadius = (int) itemShadowRadius.getValue();
        int shadowColor = itemShadowColor.getValue();
        String font = itemFont.getValue();
        preview.setTypeface(fontManager.getTypeface(font));
        ViewUtils.setPaddingDp(preview, padding);
        preview.setTextSize(textSize);
        preview.setShadowLayer(shadowRadius, preview.getShadowDx(), preview.getShadowDy(), shadowColor);
    }

    private void save() {
        float textSize = itemTextSize.getValue();
        int padding = (int) itemPadding.getValue();
        float shadowRadius = itemShadowRadius.getValue();
        int shadowColor = itemShadowColor.getValue();
        String font = itemFont.getValue();
        preferences.set(Preferences.ITEM_TEXT_SIZE, textSize);
        preferences.set(Preferences.ITEM_PADDING, padding);
        preferences.set(Preferences.ITEM_SHADOW_RADIUS, shadowRadius);
        preferences.set(Preferences.ITEM_SHADOW_COLOR, shadowColor);
        preferences.set(Preferences.ITEM_FONT, font);
    }

    private void setParams(Slider prefView, Preferences.RangePref<? extends Number> pref) {
        prefView.setValue(preferences.get(pref).intValue());
        prefView.setValueFrom(pref.min().floatValue());
        prefView.setValueTo(pref.max().floatValue());
        prefView.setStepSize(1f);
    }

    public static class AppearanceFinishedContract extends SignalFragmentResultContract {
        public AppearanceFinishedContract() {
            super("appearance_finished");
        }
    }

    private enum PreviewBackground {
        WALLPAPER, WHITE, BLACK
    }
}
