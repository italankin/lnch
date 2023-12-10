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
import androidx.activity.OnBackPressedCallback;
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
import com.italankin.lnch.feature.home.util.HomePagerHost;
import com.italankin.lnch.feature.home.util.HomeViewPagerDoNotClipChildren;
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.home.util.MainActionHandler;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.gallery.WidgetGalleryActivity;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.CellSize;
import com.italankin.lnch.feature.widgets.util.WidgetHelper;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;
import com.italankin.lnch.feature.widgets.util.WidgetSizeHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.appwidget.AppWidgetProviderInfo.WIDGET_FEATURE_CONFIGURATION_OPTIONAL;
import static android.appwidget.AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends Fragment implements IntentQueue.OnIntentAction, WidgetAdapter.WidgetActionListener,
        MainActionHandler {

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
    private CellSize cellSize;
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

    private OnBackPressedCallback onBackPressedCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intentQueue = LauncherApp.daggerService.main().intentQueue();
        preferences = LauncherApp.daggerService.main().preferences();

        onBackPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                exitEditMode();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
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
        HomeViewPagerDoNotClipChildren.apply(view);
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
        updateCellSize();
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
    public boolean handleMainAction() {
        return widgetItemsState.isResizeMode();
    }

    @Override
    public void onWidgetDelete(AppWidget appWidget) {
        int pos = widgetItemsState.removeWidgetById(appWidget.appWidgetId);
        appWidgetHost.deleteAppWidgetId(appWidget.appWidgetId);
        if (pos >= 0) {
            adapter.notifyItemRemoved(pos);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onWidgetReconfigure(AppWidget appWidget) {
        reconfigureWidget(appWidget.appWidgetId, appWidget.providerInfo);
    }

    private void bindWidgets() {
        List<Preferences.Widget> widgets = preferences.get(Preferences.WIDGETS_DATA);
        Map<Integer, Preferences.Widget> widgetsMap = new HashMap<>(widgets.size());
        List<Integer> widgetsOrder = new ArrayList<>(widgets.size());
        for (Preferences.Widget widget : widgets) {
            widgetsMap.put(widget.appWidgetId, widget);
            widgetsOrder.add(widget.appWidgetId);
        }

        widgetItemsState.clearWidgets();
        for (int appWidgetId : appWidgetHost.getAppWidgetIds()) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                addWidgetToScreen(appWidgetId, info, widgetsMap.get(appWidgetId));
            } else {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        widgetItemsState.setWidgetsOrder(widgetsOrder);
        updateActionsState();
        adapter.setItems(widgetItemsState.getItems());
        adapter.notifyDataSetChanged();

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
                callback.startAppWidgetConfigureActivity(appWidgetHost, appWidgetId);
                exitEditModeOnStop = false;
            } catch (Exception e) {
                Timber.e(e, "startAppWidgetConfigureActivityForResult: %s", e.getMessage());
            }
        }
    }

    private void startAddNewWidget() {
        newAppWidgetId = appWidgetHost.allocateAppWidgetId();
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
        if (needConfigure(info)) {
            if (WidgetHelper.isConfigureActivityExported(requireContext(), info)) {
                try {
                    configureWidgetLauncher.launch(new ConfigureWidgetContract.Input(newAppWidgetId, info.configure));
                    exitEditModeOnStop = false;
                } catch (Exception e) {
                    Timber.e(e, "ConfigureWidgetContract.launch: %s", e.getMessage());
                }
            } else if (callback != null) {
                int newAppWidgetId = this.newAppWidgetId;
                addNewWidget(info);
                callback.startAppWidgetConfigureActivity(appWidgetHost, newAppWidgetId);
            }
        } else {
            addNewWidget(info);
        }
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
        addWidgetToScreen(newAppWidgetId, info, null);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        int position = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(position);
        widgetsList.smoothScrollToPosition(position);
    }

    private void addWidgetToScreen(int appWidgetId, AppWidgetProviderInfo info, @Nullable Preferences.Widget widgetData) {
        Bundle options = new Bundle(appWidgetManager.getAppWidgetOptions(appWidgetId));
        AppWidget.Size widgetSize = widgetSizeHelper.getAppWidgetSize(cellSize, info, options, widgetData);
        widgetSizeHelper.resize(appWidgetId, options, widgetSize.width, widgetSize.height, true);
        widgetItemsState.addWidget(new AppWidget(appWidgetId, info, options, widgetSize));
    }

    private void cancelAddNewWidget(int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
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

        List<AppWidget> appWidgets = widgetItemsState.getItems();
        List<Preferences.Widget> widgetsData = new ArrayList<>(appWidgets.size());
        for (AppWidget appWidget : appWidgets) {
            int widthCells = Math.max(appWidget.size.width / cellSize.width, 1);
            int heightCells = Math.max(appWidget.size.height / cellSize.height, 1);
            widgetsData.add(new Preferences.Widget(appWidget.appWidgetId, widthCells, heightCells));
        }
        preferences.set(Preferences.WIDGETS_DATA, widgetsData);

        widgetItemsState.setResizeMode(false, false);
        updateActionsState();
        adapter.notifyItemRangeChanged(0, adapter.getItemCount(), new Object());
        if (homePagerHost != null) {
            homePagerHost.setPagerEnabled(true);
        }
    }

    private void updateActionsState() {
        boolean resizeMode = widgetItemsState.isResizeMode();
        if (resizeMode) {
            actionEditMode.setImageResource(R.drawable.ic_customize_save);
        } else {
            actionEditMode.setImageResource(R.drawable.ic_action_rename);
        }
        onBackPressedCallback.setEnabled(resizeMode);
    }

    private void updateCellSize() {
        int gridSize = preferences.get(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        Size size = calculateSizeForCell(gridSize);
        int maxHeightCells = calculateMaxHeightCells(size.getHeight());
        cellSize = new CellSize(size.getWidth(), size.getHeight(), gridSize, maxHeightCells);

        ViewGroup.LayoutParams wlp = widgetsList.getLayoutParams();
        wlp.width = cellSize.width * gridSize;
        widgetsList.setLayoutParams(wlp);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), gridSize);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                AppWidget item = adapter.getItem(position);
                return Math.min(item.size.width / cellSize.width, gridSize);
            }
        });
        widgetsList.setLayoutManager(layoutManager);
    }

    private Size calculateSizeForCell(int gridSize) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        int margins = res.getDimensionPixelSize(R.dimen.widget_list_margin) * 2;
        int size = Math.min(dm.widthPixels - margins, dm.heightPixels) / gridSize;
        int maxCellSize = res.getDimensionPixelSize(R.dimen.widget_max_cell_size);
        int cellWidth = Math.min(size, maxCellSize);
        int cellHeight = (int) (cellWidth * preferences.get(Preferences.WIDGETS_HEIGHT_CELL_RATIO));
        return new Size(cellWidth, cellHeight);
    }

    private int calculateMaxHeightCells(int cellHeight) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int maxSize = (int) (dm.heightPixels * MAX_HEIGHT_FACTOR);
        return maxSize / cellHeight;
    }

    public interface Callback {

        void startAppWidgetConfigureActivity(AppWidgetHost appWidgetHost, int appWidgetId);
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
