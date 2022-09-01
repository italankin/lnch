package com.italankin.lnch.feature.widgets.gallery;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.model.repository.prefs.Preferences;
import me.italankin.adapterdelegates.CompositeAdapter;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

@RequiresApi(Build.VERSION_CODES.O)
public class WidgetGalleryActivity extends AppCompatActivity implements
        WidgetPreviewAdapter.Listener,
        ListFilter.OnFilterResult<WidgetPreview> {

    private static final String EXTRA_APP_WIDGET_ID = "app_widget_id";

    private static final String EXTRA_RESULT = "result";

    private AppWidgetManager appWidgetManager;
    private LceLayout lce;
    private final WidgetFilter filter = new WidgetFilter(this);
    private int appWidgetId;

    private final ActivityResultLauncher<Input> bindWidgetLauncher = registerForActivityResult(
            new BindWidgetContract(),
            result -> {
                AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetId);
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, info));
                finish();
            }
    );
    private CompositeAdapter<WidgetPreview> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ImageLoader imageLoader = LauncherApp.daggerService.main().imageLoader();
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        appWidgetId = getIntent().getIntExtra(EXTRA_APP_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        appWidgetManager = (AppWidgetManager) getSystemService(Context.APPWIDGET_SERVICE);

        setContentView(R.layout.activity_widget_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.widgets_gallery_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        lce = findViewById(R.id.lce);
        RecyclerView widgetsList = findViewById(R.id.list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        widgetsList.setLayoutManager(layoutManager);
        adapter = new CompositeAdapter.Builder<WidgetPreview>(this)
                .add(new WidgetPreviewAdapter(imageLoader, this))
                .setHasStableIds(true)
                .create();
        widgetsList.setAdapter(adapter);

        populate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.widget_gallery, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.widgets_gallery_hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter.filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onWidgetSelected(AppWidgetProviderInfo info) {
        if (appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider)) {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, info));
            finish();
        } else {
            bindWidgetLauncher.launch(new Input(appWidgetId, info));
        }
    }

    @Override
    public void onFilterResult(String query, List<WidgetPreview> items) {
        if (items.isEmpty()) {
            lce.empty()
                    .message(R.string.widgets_gallery_search_empty)
                    .show();
        } else {
            adapter.setDataset(items);
            adapter.notifyDataSetChanged();
            lce.showContent();
        }
    }

    private void populate() {
        PackageManager packageManager = getPackageManager();

        List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();
        List<WidgetPreview> items = new ArrayList<>(providers.size());
        for (AppWidgetProviderInfo info : providers) {
            items.add(new WidgetPreview(packageManager, info));
        }

        filter.setDataset(items);
    }

    public static class Contract extends ActivityResultContract<Integer, AppWidgetProviderInfo> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Integer input) {
            return new Intent(context, WidgetGalleryActivity.class)
                    .putExtra(EXTRA_APP_WIDGET_ID, input);
        }

        @Override
        public AppWidgetProviderInfo parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_RESULT) : null;
        }
    }

    private static class BindWidgetContract extends ActivityResultContract<Input, Object> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Input input) {
            return new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, input.appWidgetId)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, input.info.provider)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER_PROFILE, input.info.getProfile());
        }

        @Override
        public Object parseResult(int resultCode, @Nullable Intent intent) {
            return null;
        }
    }

    static class Input {
        final int appWidgetId;
        final AppWidgetProviderInfo info;

        Input(int appWidgetId, AppWidgetProviderInfo info) {
            this.appWidgetId = appWidgetId;
            this.info = info;
        }
    }
}
