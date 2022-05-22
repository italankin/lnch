package com.italankin.lnch.feature.settings.hidden_items;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.util.adapterdelegate.CompositeAdapter;
import com.italankin.lnch.util.widget.LceLayout;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HiddenItemsFragment extends AppFragment implements HiddenItemsView {

    @InjectPresenter
    HiddenItemsPresenter presenter;

    private LceLayout lce;
    private CompositeAdapter<HiddenItem> adapter;

    @ProvidePresenter
    HiddenItemsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().hiddenItems();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_hidden_items, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView list = view.findViewById(R.id.list);
        lce = view.findViewById(R.id.lce);

        Context context = requireContext();
        Picasso picasso = LauncherApp.daggerService.main().picassoFactory().create(context);
        adapter = new CompositeAdapter.Builder<HiddenItem>(context)
                .add(new HiddenItemAdapter(picasso, item -> presenter.showItem(item.descriptor)))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();

        Drawable drawable = AppCompatResources.getDrawable(context, R.drawable.settings_list_divider);
        DividerItemDecoration decoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(drawable);
        list.addItemDecoration(decoration);
    }

    @Override
    public void showLoading() {
        lce.showLoading();
    }

    @Override
    public void onItemsUpdated(List<HiddenItem> items) {
        adapter.setDataset(items);
        adapter.notifyDataSetChanged();
        if (items.isEmpty()) {
            lce.empty()
                    .message(R.string.settings_home_hidden_items_empty)
                    .show();
        } else {
            lce.showContent();
        }
    }

    @Override
    public void showError(Throwable e) {
        lce.error()
                .button(v -> presenter.observeApps())
                .message(e.getMessage())
                .show();
    }
}
