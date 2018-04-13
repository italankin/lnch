package com.italankin.lnch.ui.feature.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.italankin.lnch.R;
import com.italankin.lnch.model.AppItem;
import com.italankin.lnch.ui.base.AppActivity;
import com.italankin.lnch.ui.util.SwapItemHelper;

import java.util.List;

public class HomeActivity extends AppActivity implements IHomeView,
        SwapItemHelper.Callback,
        PackageModelAdapter.Listener {

    @InjectPresenter
    HomePresenter presenter;

    private CoordinatorLayout root;
    private ViewGroup searchBar;
    private AutoCompleteTextView editSearch;
    private View btnSettings;
    private RecyclerView list;

    private InputMethodManager inputMethodManager;
    private BroadcastReceiver packageUpdatesReceiver;
    private Handler handler;

    private FrameLayout progressContainer;

    private boolean editMode = false;

    private TopBarBehavior searchBarBehavior;
    private ItemTouchHelper touchHelper;

    @ProvidePresenter
    HomePresenter providePresenter() {
        return daggerService().presenters().home();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        handler = new Handler(getMainLooper());

        setupWindow();

        setContentView(R.layout.activity_launcher);
        root = findViewById(R.id.root);
        list = findViewById(R.id.list);
        searchBar = findViewById(R.id.search_bar);
        editSearch = findViewById(R.id.edit_search);
        btnSettings = findViewById(R.id.btn_settings);

        setupRoot();
        setupList();
        setupSearchBar();

        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (packageUpdatesReceiver != null) {
            unregisterReceiver(packageUpdatesReceiver);
        }
    }

    @Override
    public void onBackPressed() {
        if (searchBarBehavior.isShown()) {
            searchBarBehavior.hide();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (searchBarBehavior.isShown()) {
            searchBarBehavior.hide();
        } else if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            list.smoothScrollToPosition(0);
        }
    }

    private void setupWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
                WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
    }

    private void setupList() {
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
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    private void setupSearchBar() {
        searchBarBehavior = new TopBarBehavior(searchBar, list, new TopBarBehavior.Listener() {
            @Override
            public void onShow() {
                editSearch.requestFocus();
                inputMethodManager.showSoftInput(editSearch, 0);
            }

            @Override
            public void onHide() {
                editSearch.setText("");
                inputMethodManager.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                editSearch.clearFocus();
            }
        });
        searchBarBehavior.setEnabled(!editMode);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) searchBar.getLayoutParams();
        lp.setBehavior(searchBarBehavior);
        searchBar.setLayoutParams(lp);

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                SearchAdapter adapter = (SearchAdapter) editSearch.getAdapter();
                if (adapter.getCount() > 0) {
                    presenter.startApp(this, adapter.getItem(0));
                } else {
                    String query = editSearch.getText().toString().trim();
                    presenter.startSearch(this, query);
                }
                editSearch.setText("");
                searchBarBehavior.hide();
            }
            return true;
        });
        editSearch.setOnItemClickListener((parent, view, position, id) -> {
            editSearch.setText("");
            SearchAdapter adapter = (SearchAdapter) parent.getAdapter();
            presenter.startApp(this, adapter.getItem(position));
            searchBarBehavior.hide();
        });
        editSearch.setThreshold(1);

        btnSettings.setOnClickListener(v -> {
            setEditMode(true);
        });
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
    public void onAppsLoaded(List<AppItem> appList) {
        hideProgress();
        PackageModelAdapter adapter = (PackageModelAdapter) list.getAdapter();
        if (adapter == null) {
            adapter = new PackageModelAdapter(this, this);
        }
        adapter.setDataset(appList);
        list.setAdapter(adapter);
        list.setVisibility(View.VISIBLE);
        editSearch.setAdapter(new SearchAdapter(appList));
    }

    @Override
    public void showError(Throwable e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position, AppItem item) {
        if (!editMode) {
            presenter.startApp(this, item);
        }
    }

    @Override
    public void onItemLongClick(int position, AppItem item) {
        if (editMode) {
            View view = list.getLayoutManager().findViewByPosition(position);
            touchHelper.startDrag(list.getChildViewHolder(view));
        } else {
            presenter.startAppSettings(this, item);
        }
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.swapItems(from, to);
        list.getAdapter().notifyItemMoved(from, to);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void registerReceiver() {
        packageUpdatesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handler.postDelayed(() -> presenter.loadApps(), 1000);
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(packageUpdatesReceiver, filter);
    }

    private void setEditMode(boolean value) {
        editMode = value;
        searchBarBehavior.hide();
        searchBarBehavior.setEnabled(!editMode);
        if (editMode) {
            Snackbar snackbar = Snackbar.make(root, R.string.edit_mode_hint, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.edit_mode_exit, v -> {
                setEditMode(false);
                snackbar.dismiss();
            });
            snackbar.show();
            int bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                    getResources().getDisplayMetrics());
            list.setPadding(0, 0, 0, bottom);
        } else {
            list.setPadding(0, 0, 0, 0);
        }
    }
}

