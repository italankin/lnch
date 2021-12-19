package com.italankin.lnch.feature.settings.apps;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings.apps.adapter.AppsSettingsAdapter;
import com.italankin.lnch.feature.settings.apps.adapter.AppsSettingsFilter;
import com.italankin.lnch.feature.settings.apps.dialog.FilterFlagsDialogFragment;
import com.italankin.lnch.feature.settings.apps.model.FilterFlag;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.EnumSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class AppsSettingsFragment extends AppFragment implements AppsSettingsView,
        AppsSettingsAdapter.Listener,
        FilterFlagsDialogFragment.Listener,
        ListFilter.OnFilterResult<AppDescriptorUi> {

    private static final String DATA_FILTER_FLAGS = "filter_flags";

    private static final String TAG_FILTER_FLAGS = "filter";

    @InjectPresenter
    AppsSettingsPresenter presenter;

    private LceLayout lce;
    private RecyclerView list;
    private CompositeAdapter<AppDescriptorUi> adapter;

    private final AppsSettingsFilter filter = new AppsSettingsFilter(this);

    private Callbacks callbacks;

    @ProvidePresenter
    AppsSettingsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().appsSettings();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_apps_list, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);
        initAdapter();
        addListDivider();
        if (savedInstanceState != null) {
            EnumSet<FilterFlag> flags = (EnumSet<FilterFlag>)
                    savedInstanceState.getSerializable(DATA_FILTER_FLAGS);
            if (flags != null) {
                filter.setFlags(flags);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showLoading() {
        lce.showLoading();
    }

    @Override
    public void onAppsUpdated(List<AppDescriptorUi> apps) {
        filter.setDataset(apps);
        lce.showContent();
    }

    @Override
    public void showError(Throwable e) {
        lce.error()
                .button(v -> presenter.observeApps())
                .message(e.getMessage())
                .show();
    }

    @Override
    public void onVisibilityClick(int position, AppDescriptorUi item) {
        presenter.toggleAppVisibility(item);
    }

    @Override
    public void onAppClick(int position, AppDescriptorUi item) {
        if (callbacks != null) {
            callbacks.showAppDetails(item.getDescriptor().getId());
        }
    }

    @Override
    public void onFilterResult(String query, List<AppDescriptorUi> items) {
        if (adapter == null) {
            return;
        }
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

    @Override
    public void onFlagsSet(EnumSet<FilterFlag> newFlags) {
        filter.setFlags(newFlags);
    }

    @Override
    public void onFlagsReset() {
        filter.resetFlags();
    }

    private void initAdapter() {
        Context context = requireContext();
        Picasso picasso = LauncherApp.daggerService.main().picassoFactory().create(context);
        adapter = new CompositeAdapter.Builder<AppDescriptorUi>(context)
                .add(new AppsSettingsAdapter(picasso, this))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();
    }

    private void addListDivider() {
        Drawable drawable = requireContext().getDrawable(R.drawable.settings_apps_divider);
        assert drawable != null;
        DividerItemDecoration decoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        decoration.setDrawable(drawable);
        list.addItemDecoration(decoration);
    }

    private void showFilterDialog() {
        new FilterFlagsDialogFragment.Builder()
                .setFlags(filter.getFlags())
                .build()
                .show(getChildFragmentManager(), TAG_FILTER_FLAGS);
    }

    public interface Callbacks {
        void showAppDetails(String descriptorId);
    }
}
