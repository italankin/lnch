package com.italankin.lnch.feature.settings_apps;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.settings_apps.adapter.AppsViewModelAdapter;
import com.italankin.lnch.feature.settings_apps.model.AppViewModel;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;

import java.util.List;

public class AppsVisibilityFragment extends AppFragment implements AppsVisibilityView {

    @InjectPresenter
    AppsVisibilityPresenter presenter;

    private RecyclerView list;

    @ProvidePresenter
    AppsVisibilityPresenter providePresenter() {
        return daggerService().presenters().appsVisibility();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps_visibility, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        list = view.findViewById(R.id.list);
    }

    @Override
    public void onAppsLoaded(List<AppViewModel> apps) {
        new CompositeAdapter.Builder<AppViewModel>(getContext())
                .add(new AppsViewModelAdapter(presenter::toggleAppVisibility))
                .recyclerView(list)
                .dataset(apps)
                .create();
    }

    @Override
    public void onItemChanged(int position) {
        list.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.saveChanges();
    }
}
