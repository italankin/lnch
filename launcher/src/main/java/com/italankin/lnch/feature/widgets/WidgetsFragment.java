package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
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
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class WidgetsFragment extends AppFragment implements WidgetsView {

    private static final int APP_WIDGET_HOST_ID = 101;

    private static final int REQUEST_PICK_APPWIDGET = 0;
    private static final int REQUEST_CREATE_APPWIDGET = 1;

    @InjectPresenter
    WidgetsPresenter presenter;

    private LceLayout lce;
    private ViewGroup widgetContainer;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private Picasso picasso;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ActionPopupWindow popupWindow;

    @ProvidePresenter
    WidgetsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().widgets();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = LauncherApp.daggerService.main().getPicassoFactory().create(requireContext());
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

        view.findViewById(R.id.add_widget).setOnClickListener(v -> startAddNewWidget());

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
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                    configureWidget(appWidgetId, info);
                } else {
                    cancelAddNewWidget();
                }
                break;
            case REQUEST_CREATE_APPWIDGET:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                    addWidgetView(appWidgetId, info, false);
                    newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                } else {
                    cancelAddNewWidget();
                }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
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
            showDeleteDialog(appWidgetId, widgetView);
            return true;
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

    private void showDeleteDialog(int appWidgetId, AppWidgetHostView widgetView) {
        Rect bounds = new Rect();
        lce.getWindowVisibleDisplayFrame(bounds);
        Context context = requireContext();
        popupWindow = new ActionPopupWindow(context, picasso)
                .addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(R.string.widgets_app_info)
                        .setOnClickListener(v -> showAppInfo(widgetView))
                        .setIconDrawableTintAttr(R.attr.colorAccent)
                        .setIcon(R.drawable.ic_app_info))
                .addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(R.string.widgets_remove)
                        .setOnClickListener(v -> showRemoveConfirmDialog(appWidgetId, widgetView))
                        .setIconDrawableTintAttr(R.attr.colorAccent)
                        .setIcon(R.drawable.ic_action_delete));
        popupWindow.showAtAnchor(widgetView, bounds);
    }

    private void showRemoveConfirmDialog(int appWidgetId, AppWidgetHostView widgetView) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.widgets_remove_dialog_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.widgets_remove_dialog_action, (dialog, which) -> {
                    widgetContainer.removeView(widgetView);
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                    // TODO delete widget ID
                    updateWidgetsView();
                })
                .create()
                .show();
    }

    private void showAppInfo(AppWidgetHostView widgetView) {
        String packageName = widgetView.getAppWidgetInfo().provider.getPackageName();
        IntentUtils.safeStartAppSettings(requireContext(), packageName, widgetView);
    }

    private void updateWidgetsView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appWidgetHost.getAppWidgetIds().length == 0) {
                showNoWidgets();
            }
        } else {
            // TODO
        }
    }
}
