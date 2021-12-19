package com.italankin.lnch.feature.home.apps.popup;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import com.italankin.lnch.util.IntentUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class DescriptorPopupFragment extends ActionPopupFragment {

    public static DescriptorPopupFragment newInstance(
            DescriptorUi descriptorUi,
            String requestKey,
            @Nullable Rect anchor) {
        if (descriptorUi instanceof AppDescriptorUi) {
            throw new IllegalArgumentException("For app popup use AppDescriptorPopupFragment");
        }
        DescriptorPopupFragment fragment = new DescriptorPopupFragment();
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

    private static final String BACKSTACK_NAME = "descriptor_popup";
    private static final String TAG = "descriptor_popup";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_descriptor_popup, container, false);
        root = view.findViewById(R.id.popup_root);
        containerRoot = view.findViewById(R.id.popup_container_root);
        itemsContainer = view.findViewById(R.id.popup_item_container);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        actionsContainer = view.findViewById(R.id.action_container);
        shortcutsContainer = view.findViewById(R.id.shortcut_container);

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
        Context context = requireContext();
        if (item.getDescriptor() instanceof PackageDescriptor) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_app_info)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        PackageDescriptor descriptor = (PackageDescriptor) item.getDescriptor();
                        startAppSettings(descriptor, v);
                    })
            );
        }
        if (item instanceof RemovableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_delete)
                    .setLabel(R.string.customize_item_delete)
                    .setOnClickListener(v -> {
                        String visibleLabel = ((CustomLabelDescriptorUi) item).getVisibleLabel();
                        String message = context.getString(R.string.popup_delete_message, visibleLabel);
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.popup_delete_title)
                                .setMessage(message)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.popup_delete_action, (dialog, which) -> {
                                    removeItemImmediate((RemovableDescriptorUi) item);
                                })
                                .show();
                    })
            );
        }
        if (item instanceof InFolderDescriptorUi && ((InFolderDescriptorUi) item).getFolderId() != null) {
            addShortcut(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_remove_from_folder)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.customize_item_remove_from_folder)
                    .setOnClickListener(v -> {
                        removeFromFolder(((InFolderDescriptorUi) item));
                    })
            );
        }
    }

    private void startAppSettings(PackageDescriptor item, View bounds) {
        IntentUtils.safeStartAppSettings(requireContext(), item.getPackageName(), bounds);
        dismissWithResult();
    }

    private void removeItemImmediate(RemovableDescriptorUi item) {
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.RemoveItem.KEY);
        result.putString(FragmentResults.RemoveItem.DESCRIPTOR_ID, item.getDescriptor().getId());
        sendResult(result);
        dismiss();
    }

    private void removeFromFolder(InFolderDescriptorUi item) {
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.RemoveFromFolder.KEY);
        result.putString(FragmentResults.RemoveFromFolder.DESCRIPTOR_ID, item.getDescriptor().getId());
        result.putString(FragmentResults.RemoveFromFolder.FOLDER_ID, item.getFolderId());
        sendResult(result);
        dismiss();
    }
}
