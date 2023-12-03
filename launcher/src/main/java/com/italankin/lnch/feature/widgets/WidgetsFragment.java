package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
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
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends Fragment implements IntentQueue.OnIntentAction {

    private static final String ACTION_PIN_APPWIDGET = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
    private static final int APP_WIDGET_HOST_ID = 101;

    private static final String REQUEST_KEY_WIDGETS = "widgets";
    public static final int REQUEST_CODE_CONFIGURE = 133;

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

        bindWidgets();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CONFIGURE) {
            onNewWidgetConfigured(resultCode == Activity.RESULT_OK);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        intentQueue.unregisterOnIntentAction(this);
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

    private void bindWidgets() {
        widgetItemsState.clearWidgets();
        for (int appWidgetId : appWidgetHost.getAppWidgetIds()) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                addWidget(appWidgetId, info, true);
            } else {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        updateWidgets();

        intentQueue.registerOnIntentAction(this);
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
                appWidgetHost.startAppWidgetConfigureActivityForResult(requireActivity(), newAppWidgetId, 0, REQUEST_CODE_CONFIGURE, null);
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
                info.minWidth
        );
        int minHeight = Math.max(
                getResources().getDimensionPixelSize(R.dimen.widget_min_height),
                info.minHeight
        );
        int widgetMaxHeight = getResources().getDimensionPixelSize(R.dimen.widget_max_height);
        int widgetMaxWidth = getResources().getDisplayMetrics().widthPixels;
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minWidth);
        options.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, minHeight);
        int maxHeight = Math.max(widgetMaxHeight,
                Math.min(widgetMaxHeight, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)));
        if (maxHeight > 0) {
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, maxHeight);
        } else {
            options.remove(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        }
        int maxWidth = Math.max(widgetMaxWidth,
                Math.min(widgetMaxWidth, options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)));
        if (maxWidth > 0) {
            options.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, maxWidth);
        } else {
            options.remove(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        }

        widgetItemsState.addWidget(new AppWidget(appWidgetId, info, options,
                minWidth, minHeight, widgetMaxWidth, maxHeight));
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
}
