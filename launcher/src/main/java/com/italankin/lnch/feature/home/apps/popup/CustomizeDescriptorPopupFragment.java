package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.DescriptorFragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.*;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

public class CustomizeDescriptorPopupFragment extends ActionPopupFragment {

    public static CustomizeDescriptorPopupFragment newInstance(
            DescriptorUi descriptorUi,
            String requestKey,
            @Nullable Rect anchor) {
        CustomizeDescriptorPopupFragment fragment = new CustomizeDescriptorPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptorUi.getDescriptor().getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String BACKSTACK_NAME = "customize_popup";
    private static final String TAG = "customize_popup";

    private Preferences preferences;

    public CustomizeDescriptorPopupFragment setFolderId(String folderId) {
        requireArguments().putString(ARG_FOLDER_ID, folderId);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load();
    }

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    private void load() {
        String descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
        HomeEntry<? extends DescriptorUi> entry = LauncherApp.daggerService.main()
                .homeDescriptorState()
                .find(descriptorId);
        if (entry == null) {
            throw new NullPointerException("No descriptors found by descriptorId=" + descriptorId);
        }
        buildItemPopup(entry.item);
        createItemViews();
        showPopup();
    }

    private void buildItemPopup(DescriptorUi item) {
        String folderId = item instanceof InFolderDescriptorUi ? requireArguments().getString(ARG_FOLDER_ID) : null;
        if (item instanceof IgnorableDescriptorUi && folderId == null) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_hide)
                    .setOnClickListener(v -> sendIgnoreResult((IgnorableDescriptorUi) item))
            );
        }
        if (item instanceof CustomLabelDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_rename)
                    .setIcon(R.drawable.ic_action_rename)
                    .setIconDrawableTintAttr(android.R.attr.colorAccent)
                    .setOnClickListener(v -> setRenameResult((CustomLabelDescriptorUi) item))
            );
        }
        if (item instanceof CustomColorDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_change_color)
                    .setEnabled(!preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW))
                    .setIcon(R.drawable.ic_action_color)
                    .setIconDrawableTintAttr(android.R.attr.colorAccent)
                    .setOnClickListener(v -> setColorResult((CustomColorDescriptorUi) item))
            );
        }
        if (item instanceof IntentDescriptorUi && preferences.get(Preferences.EXPERIMENTAL_INTENT_FACTORY)) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_edit_intent)
                    .setIcon(R.drawable.ic_action_intent_edit)
                    .setIconDrawableTintAttr(android.R.attr.colorAccent)
                    .setOnClickListener(v -> sendEditIntentResult((IntentDescriptorUi) item))
            );
        }
        if (item instanceof InFolderDescriptorUi) {
            if (folderId != null) {
                addShortcut(new ItemBuilder()
                        .setLabel(R.string.customize_item_remove_from_folder)
                        .setIcon(R.drawable.ic_action_remove_from_folder)
                        .setIconDrawableTintAttr(android.R.attr.colorAccent)
                        .setOnClickListener(v -> sendRemoveFromFolderResult((InFolderDescriptorUi) item, folderId, false))
                );
                if (item instanceof IgnorableDescriptorUi && ((IgnorableDescriptorUi) item).isIgnored()) {
                    addShortcut(new ItemBuilder()
                            .setLabel(R.string.customize_item_move_to_desktop)
                            .setIcon(R.drawable.ic_action_move_to_desktop)
                            .setIconDrawableTintAttr(android.R.attr.colorAccent)
                            .setOnClickListener(v -> sendRemoveFromFolderResult((InFolderDescriptorUi) item, folderId, true))
                    );
                }
            } else {
                addShortcut(new ItemBuilder()
                        .setLabel(R.string.customize_item_add_to_folder)
                        .setIcon(R.drawable.ic_folder)
                        .setIconDrawableTintAttr(android.R.attr.colorAccent)
                        .setOnClickListener(v -> sendSelectFolderResult((InFolderDescriptorUi) item, false))
                );
                if (item instanceof IgnorableDescriptorUi) {
                    addShortcut(new ItemBuilder()
                            .setLabel(R.string.customize_item_move_to_folder)
                            .setIcon(R.drawable.ic_action_move_to_folder)
                            .setIconDrawableTintAttr(android.R.attr.colorAccent)
                            .setOnClickListener(v -> sendSelectFolderResult((InFolderDescriptorUi) item, true))
                    );
                }
            }
        }
        if (item instanceof RemovableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_delete)
                    .setOnClickListener(v -> sendRemoveResult((RemovableDescriptorUi) item))
            );
        }
        if (item instanceof FolderDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_edit_folder)
                    .setIcon(R.drawable.ic_folder)
                    .setIconDrawableTintAttr(android.R.attr.colorAccent)
                    .setOnClickListener(v -> sendOpenFolderResult((FolderDescriptorUi) item))
            );
        }
    }

    private void sendRemoveResult(RemovableDescriptorUi item) {
        dismiss();
        Bundle result = new RemoveContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void sendSelectFolderResult(InFolderDescriptorUi item, boolean move) {
        dismiss();
        Bundle result = ShowSelectFolderContract.result(item.getDescriptor().getId(), move);
        sendResult(result);
    }

    private void sendRemoveFromFolderResult(InFolderDescriptorUi item, String folderId, boolean moveToDesktop) {
        dismiss();
        Bundle result = RemoveFromFolderContract.result(item.getDescriptor().getId(), folderId, moveToDesktop);
        sendResult(result);
    }

    private void sendOpenFolderResult(FolderDescriptorUi item) {
        dismiss();
        Bundle result = new EditFolderContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void sendEditIntentResult(IntentDescriptorUi item) {
        dismiss();
        Bundle result = new EditIntentContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void setColorResult(CustomColorDescriptorUi item) {
        dismiss();
        Bundle result = new SetColorContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void setRenameResult(CustomLabelDescriptorUi item) {
        dismiss();
        Bundle result = new RenameContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void sendIgnoreResult(IgnorableDescriptorUi item) {
        dismiss();
        Bundle result = new IgnoreContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    public static class ShowSelectFolderContract implements FragmentResultContract<ShowSelectFolderContract.Result> {
        private static final String KEY = "customize_show_select_folder";
        private static final String DESCRIPTOR_ID = "descriptor_id";
        private static final String MOVE = "move";

        private static Bundle result(String descriptorId, boolean move) {
            Bundle result = new Bundle();
            result.putSerializable(DESCRIPTOR_ID, descriptorId);
            result.putBoolean(MOVE, move);
            result.putString(RESULT_KEY, KEY);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(DESCRIPTOR_ID), result.getBoolean(MOVE));
        }

        public static class Result {
            public final String descriptorId;
            public final boolean move;

            Result(String descriptorId, boolean move) {
                this.descriptorId = descriptorId;
                this.move = move;
            }
        }
    }

    public static class RemoveFromFolderContract implements FragmentResultContract<RemoveFromFolderContract.Result> {
        private static final String KEY = "customize_remove_from_folder";
        private static final String DESCRIPTOR_ID = "descriptor_id";
        private static final String FOLDER_ID = "folder_id";
        private static final String MOVE_TO_DESKTOP = "move_to_desktop";

        private static Bundle result(String descriptorId, String folderId, boolean moveToDesktop) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putSerializable(DESCRIPTOR_ID, descriptorId);
            result.putSerializable(FOLDER_ID, folderId);
            result.putBoolean(MOVE_TO_DESKTOP, moveToDesktop);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(DESCRIPTOR_ID), result.getString(FOLDER_ID), result.getBoolean(MOVE_TO_DESKTOP));
        }

        public static class Result {
            public final String descriptorId;
            public final String folderId;
            public final boolean moveToDesktop;

            Result(String descriptorId, String folderId, boolean moveToDesktop) {
                this.descriptorId = descriptorId;
                this.folderId = folderId;
                this.moveToDesktop = moveToDesktop;
            }
        }
    }

    public static class EditFolderContract extends DescriptorFragmentResultContract {
        public EditFolderContract() {
            super("customize_edit_folder");
        }
    }

    public static class EditIntentContract extends DescriptorFragmentResultContract {
        public EditIntentContract() {
            super("customize_edit_intent");
        }
    }

    public static class SetColorContract extends DescriptorFragmentResultContract {
        public SetColorContract() {
            super("customize_set_color");
        }
    }

    public static class RenameContract extends DescriptorFragmentResultContract {
        public RenameContract() {
            super("customize_rename");
        }
    }

    public static class IgnoreContract extends DescriptorFragmentResultContract {
        public IgnoreContract() {
            super("customize_ignore");
        }
    }

    public static class RemoveContract extends DescriptorFragmentResultContract {
        public RemoveContract() {
            super("customize_remove");
        }
    }
}
