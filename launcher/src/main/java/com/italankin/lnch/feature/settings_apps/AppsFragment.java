package com.italankin.lnch.feature.settings_apps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings_apps.adapter.AppsFilter;
import com.italankin.lnch.feature.settings_apps.adapter.AppsViewModelAdapter;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.FilterCompositeAdapter;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

public class AppsFragment extends AppFragment implements AppsView, AppsViewModelAdapter.Listener, AppsFilter.OnFilterResult {

    @InjectPresenter
    AppsPresenter presenter;

    private LceLayout lce;
    private RecyclerView list;
    private FilterCompositeAdapter<AppViewModel> adapter;

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
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getString(R.string.hint_search));
        // TODO
        if (true) {
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
    public void onAppsLoaded(List<AppViewModel> apps) {
        adapter = (FilterCompositeAdapter<AppViewModel>)
                new FilterCompositeAdapter.Builder<AppViewModel>(getContext())
                        .filter(new AppsFilter(apps, this))
                        .add(new AppsViewModelAdapter(this))
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
    public void onVisibilityClick(int position, AppViewModel item) {
        presenter.toggleAppVisibility(position, item);
    }

    @Override
    public void onFilterResult(String query, List<AppViewModel> items) {
        if (items.isEmpty()) {
            lce.empty()
                    .message(getString(R.string.search_placeholder, query)) // TODO
                    .show();
        } else {
            adapter.setDataset(items);
            lce.showContent();
        }
    }
}
