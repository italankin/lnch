package com.italankin.lnch.feature.home.apps.folder;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.PresenterComponent;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.common.dialog.RenameDescriptorDialog;
import com.italankin.lnch.feature.common.dialog.SetColorDescriptorDialog;
import com.italankin.lnch.feature.home.apps.popup.CustomizeDescriptorPopupFragment;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.feature.home.util.MoveItemHelper;
import com.italankin.lnch.feature.intentfactory.IntentFactoryActivity;
import com.italankin.lnch.feature.intentfactory.IntentFactoryResult;
import com.italankin.lnch.model.descriptor.impl.FolderDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.ViewUtils;

public class EditFolderFragment extends BaseFolderFragment {

    public static EditFolderFragment newInstance(
            FolderDescriptor descriptor,
            String requestKey,
            @Nullable Point anchor) {
        EditFolderFragment fragment = new EditFolderFragment();
        fragment.setArguments(makeArgs(descriptor, requestKey, anchor));
        return fragment;
    }

    private EditFolderViewModel viewModel;

    private Preferences preferences;
    private HomeDescriptorsState homeDescriptorsState;

    private final ActivityResultLauncher<IntentDescriptorUi> editIntentLauncher = registerForActivityResult(
            new IntentFactoryActivity.EditContract(),
            this::onIntentEdited);

    private final ItemTouchHelper touchHelper = new ItemTouchHelper(new MoveItemHelper(new MoveItemHelper.Callback() {
        @Override
        public void onItemMove(int from, int to) {
            viewModel.moveItem(from, to);
            adapter.notifyItemMoved(from, to);
        }
    }));

    @Override
    protected BaseFolderViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, EditFolderViewModel.class, PresenterComponent::editFolder);
        preferences = LauncherApp.daggerService.main().preferences();
        homeDescriptorsState = LauncherApp.daggerService.main().homeDescriptorState();

        fragmentResultManager
                .register(new CustomizeDescriptorPopupFragment.RenameContract(), descriptorId -> {
                    HomeEntry<CustomLabelDescriptorUi> entry = homeDescriptorsState.find(CustomLabelDescriptorUi.class, descriptorId);
                    if (entry != null) {
                        onShowRenameDialog(entry.item);
                    }
                })
                .register(new CustomizeDescriptorPopupFragment.SetColorContract(), descriptorId -> {
                    HomeEntry<CustomColorDescriptorUi> entry = homeDescriptorsState.find(CustomColorDescriptorUi.class, descriptorId);
                    if (entry != null) {
                        onShowSetColorDialog(entry.item);
                    }
                })
                .register(new CustomizeDescriptorPopupFragment.RemoveContract(), descriptorId -> {
                    viewModel.removeItem(descriptorId);
                })
                .register(new CustomizeDescriptorPopupFragment.EditIntentContract(), descriptorId -> {
                    HomeEntry<IntentDescriptorUi> entry = homeDescriptorsState.find(IntentDescriptorUi.class, descriptorId);
                    if (entry != null) {
                        editIntentLauncher.launch(entry.item);
                    }
                })
                .register(new CustomizeDescriptorPopupFragment.RemoveFromFolderContract(), result -> {
                    viewModel.removeFromFolder(result.descriptorId, result.folderId, result.moveToDesktop);
                });
        drawOverlay = true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        touchHelper.attachToRecyclerView(list);
    }

    private void onShowRenameDialog(CustomLabelDescriptorUi item) {
        new RenameDescriptorDialog(requireContext(), item.getVisibleLabel(),
                newLabel -> viewModel.renameItem(item, newLabel))
                .show();
    }

    private void onShowSetColorDialog(CustomColorDescriptorUi item) {
        new SetColorDescriptorDialog(requireContext(), item.getVisibleColor(),
                newColor -> viewModel.setCustomColor(item, newColor))
                .show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clicks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onAppClick(int position, AppDescriptorUi item) {
        showCustomizePopup(position, item);
    }

    @Override
    public void onAppLongClick(int position, AppDescriptorUi item) {
        startDrag(position);
    }

    @Override
    public void onDeepShortcutClick(int position, DeepShortcutDescriptorUi item) {
        showCustomizePopup(position, item);
    }

    @Override
    public void onDeepShortcutLongClick(int position, DeepShortcutDescriptorUi item) {
        startDrag(position);
    }

    @Override
    public void onIntentClick(int position, IntentDescriptorUi item) {
        showCustomizePopup(position, item);
    }

    @Override
    public void onIntentLongClick(int position, IntentDescriptorUi item) {
        startDrag(position);
    }

    @Override
    public void onPinnedShortcutClick(int position, PinnedShortcutDescriptorUi item) {
        showCustomizePopup(position, item);
    }

    @Override
    public void onPinnedShortcutLongClick(int position, PinnedShortcutDescriptorUi item) {
        startDrag(position);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other
    ///////////////////////////////////////////////////////////////////////////

    private void startDrag(int position) {
        if (preferences.get(Preferences.APPS_SORT_MODE) != Preferences.AppsSortMode.MANUAL) {
            errorDelegate.showError(R.string.error_manual_sorting_required);
            return;
        }
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View view = layoutManager.findViewByPosition(position);
        if (view == null) {
            return;
        }
        touchHelper.startDrag(list.getChildViewHolder(view));
    }

    private void showCustomizePopup(int position, DescriptorUi item) {
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(position);
        View view = holder != null ? holder.itemView : null;
        Rect bounds = ViewUtils.getViewBoundsInsetPadding(view);
        CustomizeDescriptorPopupFragment.newInstance(item, REQUEST_KEY_FOLDER, bounds)
                .setFolderId(folderId)
                .show(getParentFragmentManager());
    }

    private void onIntentEdited(IntentFactoryResult result) {
        if (result == null || result.descriptorId == null) {
            return;
        }
        viewModel.editIntent(result.descriptorId, result.intent);
    }
}
