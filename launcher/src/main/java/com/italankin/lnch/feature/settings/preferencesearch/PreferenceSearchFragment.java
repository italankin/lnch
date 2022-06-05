package com.italankin.lnch.feature.settings.preferencesearch;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
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
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.feature.settings.searchstore.SettingsEntry;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

public class PreferenceSearchFragment extends AppFragment implements PreferenceSearchView {

    public static PreferenceSearchFragment newInstance(String requestKey) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        PreferenceSearchFragment fragment = new PreferenceSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectPresenter
    PreferenceSearchPresenter presenter;

    private LceLayout lce;
    private CompositeAdapter<PreferenceSearchItem> adapter;

    @ProvidePresenter
    PreferenceSearchPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().preferenceSearch();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

            }
        };
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.settings_preference_search, menu);

        MenuItem actionSearchItem = menu.findItem(R.id.action_search);
        SearchView searchView = new SearchView(
                new ContextThemeWrapper(requireContext(), R.style.AppTheme_Preferences_PreferenceSearch));
        actionSearchItem.setActionView(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                presenter.search(newText);
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
        actionSearchItem.expandActionView();
    }

    @Override
    public void onSearchResults(List<PreferenceSearchItem> items) {
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
