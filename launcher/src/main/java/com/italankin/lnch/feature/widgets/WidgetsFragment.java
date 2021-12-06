package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.italankin.lnch.feature.widgets.adapter.NoWidgetsAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetCompositeAdapter;
import com.italankin.lnch.feature.widgets.gallery.WidgetGalleryActivity;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends AppFragment implements WidgetsView {

    private static final int APP_WIDGET_HOST_ID = 101;

    @InjectPresenter
    WidgetsPresenter presenter;

    @ProvidePresenter
    WidgetsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().widgets();
    }

    private Picasso picasso;

    private RecyclerView widgetsList;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ActionPopupWindow popupWindow;

    private WidgetCompositeAdapter adapter;
    private final WidgetItemsState widgetItemsState = new WidgetItemsState();

    private final ActivityResultLauncher<Integer> addWidgetLauncher = registerForActivityResult(
            new WidgetGalleryActivity.Contract(),
            info -> {
                if (info != null) {
                    configureWidget(info);
                } else {
                    cancelAddNewWidget(newAppWidgetId);
                }
            });

    private final ActivityResultLauncher<ConfigureWidgetContract.Input> configureWidgetLauncher = registerForActivityResult(
            new ConfigureWidgetContract(),
            configured -> {
                if (configured) {
                    AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
                    addWidget(newAppWidgetId, info, false);
                    presenter.addWidget(newAppWidgetId);
                    newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                    updateWidgets();
                } else {
                    cancelAddNewWidget(newAppWidgetId);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        picasso = LauncherApp.daggerService.main().picassoFactory().create(requireContext());
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
        widgetsList = view.findViewById(R.id.widgets_list);

        adapter = new WidgetCompositeAdapter.Builder(requireContext())
                .add(new WidgetAdapter(appWidgetHost, (appWidgetId, hostView) -> {
                    showActionsPopup(appWidgetId, hostView);
                    return true;
                }))
                .add(new AddWidgetAdapter(v -> {
                    startAddNewWidget();
                }))
                .add(new NoWidgetsAdapter())
                .recyclerView(widgetsList)
                .create();

        registerWindowInsets(view);

        presenter.loadWidgets();
    }

    @Override
    public void onBindWidgets(List<Integer> appWidgetIds) {
        widgetItemsState.clearWidgets();
        for (int appWidgetId : appWidgetIds) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                addWidget(appWidgetId, info, true);
            } else {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
                presenter.removeWidget(appWidgetId);
            }
        }
        updateWidgets();
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

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
        addWidgetLauncher.launch(newAppWidgetId);
    }

    private void configureWidget(AppWidgetProviderInfo info) {
        if (info.configure != null) {
            try {
                configureWidgetLauncher.launch(new ConfigureWidgetContract.Input(newAppWidgetId, info.configure));
            } catch (Exception e) {
                Timber.e(e, "configureWidget: %s", e.getMessage());
                cancelAddNewWidget(newAppWidgetId);
                Toast.makeText(requireContext(), R.string.widgets_add_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            addWidget(newAppWidgetId, info, false);
            presenter.addWidget(newAppWidgetId);
            newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            updateWidgets();
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

        widgetItemsState.addWidget(new AppWidget(appWidgetId, info, options,
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

    private void showActionsPopup(int appWidgetId, AppWidgetHostView widgetView) {
        Context context = requireContext();
        popupWindow = new ActionPopupWindow(context, picasso)
                .addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(R.string.widgets_app_info)
                        .setOnClickListener(v -> showAppInfo(widgetView))
                        .setIconDrawableTintAttr(R.attr.colorAccent)
                        .setIcon(R.drawable.ic_app_info))
                .addShortcut(new ActionPopupWindow.ItemBuilder(context)
                        .setLabel(R.string.widgets_remove)
                        .setOnClickListener(v -> {
                            widgetItemsState.removeWidgetById(appWidgetId);
                            appWidgetHost.deleteAppWidgetId(appWidgetId);
                            updateWidgets();
                        })
                        .setIconDrawableTintAttr(R.attr.colorAccent)
                        .setIcon(R.drawable.ic_action_delete));
        popupWindow.showAtAnchor(widgetView, widgetsList);
    }

    private void showAppInfo(AppWidgetHostView widgetView) {
        String packageName = widgetView.getAppWidgetInfo().provider.getPackageName();
        IntentUtils.safeStartAppSettings(requireContext(), packageName, widgetView);
    }

    private void updateWidgets() {
        adapter.setDataset(widgetItemsState.getItems());
        adapter.notifyDataSetChanged();
    }

    private static class ConfigureWidgetContract extends ActivityResultContract<ConfigureWidgetContract.Input, Boolean> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Input input) {
            return new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                    .setComponent(input.configure)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, input.appWidgetId);
        }

        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK;
        }

        static class Input {
            final int appWidgetId;
            final ComponentName configure;

            Input(int appWidgetId, ComponentName configure) {
                this.appWidgetId = appWidgetId;
                this.configure = configure;
            }
        }
    }
}
