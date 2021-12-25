package com.italankin.lnch.feature.home.apps.folder;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.AppClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.CustomizeDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.DeepShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.IntentClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ItemPopupDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegate;
import com.italankin.lnch.feature.home.apps.delegate.PinnedShortcutClickDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.SearchIntentStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegateImpl;
import com.italankin.lnch.feature.home.apps.popup.AppDescriptorPopupFragment;
import com.italankin.lnch.feature.home.apps.popup.DescriptorPopupFragment;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.ViewUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FolderFragment extends BaseFolderFragment implements FolderView {

    public static FolderFragment newInstance(
            FolderDescriptor descriptor,
            String requestKey,
            @Nullable Point anchor) {
        FolderFragment fragment = new FolderFragment();
        fragment.setArguments(makeArgs(descriptor, requestKey, anchor));
        return fragment;
    }

    @InjectPresenter
    FolderPresenter presenter;

    protected AppClickDelegate appClickDelegate;
    protected PinnedShortcutClickDelegate pinnedShortcutClickDelegate;
    protected DeepShortcutClickDelegate deepShortcutClickDelegate;
    protected IntentClickDelegate intentClickDelegate;

    @ProvidePresenter
    FolderPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().folder();
    }

    @Override
    protected BaseFolderPresenter<? extends BaseFolderView> getPresenter() {
        return presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentResultManager
                .register(new AppDescriptorPopupFragment.RemoveFromFolderContract(), result -> {
                    presenter.removeFromFolderImmediate(result.descriptorId);
                });
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void initDelegates(Context context) {
        super.initDelegates(context);

        ShortcutsRepository shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
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

    public static class CustomizeContract extends SignalFragmentResultContract {
        public CustomizeContract() {
            super("customize_from_folder");
        }
    }
}
