package com.italankin.lnch.feature.home.apps.folder;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.android.material.animation.ArgbEvaluatorCompat;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.dialog.RenameDescriptorDialog;
import com.italankin.lnch.feature.common.dialog.SetColorDescriptorDialog;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.feature.home.apps.popup.CustomizeDescriptorPopupFragment;
import com.italankin.lnch.feature.home.util.MoveItemHelper;
import com.italankin.lnch.feature.intentfactory.IntentFactoryActivity;
import com.italankin.lnch.feature.intentfactory.IntentFactoryResult;
import com.italankin.lnch.model.descriptor.DescriptorArg;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class EditFolderFragment extends BaseFolderFragment implements EditFolderView, MoveItemHelper.Callback {

    public static EditFolderFragment newInstance(
            FolderDescriptor descriptor,
            String requestKey,
            @Nullable Point anchor) {
        EditFolderFragment fragment = new EditFolderFragment();
        fragment.setArguments(makeArgs(descriptor, requestKey, anchor));
        return fragment;
    }

    @InjectPresenter
    EditFolderPresenter presenter;

    private Preferences preferences;

    private final ActivityResultLauncher<IntentDescriptorUi> editIntentLauncher = registerForActivityResult(
            new IntentFactoryActivity.EditContract(),
            this::onIntentEdited);

    private final ItemTouchHelper touchHelper = new ItemTouchHelper(new MoveItemHelper(this));

    @ProvidePresenter
    EditFolderPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().editFolder();
    }

    @Override
    protected BaseFolderPresenter<? extends BaseFolderView> getPresenter() {
        return presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    protected void handleFragmentResult(String resultKey, @NonNull Bundle result) {
        switch (resultKey) {
            case FragmentResults.Customize.Rename.KEY: {
                DescriptorArg arg = (DescriptorArg) result.getSerializable(FragmentResults.Customize.Rename.DESCRIPTOR);
                presenter.showRenameDialog(arg);
                break;
            }
            case FragmentResults.Customize.SetColor.KEY: {
                DescriptorArg arg = (DescriptorArg) result.getSerializable(
                        FragmentResults.Customize.SetColor.DESCRIPTOR);
                presenter.showSetColorDialog(arg);
                break;
            }
            case FragmentResults.Customize.Remove.KEY: {
                DescriptorArg arg = (DescriptorArg) result.getSerializable(
                        FragmentResults.Customize.Remove.DESCRIPTOR);
                presenter.removeItem(arg);
                break;
            }
            case FragmentResults.Customize.EditIntent.KEY: {
                String descriptorId = result.getString(FragmentResults.Customize.EditIntent.DESCRIPTOR_ID);
                presenter.startEditIntent(descriptorId);
                break;
            }
            case FragmentResults.Customize.RemoveFromFolder.KEY: {
                DescriptorArg descriptor = (DescriptorArg) result.getSerializable(
                        FragmentResults.Customize.RemoveFromFolder.DESCRIPTOR);
                DescriptorArg folder = (DescriptorArg) result.getSerializable(
                        FragmentResults.Customize.RemoveFromFolder.FOLDER);
                presenter.removeFromFolder(descriptor.id, folder.id);
                break;
            }
            default:
                super.handleFragmentResult(resultKey, result);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        touchHelper.attachToRecyclerView(list);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View state
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onShowRenameDialog(int position, CustomLabelDescriptorUi item) {
        new RenameDescriptorDialog(requireContext(), item.getVisibleLabel(),
                newLabel -> presenter.renameItem(position, item, newLabel))
                .show();
    }

    @Override
    public void onShowSetColorDialog(int position, CustomColorDescriptorUi item) {
        new SetColorDescriptorDialog(requireContext(), item.getVisibleColor(),
                newColor -> presenter.changeItemCustomColor(position, item, newColor))
                .show();
    }

    @Override
    public void onEditIntent(IntentDescriptorUi item) {
        editIntentLauncher.launch(item);
    }

    @Override
    public void onItemChanged(int position) {
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onFolderItemMove(int from, int to) {
        adapter.notifyItemMoved(from, to);
    }

    @Override
    public void onItemMove(int from, int to) {
        presenter.moveItem(from, to);
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

    @Override
    protected void animatePopupAppearance() {
        super.animatePopupAppearance();

        ValueAnimator animator = ValueAnimator.ofInt(Color.TRANSPARENT, 0xd0000000);
        animator.setEvaluator(new ArgbEvaluatorCompat());
        animator.addUpdateListener(animation -> {
            alignFrameView.setBackgroundColor((int) animation.getAnimatedValue());
        });
        animator.setDuration(ANIM_DURATION);
        animator.start();
    }

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
        presenter.editIntent(result.descriptorId, result.intent);
    }
}