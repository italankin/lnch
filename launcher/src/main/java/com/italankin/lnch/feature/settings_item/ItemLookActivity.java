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
import android.widget.SeekBar;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.SeekBarChangeListener;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

public class ItemLookActivity extends AppActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ItemLookActivity.class);
    }

    private static final int TEXT_SIZE_MIN = 12;
    private static final int TEXT_SIZE_MAX = 40;
    private static final int PADDING_MIN = 4;
    private static final int PADDING_MAX = 28;
    private static final int SHADOW_RADIUS_MAX = 16;

    private Preferences preferences;
    private TextView preview;
    private SeekBar paddingSeekBar;
    private SeekBar textSizeSeekBar;
    private SeekBar shadowRadiusSeekBar;
    private TextView textFontValue;
    private int fontFamily;

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
        preview.setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setSelectedColor(preview.getCurrentTextColor())
                    .setOnColorPickedListener(preview::setTextColor)
                    .show();
        });
        preview.setTextSize(preferences.itemTextSize());
        preview.setTypeface(preferences.itemFont().typeface());
        setItemAppPadding(preferences.itemPadding());
        setItemAppShadowRadius(preferences.itemShadowRadius());
        setItemAppShadowColor(preferences.itemShadowColor());
    }

    private void initTextSize() {
        textSizeSeekBar = findViewById(R.id.text_size_seekbar);
        textSizeSeekBar.setProgress((int) (preferences.itemTextSize() - TEXT_SIZE_MIN));
        textSizeSeekBar.setMax(TEXT_SIZE_MAX - TEXT_SIZE_MIN);
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            preview.setTextSize(TEXT_SIZE_MIN + progress);
        }));
    }

    private void initFont() {
        CharSequence[] items = {"Default", "Sans Serif", "Serif", "Monospace"};
        fontFamily = preferences.itemFont().ordinal();
        View viewById = findViewById(R.id.text_font);
        viewById.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.settings_item_look_text_font);
            builder.setItems(items, (dialog, which) -> {
                preview.setTypeface(Preferences.Font.values()[which].typeface());
                fontFamily = which;
                textFontValue.setText(items[fontFamily]);
            });
            builder.show();
        });
        textFontValue = findViewById(R.id.text_font_value);
        textFontValue.setText(items[fontFamily]);
    }

    private void initPadding() {
        paddingSeekBar = findViewById(R.id.padding_seekbar);
        paddingSeekBar.setProgress(preferences.itemPadding() - PADDING_MIN);
        paddingSeekBar.setMax(PADDING_MAX - PADDING_MIN);
        paddingSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            setItemAppPadding(PADDING_MIN + progress);
        }));
    }

    private void initShadowRadius() {
        shadowRadiusSeekBar = findViewById(R.id.shadow_radius_seekbar);
        shadowRadiusSeekBar.setProgress((int) preferences.itemShadowRadius());
        shadowRadiusSeekBar.setMax(SHADOW_RADIUS_MAX);
        shadowRadiusSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            setItemAppShadowRadius(progress);
        }));
    }

    private void initShadowColor() {
        findViewById(R.id.shadow_color).setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(preview.getShadowColor())
                    .setOnColorPickedListener(this::setItemAppShadowColor)
                    .setResetButton(getString(R.string.customize_action_reset), (dialog, which) -> {
                        setItemAppShadowColor(getColor(R.color.item_default_shadow_color));
                    })
                    .show();
        });
    }

    private void setItemAppPadding(int padding) {
        int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                padding, getResources().getDisplayMetrics());
        preview.setPadding(p, p, p, p);
    }

    private void setItemAppShadowRadius(float radius) {
        preview.setShadowLayer(radius, preview.getShadowDx(), preview.getShadowDy(),
                preview.getShadowColor());
    }

    private void setItemAppShadowColor(int color) {
        preview.setShadowLayer(preview.getShadowRadius(), preview.getShadowDx(),
                preview.getShadowDy(), color);
    }

    private void save() {
        float textSize = textSizeSeekBar.getProgress() + TEXT_SIZE_MIN;
        int padding = paddingSeekBar.getProgress() + PADDING_MIN;
        float shadowRadius = shadowRadiusSeekBar.getProgress();
        int shadowColor = preview.getShadowColor();
        preferences.setItemTextSize(textSize);
        preferences.setItemPadding(padding);
        preferences.setItemShadowRadius(shadowRadius);
        preferences.setItemShadowColor(shadowColor);
        preferences.setItemFont(Preferences.Font.values()[fontFamily]);
    }
}
