package com.italankin.lnch.feature.intentfactory.componentselector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.intentfactory.componentselector.adapter.ComponentNameAdapter;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.filter.SimpleListFilter;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.imageloader.cache.LruCache;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

public class ComponentSelectorActivity extends AppActivity implements ComponentNameAdapter.Listener,
        ListFilter.OnFilterResult<ComponentNameUi> {

    private static final String EXTRA_RESULT = "result";

    private LceLayout lce;
    private ComponentNameAdapter adapter;

    private final SimpleListFilter<ComponentNameUi> filter = SimpleListFilter.createSearchable(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intent_component_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        lce = findViewById(R.id.lce);

        ImageLoader imageLoader = new ImageLoader.Builder(this)
                .cache(new LruCache(48))
                .build();

        adapter = new ComponentNameAdapter(imageLoader, this);
        RecyclerView list = findViewById(R.id.list);
        list.setAdapter(adapter);

        lce.showLoading();

        ComponentSelectorViewModel viewModel = AppViewModelProvider.get(this, ComponentSelectorViewModel.class,
                ViewModelComponent::componentSelector);
        viewModel.componentsEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<ComponentNameUi> componentNames) {
                        filter.setDataset(componentNames);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.intent_component_selector, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.hint_search));
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
    public void onItemClick(ComponentNameUi componentName) {
        Intent data = new Intent()
                .putExtra(EXTRA_RESULT, componentName.componentName);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onFilterResult(String query, List<ComponentNameUi> items) {
        adapter.setDataset(items);
        if (!items.isEmpty()) {
            lce.showContent();
        } else {
            lce.empty()
                    .message(R.string.intent_component_selector_empty)
                    .show();
        }
    }

    public static class Contract extends ActivityResultContract<Void, ComponentName> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(context, ComponentSelectorActivity.class);
        }

        @Nullable
        @Override
        public ComponentName parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_RESULT) : null;
        }
    }
}
