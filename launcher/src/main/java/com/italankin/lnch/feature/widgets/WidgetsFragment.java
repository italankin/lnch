package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
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
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultManager;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.widgets.adapter.AddWidgetAdapter;
import com.italankin.lnch.feature.widgets.adapter.NoWidgetsAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.adapter.WidgetCompositeAdapter;
import com.italankin.lnch.feature.widgets.gallery.WidgetGalleryActivity;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.popup.WidgetPopupFragment;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ViewUtils;

import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends AppFragment implements WidgetsView, IntentQueue.OnIntentAction {

    private static final String ACTION_PIN_APPWIDGET = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
    private static final int APP_WIDGET_HOST_ID = 101;

    private static final String REQUEST_KEY_WIDGETS = "widgets";

    @InjectPresenter
    WidgetsPresenter presenter;

    @ProvidePresenter
    WidgetsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().widgets();
    }

    private IntentQueue intentQueue;

    private RecyclerView widgetsList;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private WidgetCompositeAdapter adapter;
    private final WidgetItemsState widgetItemsState = new WidgetItemsState();

    private final ActivityResultLauncher<Integer> addWidgetLauncher = registerForActivityResult(
            new WidgetGalleryActivity.Contract(),
            this::onNewWidgetSelected);

    private final ActivityResultLauncher<ConfigureWidgetContract.Input> configureWidgetLauncher = registerForActivityResult(
            new ConfigureWidgetContract(),
            this::onNewWidgetConfigured);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentQueue = LauncherApp.daggerService.main().intentQueue();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appWidgetHost = new LauncherAppWidgetHost(context, APP_WIDGET_HOST_ID);
        appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);

        new FragmentResultManager(getParentFragmentManager(), this, REQUEST_KEY_WIDGETS)
                .register(new WidgetPopupFragment.AppInfoContract(), appWidgetId -> {
                    AppWidget appWidget = widgetItemsState.getWidgetById(appWidgetId);
                    if (appWidget == null) {
                        return;
                    }
                    String packageName = appWidget.providerInfo.provider.getPackageName();
                    IntentUtils.safeStartAppSettings(requireContext(), packageName, null);
                })
                .register(new WidgetPopupFragment.RemoveWidgetContract(), appWidgetId -> {
                    widgetItemsState.removeWidgetById(appWidgetId);
                    appWidgetHost.deleteAppWidgetId(appWidgetId);
                    updateWidgets();
                })
                .attach();
    }

    @Override
    public boolean onIntent(Intent intent) {
        if (!ACTION_PIN_APPWIDGET.equals(intent.getAction())) {
            return false;
        }
        Context context = requireContext();
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        PinItemRequest request = launcherApps.getPinItemRequest(intent);
        if (request == null || request.getRequestType() != PinItemRequest.REQUEST_TYPE_APPWIDGET || !request.isValid()) {
            return false;
        }
        AppWidgetProviderInfo info = request.getAppWidgetProviderInfo(context);
        if (info != null) {
            newAppWidgetId = appWidgetHost.allocateAppWidgetId();
            Bundle options = new Bundle();
            options.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, newAppWidgetId);
            if (request.accept(options)) {
                addWidget(info);
            } else {
                cancelAddNewWidget(newAppWidgetId);
            }
        }
        return true;
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
                    Rect bounds = ViewUtils.getViewBounds(hostView);
                    WidgetPopupFragment.newInstance(appWidgetId, REQUEST_KEY_WIDGETS, bounds)
                            .show(getParentFragmentManager());
                    return true;
                }))
                .add(new AddWidgetAdapter(
                        v -> startAddNewWidget(),
                        v -> {
                            Toast.makeText(requireContext(), R.string.widgets_add, Toast.LENGTH_SHORT).show();
                            return true;
                        }))
                .add(new NoWidgetsAdapter())
                .recyclerView(widgetsList)
                .create();

        registerWindowInsets(view);

        presenter.loadWidgets();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        intentQueue.unregisterOnIntentAction(this);
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

        intentQueue.registerOnIntentAction(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        appWidgetHost.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            appWidgetHost.stopListening();
        } catch (Exception e) {
            Timber.w(e, "onStop:");
        }
    }

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
        addWidgetLauncher.launch(newAppWidgetId);
    }

    private void onNewWidgetSelected(AppWidgetProviderInfo info) {
        if (info != null) {
            configureWidget(info);
        } else {
            cancelAddNewWidget(newAppWidgetId);
        }
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
            addWidget(info);
        }
    }

    private void onNewWidgetConfigured(boolean configured) {
        if (configured) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
            if (info == null) {
                cancelAddNewWidget(newAppWidgetId);
                return;
            }
            addWidget(info);
        } else {
            cancelAddNewWidget(newAppWidgetId);
        }
    }

    private void addWidget(AppWidgetProviderInfo info) {
        addWidget(newAppWidgetId, info, false);
        presenter.addWidget(newAppWidgetId);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        updateWidgets();
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
