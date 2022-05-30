package com.italankin.lnch.feature.home.apps.folder;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.adapter.AppDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapterDelegate;
import com.italankin.lnch.feature.home.adapter.IntentDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.folder.widget.AlignFrameView;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultManager;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

import java.util.List;

abstract class BaseFolderFragment extends AppFragment implements BaseFolderView,
        AppDescriptorUiAdapter.Listener,
        PinnedShortcutDescriptorUiAdapter.Listener,
        IntentDescriptorUiAdapter.Listener,
        DeepShortcutDescriptorUiAdapter.Listener {

    @NonNull
    protected static Bundle makeArgs(FolderDescriptor descriptor, String requestKey, @Nullable Point anchor) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_FOLDER_ID, descriptor.getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        return args;
    }

    protected static final String REQUEST_KEY_FOLDER = "folder";
    protected static final long ANIM_DURATION = 150;
    private static final float ANIM_INITIAL_SCALE = .4f;

    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String ARG_ANCHOR = "anchor";

    private static final String STATE_BACKSTACK_ID = "backstack_id";
    private static final String BACKSTACK_NAME = "folder";
    private static final String TAG = "folder";

    private static final float MIN_FULLSCREEN_WIDTH_FACTOR = .75f;
    private static final float MIN_FULLSCREEN_HEIGHT_FACTOR = .3f;

    protected FragmentResultManager fragmentResultManager;

    protected RecyclerView list;
    protected AlignFrameView alignFrameView;
    protected View container;
    protected TextView title;

    protected HomeAdapter adapter;

    protected ErrorDelegate errorDelegate;

    protected String folderId;
    protected boolean darkBackground;

    private boolean isFullscreen;
    private int backstackId = -1;

    protected abstract BaseFolderPresenter<? extends BaseFolderView> getPresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFullscreen = LauncherApp.daggerService.main()
                .preferences()
                .get(Preferences.FULLSCREEN_FOLDERS);
        darkBackground = isFullscreen;
        folderId = requireArguments().getString(ARG_FOLDER_ID);
        if (savedInstanceState != null) {
            backstackId = savedInstanceState.getInt(STATE_BACKSTACK_ID);
        }
        fragmentResultManager = new FragmentResultManager(getParentFragmentManager(), this, REQUEST_KEY_FOLDER)
                .register(new ActionPopupFragment.ActionDoneContract(), ignored -> {
                    // empty
                })
                .setUnhandledResultListener((key, result) -> {
                    dismiss();
                    sendResult(result);
                });
        fragmentResultManager.attach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BACKSTACK_ID, backstackId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        alignFrameView = view.findViewById(R.id.folder_frame);
        container = view.findViewById(R.id.folder_container);
        container.setClipToOutline(true);
        list = view.findViewById(R.id.folder_list);
        title = view.findViewById(R.id.folder_title);

        if (isFullscreen) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            container.setMinimumWidth((int) (dm.widthPixels * MIN_FULLSCREEN_WIDTH_FACTOR));
            container.setMinimumHeight((int) (dm.heightPixels * MIN_FULLSCREEN_HEIGHT_FACTOR));
        } else {
            Point anchor = requireArguments().getParcelable(ARG_ANCHOR);
            alignFrameView.setAnchorPoint(anchor.x, anchor.y);
        }

        alignFrameView.post(() -> {
            WindowInsets insets = alignFrameView.getRootWindowInsets();
            alignFrameView.setPaddingRelative(alignFrameView.getPaddingStart(), insets.getStableInsetTop(),
                    alignFrameView.getPaddingEnd(), insets.getStableInsetBottom());
        });
        alignFrameView.setOnClickListener(v -> dismiss());

        HomeAdapterDelegate.Params params = new HomeAdapterDelegate.Params(true, true);
        adapter = new HomeAdapter.Builder(requireContext())
                .add(new AppDescriptorUiAdapter(this, params))
                .add(new PinnedShortcutDescriptorUiAdapter(this, params))
                .add(new IntentDescriptorUiAdapter(this, params))
                .add(new DeepShortcutDescriptorUiAdapter(this, params))
                .add(new EmptyFolderDescriptorUiAdapter())
                .setHasStableIds(true)
                .create();
        list.setLayoutManager(new FlexboxLayoutManager(requireContext(), FlexDirection.ROW));
        list.setAdapter(adapter);

        initDelegates(requireContext());

        getPresenter().loadFolder(folderId);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onShowFolder(String folderTitle, List<DescriptorUi> items, UserPrefs userPrefs) {
        title.setText(folderTitle);
        adapter.updateUserPrefs(userPrefs);
        onFolderUpdated(items);
        animatePopupAppearance();
    }

    @Override
    public void onFolderUpdated(List<DescriptorUi> items) {
        adapter.setDataset(items);
        adapter.notifyDataSetChanged();
    }

    protected void initDelegates(Context context) {
        errorDelegate = new ErrorDelegateImpl(context) {
            @Override
            public void showError(CharSequence message) {
                super.showError(message);
                dismiss();
            }
        };
    }

    @Override
    public void onError(Throwable error) {
        errorDelegate.showError(error.getMessage());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

    public void show(FragmentManager fragmentManager, @IdRes int containerId) {
        backstackId = fragmentManager.beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.fragment_popup_out)
                .add(containerId, this, TAG)
                .addToBackStack(BACKSTACK_NAME)
                .commit();
    }

    public void dismiss() {
        if (backstackId >= 0) {
            getParentFragmentManager()
                    .popBackStack(backstackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            backstackId = -1;
        }
    }

    private void animatePopupAppearance() {
        container.setScaleX(ANIM_INITIAL_SCALE);
        container.setScaleY(ANIM_INITIAL_SCALE);
        container.setAlpha(0);

        container.animate()
                .withStartAction(() -> {
                    container.setVisibility(View.VISIBLE);
                })
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(ANIM_DURATION)
                .withEndAction(() -> {
                    alignFrameView.setLayoutTransition(new LayoutTransition());
                })
                .start();

        if (darkBackground) {
            int bgColor = ContextCompat.getColor(requireContext(), R.color.dark_folder_background);
            ValueAnimator animator = ValueAnimator.ofInt(Color.TRANSPARENT, bgColor);
            animator.setEvaluator(new ArgbEvaluatorCompat());
            animator.addUpdateListener(animation -> {
                alignFrameView.setBackgroundColor((int) animation.getAnimatedValue());
            });
            animator.setDuration(ANIM_DURATION);
            animator.start();
        }
    }
}
