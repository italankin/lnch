package com.italankin.lnch.feature.settings.apps.details.aliases;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class AppAliasesFragment extends AppFragment implements AppAliasesView, SettingsToolbarTitle {

    public static AppAliasesFragment newInstance(String descriptorId) {
        Bundle args = new Bundle();
        args.putString(ARG_DESCRIPTOR_ID, descriptorId);
        AppAliasesFragment fragment = new AppAliasesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int MAX_ALIAS_LENGTH = 255;
    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";

    @InjectPresenter
    AppAliasesPresenter presenter;

    private LceLayout lce;
    private RecyclerView list;

    private AppAliasesAdapter adapter;

    private MenuItem itemAdd;

    @ProvidePresenter
    AppAliasesPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().appAliases();
    }

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_app_aliases);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_app_aliases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lce = view.findViewById(R.id.lce);
        list = view.findViewById(R.id.list);

        presenter.loadAliases(requireArguments().getString(ARG_DESCRIPTOR_ID));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.settings_app_aliases, menu);
        itemAdd = menu.findItem(R.id.action_add);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            EditTextAlertDialog.builder(requireContext())
                    .setTitle(R.string.settings_app_aliases_title)
                    .setCancellable(true)
                    .customizeEditText(editText -> {
                        editText.setMaxLines(1);
                        editText.setSingleLine(true);
                        editText.setHint(getString(R.string.settings_app_aliases_hint, MAX_ALIAS_LENGTH));
                        editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_ALIAS_LENGTH)});
                    })
                    .setPositiveButton(R.string.settings_app_aliases_add, (dialog, editText) -> {
                        String alias = editText.getText().toString();
                        presenter.addAlias(alias);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAliasesChanged(List<String> aliases, boolean canAddMore) {
        if (adapter == null) {
            adapter = new AppAliasesAdapter(presenter::deleteAlias);
            list.setAdapter(adapter);
        }
        adapter.setDataset(aliases);
        updateItemAddState(canAddMore);
        updateLceState(aliases.size());
    }

    @Override
    public void notifyAliasRemoved(int size, boolean canAddMore) {
        adapter.notifyDataSetChanged();
        updateItemAddState(canAddMore);
        updateLceState(size);
    }

    @Override
    public void notifyAliasAdded(int size, boolean canAddMore) {
        adapter.notifyDataSetChanged();
        updateItemAddState(canAddMore);
        updateLceState(size);
    }

    @Override
    public void onError(Throwable e) {
        lce.error().message(e.getMessage()).show();
    }

    @Override
    public void onInvalidAlias() {
        Toast.makeText(requireContext(), R.string.settings_app_aliases_invalid, Toast.LENGTH_SHORT).show();
    }

    private void updateLceState(int size) {
        if (size == 0) {
            lce.empty().message(R.string.settings_app_aliases_empty).show();
        } else {
            lce.showContent();
        }
    }

    private void updateItemAddState(boolean canAddMore) {
        itemAdd.setEnabled(canAddMore);
        itemAdd.getIcon().setAlpha(canAddMore ? 255 : 32);
    }
}
