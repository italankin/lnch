package com.italankin.lnch.feature.home.apps.folder;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.home.apps.delegate.*;
import com.italankin.lnch.feature.home.apps.popup.AppDescriptorPopupFragment;
import com.italankin.lnch.feature.home.apps.popup.DescriptorPopupFragment;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.ViewUtils;

public class FolderFragment extends BaseFolderFragment {

    public static FolderFragment newInstance(
            FolderDescriptor descriptor,
            String requestKey,
            @Nullable Point anchor) {
        FolderFragment fragment = new FolderFragment();
        fragment.setArguments(makeArgs(descriptor, requestKey, anchor));
        return fragment;
    }

    private FolderViewModel viewModel;

    protected AppClickDelegate appClickDelegate;
    protected PinnedShortcutClickDelegate pinnedShortcutClickDelegate;
    protected DeepShortcutClickDelegate deepShortcutClickDelegate;
    protected IntentClickDelegate intentClickDelegate;

    @Override
    protected BaseFolderViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, FolderViewModel.class, ViewModelComponent::folder);
        fragmentResultManager
                .register(new AppDescriptorPopupFragment.RemoveFromFolderContract(), result -> {
                    viewModel.removeFromFolderImmediate(result.descriptorId);
                });
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void initDelegates(Context context) {
        super.initDelegates(context);

        ShortcutsRepository shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
        UsageTracker usageTracker = LauncherApp.daggerService.main().usageTracker();
        Preferences preferences = LauncherApp.daggerService.main().preferences();

        ItemPopupDelegate itemPopupDelegate = (item, anchor) -> {
            Rect bounds = ViewUtils.getViewBoundsInsetPadding(anchor);
            if (item instanceof AppDescriptorUi) {
                AppDescriptorPopupFragment.newInstance((AppDescriptorUi) item, REQUEST_KEY_FOLDER, bounds)
                        .setFolderId(folderId)
                        .show(getParentFragmentManager());
            } else {
                DescriptorPopupFragment.newInstance(item, REQUEST_KEY_FOLDER, bounds)
                        .setFolderId(folderId)
                        .show(getParentFragmentManager());
            }
        };
        CustomizeDelegate customizeDelegate = () -> {
            dismiss();
            Bundle result = new CustomizeContract().result();
            sendResult(result);
        };
        ShortcutStarterDelegate shortcutStarterDelegate = new ShortcutStarterDelegateImpl(context, errorDelegate,
                customizeDelegate, usageTracker);
        pinnedShortcutClickDelegate = new PinnedShortcutClickDelegateImpl(context, errorDelegate, itemPopupDelegate,
                usageTracker);
        deepShortcutClickDelegate = new DeepShortcutClickDelegateImpl(shortcutStarterDelegate, itemPopupDelegate,
                shortcutsRepository);
        SearchIntentStarterDelegate searchIntentStarterDelegate = new SearchIntentStarterDelegateImpl(context,
                preferences, errorDelegate, customizeDelegate, usageTracker);
        intentClickDelegate = new IntentClickDelegateImpl(searchIntentStarterDelegate, itemPopupDelegate);
        appClickDelegate = new AppClickDelegateImpl(context, preferences, errorDelegate, itemPopupDelegate,
                usageTracker);
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

    public static class CustomizeContract extends SignalFragmentResultContract {
        public CustomizeContract() {
            super("customize_from_folder");
        }
    }
}
