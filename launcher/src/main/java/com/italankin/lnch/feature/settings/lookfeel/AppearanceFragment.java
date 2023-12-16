package com.italankin.lnch.feature.settings.lookfeel;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultManager;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.feature.settings.fonts.FontsFragment;
import com.italankin.lnch.feature.settings.util.TargetPreference;
import com.italankin.lnch.model.fonts.FontManager;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.adapter.SeekBarChangeListener;
import com.italankin.lnch.util.dialogfragment.SimpleDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.BackdropDrawable;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialogFragment;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;
import com.italankin.lnch.util.widget.pref.SliderPrefView;
import com.italankin.lnch.util.widget.pref.ValuePrefView;

public class AppearanceFragment extends AppFragment implements
        ColorPickerDialogFragment.Listener,
        SimpleDialogFragment.Listener,
        SettingsToolbarTitle {

    public static AppearanceFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        AppearanceFragment fragment = new AppearanceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TAG_OVERLAY_COLOR = "overlay_color";
    private static final String TAG_SHADOW_COLOR = "shadow_color";
    private static final String TAG_PREVIEW_OVERLAY = "preview_overlay";
    private static final String TAG_DISCARD_CHANGES = "discard_changes";

    private static final String REQUEST_KEY_APPEARANCE = "appearance";

    private Preferences preferences;
    private FontManager fontManager;

    private OnBackPressedCallback onBackPressedCallback;

    private ImageView wallpaper;
    private TextView preview;
    private View overlay;

    private SliderPrefView itemTextSize;
    private ValuePrefView itemFont;
    private SliderPrefView itemPadding;
    private SliderPrefView itemShadowRadius;
    private ValuePrefView itemShadowColor;

    private PreviewBackground previewBackground = PreviewBackground.WALLPAPER;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_laf_appearance);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        fontManager = LauncherApp.daggerService.main().typefaceStorage();
        setHasOptionsMenu(true);

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                new SimpleDialogFragment.Builder()
                        .setMessage(R.string.settings_home_laf_appearance_discard_message)
                        .setPositiveButton(R.string.settings_home_laf_appearance_discard_button)
                        .setNegativeButton(R.string.cancel)
                        .build()
                        .show(getChildFragmentManager(), TAG_DISCARD_CHANGES);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        new FragmentResultManager(getParentFragmentManager(), this, REQUEST_KEY_APPEARANCE)
                .register(new FontsFragment.OnFontSelected(), result -> {
                    itemFont.setValue(result);
                    updatePreview();
                    onBackPressedCallback.setEnabled(true);
                })
                .register(new FontsFragment.OnFontDeleted(), result -> {
                    itemFont.setValue(preferences.get(Preferences.ITEM_FONT));
                    updatePreview();
                    onBackPressedCallback.setEnabled(true);
                })
                .attach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_appearance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        wallpaper = view.findViewById(R.id.wallpaper);

        initOverlay(view);
        initPreview(view);

        initTextSize(view);
        initFont(view);
        initPadding(view);
        initShadowRadius(view);
        initShadowColor(view);

        updatePreview();
        updatePreviewBackground();

        String target = TargetPreference.get(this);
        if (target != null) {
            if (Preferences.ITEM_TEXT_SIZE.key().equals(target)) {
                highlightTarget(itemTextSize);
            } else if (Preferences.ITEM_PADDING.key().equals(target)) {
                highlightTarget(itemPadding);
            } else if (Preferences.ITEM_SHADOW_RADIUS.key().equals(target)) {
                highlightTarget(itemShadowRadius);
            } else if (Preferences.ITEM_SHADOW_COLOR.key().equals(target)) {
                highlightTarget(itemShadowColor);
            } else if (Preferences.ITEM_FONT.key().equals(target)) {
                highlightTarget(itemFont);
            }
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        updatePreview();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_appearance, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_save) {
            save();
            sendResult(new AppearanceFinishedContract().result());
            return true;
        } else if (itemId == R.id.action_reset) {
            preferences.reset(
                    Preferences.ITEM_TEXT_SIZE,
                    Preferences.ITEM_PADDING,
                    Preferences.ITEM_SHADOW_RADIUS,
                    Preferences.ITEM_FONT,
                    Preferences.ITEM_SHADOW_COLOR
            );
            sendResult(new AppearanceFinishedContract().result());
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onColorPicked(@Nullable String tag, int newColor) {
        if (tag == null) {
            return;
        }
        switch (tag) {
            case TAG_OVERLAY_COLOR: {
                overlay.setBackgroundColor(newColor);
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
            int color = ResUtils.resolveColor(requireContext(), R.attr.colorItemShadowDefault);
            itemShadowColor.setValue(color);
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        }
    }

    @Override
    public void onPositiveButtonClick(String tag) {
        if (TAG_DISCARD_CHANGES.equals(tag)) {
            sendResult(new AppearanceFinishedContract().result());
        }
    }

    private void initOverlay(View view) {
        int overlayColor = preferences.get(Preferences.WALLPAPER_OVERLAY_SHOW)
                ? preferences.get(Preferences.WALLPAPER_OVERLAY_COLOR)
                : Color.TRANSPARENT;
        overlay = view.findViewById(R.id.overlay);
        overlay.setBackgroundColor(overlayColor);
        overlay.setOnClickListener(v -> {
            Drawable background = v.getBackground();
            int selectedColor;
            if (background instanceof ColorDrawable) {
                selectedColor = ((ColorDrawable) background).getColor();
            } else {
                selectedColor = overlayColor;
            }
            new ColorPickerDialogFragment.Builder()
                    .setColorModel(ColorPickerView.ColorModel.ARGB)
                    .setSelectedColor(selectedColor)
                    .build()
                    .show(getChildFragmentManager(), TAG_OVERLAY_COLOR);
        });
    }

    private void initPreview(View view) {
        preview = view.findViewById(R.id.item_preview).findViewById(R.id.label);
        preview.setBackgroundResource(R.drawable.selector_item_appearance);
        String previewText = LauncherApp.daggerService.main()
                .nameNormalizer()
                .normalize(getString(R.string.preview));
        preview.setText(previewText);
        preview.setTextColor(ResUtils.resolveColor(requireContext(), android.R.attr.colorPrimary));
        preview.setOnClickListener(v -> {
            new ColorPickerDialogFragment.Builder()
                    .setSelectedColor(preview.getCurrentTextColor())
                    .build()
                    .show(getChildFragmentManager(), TAG_PREVIEW_OVERLAY);
        });
        View previewBackgroundSwitcher = view.findViewById(R.id.preview_background_switcher);
        previewBackgroundSwitcher.setOnClickListener(v -> {
            previewBackground = PreviewBackground.values()[
                    (previewBackground.ordinal() + 1) % PreviewBackground.values().length];
            updatePreviewBackground();
        });
    }

    private void updatePreviewBackground() {
        switch (previewBackground) {
            case WALLPAPER:
                wallpaper.setImageDrawable(new BackdropDrawable(requireContext()));
                overlay.setVisibility(View.VISIBLE);
                break;
            case WHITE:
                wallpaper.setImageDrawable(new ColorDrawable(Color.WHITE));
                overlay.setVisibility(View.INVISIBLE);
                break;
            case BLACK:
                wallpaper.setImageDrawable(new ColorDrawable(Color.BLACK));
                overlay.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void initTextSize(View view) {
        itemTextSize = view.findViewById(R.id.item_text_size);
        setParams(itemTextSize, Preferences.ITEM_TEXT_SIZE);
        itemTextSize.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        }));
    }

    private void initFont(View view) {
        itemFont = view.findViewById(R.id.item_font);
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
        itemFont.setOnClickListener(v -> sendResult(ShowFontSelectContract.result(REQUEST_KEY_APPEARANCE)));
    }

    private void initPadding(View view) {
        itemPadding = view.findViewById(R.id.item_padding);
        setParams(itemPadding, Preferences.ITEM_PADDING);
        itemPadding.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
        }));
    }

    private void initShadowRadius(View view) {
        itemShadowRadius = view.findViewById(R.id.item_shadow_radius);
        setParams(itemShadowRadius, Preferences.ITEM_SHADOW_RADIUS);
        itemShadowRadius.setOnSeekBarChangeListener(new SeekBarChangeListener((progress, fromUser) -> {
            updatePreview();
            onBackPressedCallback.setEnabled(true);
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
                    .build()
                    .show(getChildFragmentManager(), TAG_SHADOW_COLOR);
        });
    }

    private void updatePreview() {
        int textSize = (int) (Preferences.ITEM_TEXT_SIZE.min() + itemTextSize.getProgress());
        int padding = Preferences.ITEM_PADDING.min() + itemPadding.getProgress();
        int shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        String font = itemFont.getValue();
        preview.setTypeface(fontManager.getTypeface(font));
        ViewUtils.setPaddingDp(preview, padding);
        preview.setTextSize(textSize);
        preview.setShadowLayer(shadowRadius, preview.getShadowDx(), preview.getShadowDy(), shadowColor);
    }

    private void save() {
        float textSize = itemTextSize.getProgress() + Preferences.ITEM_TEXT_SIZE.min();
        int padding = itemPadding.getProgress() + Preferences.ITEM_PADDING.min();
        float shadowRadius = itemShadowRadius.getProgress();
        int shadowColor = itemShadowColor.getValue();
        String font = itemFont.getValue();
        preferences.set(Preferences.ITEM_TEXT_SIZE, textSize);
        preferences.set(Preferences.ITEM_PADDING, padding);
        preferences.set(Preferences.ITEM_SHADOW_RADIUS, shadowRadius);
        preferences.set(Preferences.ITEM_SHADOW_COLOR, shadowColor);
        preferences.set(Preferences.ITEM_FONT, font);
    }

    private void setParams(SliderPrefView prefView, Preferences.RangePref<? extends Number> pref) {
        int min = pref.min().intValue();
        prefView.setProgress(preferences.get(pref).intValue() - min);
        prefView.setMax(pref.max().intValue() - min);
    }

    private void highlightTarget(View view) {
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> ViewUtils.setTemporaryPressedState(view, TargetPreference.HIGHLIGHT_DURATION),
                TargetPreference.HIGHLIGHT_DELAY);
    }

    public static class ShowFontSelectContract implements FragmentResultContract<String> {
        private static final String KEY = "show_font_select";
        private static final String REQUEST_KEY = "request_key";

        static Bundle result(String requestKey) {
            Bundle bundle = new Bundle();
            bundle.putString(RESULT_KEY, KEY);
            bundle.putString(REQUEST_KEY, requestKey);
            return bundle;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public String parseResult(Bundle result) {
            return result.getString(REQUEST_KEY);
        }
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
