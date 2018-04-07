package com.italankin.lnch.ui.feature.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.italankin.lnch.R;
import com.italankin.lnch.model.PackageModel;
import com.italankin.lnch.ui.base.AppActivity;
import com.italankin.lnch.ui.util.SwapItemHelper;

import java.util.List;

public class HomeActivity extends AppActivity implements IHomeView,
        SwapItemHelper.Callback,
        PackageModelAdapter.Listener {

    @InjectPresenter
    HomePresenter presenter;

    private CoordinatorLayout root;
    private RecyclerView list;
    private ItemTouchHelper touchHelper;

    private PackageManager pm;
    private FrameLayout progressContainer;
    private BroadcastReceiver br;

    private boolean editMode = false;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return daggerService().presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pm = getPackageManager();

        setContentView(R.layout.activity_launcher);
        setupRoot();
        setupList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                presenter.loadApps();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(br, filter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            list.smoothScrollToPosition(0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(br);
    }

    private void setupList() {
        list = findViewById(R.id.list);
        RecyclerView.LayoutManager layoutManager = getFlexboxLayoutManager();
        touchHelper = new ItemTouchHelper(new SwapItemHelper(this));
        touchHelper.attachToRecyclerView(list);
        list.setLayoutManager(layoutManager);
    }

    private RecyclerView.LayoutManager getStaggeredGridLayoutManager() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        return layoutManager;
    }

    private RecyclerView.LayoutManager getGridLayoutManager() {
        return new GridLayoutManager(this, 2);
    }

    private RecyclerView.LayoutManager getFlexboxLayoutManager() {
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        return layoutManager;
    }

    private void setupRoot() {
        root = findViewById(R.id.root);
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void showProgress() {
        if (progressContainer != null) {
            return;
        }
        list.setVisibility(View.INVISIBLE);
        progressContainer = new FrameLayout(this);
        ProgressBar progressBar = new ProgressBar(this);
        int size = getResources().getDimensionPixelSize(R.dimen.progress_indicator_size);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size, Gravity.CENTER);
        progressContainer.addView(progressBar, params);
        root.addView(progressContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void hideProgress() {
        if (progressContainer != null) {
            root.removeView(progressContainer);
            progressContainer = null;
        }
    }

    @Override
    public void onAppsLoaded(List<PackageModel> appList) {
        hideProgress();
        PackageModelAdapter adapter = new PackageModelAdapter(this, appList, this);
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // TODO stub
    }

    @Override
    public void onItemClick(int position, PackageModel item) {
        Intent intent = pm.getLaunchIntentForPackage(item.packageName);
        if (intent != null && intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(int position, PackageModel item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        } else {
            Uri uri = Uri.fromParts("package", item.packageName, null);
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
            }
        }
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.swap(from, to);
        list.getAdapter().notifyItemMoved(from, to);
    }
}

