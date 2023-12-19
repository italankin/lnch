package com.italankin.lnch.feature.settings.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.os.Bundle;
import android.util.Size;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.slider.Slider;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.CellSize;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;
import com.italankin.lnch.feature.widgets.util.WidgetSizeHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;

public class WidgetGridSettingsActivity extends AppCompatActivity {

    private Preferences preferences;

    private int gridSize;
    private float heightRatio;

    private WidgetResizeFrame widgetResizeFrame;
    private PreviewWidgetHostView widgetHostView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        setContentView(R.layout.activity_widget_grid_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        widgetResizeFrame = findViewById(R.id.widget_frame);
        widgetResizeFrame.setResizeMode(true, false);
        widgetHostView = new PreviewWidgetHostView(this);

        gridSize = preferences.get(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        heightRatio = preferences.get(Preferences.WIDGETS_HEIGHT_CELL_RATIO);

        setupSliders();

        updateWidgetPreview();
    }

    @SuppressLint("DefaultLocale")
    private void setupSliders() {
        Slider sliderGridSize = findViewById(R.id.slider_grid_size);
        sliderGridSize.setValueFrom(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.min());
        sliderGridSize.setValueTo(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE.max());
        sliderGridSize.setStepSize(1);
        sliderGridSize.setValue(gridSize);
        sliderGridSize.addOnChangeListener((slider, value, fromUser) -> {
            gridSize = (int) value;
            preferences.set(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE, gridSize);
            updateWidgetPreview();
        });

        Slider sliderHeightRatio = findViewById(R.id.slider_height_ratio);
        sliderHeightRatio.setValueFrom(Preferences.WIDGETS_HEIGHT_CELL_RATIO.min());
        sliderHeightRatio.setValueTo(Preferences.WIDGETS_HEIGHT_CELL_RATIO.max());
        sliderHeightRatio.setStepSize(0.25f);
        sliderHeightRatio.setValue(heightRatio);
        sliderHeightRatio.setLabelFormatter(value -> String.format("%.2f", value));
        sliderHeightRatio.addOnChangeListener((slider, value, fromUser) -> {
            heightRatio = value;
            preferences.set(Preferences.WIDGETS_HEIGHT_CELL_RATIO, heightRatio);
            updateWidgetPreview();
        });
    }

    private void updateWidgetPreview() {
        Size size = WidgetSizeHelper.calculateSizeForCell(this, gridSize, heightRatio);
        CellSize cellSize = new CellSize(size.getWidth(), size.getHeight(), gridSize, 2);
        AppWidget.Size appWidgetSize = new AppWidget.Size(cellSize.maxAvailableWidth(), cellSize.maxAvailableHeight());
        AppWidget appWidget = new AppWidget(AppWidgetManager.INVALID_APPWIDGET_ID, null, new Bundle(), appWidgetSize);

        widgetResizeFrame.bindAppWidget(appWidget, widgetHostView);
        widgetResizeFrame.setCellSize(cellSize);

        ViewGroup.LayoutParams lp = widgetResizeFrame.getLayoutParams();
        lp.width = appWidgetSize.width;
        lp.height = appWidgetSize.height;
        widgetResizeFrame.setLayoutParams(lp);
    }
}
