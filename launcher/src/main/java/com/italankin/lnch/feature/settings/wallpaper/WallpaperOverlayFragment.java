package com.italankin.lnch.feature.settings.wallpaper;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WallpaperOverlayFragment extends AppFragment implements ActivityResultCallback<Boolean>,
        SettingsToolbarTitle {

    public static WallpaperOverlayFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        WallpaperOverlayFragment fragment = new WallpaperOverlayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Preferences preferences;
    private FontManager fontManager;
    private ColorPickerView colorPicker;
    private ImageView wallpaper;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this);

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_wallpaper_overlay_color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferences = LauncherApp.daggerService.main().preferences();
        fontManager = LauncherApp.daggerService.main().typefaceStorage();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_wallpaper_overlay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initItemPreview(view);

        wallpaper = view.findViewById(R.id.wallpaper);
        initWallpaper();

        colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setColorChangedListener(color -> {
            Drawable background = wallpaper.getDrawable();
            if (background != null) {
                background.setTint(color);
            }
        });
        colorPicker.setSelectedColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_wallpaper_overlay, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            preferences.set(Preferences.WALLPAPER_OVERLAY_COLOR, colorPicker.getSelectedColor());
            sendResult(new WallpaperOverlayFinishContract().result());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        colorPicker = null;
        wallpaper = null;
    }

    @Override
    public void onActivityResult(Boolean permissionGranted) {
        if (permissionGranted) {
            showWallpaper();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(requireContext(), R.string.error_no_wallpaper_permission, Toast.LENGTH_LONG).show();
        }
    }

    private void initWallpaper() {
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            showWallpaper();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void initItemPreview(View view) {
        TextView itemPreview = view.findViewById(R.id.item_preview).findViewById(R.id.label);
        itemPreview.setText(R.string.preview);
        itemPreview.setAllCaps(true);
        itemPreview.setTextColor(ResUtils.resolveColor(requireContext(), android.R.attr.colorAccent));
        itemPreview.setOnClickListener(v -> {
            ColorPickerDialog.builder(requireContext())
                    .setSelectedColor(itemPreview.getCurrentTextColor())
                    .setOnColorPickedListener(itemPreview::setTextColor)
                    .show();
        });

        Context context = requireContext();
        ViewUtils.setPaddingDp(itemPreview, preferences.get(Preferences.ITEM_PADDING));
        itemPreview.setTextSize(preferences.get(Preferences.ITEM_TEXT_SIZE));
        Integer shadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
        if (shadowColor == null) {
            shadowColor = ResUtils.resolveColor(context, R.attr.colorItemShadowDefault);
        }
        itemPreview.setShadowLayer(preferences.get(Preferences.ITEM_SHADOW_RADIUS),
                itemPreview.getShadowDx(), itemPreview.getShadowDy(), shadowColor);
        itemPreview.setTypeface(fontManager.getTypeface(preferences.get(Preferences.ITEM_FONT)));
    }

    private void showWallpaper() {
        Context context = requireContext();
        WallpaperManager wm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
        if (wm == null || context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Drawable drawable = wm.getDrawable();
        if (drawable == null) {
            return;
        }
        drawable.setTintMode(PorterDuff.Mode.SRC_ATOP);
        drawable.setTint(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        wallpaper.setImageDrawable(drawable);
    }

    public static class WallpaperOverlayFinishContract extends SignalFragmentResultContract {
        public WallpaperOverlayFinishContract() {
            super("wallpaper_overlay_finish");
        }
    }
}
