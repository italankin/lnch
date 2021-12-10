package com.italankin.lnch.feature.home.apps.folder;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.CustomizeDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ItemPopupDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.folder.widget.AlignFrameView;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.apps.popup.DescriptorPopupFragment;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

public class FolderFragment extends AppFragment implements FolderView,
        FragmentResultListener,
        AppDescriptorUiAdapter.Listener,
        PinnedShortcutDescriptorUiAdapter.Listener,
        IntentDescriptorUiAdapter.Listener,
        DeepShortcutDescriptorUiAdapter.Listener {

    public static FolderFragment newInstance(
            FolderDescriptor descriptor,
            String requestKey,
            @Nullable Point anchor) {
        FolderFragment fragment = new FolderFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptor.getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean dismiss(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) != null) {
            fragmentManager.popBackStack(FolderFragment.BACKSTACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        }
        return false;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_ANCHOR = "anchor";
    private static final String ARG_REQUEST_KEY = "request_key";

    private static final String STATE_BACKSTACK_ID = "backstack_id";
    private static final String FOLDER_REQUEST_KEY = "folder";
    private static final String BACKSTACK_NAME = "folder";
    private static final String TAG = "folder";

    @InjectPresenter
    FolderPresenter presenter;

    private RecyclerView list;
    private AlignFrameView alignFrameView;
    private View container;
    private LceLayout lce;
    private TextView title;

    private AppClickDelegate appClickDelegate;
    private ErrorDelegate errorDelegate;
    private PinnedShortcutClickDelegate pinnedShortcutClickDelegate;
    private DeepShortcutClickDelegate deepShortcutClickDelegate;
    private IntentClickDelegate intentClickDelegate;

    private int backstackId = -1;

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
        getParentFragmentManager().setFragmentResultListener(FOLDER_REQUEST_KEY, this, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_BACKSTACK_ID, backstackId);
    }

    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
        if (!FOLDER_REQUEST_KEY.equals(requestKey)) {
            return;
        }
        String resultKey = result.getString(FragmentResults.RESULT);
        switch (resultKey) {
            case FragmentResults.PinShortcut.KEY:
                sendResult(result);
                return;
            case FragmentResults.OnActionHandled.KEY:
                dismiss();
                return;
            default: {
                sendResult(result);
                dismiss();
            }
        }
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
        lce = view.findViewById(R.id.folder_lce);
        list = view.findViewById(R.id.folder_list);
        title = view.findViewById(R.id.folder_title);

        Point anchor = requireArguments().getParcelable(ARG_ANCHOR);
        alignFrameView.setAnchorPoint(anchor.x, anchor.y);
        alignFrameView.post(() -> {
            WindowInsets insets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
            alignFrameView.setPaddingRelative(0, insets.getStableInsetTop(), 0, insets.getStableInsetBottom());
        });
        alignFrameView.setOnClickListener(v -> dismiss());

        container.setClipToOutline(true);

        String descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
        presenter.loadFolder(descriptorId);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onShowFolder(FolderDescriptor descriptor, List<DescriptorUi> items, UserPrefs userPrefs) {
        Context context = requireContext();

        title.setText(descriptor.getVisibleLabel());

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

        initDelegates(context);

        animatePopupAppearance();
    }

    private void initDelegates(Context context) {
        ShortcutsRepository shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
        Preferences preferences = LauncherApp.daggerService.main().preferences();

        errorDelegate = new ErrorDelegateImpl(context) {
            @Override
            public void showError(CharSequence message) {
                super.showError(message);
                dismiss();
            }
        };
        ItemPopupDelegate itemPopupDelegate = (item, anchor) -> {
            Rect bounds = ViewUtils.getViewBoundsInsetPadding(anchor);
            DescriptorPopupFragment.newInstance(item, FOLDER_REQUEST_KEY, bounds)
                    .show(getParentFragmentManager());
        };
        CustomizeDelegate customizeDelegate = () -> {
            Bundle result = new Bundle();
            result.putString(FragmentResults.RESULT, FragmentResults.Customize.KEY);
            sendResult(result);
            dismiss();
        };
        ShortcutStarterDelegate shortcutStarterDelegate = new ShortcutStarterDelegateImpl(context, errorDelegate,
                customizeDelegate);
        pinnedShortcutClickDelegate = new PinnedShortcutClickDelegateImpl(context, errorDelegate, itemPopupDelegate);
        deepShortcutClickDelegate = new DeepShortcutClickDelegateImpl(shortcutStarterDelegate, itemPopupDelegate,
                shortcutsRepository);
        SearchIntentStarterDelegate searchIntentStarterDelegate = new SearchIntentStarterDelegateImpl(context,
                preferences, errorDelegate, customizeDelegate);
        intentClickDelegate = new IntentClickDelegateImpl(searchIntentStarterDelegate, itemPopupDelegate);
        appClickDelegate = new AppClickDelegateImpl(context, preferences, errorDelegate, itemPopupDelegate);
    }

    @Override
    public void onError(Throwable error) {
        errorDelegate.showError(error.getMessage());
    }

    @Override
    public void onShortcutPinned(Shortcut shortcut) {
        Toast.makeText(requireContext(), getString(R.string.deep_shortcut_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShortcutAlreadyPinnedError(Shortcut shortcut) {
        Toast.makeText(requireContext(), getString(R.string.deep_shortcut_already_pinned, shortcut.getShortLabel()),
                Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clicks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        appClickDelegate.onAppClick(item, view);
        dismiss();
    }

    @Override
    public void onAppLongClick(int position, AppDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        appClickDelegate.onAppLongClick(item, view);
    }

    @Override
    public void onDeepShortcutClick(int position, DeepShortcutDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        deepShortcutClickDelegate.onDeepShortcutClick(item, view);
        dismiss();
    }

    @Override
    public void onDeepShortcutLongClick(int position, DeepShortcutDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        deepShortcutClickDelegate.onDeepShortcutLongClick(item, view);
    }

    @Override
    public void onIntentClick(int position, IntentDescriptorUi item) {
        intentClickDelegate.onIntentClick(item);
        dismiss();
    }

    @Override
    public void onIntentLongClick(int position, IntentDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        intentClickDelegate.onIntentLongClick(item, view);
    }

    @Override
    public void onPinnedShortcutClick(int position, PinnedShortcutDescriptorUi item) {
        pinnedShortcutClickDelegate.onPinnedShortcutClick(item);
        dismiss();
    }

    @Override
    public void onPinnedShortcutLongClick(int position, PinnedShortcutDescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        pinnedShortcutClickDelegate.onPinnedShortcutLongClick(item, view);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

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

    private void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    private void animatePopupAppearance() {
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
                .setDuration(150)
                .start();
    }
}
