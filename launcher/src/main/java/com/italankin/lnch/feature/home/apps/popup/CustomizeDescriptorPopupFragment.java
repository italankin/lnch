package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorArg;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.CustomColorDescriptorUi;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.IgnorableDescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        if (descriptorUi instanceof InFolderDescriptorUi) {
            args.putString(ARG_FOLDER_ID, ((InFolderDescriptorUi) descriptorUi).getFolderId());
        }
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String BACKSTACK_NAME = "customize_popup";
    private static final String TAG = "customize_popup";

    private Preferences preferences;

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
        Bundle args = requireArguments();
        String descriptorId = args.getString(ARG_DESCRIPTOR_ID);
        Descriptor descriptor = LauncherApp.daggerService.main()
                .descriptorRepository()
                .findById(Descriptor.class, descriptorId);
        DescriptorUi item = DescriptorUiFactory.createItem(descriptor);
        if (item instanceof InFolderDescriptorUi) {
            String folderId = args.getString(ARG_FOLDER_ID);
            ((InFolderDescriptorUi) item).setFolderId(folderId);
        }
        buildItemPopup(item);
        createItemViews();
        showPopup();
    }

    private void buildItemPopup(DescriptorUi item) {
        if (item instanceof IgnorableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_hide)
                    .setOnClickListener(v -> sendIgnoreResult((IgnorableDescriptorUi) item))
            );
        }
        if (item instanceof CustomLabelDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_rename)
                    .setIcon(R.drawable.ic_action_rename)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setRenameResult((CustomLabelDescriptorUi) item))
            );
        }
        if (item instanceof CustomColorDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_color)
                    .setEnabled(!preferences.get(Preferences.APPS_COLOR_OVERLAY_SHOW))
                    .setIcon(R.drawable.ic_action_color)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> setColorResult((CustomColorDescriptorUi) item))
            );
        }
        if (item instanceof IntentDescriptorUi && preferences.get(Preferences.EXPERIMENTAL_INTENT_FACTORY)) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_edit_intent)
                    .setIcon(R.drawable.ic_action_intent_edit)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> sendEditIntentResult((IntentDescriptorUi) item))
            );
        }
        if (item instanceof InFolderDescriptorUi) {
            addShortcut(new ItemBuilder()
                    .setLabel(R.string.customize_item_add_to_folder)
                    .setIcon(R.drawable.ic_action_add_to_folder)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setOnClickListener(v -> sendSelectFolderResult((InFolderDescriptorUi) item))
            );
        }
        if (item instanceof RemovableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_delete)
                    .setOnClickListener(v -> sendRemoveResult((RemovableDescriptorUi) item))
            );
        }
    }

    private void sendRemoveResult(RemovableDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.Remove.KEY);
        result.putSerializable(FragmentResults.Customize.Remove.DESCRIPTOR, new DescriptorArg(item.getDescriptor()));
        sendResult(result);
    }

    private void sendSelectFolderResult(InFolderDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.SelectFolder.KEY);
        result.putSerializable(FragmentResults.Customize.SelectFolder.DESCRIPTOR, new DescriptorArg(item.getDescriptor()));
        sendResult(result);
    }

    private void sendEditIntentResult(IntentDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.EditIntent.KEY);
        result.putString(FragmentResults.Customize.EditIntent.DESCRIPTOR_ID, item.getDescriptor().getId());
        sendResult(result);
    }

    private void setColorResult(CustomColorDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.SetColor.KEY);
        result.putSerializable(FragmentResults.Customize.SetColor.DESCRIPTOR, new DescriptorArg(item.getDescriptor()));
        sendResult(result);
    }

    private void setRenameResult(CustomLabelDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.Rename.KEY);
        result.putSerializable(FragmentResults.Customize.Rename.DESCRIPTOR, new DescriptorArg(item.getDescriptor()));
        sendResult(result);
    }

    private void sendIgnoreResult(IgnorableDescriptorUi item) {
        dismiss();
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.Customize.Ignore.KEY);
        result.putSerializable(FragmentResults.Customize.Ignore.DESCRIPTOR, new DescriptorArg(item.getDescriptor()));
        sendResult(result);
    }
}
