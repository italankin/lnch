package com.italankin.lnch.feature.settings.preferencesearch;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;
import com.italankin.lnch.util.widget.LceLayout;
import com.italankin.lnch.util.widget.SearchViewFixed;
import me.italankin.adapterdelegates.CompositeAdapter;

import java.util.List;

public class PreferenceSearchFragment extends AppFragment {

    public static PreferenceSearchFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        PreferenceSearchFragment fragment = new PreferenceSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private PreferenceSearchViewModel viewModel;

    private LceLayout lce;
    private CompositeAdapter<PreferenceSearchItem> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, PreferenceSearchViewModel.class, ViewModelComponent::preferenceSearch);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_items_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);

        Context context = requireContext();
        adapter = new CompositeAdapter.Builder<PreferenceSearchItem>(context)
                .add(new PreferenceSearchAdapter(item -> {
                    sendResult(ShowPreferenceContract.result(item.key));
                }))
                .add(new PreferenceSearchCategoryAdapter())
                .recyclerView(list)
                .setHasStableIds(true)
                .create();

        viewModel.searchResultsEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<PreferenceSearchItem> results) {
                        onSearchResults(results);
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.settings_preference_search, menu);

        MenuItem actionSearchItem = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchViewFixed(requireContext());
        actionSearchItem.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.search(newText);
                return true;
            }
        });
        actionSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                item.getActionView().requestFocus();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                sendResult(new ClosePreferenceSearchContract().result());
                return true;
            }
        });
        searchView.setQueryHint(getString(R.string.hint_search_preference));
        actionSearchItem.expandActionView();
    }

    private void onSearchResults(List<PreferenceSearchItem> items) {
        adapter.setDataset(items);
        adapter.notifyDataSetChanged();
        lce.showContent();
    }

    public static class ClosePreferenceSearchContract extends SignalFragmentResultContract {
        public ClosePreferenceSearchContract() {
            super("close_preference_search");
        }
    }

    public static class ShowPreferenceContract implements FragmentResultContract<SettingsEntry.Key> {
        private static final String KEY = "show_preference";
        private static final String PREFERENCE_KEY = "preference_key";

        static Bundle result(SettingsEntry.Key key) {
            Bundle bundle = new Bundle(2);
            bundle.putString(RESULT_KEY, KEY);
            bundle.putSerializable(PREFERENCE_KEY, key);
            return bundle;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public SettingsEntry.Key parseResult(Bundle result) {
            return (SettingsEntry.Key) result.getSerializable(PREFERENCE_KEY);
        }
    }
}
