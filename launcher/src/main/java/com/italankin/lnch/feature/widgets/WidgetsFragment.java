package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class WidgetsFragment extends AppFragment implements WidgetsView {

    private static final int APP_WIDGET_HOST_ID = 101;

    private static final int REQUEST_PICK_APPWIDGET = 0;
    private static final int REQUEST_CREATE_APPWIDGET = 1;
    private static final int REQUEST_BIND_APPWIDGET = 2;

    @InjectPresenter
    WidgetsPresenter presenter;

    private LceLayout lce;
    private ViewGroup widgetContainer;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @ProvidePresenter
    WidgetsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().widgets();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appWidgetHost = new LauncherAppWidgetHost(context, APP_WIDGET_HOST_ID);
        appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_widgets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lce = view.findViewById(R.id.lce_widgets);
        widgetContainer = view.findViewById(R.id.widget_container);

        registerWindowInsets(view);

        widgetContainer.setOnLongClickListener(v -> {
            startAddNewWidget();
            return true;
        });

        View addWidgetButton = view.findViewById(R.id.add_widget);
        addWidgetButton.setOnClickListener(v -> startAddNewWidget());
        addWidgetButton.setOnLongClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Reset widgets?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reset", (dialog, which) -> {
                        for (int i = widgetContainer.getChildCount(); i >= 0; i--) {
                            View child = widgetContainer.getChildAt(i);
                            if (child instanceof AppWidgetHostView) {
                                int appWidgetId = ((AppWidgetHostView) child).getAppWidgetId();
                                appWidgetHost.deleteAppWidgetId(appWidgetId);
                                widgetContainer.removeViewAt(i);
                            }
                        }
                        appWidgetHost.deleteHost();
                        showNoWidgets();
                    })
                    .create();
            alertDialog.show();
            return true;
        });

        if (addBoundWidgets()) {
            lce.showContent();
        } else {
            showNoWidgets();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_APPWIDGET:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                    addAppWidget(appWidgetId, info);
                } else {
                    cancelAddNewWidget();
                }
                break;
            case REQUEST_CREATE_APPWIDGET:
                if (resultCode == Activity.RESULT_OK) {
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
                    addWidgetView(newAppWidgetId, info, false);
                    newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                } else {
                    cancelAddNewWidget();
                }
                break;
            case REQUEST_BIND_APPWIDGET:
                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
                configureWidget(newAppWidgetId, info);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        appWidgetHost.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        appWidgetHost.stopListening();
    }

    private boolean addBoundWidgets() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // TODO get a list of all the appWidgetIds that are bound to the current host
            return false;
        } else {
            int[] appWidgetIds = appWidgetHost.getAppWidgetIds();
            for (int appWidgetId : appWidgetIds) {
                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                addWidgetView(appWidgetId, info, true);
            }
            return appWidgetIds.length > 0;
        }
    }

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newAppWidgetId)
                .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, new ArrayList<>())
                .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, new ArrayList<>());
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void addAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        if (appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)) {
            configureWidget(appWidgetId, info);
        } else {
            Bundle options = createWidgetOptions(false);
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN);
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, info.getProfile())
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
            startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
        }
    }

    private void configureWidget(int appWidgetId, AppWidgetProviderInfo info) {
        if (info.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                    .setComponent(info.configure)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            addWidgetView(appWidgetId, info, false);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // TODO save appWidgetId
            }
            newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        }
    }

    private void addWidgetView(int appWidgetId, AppWidgetProviderInfo info, boolean restored) {
        AppWidgetHostView widgetView = appWidgetHost.createView(appWidgetId, info);
        Bundle options = createWidgetOptions(restored);
        widgetView.updateAppWidgetOptions(options);
        widgetView.setOnLongClickListener(v -> {
            AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                    .setTitle("Delete widget?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        widgetContainer.removeView(widgetView);
                        appWidgetHost.deleteAppWidgetId(appWidgetId);
                        if (widgetContainer.getChildCount() == 1) {
                            showNoWidgets();
                        }
                    })
                    .create();
            alertDialog.show();
            return false;
        });
        widgetContainer.addView(widgetView);
        lce.showContent();
    }

    private void cancelAddNewWidget() {
        if (newAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void registerWindowInsets(View view) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            widgetContainer.setPadding(0, 0, 0, insets.getStableInsetBottom());
            return insets;
        });

        WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
            widgetContainer.setPadding(0, 0, 0, insets.getStableInsetBottom());
        }
    }

    private void showNoWidgets() {
        lce.error()
                .message(R.string.widgets_empty)
                .button(R.string.widgets_add, v -> {
                    // TODO
                    startAddNewWidget();
                })
                .show();
    }

    private Bundle createWidgetOptions(boolean restored) {
        Bundle options = new Bundle();
        // TODO set min width & height
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widgetContainer.getMinimumWidth());
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widgetContainer.getWidth());
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widgetContainer.getMinimumHeight());
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, widgetContainer.getHeight()); // TODO constrain height
        if (restored && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            options.putBoolean(AppWidgetManager.OPTION_APPWIDGET_RESTORE_COMPLETED, true);
        }
        return options;
    }
}
