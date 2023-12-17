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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.adapter.*;
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

abstract class BaseFolderFragment extends AppFragment implements AppDescriptorUiAdapter.Listener,
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
    protected Preferences preferences;

    protected String folderId;
    protected boolean drawOverlay;

    private boolean isFullscreen;
    private int backstackId = -1;

    protected abstract BaseFolderViewModel getViewModel();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
        isFullscreen = preferences.get(Preferences.FULLSCREEN_FOLDERS);
        drawOverlay = isFullscreen || preferences.get(Preferences.FOLDER_SHOW_OVERLAY);
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
            if (anchor != null) {
                alignFrameView.setAnchorPoint(anchor.x, anchor.y);
            }
        }

        alignFrameView.post(() -> {
            WindowInsets insets = alignFrameView.getRootWindowInsets();
            alignFrameView.setPaddingRelative(alignFrameView.getPaddingStart(), insets.getStableInsetTop(),
                    alignFrameView.getPaddingEnd(), insets.getStableInsetBottom());
        });
        alignFrameView.setOnClickListener(v -> dismiss());

        Preferences.ItemWidth folderItemWidth = preferences.get(Preferences.FOLDER_ITEM_WIDTH);
        HomeAdapterDelegate.Params params = new HomeAdapterDelegate.Params(true,
                itemPrefs -> folderItemWidth,
                itemPrefs -> Preferences.HomeAlignment.START);
        adapter = new HomeAdapter.Builder(requireContext())
                .add(new AppDescriptorUiAdapter(this, params))
                .add(new PinnedShortcutDescriptorUiAdapter(this, params))
                .add(new IntentDescriptorUiAdapter(this, params))
                .add(new DeepShortcutDescriptorUiAdapter(this, params))
                .add(new EmptyFolderDescriptorUiAdapter())
                .setHasStableIds(true)
                .create();
        if (folderItemWidth == Preferences.ItemWidth.WRAP) {
            list.setLayoutManager(new FlexboxLayoutManager(requireContext(), FlexDirection.ROW));
        } else {
            list.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        list.setAdapter(adapter);

        initDelegates(requireContext());

        getViewModel()
                .showFolderEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(FolderState state) {
                        onShowFolder(state.folder.getVisibleLabel(), state.items, state.userPrefs, state.animated);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorDelegate.showError(e.getMessage());
                    }
                });

        getViewModel()
                .folderUpdateEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<DescriptorUi> items) {
                        onFolderUpdated(items);
                    }

                    @Override
                    public void onError(Throwable e) {
                        errorDelegate.showError(e.getMessage());
                    }
                });

        if (savedInstanceState == null) {
            getViewModel().loadFolder(folderId, true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    protected void onShowFolder(String folderTitle, List<DescriptorUi> items, UserPrefs userPrefs, boolean animated) {
        title.setText(folderTitle);
        title.setTypeface(userPrefs.itemPrefs.typeface);
        adapter.updateUserPrefs(userPrefs);
        onFolderUpdated(items);
        if (animated) {
            animatePopupAppearance();
        } else {
            container.setVisibility(View.VISIBLE);
        }
    }

    protected void onFolderUpdated(List<DescriptorUi> items) {
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

        if (drawOverlay) {
            Integer overlayColor = preferences.get(Preferences.FOLDER_OVERLAY_COLOR);
            if (overlayColor == null) {
                overlayColor = ContextCompat.getColor(requireContext(), R.color.fullscreen_folder_overlay);
            }
            ValueAnimator animator = ValueAnimator.ofInt(Color.TRANSPARENT, overlayColor);
            animator.setEvaluator(new ArgbEvaluatorCompat());
            animator.addUpdateListener(animation -> {
                alignFrameView.setBackgroundColor((int) animation.getAnimatedValue());
            });
            animator.setDuration(ANIM_DURATION);
            animator.start();
        }
    }
}
