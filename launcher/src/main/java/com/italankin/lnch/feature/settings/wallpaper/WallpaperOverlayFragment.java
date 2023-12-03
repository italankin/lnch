package com.italankin.lnch.feature.settings.wallpaper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.widget.colorpicker.BackdropDrawable;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;

public class WallpaperOverlayFragment extends AppFragment implements SettingsToolbarTitle {

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
        BackdropDrawable drawable = new BackdropDrawable(requireContext());
        drawable.setColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        wallpaper.setImageDrawable(drawable);

        colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setColorChangedListener(color -> {
            Drawable background = wallpaper.getDrawable();
            if (background instanceof BackdropDrawable) {
                ((BackdropDrawable) background).setColor(color);
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

    public static class WallpaperOverlayFinishContract extends SignalFragmentResultContract {
        public WallpaperOverlayFinishContract() {
            super("wallpaper_overlay_finish");
        }
    }
}
