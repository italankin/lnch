package com.italankin.lnch.feature.home.apps.folder;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.home.adapter.AppDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.DeepShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.HomeAdapter;
import com.italankin.lnch.feature.home.adapter.IntentDescriptorUiAdapter;
import com.italankin.lnch.feature.home.adapter.PinnedShortcutDescriptorUiAdapter;
import com.italankin.lnch.feature.home.apps.folder.widget.AlignFrameView;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.DescriptorUtils;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class FolderFragment extends AppFragment implements FolderView,
        AppDescriptorUiAdapter.Listener,
        PinnedShortcutDescriptorUiAdapter.Listener,
        IntentDescriptorUiAdapter.Listener,
        DeepShortcutDescriptorUiAdapter.Listener {

    public static FolderFragment newInstance(
            GroupDescriptor descriptor,
            @Nullable Point anchor) {
        FolderFragment fragment = new FolderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptor.getId());
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_ANCHOR = "anchor";
    private static final String STATE_BACKSTACK_ID = "backstack_id";
    private static final String BACKSTACK_NAME = "folder";
    private static final String TAG = "folder";

    @InjectPresenter
    FolderPresenter presenter;

    private RecyclerView list;
    private int backstackId = -1;

    public static boolean dismiss(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) != null) {
            fragmentManager.popBackStack(FolderFragment.BACKSTACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        }
        return false;
    }

    @ProvidePresenter
    FolderPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().folder();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            backstackId = savedInstanceState.getInt(STATE_BACKSTACK_ID);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BACKSTACK_ID, backstackId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AlignFrameView frame = new AlignFrameView(requireContext());
        frame.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        Point anchor = requireArguments().getParcelable(ARG_ANCHOR);
        frame.setAnchorPoint(anchor.x, anchor.y);
        WindowInsets insets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
        int p8 = ResUtils.px2dp(requireContext(), 8);
        frame.setPadding(p8, p8 + insets.getStableInsetTop(), p8, p8 + insets.getStableInsetBottom());
        frame.setClipChildren(false);
        return frame;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.setOnClickListener(v -> dismiss());
        String descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
        presenter.loadFolder(descriptorId);
    }

    @Override
    public void onShowFolder(GroupDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
        Context context = requireContext();

        AlignFrameView root = (AlignFrameView) requireView();
        View folderView = LayoutInflater.from(context)
                .inflate(R.layout.widget_folder, root, false);
        root.addView(folderView);
        folderView.setClipToOutline(true);

        TextView title = folderView.findViewById(R.id.folder_title);
        title.setText(descriptor.getVisibleLabel());

        list = folderView.findViewById(R.id.folder_list);
        LceLayout lce = folderView.findViewById(R.id.folder_lce);
        if (items.isEmpty()) {
            lce.empty()
                    .message(R.string.folder_empty)
                    .show();
        } else {
            list.setLayoutManager(new FlexboxLayoutManager(context, FlexDirection.ROW));
            HomeAdapter adapter = new HomeAdapter.Builder(context)
                    .add(new AppDescriptorUiAdapter(this, true))
                    .add(new PinnedShortcutDescriptorUiAdapter(this))
                    .add(new IntentDescriptorUiAdapter(this))
                    .add(new DeepShortcutDescriptorUiAdapter(this))
                    .setHasStableIds(true)
                    .create();
            adapter.updateUserPrefs(userPrefs);
            adapter.setDataset(items);
            list.setAdapter(adapter);

            lce.showContent();
        }

        animateAppearance(folderView);
    }

    @Override
    public void onError(Throwable error) {
        showError(error.getMessage());
    }

    @Override
    public void onAppClick(int position, AppDescriptorUi item) {
        ComponentName componentName = DescriptorUtils.getComponentName(requireContext(), item.getDescriptor());
        if (componentName != null) {
            View boundsView = findViewByPosition(position);
            if (IntentUtils.safeStartMainActivity(requireContext(), componentName, boundsView)) {
                dismiss();
                return;
            }
        }
        showError(R.string.error);
    }

    @Override
    public void onAppLongClick(int position, AppDescriptorUi item) {
        // TODO
    }

    @Override
    public void onDeepShortcutClick(int position, DeepShortcutDescriptorUi item) {
        // TODO
    }

    @Override
    public void onDeepShortcutLongClick(int position, DeepShortcutDescriptorUi item) {
        // TODO
    }

    @Override
    public void onIntentClick(int position, IntentDescriptorUi item) {
        // TODO
    }

    @Override
    public void onIntentLongClick(int position, IntentDescriptorUi item) {
        // TODO
    }

    @Override
    public void onPinnedShortcutClick(int position, PinnedShortcutDescriptorUi item) {
        // TODO
    }

    @Override
    public void onPinnedShortcutLongClick(int position, PinnedShortcutDescriptorUi item) {
        // TODO
    }

    public void show(FragmentManager fragmentManager, @IdRes int containerId) {
        backstackId = fragmentManager.beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.fragment_folder_out)
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

    @Nullable
    private View findViewByPosition(int position) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        return holder != null ? holder.itemView : null;
    }

    private void showError(@StringRes int message) {
        showError(getString(message));
    }

    private void showError(CharSequence message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void animateAppearance(View folderView) {
        folderView.setScaleX(0.4f);
        folderView.setScaleY(0.4f);
        folderView.setAlpha(0);

        folderView.animate()
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(150)
                .start();
    }
}
