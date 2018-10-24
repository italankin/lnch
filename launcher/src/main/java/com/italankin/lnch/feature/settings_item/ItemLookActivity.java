package com.italankin.lnch.feature.settings_item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.prefs.Preferences.Constraints;
import com.italankin.lnch.util.SeekBarChangeListener;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;
import com.italankin.lnch.util.widget.pref.SliderPrefView;
import com.italankin.lnch.util.widget.pref.ValuePrefView;

public class ItemLookActivity extends AppActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ItemLookActivity.class);
    }

    private Preferences preferences;

    private TextView preview;

    private SliderPrefView itemTextSize;
    private ValuePrefView itemFont;
    private SliderPrefView itemPadding;
    private SliderPrefView itemShadowRadius;
    private ValuePrefView itemShadowColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = daggerService().main().getPreferences();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        setContentView(R.layout.activity_settings_item_look);

        initOverlay();
        initToolbar();
        initPreview();

        initTextSize();
        initFont();
        initPadding();
        initShadowRadius();
        initShadowColor();

        updatePreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_item_look, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                finish();
                return true;
            case R.id.action_reset:
                preferences.resetItemSettings();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initOverlay() {
        View overlay = findViewById(R.id.overlay);
        overlay.setBackgroundColor(preferences.overlayColor());
        overlay.setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(preferences.overlayColor())
                    .setOnColorPickedListener(overlay::setBackgroundColor)
                    .show();
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initPreview() {
        preview = findViewById(R.id.item_preview);
        preview.setBackgroundColor(0x20ffffff);
        preview.setText(R.string.settings_item_preview);
        preview.setAllCaps(true);
        preview.setTextColor(ContextCompat.getColor(this, R.color.accent));
        preview.setOnClickListener(v -> ColorPickerDialog.builder(this)
                .setSelectedColor(preview.getCurrentTextColor())
                .setOnColorPickedListener(preview::setTextColor)
                .show());
    }

    private void initTextSize() {
        itemTextSize = findViewById(R.id.item_text_size);
        itemTextSize.setProgress((int) (preferences.itemTextSize() - Constraints.ITEM_TEXT_SIZE_MIN));
        itemTextSize.setMax(Constraints.ITEM_TEXT_SIZE_MAX - Constraints.ITEM_TEXT_SIZE_MIN);
        itemTextSize.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initFont() {
        String[] fontTitles = getResources().getStringArray(R.array.settings_item_look_text_font_titles);

        itemFont = findViewById(R.id.item_font);
        Preferences.Font font = preferences.itemFont();
        itemFont.setValueHolder(new ValuePrefView.ValueHolder<Preferences.Font>() {
            private Preferences.Font value;

            @Override
            public void set(Preferences.Font value) {
                this.value = value;
            }

            @Override
            public Preferences.Font get() {
                return value;
            }

            @Override
            public CharSequence getTitle() {
                return fontTitles[value.ordinal()];
            }
        });
        itemFont.setValue(font);
        itemFont.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.settings_item_look_text_font);
            builder.setItems(fontTitles, (dialog, which) -> {
                Preferences.Font newFont = Preferences.Font.values()[which];
                itemFont.setValue(newFont);
                updatePreview();
            });
            builder.show();
        });
    }

    private void initPadding() {
        itemPadding = findViewById(R.id.item_padding);
        itemPadding.setProgress(preferences.itemPadding() - Constraints.ITEM_PADDING_MIN);
        itemPadding.setMax(Constraints.ITEM_PADDING_MAX - Constraints.ITEM_PADDING_MIN);
        itemPadding.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initShadowRadius() {
        itemShadowRadius = findViewById(R.id.item_shadow_radius);
        itemShadowRadius.setProgress((int) preferences.itemShadowRadius() - Constraints.ITEM_SHADOW_RADIUS_MIN);
        itemShadowRadius.setMax(Constraints.ITEM_SHADOW_RADIUS_MAX - Constraints.ITEM_SHADOW_RADIUS_MIN);
        itemShadowRadius.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initShadowColor() {
        itemShadowColor = findViewById(R.id.item_shadow_color);
        int shadowColor = preferences.itemShadowColor();
        itemShadowColor.setValueHolder(new ValuePrefView.ColorValueHolder());
        itemShadowColor.setValue(shadowColor);
        itemShadowColor.setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(preview.getShadowColor())
                    .setOnColorPickedListener(color -> {
                        itemShadowColor.setValue(color);
                        updatePreview();
                    })
                    .setResetButton(getString(R.string.customize_action_reset), (dialog, which) -> {
                        int color = getColor(R.color.item_default_shadow_color);
                        itemShadowColor.setValue(color);
                        updatePreview();
                    })
                    .show();
        });
    }

    private void updatePreview() {
        Preferences.Font font = itemFont.getValue();
        int textSize = Constraints.ITEM_TEXT_SIZE_MIN + itemTextSize.getProgress();
        int padding = Constraints.ITEM_PADDING_MIN + itemPadding.getProgress();
        int shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        preview.setTypeface(font.typeface());
        int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                padding, getResources().getDisplayMetrics());
        preview.setPadding(p, p, p, p);
        preview.setTextSize(textSize);
        preview.setShadowLayer(shadowRadius, preview.getShadowDx(), preview.getShadowDy(), shadowColor);
    }

    private void save() {
        float textSize = itemTextSize.getProgress() + Constraints.ITEM_TEXT_SIZE_MIN;
        int padding = itemPadding.getProgress() + Constraints.ITEM_PADDING_MIN;
        float shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        Preferences.Font font = itemFont.getValue();
        preferences.setItemTextSize(textSize);
        preferences.setItemPadding(padding);
        preferences.setItemShadowRadius(shadowRadius);
        preferences.setItemShadowColor(shadowColor);
        preferences.setItemFont(font);
    }
}
