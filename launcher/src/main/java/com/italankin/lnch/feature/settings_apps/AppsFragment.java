package com.italankin.lnch.feature.settings_apps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings_apps.adapter.AppsFilter;
import com.italankin.lnch.feature.settings_apps.adapter.AppsViewModelAdapter;
import com.italankin.lnch.feature.settings_apps.model.DecoratedAppViewModel;
import com.italankin.lnch.util.adapterdelegate.FilterCompositeAdapter;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AppsFragment extends AppFragment implements AppsView, AppsViewModelAdapter.Listener, AppsFilter.OnFilterResult {

    @InjectPresenter
    AppsPresenter presenter;

    private LceLayout lce;
    private RecyclerView list;
    private FilterCompositeAdapter<DecoratedAppViewModel> adapter;
    private AppsFilter filter;

    @ProvidePresenter
    AppsPresenter providePresenter() {
        return daggerService().presenters().apps();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_apps, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_apps, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                showFilterDialog();
                return true;
            case R.id.action_reset:
                showResetDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.saveChanges();
    }

    @Override
    public void showLoading() {
        lce.showLoading();
    }

    @Override
    public void onAppsLoaded(List<DecoratedAppViewModel> apps) {
        filter = new AppsFilter(apps, this);
        Picasso picasso = daggerService().main().getPicassoFactory().create(getContext());
        adapter = (FilterCompositeAdapter<DecoratedAppViewModel>)
                new FilterCompositeAdapter.Builder<DecoratedAppViewModel>(getContext())
                        .filter(filter)
                        .add(new AppsViewModelAdapter(picasso, this))
                        .recyclerView(list)
                        .dataset(apps)
                        .create();
        lce.showContent();
    }

    @Override
    public void onItemChanged(int position) {
        list.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void showError(Throwable e) {
        lce.error()
                .button(v -> presenter.loadApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void onItemClick(int position, DecoratedAppViewModel item) {
        onVisibilityClick(position, item);
    }

    @Override
    public void onVisibilityClick(int position, DecoratedAppViewModel item) {
        presenter.toggleAppVisibility(position, item);
    }

    @Override
    public void onFilterResult(String query, List<DecoratedAppViewModel> items) {
        if (items.isEmpty()) {
            String message = TextUtils.isEmpty(query) ? getString(R.string.search_empty)
                    : getString(R.string.search_placeholder, query);
            lce.empty()
                    .message(message)
                    .show();
        } else {
            adapter.setDataset(items);
            adapter.notifyDataSetChanged();
            lce.showContent();
        }
    }

    private void showFilterDialog() {
        int flags = filter.getFlags();
        boolean[] itemsState = {
                (flags & AppsFilter.FLAG_VISIBLE) > 0,
                (flags & AppsFilter.FLAG_HIDDEN) > 0
        };
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.settings_apps_filter)
                .setMultiChoiceItems(R.array.settings_apps_filter_items, itemsState, (dialog, which, isChecked) -> {
                    itemsState[which] = isChecked;
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.settings_apps_filter_reset, (dialog, which) -> {
                    filter.resetFlags();
                })
                .setPositiveButton(R.string.settings_apps_filter_apply, (dialog, which) -> {
                    int newFlags = 0;
                    if (itemsState[0]) {
                        newFlags |= AppsFilter.FLAG_VISIBLE;
                    }
                    if (itemsState[1]) {
                        newFlags |= AppsFilter.FLAG_HIDDEN;
                    }
                    filter.setFlags(newFlags);
                })
                .show();
    }

    private void showResetDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.settings_apps_reset)
                .setMessage(R.string.settings_apps_reset_message)
                .setPositiveButton(R.string.settings_apps_reset_action, (dialog, which) -> {
                    presenter.resetAppsSettings();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
