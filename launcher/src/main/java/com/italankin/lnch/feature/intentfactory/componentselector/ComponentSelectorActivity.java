package com.italankin.lnch.feature.intentfactory.componentselector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.intentfactory.componentselector.adapter.ComponentNameAdapter;
import com.italankin.lnch.feature.intentfactory.componentselector.adapter.ComponentNameFilter;
import com.italankin.lnch.feature.intentfactory.componentselector.model.ComponentNameUi;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

public class ComponentSelectorActivity extends AppActivity implements ComponentSelectorView,
        ComponentNameAdapter.Listener, ListFilter.OnFilterResult<ComponentNameUi> {

    private static final String EXTRA_RESULT = "result";

    @InjectPresenter
    ComponentSelectorPresenter presenter;

    @ProvidePresenter
    ComponentSelectorPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().componentSelector();
    }

    private LceLayout lce;
    private ComponentNameAdapter adapter;

    private final ComponentNameFilter filter = new ComponentNameFilter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intent_component_selector);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.intent_component_selector_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        lce = findViewById(R.id.lce);

        Picasso picasso = LauncherApp.daggerService.main()
                .picassoFactory()
                .create(this);

        adapter = new ComponentNameAdapter(picasso, this);
        RecyclerView list = findViewById(R.id.list);
        list.setAdapter(adapter);

        lce.showLoading();
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

    @Override
    public void onComponentsLoaded(List<ComponentNameUi> componentNames) {
        filter.setDataset(componentNames);
    }

    public static class Contract extends ActivityResultContract<Object, ComponentName> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object input) {
            return new Intent(context, ComponentSelectorActivity.class);
        }

        @Nullable
        @Override
        public ComponentName parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_RESULT) : null;
        }
    }
}
