package com.italankin.lnch.feature.home.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
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
import com.italankin.lnch.util.widget.LceLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class WidgetsFragment extends AppFragment implements WidgetsView {

    private static final int APP_WIDGET_HOST_ID = 101;

    private static final int APP_WIDGET_ID_NONE = -1;

    private static final int REQUEST_PICK_APPWIDGET = 0;
    private static final int REQUEST_CREATE_APPWIDGET = 1;
    private static final int REQUEST_BIND_APPWIDGET = 2;

    @InjectPresenter
    WidgetsPresenter presenter;

    private LceLayout lce;
    private ViewGroup widgetContainer;

    private AppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private int newAppWidgetId = APP_WIDGET_ID_NONE;

    @ProvidePresenter
    WidgetsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().widgets();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appWidgetHost = new AppWidgetHost(context, APP_WIDGET_HOST_ID);
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

        view.findViewById(R.id.add_widget).setOnClickListener(v -> startAddNewWidget());

        view.findViewById(R.id.add_widget).setOnLongClickListener(v -> {
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
                if (data != null) {
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                    addAppWidget(appWidgetId, info);
                } else if (newAppWidgetId != APP_WIDGET_ID_NONE) {
                    cancelAddNewWidget();
                }
                break;
            case REQUEST_CREATE_APPWIDGET:
                if (resultCode == Activity.RESULT_OK) {
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
                    addWidgetView(newAppWidgetId, info);
                    newAppWidgetId = APP_WIDGET_ID_NONE;
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
            // TODO
            return false;
        } else {
            int[] appWidgetIds = appWidgetHost.getAppWidgetIds();
            for (int appWidgetId : appWidgetIds) {
                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                addWidgetView(appWidgetId, info);
            }
            return appWidgetIds.length > 0;
        }
    }

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newAppWidgetId);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    private void addAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        if (appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)) {
            configureWidget(appWidgetId, info);
        } else {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider);
            startActivityForResult(intent, REQUEST_BIND_APPWIDGET);
        }
    }

    private void configureWidget(int appWidgetId, AppWidgetProviderInfo info) {
        if (info.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(info.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            addWidgetView(appWidgetId, info);
            // TODO write widget id
            newAppWidgetId = APP_WIDGET_ID_NONE;
        }
    }

    private void addWidgetView(int appWidgetId, AppWidgetProviderInfo info) {
        AppWidgetHostView widgetView = appWidgetHost.createView(requireContext(), appWidgetId, info);
        widgetContainer.addView(widgetView);
        lce.showContent();
    }

    private void cancelAddNewWidget() {
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
        newAppWidgetId = APP_WIDGET_ID_NONE;
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
}
