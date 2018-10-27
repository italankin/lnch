package com.italankin.lnch.feature.settings_wallpaper.overlay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

public class WallpaperOverlayActivity extends AppActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, WallpaperOverlayActivity.class);
    }

    private ColorPickerView colorPicker;
    private Preferences preferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = daggerService().main().getPreferences();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);

        setContentView(R.layout.activity_wallpaper_overlay);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        initItemPreview();

        colorPicker = findViewById(R.id.color_picker);
        ViewGroup root = findViewById(R.id.root);
        colorPicker.setColorChangedListener(root::setBackgroundColor);
        colorPicker.setSelectedColor(preferences.overlayColor());
    }

    private void initItemPreview() {
        TextView itemPreview = findViewById(R.id.item_preview);
        itemPreview.setText(R.string.settings_overlay_preview);
        itemPreview.setAllCaps(true);
        itemPreview.setTextColor(ContextCompat.getColor(this, R.color.accent));
        itemPreview.setOnClickListener(v -> {
            ColorPickerDialog.builder(this)
                    .setSelectedColor(itemPreview.getCurrentTextColor())
                    .setOnColorPickedListener(itemPreview::setTextColor)
                    .show();
        });

        int padding = ResUtils.px2dp(this, preferences.itemPadding());
        itemPreview.setPadding(padding, padding, padding, padding);
        itemPreview.setTextSize(preferences.itemTextSize());
        itemPreview.setShadowLayer(preferences.itemShadowRadius(), itemPreview.getShadowDx(),
                itemPreview.getShadowDy(), preferences.itemShadowColor());
        itemPreview.setTypeface(preferences.itemFont().typeface());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_overlay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                preferences.setOverlayColor(colorPicker.getSelectedColor());
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
