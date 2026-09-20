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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.settings.SettingsToolbarTitle;
import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

public class AppAliasesFragment extends AppFragment implements SettingsToolbarTitle {

    public static AppAliasesFragment newInstance(String descriptorId) {
        Bundle args = new Bundle();
        args.putString(ARG_DESCRIPTOR_ID, descriptorId);
        AppAliasesFragment fragment = new AppAliasesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final int MAX_ALIAS_LENGTH = 255;
    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";

    private AppAliasesViewModel viewModel;

    private LceLayout lce;
    private RecyclerView list;

    private AppAliasesAdapter adapter;

    private AliasesMenuProvider menuProvider;

    @Override
    public CharSequence getToolbarTitle(Context context) {
        return context.getString(R.string.settings_app_aliases);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, AppAliasesViewModel.class, ViewModelComponent::appAliases);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_app_aliases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupMenu();
        lce = view.findViewById(R.id.lce);
        list = view.findViewById(R.id.list);

        viewModel.aliasesEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<String> aliases) {
                        onAliasesChanged(aliases);
                    }

                    @Override
                    public void onError(Throwable e) {
                        lce.error().message(e.getMessage()).show();
                    }
                });

        if (savedInstanceState == null) {
            viewModel.loadAliases(requireArguments().getString(ARG_DESCRIPTOR_ID));
        }
    }

    private void setupMenu() {
        menuProvider = new AliasesMenuProvider();
        requireActivity().addMenuProvider(menuProvider, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        menuProvider = null;
    }

    private void onAliasesChanged(List<String> aliases) {
        if (adapter == null) {
            adapter = new AppAliasesAdapter(viewModel::deleteAlias);
            list.setAdapter(adapter);
        }
        adapter.setDataset(aliases);
        menuProvider.setItemAddEnabled(aliases.size() < AliasDescriptor.MAX_ALIASES);
        updateLceState(aliases.size());
    }

    private void updateLceState(int size) {
        if (size == 0) {
            lce.empty().message(R.string.settings_app_aliases_empty).show();
        } else {
            lce.showContent();
        }
    }

    private class AliasesMenuProvider implements MenuProvider {

        private boolean itemAddEnabled;
        private MenuItem itemAdd;

        @Override
        public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
            inflater.inflate(R.menu.settings_app_aliases, menu);
            itemAdd = menu.findItem(R.id.action_add);
            setItemAddEnabled(itemAddEnabled);
        }

        @Override
        public boolean onMenuItemSelected(@NonNull MenuItem item) {
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
                            viewModel.addAlias(alias);
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show(AppAliasesFragment.this);
                return true;
            } else {
                return false;
            }
        }

        void setItemAddEnabled(boolean enabled) {
            itemAddEnabled = enabled;
            if (itemAdd != null) {
                itemAdd.setEnabled(enabled);
                itemAdd.getIcon().setAlpha(enabled ? 255 : 32);
            }
        }
    }
}
