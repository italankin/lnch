package com.italankin.lnch.feature.settings_wallpaper.overlay;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

public class WallpaperOverlayFragment extends AppFragment {

    private static final int REQUEST_CODE_PERMISSION = 1;

    private Preferences preferences;
    private ColorPickerView colorPicker;

    private Callbacks callbacks;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        preferences = daggerService().main().getPreferences();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallpaper_overlay, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initRoot(view);
        initItemPreview(view);

        colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setColorChangedListener(color -> {
            Drawable background = view.getBackground();
            if (background != null) {
                background.setTint(color);
            }
        });
        colorPicker.setSelectedColor(preferences.overlayColor());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_wallpaper_overlay, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                preferences.setOverlayColor(colorPicker.getSelectedColor());
                if (callbacks != null) {
                    callbacks.onWallpaperOverlayFinish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showWallpaper(getView());
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), R.string.error_no_wallpaper_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initRoot(View view) {
        if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            showWallpaper(view);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION);
        }
    }

    private void initItemPreview(View view) {
        TextView itemPreview = view.findViewById(R.id.item_preview);
        itemPreview.setText(R.string.settings_overlay_preview);
        itemPreview.setAllCaps(true);
        itemPreview.setTextColor(ResUtils.resolveColor(getContext(), R.attr.colorAccent));
        itemPreview.setOnClickListener(v -> {
            ColorPickerDialog.builder(getContext())
                    .setSelectedColor(itemPreview.getCurrentTextColor())
                    .setOnColorPickedListener(itemPreview::setTextColor)
                    .show();
        });

        int padding = ResUtils.px2dp(getContext(), preferences.itemPadding());
        itemPreview.setPadding(padding, padding, padding, padding);
        itemPreview.setTextSize(preferences.itemTextSize());
        itemPreview.setShadowLayer(preferences.itemShadowRadius(), itemPreview.getShadowDx(),
                itemPreview.getShadowDy(), preferences.itemShadowColor());
        itemPreview.setTypeface(preferences.itemFont().typeface());
    }

    private void showWallpaper(View view) {
        WallpaperManager wm = (WallpaperManager) getContext().getSystemService(Context.WALLPAPER_SERVICE);
        if (wm == null || getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Drawable drawable = wm.getDrawable();
        if (drawable == null) {
            return;
        }
        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);
        drawable.setTint(preferences.overlayColor());
        view.setBackground(drawable);
    }

    public interface Callbacks {
        void onWallpaperOverlayFinish();
    }
}
