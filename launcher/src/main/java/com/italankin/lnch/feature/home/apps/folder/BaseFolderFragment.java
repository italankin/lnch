package com.italankin.lnch.feature.home.apps.folder;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.adapter.AppDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.IntentDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.folder.empty.EmptyFolderDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.folder.widget.AlignFrameView;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

abstract class BaseFolderFragment extends AppFragment implements BaseFolderView,
        FragmentResultListener,
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

    protected static final String FOLDER_REQUEST_KEY = "folder";
    protected static final long ANIM_DURATION = 150;

    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String ARG_ANCHOR = "anchor";
    private static final String ARG_REQUEST_KEY = "request_key";

    private static final String STATE_BACKSTACK_ID = "backstack_id";
    private static final String BACKSTACK_NAME = "folder";
    private static final String TAG = "folder";

    protected RecyclerView list;
    protected AlignFrameView alignFrameView;
    protected View container;
    protected TextView title;

    protected HomeAdapter adapter;

    protected ErrorDelegate errorDelegate;

    private int backstackId = -1;

    protected abstract BaseFolderPresenter<? extends BaseFolderView> getPresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            backstackId = savedInstanceState.getInt(STATE_BACKSTACK_ID);
        }
        getParentFragmentManager().setFragmentResultListener(FOLDER_REQUEST_KEY, this, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BACKSTACK_ID, backstackId);
    }

    @Override
    public final void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
        if (!FOLDER_REQUEST_KEY.equals(requestKey)) {
            return;
        }
        String resultKey = result.getString(FragmentResults.RESULT);
        handleFragmentResult(resultKey, result);
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

        Point anchor = requireArguments().getParcelable(ARG_ANCHOR);
        alignFrameView.setAnchorPoint(anchor.x, anchor.y);
        alignFrameView.post(() -> {
            WindowInsets insets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
            alignFrameView.setPaddingRelative(alignFrameView.getPaddingStart(), insets.getStableInsetTop(),
                    alignFrameView.getPaddingEnd(), insets.getStableInsetBottom());
        });
        alignFrameView.setOnClickListener(v -> dismiss());

        adapter = new HomeAdapter.Builder(requireContext())
                .add(new AppDescriptorUiAdapter(this, true))
                .add(new PinnedShortcutDescriptorUiAdapter(this))
                .add(new IntentDescriptorUiAdapter(this))
                .add(new DeepShortcutDescriptorUiAdapter(this))
                .add(new EmptyFolderDescriptorUiAdapter())
                .setHasStableIds(true)
                .create();
        list.setLayoutManager(new FlexboxLayoutManager(requireContext(), FlexDirection.ROW));
        list.setAdapter(adapter);

        initDelegates(requireContext());

        getPresenter().loadFolder(requireArguments().getString(ARG_FOLDER_ID));
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onShowFolder(FolderDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
        title.setText(descriptor.getVisibleLabel());
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

    protected void handleFragmentResult(String resultKey, @NonNull Bundle result) {
        dismiss();
        if (!FragmentResults.OnActionHandled.KEY.equals(resultKey)) {
            sendResult(result);
        }
    }

    protected void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    protected void animatePopupAppearance() {
        container.setScaleX(0.4f);
        container.setScaleY(0.4f);
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
    }
}
