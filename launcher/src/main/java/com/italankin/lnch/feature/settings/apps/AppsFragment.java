package com.italankin.lnch.feature.settings.apps;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.italankin.lnch.feature.settings.apps.adapter.AppsFilter;
import com.italankin.lnch.feature.settings.apps.adapter.AppsViewModelAdapter;
import com.italankin.lnch.feature.settings.apps.dialog.FilterFlagsDialogFragment;
import com.italankin.lnch.feature.settings.apps.model.FilterFlag;
import com.italankin.lnch.model.viewmodel.impl.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.dialogfragment.ListenerFragment;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.SimpleDialogFragment;
import com.squareup.picasso.Picasso;

import java.util.EnumSet;
import java.util.List;

public class AppsFragment extends AppFragment implements AppsView,
        AppsViewModelAdapter.Listener,
        AppsFilter.OnFilterResult {

    private static final String DATA_FILTER_FLAGS = "filter_flags";

    private static final String TAG_RESET_DIALOG = "reset_dialog";
    private static final String TAG_FILTER_FLAGS = "filter";

    @InjectPresenter
    AppsPresenter presenter;

    private LceLayout lce;
    private RecyclerView list;
    private CompositeAdapter<AppViewModel> adapter;

    private final AppsFilter filter = new AppsFilter(this);

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_apps, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);
        initAdapter();
        if (savedInstanceState != null) {
            EnumSet<FilterFlag> flags = (EnumSet<FilterFlag>)
                    savedInstanceState.getSerializable(DATA_FILTER_FLAGS);
            if (flags != null) {
                filter.setFlags(flags);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DATA_FILTER_FLAGS, filter.getFlags());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter = null;
        list = null;
        lce = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_apps, menu);
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
    public void onAppsLoaded(List<AppViewModel> apps) {
        adapter.setDataset(apps);
        adapter.notifyDataSetChanged();
        filter.setDataset(apps);
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
            String message = TextUtils.isEmpty(query)
                    ? getString(R.string.search_empty)
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

    public void initAdapter() {
        Context context = requireContext();
        Picasso picasso = daggerService().main().getPicassoFactory().create(context);
        adapter = new CompositeAdapter.Builder<AppViewModel>(context)
                .add(new AppsViewModelAdapter(picasso, this))
                .recyclerView(list)
                .create();
    }

    private void showFilterDialog() {
        new FilterFlagsDialogFragment.Builder()
                .setFlags(filter.getFlags())
                .setListenerProvider(new FilterFlagsDialogListenerProvider())
                .build()
                .show(getChildFragmentManager(), TAG_FILTER_FLAGS);
    }

    private static class FilterFlagsDialogListenerProvider implements FilterFlagsDialogFragment.ListenerProvider {
        @Override
        public FilterFlagsDialogFragment.Listener get(Fragment parentFragment) {
            AppsFragment fragment = (AppsFragment) parentFragment;
            return new FilterFlagsDialogFragment.Listener() {
                @Override
                public void onFlagsSet(EnumSet<FilterFlag> newFlags) {
                    fragment.filter.setFlags(newFlags);
                }

                @Override
                public void onFlagsReset() {
                    fragment.filter.resetFlags();
                }
            };
        }
    }

    private void showResetDialog() {
        new SimpleDialogFragment.Builder()
                .setTitle(R.string.settings_apps_reset)
                .setMessage(R.string.settings_apps_reset_message)
                .setPositiveButton(R.string.settings_apps_reset_action)
                .setNegativeButton(R.string.cancel)
                .setListenerProvider(new ResetDialogListenerProvider())
                .build()
                .show(getChildFragmentManager(), TAG_RESET_DIALOG);
    }

    private static class ResetDialogListenerProvider implements ListenerFragment<SimpleDialogFragment.Listener> {
        @Override
        public SimpleDialogFragment.Listener get(Fragment parentFragment) {
            AppsFragment fragment = (AppsFragment) parentFragment;
            return fragment.presenter::resetAppsSettings;
        }
    }
}
