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
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.widgets.adapter.AddWidgetAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetCompositeAdapter;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AddWidget;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.WidgetAdapterItem;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class WidgetsFragment extends AppFragment implements WidgetsView {

    private static final int APP_WIDGET_HOST_ID = 101;

    private static final int REQUEST_PICK_APPWIDGET = 0;
    private static final int REQUEST_CREATE_APPWIDGET = 1;

    @InjectPresenter
    WidgetsPresenter presenter;

    private LceLayout lce;
    private RecyclerView widgetsList;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private Picasso picasso;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ActionPopupWindow popupWindow;

    private WidgetCompositeAdapter adapter;
    private final List<WidgetAdapterItem> appWidgets = new ArrayList<>(Collections.singleton(new AddWidget()));

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
        widgetsList = view.findViewById(R.id.widgets_list);

        adapter = new WidgetCompositeAdapter.Builder(requireContext())
                .add(new WidgetAdapter(appWidgetHost, (appWidgetId, hostView) -> {
                    showActionsPopup(appWidgetId, hostView);
                    return true;
                }))
                .add(new AddWidgetAdapter(v -> {
                    startAddNewWidget();
                }))
                .recyclerView(widgetsList)
                .create();

        registerWindowInsets(view);

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
                    cancelAddNewWidget(newAppWidgetId);
                }
                break;
            case REQUEST_CREATE_APPWIDGET:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                    addWidget(appWidgetId, info, false);
                    newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                    adapter.setDataset(appWidgets);
                    adapter.notifyDataSetChanged();
                    lce.showContent();
                } else {
                    cancelAddNewWidget(newAppWidgetId);
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
            appWidgets.clear();
            appWidgets.add(new AddWidget());
            for (int appWidgetId : appWidgetIds) {
                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                addWidget(appWidgetId, info, true);
            }
            adapter.setDataset(appWidgets);
            adapter.notifyDataSetChanged();
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
            try {
                startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
            } catch (Exception e) {
                cancelAddNewWidget(appWidgetId);
                Toast.makeText(requireContext(), R.string.widgets_add_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            addWidget(appWidgetId, info, false);
            newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            adapter.setDataset(appWidgets);
            adapter.notifyDataSetChanged();
            lce.showContent();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                // TODO save appWidgetId
            }
        }
    }

    private void addWidget(int appWidgetId, AppWidgetProviderInfo info, boolean restored) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (restored && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            options.putBoolean(AppWidgetManager.OPTION_APPWIDGET_RESTORE_COMPLETED, true);
        }
        int minWidth = Math.max(
                getResources().getDimensionPixelSize(R.dimen.widget_min_width),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        );
        int minHeight = Math.max(
                getResources().getDimensionPixelSize(R.dimen.widget_min_height),
                options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        );
        int size = getResources().getDimensionPixelSize(R.dimen.widget_max_height);
        int maxHeight = Math.max(size, Math.min(size, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)));
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWidth);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHeight);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, maxHeight);

        appWidgets.add(new AppWidget(appWidgetId, info, options,
                minWidth, minHeight, getResources().getDisplayMetrics().widthPixels, maxHeight));
    }

    private void cancelAddNewWidget(int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void registerWindowInsets(View view) {
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            widgetsList.setPadding(0, 0, 0, insets.getStableInsetBottom());
            return insets;
        });

        WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
            widgetsList.setPadding(0, 0, 0, insets.getStableInsetBottom());
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

    private void showActionsPopup(int appWidgetId, AppWidgetHostView widgetView) {
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
                        .setOnClickListener(v -> showRemoveConfirmDialog(appWidgetId))
                        .setIconDrawableTintAttr(R.attr.colorAccent)
                        .setIcon(R.drawable.ic_action_delete));
        popupWindow.showAtAnchor(widgetView, bounds);
    }

    private void showRemoveConfirmDialog(int appWidgetId) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.widgets_remove_dialog_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.widgets_remove_dialog_action, (dialog, which) -> {
                    Iterator<WidgetAdapterItem> iterator = appWidgets.iterator();
                    while (iterator.hasNext()) {
                        WidgetAdapterItem next = iterator.next();
                        if (!(next instanceof AppWidget)) {
                            continue;
                        }
                        if (((AppWidget) next).appWidgetId == appWidgetId) {
                            iterator.remove();
                            break;
                        }
                    }
                    adapter.setDataset(appWidgets);
                    adapter.notifyDataSetChanged();
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
