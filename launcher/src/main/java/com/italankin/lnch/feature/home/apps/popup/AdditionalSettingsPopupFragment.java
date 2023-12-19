package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.slider.Slider;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.editmode.EditModeProperties;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.Debouncer;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerDialog;
import com.italankin.lnch.util.widget.colorpicker.ColorPickerView;
import com.italankin.lnch.util.widget.popup.PopupFragment;

public class AdditionalSettingsPopupFragment extends PopupFragment {

    public static AdditionalSettingsPopupFragment newInstance(String requestKey, @Nullable Rect anchor) {
        AdditionalSettingsPopupFragment fragment = new AdditionalSettingsPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String BACKSTACK_NAME = "additional_settings";
    private static final String TAG = "additional_settings";

    private Preferences preferences;
    private EditModeState editModeState;

    private int baseColor = Color.BLACK;

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editModeState = LauncherApp.daggerService.main().editModeState();
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.widget_edit_mode_additional, itemsContainer, true);
        containerRoot.setClickable(true);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupWallpaperDim(view);
        setupTextSize(view);
        setupItemPadding(view);

        showPopup();
    }

    private void setupWallpaperDim(@NonNull View view) {
        Integer wallpaperDimColor = null;
        if (editModeState.isPropertySet(EditModeProperties.WALLPAPER_DIM)) {
            wallpaperDimColor = editModeState.getProperty(EditModeProperties.WALLPAPER_DIM);
        } else {
            int dimColor = preferences.get(Preferences.WALLPAPER_DIM_COLOR);
            if (dimColor != Color.TRANSPARENT) {
                wallpaperDimColor = baseColor = dimColor;
            }
        }

        Slider dimSlider = view.findViewById(R.id.wallpaper_dim_slider);
        dimSlider.setEnabled(wallpaperDimColor != null);
        dimSlider.setValue(wallpaperDimAmount(wallpaperDimColor));
        dimSlider.addOnChangeListener(new DebounceChangeListener(value -> {
            editModeState.setProperty(EditModeProperties.WALLPAPER_DIM, wallpaperDimColor(value, baseColor));
        }));

        View colorSelector = view.findViewById(R.id.wallpaper_dim_color);
        colorSelector.setEnabled(wallpaperDimColor != null);
        colorSelector.setOnClickListener(v -> {
            ColorPickerDialog.builder(requireContext())
                    .setColorModel(ColorPickerView.ColorModel.RGB)
                    .setSelectedColor(baseColor)
                    .setOnColorPickedListener(newColor -> {
                        baseColor = newColor;
                        editModeState.setProperty(EditModeProperties.WALLPAPER_DIM,
                                wallpaperDimColor(dimSlider.getValue(), baseColor));
                    })
                    .show(this);
        });

        CheckBox dimCheckBox = view.findViewById(R.id.wallpaper_dim_checkbox);
        dimCheckBox.setChecked(wallpaperDimColor != null);
        dimCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dimSlider.setEnabled(isChecked);
            colorSelector.setEnabled(isChecked);
            if (isChecked) {
                int newColor = wallpaperDimColor(dimSlider.getValue(), baseColor);
                editModeState.setProperty(EditModeProperties.WALLPAPER_DIM, newColor);
            } else {
                editModeState.setProperty(EditModeProperties.WALLPAPER_DIM, null);
            }
        });
    }

    private void setupTextSize(View view) {
        Slider slider = view.findViewById(R.id.text_size_slider);
        slider.setValueFrom(Preferences.ITEM_TEXT_SIZE.min());
        slider.setValueTo(Preferences.ITEM_TEXT_SIZE.max());
        // convert to int in case of non-zero decimal part
        slider.setValue(getCurrentValue(EditModeProperties.ITEM_TEXT_SIZE, Preferences.ITEM_TEXT_SIZE).intValue());
        slider.addOnChangeListener(new DebounceChangeListener(value -> {
            editModeState.setProperty(EditModeProperties.ITEM_TEXT_SIZE, value);
        }));
        View reset = view.findViewById(R.id.text_size_reset);
        reset.setOnClickListener(v -> {
            slider.setValue(Preferences.ITEM_TEXT_SIZE.defaultValue());
        });
    }

    private void setupItemPadding(View view) {
        Slider slider = view.findViewById(R.id.text_padding_slider);
        slider.setValueFrom(Preferences.ITEM_PADDING.min());
        slider.setValueTo(Preferences.ITEM_PADDING.max());
        slider.setValue(getCurrentValue(EditModeProperties.ITEM_PADDING, Preferences.ITEM_PADDING));
        slider.addOnChangeListener(new DebounceChangeListener(value -> {
            editModeState.setProperty(EditModeProperties.ITEM_PADDING, (int) value);
        }));
        View reset = view.findViewById(R.id.text_padding_reset);
        reset.setOnClickListener(v -> {
            slider.setValue(Preferences.ITEM_PADDING.defaultValue());
        });
    }

    private <T> T getCurrentValue(EditModeState.Property<T> prop, Preferences.Pref<T> pref) {
        T value = editModeState.getProperty(prop);
        return value != null ? value : preferences.get(pref);
    }

    private static int wallpaperDimColor(float dimAmount, int baseColor) {
        return baseColor & 0xffffff | (int) (255f * dimAmount) << 24;
    }

    private static float wallpaperDimAmount(Integer color) {
        if (color == null) {
            return 0f;
        }
        return (color >>> 24 & 0xff) / 255f;
    }

    private static class DebounceChangeListener implements Slider.OnChangeListener, Runnable {
        private final OnValueChange listener;
        private final Debouncer debouncer;

        private float value;

        private DebounceChangeListener(OnValueChange listener) {
            this.listener = listener;
            this.debouncer = new Debouncer(250);
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (fromUser) {
                this.value = value;
                debouncer.send(this);
            }
        }

        @Override
        public void run() {
            listener.onValueChange(value);
        }

        interface OnValueChange {
            void onValueChange(float value);
        }
    }
}
