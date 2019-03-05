package com.italankin.lnch.feature.settings.lookfeel;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
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

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings.BackButtonHandler;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.adapter.SeekBarChangeListener;
import com.italankin.lnch.util.dialogfragment.ListenerFragment;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;
import com.italankin.lnch.util.widget.pref.SliderPrefView;
import com.italankin.lnch.util.widget.pref.ValuePrefView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ItemAppearanceFragment extends AppFragment implements BackButtonHandler {

    private static final String TAG_OVERLAY_COLOR_PICKER = "overlay_color_picker";
    private static final String TAG_SHADOW_COLOR_PICKER = "shadow_color_picker";
    private static final String TAG_PREVIEW_OVERLAY = "preview_overlay";
    private static final String TAG_DISCARD_CHANGES = "discard_changes";

    private static final int REQUEST_CODE_PERMISSION = 1;

    private Preferences preferences;

    private TextView preview;

    private SliderPrefView itemTextSize;
    private ValuePrefView itemFont;
    private SliderPrefView itemPadding;
    private SliderPrefView itemShadowRadius;
    private ValuePrefView itemShadowColor;

    private Callbacks callbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        preferences = daggerService().main().getPreferences();
    }

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_item_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRoot(view);

        initOverlay(view);
        initPreview(view);

        initTextSize(view);
        initFont(view);
        initPadding(view);
        initShadowRadius(view);
        initShadowColor(view);

        updatePreview();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updatePreview();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_item_appearance, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                if (callbacks != null) {
                    callbacks.onItemAppearanceFinish();
                }
                return true;
            case R.id.action_reset:
                preferences.reset(
                        Preferences.ITEM_TEXT_SIZE,
                        Preferences.ITEM_PADDING,
                        Preferences.ITEM_SHADOW_RADIUS,
                        Preferences.ITEM_FONT,
                        Preferences.ITEM_SHADOW_COLOR
                );
                if (callbacks != null) {
                    callbacks.onItemAppearanceFinish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showWallpaper(getView());
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(requireContext(), R.string.error_no_wallpaper_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isChanged()) {
            new SimpleDialogFragment.Builder()
                    .setMessage(R.string.settings_item_appearance_discard_message)
                    .setPositiveButton(R.string.settings_item_appearance_discard_button)
                    .setNegativeButton(R.string.cancel)
                    .setListenerProvider(new DiscardChangesListenerProvider())
                    .build()
                    .show(getChildFragmentManager(), TAG_DISCARD_CHANGES);
            return false;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        preview = null;
        itemTextSize = null;
        itemFont = null;
        itemPadding = null;
        itemShadowRadius = null;
        itemShadowColor = null;
    }

    private static class DiscardChangesListenerProvider implements ListenerFragment<SimpleDialogFragment.Listener> {
        @Override
        public SimpleDialogFragment.Listener get(Fragment parentFragment) {
            return () -> {
                Callbacks callbacks = ((ItemAppearanceFragment) parentFragment).callbacks;
                if (callbacks != null) {
                    callbacks.onItemAppearanceFinish();
                }
            };
        }
    }

    private void initRoot(View view) {
        if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            return;
        }
        showWallpaper(view);
    }

    private void showWallpaper(View view) {
        Context context = requireContext();
        WallpaperManager wm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
        if (wm == null || context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        view.<ImageView>findViewById(R.id.wallpaper).setImageDrawable(wm.getDrawable());
    }

    private void initOverlay(View view) {
        View overlay = view.findViewById(R.id.overlay);
        overlay.setBackgroundColor(preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR));
        overlay.setOnClickListener(v -> {
            Drawable background = v.getBackground();
            int selectedColor;
            if (background instanceof ColorDrawable) {
                selectedColor = ((ColorDrawable) background).getColor();
            } else {
                selectedColor = preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR);
            }
            new ColorPickerDialogFragment.Builder()
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(selectedColor)
                    .setListenerProvider(new OverlayColorPickerListenerProvider())
                    .build()
                    .show(getChildFragmentManager(), TAG_OVERLAY_COLOR_PICKER);
        });
    }

    private static class OverlayColorPickerListenerProvider implements ListenerFragment<ColorPickerDialogFragment.Listener> {
        @Override
        public ColorPickerDialogFragment.Listener get(Fragment fragment) {
            return newColor -> {
                View view = fragment.getView();
                if (view != null) {
                    view.findViewById(R.id.overlay).setBackgroundColor(newColor);
                }
            };
        }
    }

    private void initPreview(View view) {
        preview = view.findViewById(R.id.item_preview);
        int backgroundColor = ResUtils.resolveColor(requireContext(), R.attr.colorSelector);
        preview.setBackgroundColor(backgroundColor & 0x20ffffff);
        preview.setText(R.string.settings_item_preview);
        preview.setAllCaps(true);
        preview.setTextColor(ResUtils.resolveColor(requireContext(), R.attr.colorAccent));
        preview.setOnClickListener(v -> {
            new ColorPickerDialogFragment.Builder()
                    .setSelectedColor(preview.getCurrentTextColor())
                    .setListenerProvider(new PreviewColorPickerListenerProvider())
                    .build()
                    .show(getChildFragmentManager(), TAG_PREVIEW_OVERLAY);
        });
    }

    private static class PreviewColorPickerListenerProvider implements ListenerFragment<ColorPickerDialogFragment.Listener> {
        @Override
        public ColorPickerDialogFragment.Listener get(Fragment parentFragment) {
            ItemAppearanceFragment fragment = (ItemAppearanceFragment) parentFragment;
            return fragment.preview::setTextColor;
        }
    }

    private void initTextSize(View view) {
        itemTextSize = view.findViewById(R.id.item_text_size);
        setParams(itemTextSize, Preferences.ITEM_TEXT_SIZE);
        itemTextSize.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initFont(View view) {
        String[] fontTitles = getResources().getStringArray(R.array.settings_item_appearance_text_font_titles);

        itemFont = view.findViewById(R.id.item_font);
        Preferences.Font font = preferences.get(Preferences.ITEM_FONT);
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
            public CharSequence getDescription() {
                return fontTitles[value.ordinal()];
            }
        });
        itemFont.setValue(font);
        itemFont.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.settings_item_appearance_text_font);
            builder.setItems(fontTitles, (dialog, which) -> {
                Preferences.Font newFont = Preferences.Font.values()[which];
                itemFont.setValue(newFont);
                updatePreview();
            });
            builder.show();
        });
    }

    private void initPadding(View view) {
        itemPadding = view.findViewById(R.id.item_padding);
        setParams(itemPadding, Preferences.ITEM_PADDING);
        itemPadding.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initShadowRadius(View view) {
        itemShadowRadius = view.findViewById(R.id.item_shadow_radius);
        setParams(itemShadowRadius, Preferences.ITEM_SHADOW_RADIUS);
        itemShadowRadius.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
        }));
    }

    private void initShadowColor(View view) {
        itemShadowColor = view.findViewById(R.id.item_shadow_color);
        Integer shadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
        itemShadowColor.setValueHolder(new ValuePrefView.ColorValueHolder());
        itemShadowColor.setValue(shadowColor != null
                ? shadowColor
                : ResUtils.resolveColor(requireContext(), R.attr.colorItemShadowDefault));
        itemShadowColor.setOnClickListener(v -> {
            new ColorPickerDialogFragment.Builder()
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(preview.getShadowColor())
                    .showResetButton(true)
                    .setListenerProvider(new ShadowColorListenerProvider())
                    .build()
                    .show(getChildFragmentManager(), TAG_SHADOW_COLOR_PICKER);
        });
    }

    private static class ShadowColorListenerProvider implements ListenerFragment<ColorPickerDialogFragment.Listener> {
        @Override
        public ColorPickerDialogFragment.Listener get(Fragment parentFragment) {
            ItemAppearanceFragment fragment = (ItemAppearanceFragment) parentFragment;
            return new ColorPickerDialogFragment.Listener() {
                @Override
                public void onColorPicked(int newColor) {
                    fragment.itemShadowColor.setValue(newColor);
                    fragment.updatePreview();
                }

                @Override
                public void onColorReset() {
                    int color = ResUtils.resolveColor(fragment.requireContext(), R.attr.colorItemShadowDefault);
                    fragment.itemShadowColor.setValue(color);
                    fragment.updatePreview();
                }
            };
        }
    }

    private void updatePreview() {
        Preferences.Font font = itemFont.getValue();
        int textSize = (int) (Preferences.ITEM_TEXT_SIZE.min() + itemTextSize.getProgress());
        int padding = Preferences.ITEM_PADDING.min() + itemPadding.getProgress();
        int shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        preview.setTypeface(font.typeface());
        int p = ResUtils.px2dp(requireContext(), padding);
        ViewUtils.setPadding(preview, p);
        preview.setTextSize(textSize);
        preview.setShadowLayer(shadowRadius, preview.getShadowDx(), preview.getShadowDy(), shadowColor);
    }

    private void save() {
        float textSize = itemTextSize.getProgress() + Preferences.ITEM_TEXT_SIZE.min();
        int padding = itemPadding.getProgress() + Preferences.ITEM_PADDING.min();
        float shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        Preferences.Font font = itemFont.getValue();
        preferences.set(Preferences.ITEM_TEXT_SIZE, textSize);
        preferences.set(Preferences.ITEM_PADDING, padding);
        preferences.set(Preferences.ITEM_SHADOW_RADIUS, shadowRadius);
        preferences.set(Preferences.ITEM_SHADOW_COLOR, shadowColor);
        preferences.set(Preferences.ITEM_FONT, font);
    }

    private boolean isChanged() {
        float textSize = itemTextSize.getProgress() + Preferences.ITEM_TEXT_SIZE.min();
        if (Float.compare(textSize, preferences.get(Preferences.ITEM_TEXT_SIZE)) != 0) {
            return true;
        }

        int padding = itemPadding.getProgress() + Preferences.ITEM_PADDING.min();
        if (padding != preferences.get(Preferences.ITEM_PADDING)) {
            return true;
        }

        float shadowRadius = itemShadowRadius.getProgress();
        if (Float.compare(shadowRadius, preferences.get(Preferences.ITEM_SHADOW_RADIUS)) != 0) {
            return true;
        }

        int shadowColor = itemShadowColor.getValue();
        Integer currentShadowColor = preferences.get(Preferences.ITEM_SHADOW_COLOR);
        if (currentShadowColor == null) {
            if (shadowColor != ResUtils.resolveColor(requireContext(), R.attr.colorItemShadowDefault)) {
                return true;
            }
        } else if (currentShadowColor != shadowColor) {
            return true;
        }

        return itemFont.getValue() != preferences.get(Preferences.ITEM_FONT);
    }

    private void setParams(SliderPrefView prefView, Preferences.RangePref<? extends Number> pref) {
        int min = pref.min().intValue();
        prefView.setProgress(preferences.get(pref).intValue() - min);
        prefView.setMax(pref.max().intValue() - min);
    }

    public interface Callbacks {
        void onItemAppearanceFinish();
    }
}
