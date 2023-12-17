package com.italankin.lnch.feature.settings.hidden_items;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.util.filter.ListFilter;
import com.italankin.lnch.util.filter.SimpleListFilter;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.imageloader.cache.Cache;
import com.italankin.lnch.util.imageloader.cache.LruCache;
import com.italankin.lnch.util.widget.LceLayout;
import me.italankin.adapterdelegates.CompositeAdapter;

import java.util.List;

public class HiddenItemsFragment extends AppFragment implements SettingsToolbarTitle,
        ListFilter.OnFilterResult<HiddenItem> {

    private HiddenItemsViewModel viewModel;

    private LceLayout lce;
    private CompositeAdapter<HiddenItem> adapter;

    private final SimpleListFilter<HiddenItem> filter = SimpleListFilter.createSearchable(this);
    private final Cache imageLoaderCache = new LruCache(48);

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_home_hidden_items);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, HiddenItemsViewModel.class, ViewModelComponent::hiddenItems);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_items_list, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);

        Context context = requireContext();
        ImageLoader imageLoader = new ImageLoader.Builder(context)
                .cache(imageLoaderCache)
                .build();
        adapter = new CompositeAdapter.Builder<HiddenItem>(context)
                .add(new HiddenItemAdapter(imageLoader, item -> viewModel.showItem(item.descriptor)))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();

        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.settings_list_divider);
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(drawable);
        list.addItemDecoration(decoration);

        lce.showLoading();
        viewModel.hiddenItemsEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<HiddenItem> hiddenItems) {
                        filter.setDataset(hiddenItems);
                    }

                    @Override
                    public void onError(Throwable e) {
                        lce.error()
                                .message(e.getMessage())
                                .show();
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.settings_hidden, menu);
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
    public void onFilterResult(String query, List<HiddenItem> items) {
        adapter.setDataset(items);
        adapter.notifyDataSetChanged();
        if (items.isEmpty()) {
            lce.empty()
                    .message(R.string.settings_home_hidden_items_filter_empty)
                    .show();
        } else {
            lce.showContent();
        }
    }
}
