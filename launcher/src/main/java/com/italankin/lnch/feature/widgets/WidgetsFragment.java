package com.italankin.lnch.feature.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
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
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
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
import com.italankin.lnch.feature.home.util.IntentQueue;
import com.italankin.lnch.feature.widgets.adapter.WidgetAdapter;
import com.italankin.lnch.feature.widgets.gallery.WidgetGalleryActivity;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHost;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.util.WidgetResizeFrame;
import com.italankin.lnch.feature.widgets.util.WidgetSizeHelper;
import com.italankin.lnch.model.repository.prefs.Preferences;
import timber.log.Timber;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetsFragment extends Fragment implements IntentQueue.OnIntentAction, BackButtonHandler,
        WidgetAdapter.WidgetActionListener {

    public static final int REQUEST_CODE_CONFIGURE = 133;
    public static final int REQUEST_CODE_RECONFIGURE = 173;

    public static boolean isWidgetRequestCode(int requestCode) {
        return requestCode == REQUEST_CODE_CONFIGURE || requestCode == REQUEST_CODE_RECONFIGURE;
    }

    private static final String ACTION_PIN_APPWIDGET = "android.content.pm.action.CONFIRM_PIN_APPWIDGET";
    private static final int APP_WIDGET_HOST_ID = 101;
    private static final float MAX_HEIGHT_FACTOR = .75f;

    private IntentQueue intentQueue;
    private Preferences preferences;

    private RecyclerView widgetsList;
    private ItemTouchHelper itemTouchHelper;

    private View actionEdit;
    private View actionCommit;

    private LauncherAppWidgetHost appWidgetHost;
    private AppWidgetManager appWidgetManager;
    private WidgetSizeHelper widgetSizeHelper;
    private int cellSize;
    private int maxHeightCells;

    private int newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private WidgetAdapter adapter;
    private final WidgetItemsState widgetItemsState = new WidgetItemsState();

    private final ActivityResultLauncher<Integer> addWidgetLauncher = registerForActivityResult(
            new WidgetGalleryActivity.SelectContract(),
            this::onNewWidgetSelected);

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
        View actionAddWidget = view.findViewById(R.id.add_widget);
        actionAddWidget.setOnClickListener(v -> startAddNewWidget());
        actionEdit = view.findViewById(R.id.edit);
        actionEdit.setOnClickListener(v -> {
            widgetItemsState.setResizeMode(true, preferences.get(Preferences.WIDGETS_FORCE_RESIZE));
            updateWidgets();
        });
        actionCommit = view.findViewById(R.id.commit);
        actionCommit.setOnClickListener(v -> exitEditMode());

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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_CONFIGURE) {
            onNewWidgetConfigured(resultCode == Activity.RESULT_OK);
            return;
        } else if (requestCode == REQUEST_CODE_RECONFIGURE) {
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

    @Override
    public boolean onBackPressed() {
        if (widgetItemsState.isResizeMode()) {
            exitEditMode();
            return true;
        }
        return false;
    }

    @Override
    public void onWidgetDelete(AppWidget appWidget) {
        widgetItemsState.removeWidgetById(appWidget.appWidgetId);
        appWidgetHost.deleteAppWidgetId(appWidget.appWidgetId);
        updateWidgets();
    }

    @Override
    public void onWidgetConfigure(AppWidget appWidget) {
        try {
            appWidgetHost.startAppWidgetConfigureActivityForResult(requireActivity(), appWidget.appWidgetId, 0, REQUEST_CODE_RECONFIGURE, null);
        } catch (Exception e) {
            Timber.e(e, "onWidgetConfigure: %s", e.getMessage());
        }
    }

    private void bindWidgets() {
        widgetItemsState.clearWidgets();
        for (int appWidgetId : appWidgetHost.getAppWidgetIds()) {
            AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
            if (info != null) {
                addWidget(appWidgetId, info, false);
            } else {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
        widgetItemsState.setWidgetsOrder(preferences.get(Preferences.WIDGETS_ORDER));
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
            widgetsList.smoothScrollToPosition(adapter.getItemCount() - 1);
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
            widgetsList.smoothScrollToPosition(adapter.getItemCount() - 1);
        } else {
            cancelAddNewWidget(newAppWidgetId);
        }
    }

    private void addWidget(AppWidgetProviderInfo info) {
        addWidget(newAppWidgetId, info, true);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        updateWidgets();
    }

    private void addWidget(int appWidgetId, AppWidgetProviderInfo info, boolean isNew) {
        Bundle options = new Bundle(appWidgetManager.getAppWidgetOptions(appWidgetId));
        int gridSize = preferences.get(Preferences.WIDGETS_HORIZONTAL_GRID_SIZE);
        int maxAvailWidth = cellSize * gridSize;
        int maxAvailHeight = cellSize * maxHeightCells;
        Size minSize = widgetSizeHelper.getMinSize(info, options);
        int width = cellSize(cellSize, minSize.getWidth(), maxAvailWidth);
        int height = cellSize(cellSize, minSize.getHeight(), maxAvailHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isNew) {
            if (info.targetCellWidth > 0) {
                width = cellSize * Math.min(gridSize, info.targetCellWidth);
            }
            if (info.targetCellHeight > 0) {
                height = cellSize * Math.min(gridSize, info.targetCellHeight);
            }
        }
        Size minSizeFromInfo = widgetSizeHelper.getMinSizeFromInfo(info);
        int minWidth = cellSize(cellSize, minSizeFromInfo.getWidth(), maxAvailHeight);
        int minHeight = cellSize(cellSize, minSizeFromInfo.getHeight(), maxAvailHeight);
        AppWidget.Size size = new AppWidget.Size(
                minWidth, minHeight,
                width, height,
                maxAvailWidth, maxAvailHeight);
        widgetSizeHelper.resize(appWidgetId, options, width, height);
        widgetItemsState.addWidget(new AppWidget(appWidgetId, info, options, size));
    }

    private void cancelAddNewWidget(int appWidgetId) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return;
        }
        appWidgetHost.deleteAppWidgetId(newAppWidgetId);
        newAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    }

    private void registerWindowInsets(View view) {
        int extraPadding = getResources().getDimensionPixelSize(R.dimen.widget_list_padding_bottom_extra);
        view.setOnApplyWindowInsetsListener((v, insets) -> {
            widgetsList.setPadding(0, 0, 0, insets.getStableInsetBottom() + extraPadding);
            return insets;
        });

        WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
            widgetsList.setPadding(0, 0, 0, insets.getStableInsetBottom() + extraPadding);
        }
    }

    private void exitEditMode() {
        preferences.set(Preferences.WIDGETS_ORDER, widgetItemsState.getWidgetsOrder());
        widgetItemsState.setResizeMode(false, false);
        updateWidgets();
    }

    private void updateWidgets() {
        if (widgetItemsState.isResizeMode()) {
            actionCommit.setVisibility(View.VISIBLE);
            actionEdit.setVisibility(View.GONE);
        } else {
            actionCommit.setVisibility(View.GONE);
            actionEdit.setVisibility(View.VISIBLE);
        }
        adapter.setItems(widgetItemsState.getItems());
        adapter.notifyDataSetChanged();
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

    private static int cellSize(int cellSize, int size, int max) {
        if (size <= cellSize) {
            return cellSize;
        }
        if (size % cellSize == 0) {
            return Math.min(size, max);
        }
        return Math.min(size + (cellSize - (size % cellSize)), max);
    }
}
