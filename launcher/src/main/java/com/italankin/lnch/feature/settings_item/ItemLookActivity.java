package com.italankin.lnch.feature.settings_item;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
    private TextView itemApp;
    private SeekBar paddingSeekBar;
    private SeekBar textSizeSeekBar;
    private SeekBar shadowSeekBar;

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
        initPadding();
        initShadow();
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
        itemApp = findViewById(R.id.item_app);
        itemApp.setBackgroundColor(0x20ffffff);
        itemApp.setText(R.string.settings_item_preview);
        itemApp.setAllCaps(true);
        itemApp.setTextColor(ContextCompat.getColor(this, R.color.accent));
        itemApp.setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setSelectedColor(itemApp.getCurrentTextColor())
                    .setOnColorPickedListener(itemApp::setTextColor)
                    .show();
        });
        itemApp.setTextSize(preferences.itemTextSize());
        setItemAppPadding(preferences.itemPadding());
        setItemAppShadow(preferences.itemShadowRadius());
    }

    private void initTextSize() {
        textSizeSeekBar = findViewById(R.id.text_size_seekbar);
        textSizeSeekBar.setProgress((int) (preferences.itemTextSize() - TEXT_SIZE_MIN));
        textSizeSeekBar.setMax(TEXT_SIZE_MAX - TEXT_SIZE_MIN);
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            itemApp.setTextSize(TEXT_SIZE_MIN + progress);
        }));
    }

    private void initPadding() {
        paddingSeekBar = findViewById(R.id.padding_seekbar);
        paddingSeekBar.setProgress(preferences.itemPadding() - PADDING_MIN);
        paddingSeekBar.setMax(PADDING_MAX - PADDING_MIN);
        paddingSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            setItemAppPadding(PADDING_MIN + progress);
        }));
    }

    private void initShadow() {
        shadowSeekBar = findViewById(R.id.shadow_seekbar);
        shadowSeekBar.setProgress((int) preferences.itemShadowRadius());
        shadowSeekBar.setMax(SHADOW_RADIUS_MAX);
        shadowSeekBar.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            setItemAppShadow(progress);
        }));
    }

    private void setItemAppPadding(int padding) {
        int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                padding, getResources().getDisplayMetrics());
        itemApp.setPadding(p, p, p, p);
    }

    private void setItemAppShadow(float radius) {
        itemApp.setShadowLayer(radius, itemApp.getShadowDx(), itemApp.getShadowDy(), itemApp.getShadowColor());
    }

    private void save() {
        float textSize = textSizeSeekBar.getProgress() + TEXT_SIZE_MIN;
        int padding = paddingSeekBar.getProgress() + PADDING_MIN;
        float shadowRadius = shadowSeekBar.getProgress();
        preferences.setItemTextSize(textSize);
        preferences.setItemPadding(padding);
        preferences.setItemShadowRadius(shadowRadius);
    }
}
