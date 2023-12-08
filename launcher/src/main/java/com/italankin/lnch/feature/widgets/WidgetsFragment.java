package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.BackButtonHandler;
import com.italankin.lnch.feature.home.util.HomePagerHost;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.MainActionHandler;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.gallery.WidgetGalleryActivity;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.util.WidgetHelper;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;
import com.italankin.lnch.feature.widgets.util.WidgetSizeHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;
import timber.log.Timber;

import static android.appwidget.AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL;
import static android.appwidget.AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends Fragment implements IntentQueue.OnIntentAction, BackButtonHandler,
        WidgetAdapter.WidgetActionListener, MainActionHandler {

    public static final String ACTION_RELOAD_WIDGETS = "com.italankin.lnch.widgets.RELOAD";

    private static final String ACTION_PIN_APPWIDGET = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
    private static final int APP_WIDGET_HOST_ID = 101;
    private static final float MAX_HEIGHT_FACTOR = .75f;

    private IntentQueue intentQueue;
    private Preferences preferences;

    private RecyclerView widgetsList;
    private ItemTouchHelper itemTouchHelper;

    private ImageView actionEditMode;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private WidgetSizeHelper widgetSizeHelper;
    private int cellSize;
    private int maxHeightCells;
    private HomePagerHost homePagerHost;
    private Callback callback;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private boolean exitEditModeOnStop = true;

    private WidgetAdapter adapter;
    private final WidgetItemsState widgetItemsState = new WidgetItemsState();

    private final ActivityResultLauncher<Integer> addWidgetLauncher = registerForActivityResult(
            new WidgetGalleryActivity.SelectContract(),
            this::onNewWidgetSelected);

    private final ActivityResultLauncher<ConfigureWidgetContract.Input> configureWidgetLauncher = registerForActivityResult(
            new ConfigureWidgetContract(),
            this::onNewWidgetConfigured);

    private final ActivityResultLauncher<ConfigureWidgetContract.Input> reconfigureWidgetLauncher = registerForActivityResult(
            new ConfigureWidgetContract(),
            o -> {
                // empty
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentQueue = LauncherApp.daggerService.main().intentQueue();
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appWidgetHost = new LauncherAppWidgetHost(context, APP_WIDGET_HOST_ID);
        appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
        widgetSizeHelper = new WidgetSizeHelper(context);
        if (context instanceof HomePagerHost) {
            homePagerHost = (HomePagerHost) context;
        }
        if (context instanceof Callback) {
            callback = (Callback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        homePagerHost = null;
        callback = null;
    }

    @Override
    public boolean onIntent(Intent intent) {
        if (ACTION_RELOAD_WIDGETS.equals(intent.getAction())) {
            bindWidgets();
            return true;
        } else if (!ACTION_PIN_APPWIDGET.equals(intent.getAction())) {
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
            Timber.d("allocate: %d", newAppWidgetId);
            Bundle options = new Bundle();
            options.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, newAppWidgetId);
            if (request.accept(options)) {
                addNewWidget(info);
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
        View actionAddWidget = view.findViewById(R.id.add_widget);
        actionAddWidget.setOnClickListener(v -> startAddNewWidget());
        actionEditMode = view.findViewById(R.id.edit_mode);
        actionEditMode.setOnClickListener(v -> {
            if (widgetItemsState.isResizeMode()) {
                exitEditMode();
            } else {
                widgetItemsState.setResizeMode(true, preferences.get(Preferences.WIDGETS_FORCE_RESIZE));
                updateActionsState();
                adapter.notifyItemRangeChanged(0, adapter.getItemCount(), new Object());
                if (homePagerHost != null) {
                    homePagerHost.setPagerEnabled(false);
                }
            }
        });

        widgetsList = view.findViewById(R.id.widgets_list);
        int dragDirs = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(dragDirs, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                int from = viewHolder.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                widgetItemsState.swapWidgets(from, to);
                adapter.notifyItemMoved(from, to);
                return true;
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        itemTouchHelper.attachToRecyclerView(widgetsList);
        recalculateCellSize();
        WidgetResizeFrame.OnStartDragListener widgetLongClickListener = resizeFrame -> {
            RecyclerView.ViewHolder holder = widgetsList.findContainingViewHolder(resizeFrame);
            if (holder != null) {
                itemTouchHelper.startDrag(holder);
            }
        };
        adapter = new WidgetAdapter(appWidgetHost, cellSize, widgetLongClickListener, this);
        widgetsList.setAdapter(adapter);
        registerWindowInsets(view);
        bindWidgets();
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
        if (exitEditModeOnStop) {
            exitEditMode();
        } else {
            exitEditModeOnStop = true;
        }
        try {
            appWidgetHost.stopListening();
        } catch (Exception e) {
            Timber.w(e, "onStop:");
        }
    }

    @Override
    public boolean onBackPressed() {
        if (widgetItemsState.isResizeMode()) {
            exitEditMode();
            return true;
        }
        return false;
    }

    @Override
    public boolean handle() {
        return widgetItemsState.isResizeMode();
    }

    @Override
    public void onWidgetDelete(AppWidget appWidget) {
        widgetItemsState.removeWidgetById(appWidget.appWidgetId);
        appWidgetHost.deleteAppWidgetId(appWidget.appWidgetId);
        Timber.d("deallocate: %d", appWidget.appWidgetId);
        updateWidgets();
    }

    @Override
    public void onWidgetReconfigure(AppWidget appWidget) {
        reconfigureWidget(appWidget.appWidgetId, appWidget.providerInfo);
    }

    private void bindWidgets() {
        widgetItemsState.clearWidgets();
        for (int appWidgetId : appWidgetHost.getAppWidgetIds()) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                addWidgetToScreen(appWidgetId, info, false);
            } else {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
                Timber.d("deallocate: %d", appWidgetId);
            }
        }
        widgetItemsState.setWidgetsOrder(preferences.get(Preferences.WIDGETS_ORDER));
        updateWidgets();

        intentQueue.registerOnIntentAction(this);
        exitEditModeOnStop = true;
    }

    private void reconfigureWidget(int appWidgetId, AppWidgetProviderInfo info) {
        if (info.configure == null) {
            return;
        }
        if (WidgetHelper.isConfigureActivityExported(requireContext(), info)) {
            try {
                reconfigureWidgetLauncher.launch(new ConfigureWidgetContract.Input(appWidgetId, info.configure));
                exitEditModeOnStop = false;
            } catch (Exception e) {
                Timber.e(e, "reconfigureWidgetLauncher.launch: %s", e.getMessage());
            }
        } else if (callback != null) {
            try {
                callback.startAppWidgetConfigureActivityForResult(appWidgetHost, appWidgetId);
                exitEditModeOnStop = false;
            } catch (Exception e) {
                Timber.e(e, "startAppWidgetConfigureActivityForResult: %s", e.getMessage());
            }
        }
    }

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
        Timber.d("allocate: %d", newAppWidgetId);
        addWidgetLauncher.launch(newAppWidgetId);
        exitEditModeOnStop = false;
    }

    private void onNewWidgetSelected(AppWidgetProviderInfo info) {
        if (info != null) {
            configureWidget(info);
        } else {
            cancelAddNewWidget(newAppWidgetId);
        }
    }

    private void configureWidget(AppWidgetProviderInfo info) {
        if (needConfigure(info) && WidgetHelper.isConfigureActivityExported(requireContext(), info)) {
            try {
                configureWidgetLauncher.launch(new ConfigureWidgetContract.Input(newAppWidgetId, info.configure));
                exitEditModeOnStop = false;
                return;
            } catch (Exception e) {
                Timber.e(e, "ConfigureWidgetContract.launch: %s", e.getMessage());
            }
        }
        addNewWidget(info);
    }

    private static boolean needConfigure(AppWidgetProviderInfo info) {
        if (info.configure == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                boolean optional = (info.widgetFeatures & WIDGET_FEATURE_CONFIGURATION_OPTIONAL) != 0 &&
                        (info.widgetFeatures & WIDGET_FEATURE_RECONFIGURABLE) != 0;
                return !optional;
            }
            // configure reconfigurable widgets later
            return (info.widgetFeatures & WIDGET_FEATURE_RECONFIGURABLE) == 0;
        }
        return false;
    }

    private void onNewWidgetConfigured(boolean configured) {
        exitEditModeOnStop = true;
        if (configured) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(newAppWidgetId);
            if (info == null) {
                cancelAddNewWidget(newAppWidgetId);
                return;
            }
            addNewWidget(info);
        } else {
            cancelAddNewWidget(newAppWidgetId);
        }
    }

    private void addNewWidget(AppWidgetProviderInfo info) {
        addWidgetToScreen(newAppWidgetId, info, true);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        updateWidgets();
        widgetsList.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    private void addWidgetToScreen(int appWidgetId, AppWidgetProviderInfo info, boolean isNew) {
        Bundle options = new Bundle(appWidgetManager.getAppWidgetOptions(appWidgetId));
        int gridSize = preferences.get(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        int maxAvailWidth = cellSize * gridSize;
        int maxAvailHeight = cellSize * maxHeightCells;
        Size size = widgetSizeHelper.getSize(info, options, true);
        int width = minMultipleOfCellSize(cellSize, size.getWidth(), maxAvailWidth);
        int height = minMultipleOfCellSize(cellSize, size.getHeight(), maxAvailHeight);
        Size minSize = widgetSizeHelper.getMinSize(info, true);
        int minWidth = minMultipleOfCellSize(cellSize, minSize.getWidth(), maxAvailHeight);
        int minHeight = minMultipleOfCellSize(cellSize, minSize.getHeight(), maxAvailHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isNew) {
            if (info.targetCellWidth > 0) {
                width = Math.max(cellSize * Math.min(gridSize, info.targetCellWidth), minWidth);
            }
            if (info.targetCellHeight > 0) {
                height = Math.max(cellSize * Math.min(gridSize, info.targetCellHeight), minHeight);
            }
        }
        int maxWidth = maxAvailWidth, maxHeight = maxAvailHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (info.maxResizeWidth > minWidth) {
                maxWidth = minMultipleOfCellSize(cellSize, info.maxResizeWidth, maxAvailWidth);
            }
            if (info.maxResizeHeight > minHeight) {
                maxHeight = minMultipleOfCellSize(cellSize, info.maxResizeHeight, maxAvailHeight);
            }
        }
        AppWidget.Size widgetSize = new AppWidget.Size(
                minWidth, minHeight,
                width, height,
                maxWidth, maxHeight);
        widgetSizeHelper.resize(appWidgetId, options, width, height, true);
        widgetItemsState.addWidget(new AppWidget(appWidgetId, info, options, widgetSize));
    }

    private void cancelAddNewWidget(int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
        Timber.d("deallocate: %d", newAppWidgetId);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void registerWindowInsets(View view) {
        int extraBottom = getResources().getDimensionPixelSize(R.dimen.widget_list_padding_bottom_extra);
        int extraTop = getResources().getDimensionPixelSize(R.dimen.widget_list_padding_top_extra);
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            widgetsList.setPadding(0, extraTop, 0, insets.getStableInsetBottom() + extraBottom);
            return insets;
        });

        WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
            widgetsList.setPadding(0, extraTop, 0, insets.getStableInsetBottom() + extraBottom);
        }
    }

    private void exitEditMode() {
        if (!widgetItemsState.isResizeMode()) {
            return;
        }
        preferences.set(Preferences.WIDGETS_ORDER, widgetItemsState.getWidgetsOrder());
        widgetItemsState.setResizeMode(false, false);
        updateActionsState();
        adapter.notifyItemRangeChanged(0, adapter.getItemCount(), new Object());
        if (homePagerHost != null) {
            homePagerHost.setPagerEnabled(true);
        }
    }

    private void updateWidgets() {
        updateActionsState();
        adapter.setItems(widgetItemsState.getItems());
        adapter.notifyDataSetChanged();
    }

    private void updateActionsState() {
        if (widgetItemsState.isResizeMode()) {
            actionEditMode.setImageResource(R.drawable.ic_customize_save);
        } else {
            actionEditMode.setImageResource(R.drawable.ic_action_rename);
        }
    }

    private void recalculateCellSize() {
        int gridSize = preferences.get(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        cellSize = calculateCellSize(gridSize);
        maxHeightCells = calculateMaxHeightCells(cellSize);
        ViewGroup.LayoutParams wlp = widgetsList.getLayoutParams();
        wlp.width = cellSize * gridSize;
        widgetsList.setLayoutParams(wlp);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), gridSize);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                AppWidget item = adapter.getItem(position);
                return Math.min(item.size.width / cellSize, gridSize);
            }
        });
        widgetsList.setLayoutManager(layoutManager);
    }

    private int calculateCellSize(int gridSize) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        int margins = res.getDimensionPixelSize(R.dimen.widget_list_margin) * 2;
        int size = Math.min(dm.widthPixels - margins, dm.heightPixels) / gridSize;
        int maxCellSize = res.getDimensionPixelSize(R.dimen.widget_max_cell_size);
        return Math.min(size, maxCellSize);
    }

    private int calculateMaxHeightCells(int cellSize) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxSize = (int) (dm.heightPixels * MAX_HEIGHT_FACTOR);
        return maxSize / cellSize;
    }

    private static int minMultipleOfCellSize(int cellSize, int size, int max) {
        if (size <= cellSize) {
            return cellSize;
        }
        if (size % cellSize == 0) {
            return Math.min(size, max);
        }
        return Math.min(size + (cellSize - (size % cellSize)), max);
    }

    public interface Callback {

        void startAppWidgetConfigureActivityForResult(AppWidgetHost appWidgetHost, int appWidgetId);
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
            return resultCode != Activity.RESULT_CANCELED;
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
