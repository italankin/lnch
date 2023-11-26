package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.DescriptorFragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDER_ID = "folder_id";

    private static final String BACKSTACK_NAME = "descriptor_popup";
    private static final String TAG = "descriptor_popup";

    public DescriptorPopupFragment setFolderId(String folderId) {
        requireArguments().putString(ARG_FOLDER_ID, folderId);
        return this;
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
        HomeEntry<DescriptorUi> entry = LauncherApp.daggerService.main()
                .homeDescriptorState()
                .find(DescriptorUi.class, descriptorId);
        if (entry == null) {
            throw new NullPointerException("No descriptor found by descriptorId=" + descriptorId);
        }
        buildItemPopup(entry.item);
        createItemViews();
        showPopup();
    }

    private void buildItemPopup(DescriptorUi item) {
        boolean destructiveNonEdit = LauncherApp.daggerService.main()
                .preferences()
                .get(Preferences.DESTRUCTIVE_NON_EDIT);
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
        if (destructiveNonEdit && item instanceof RemovableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_delete)
                    .setLabel(R.string.customize_item_delete)
                    .setOnClickListener(v -> {
                        requestRemoval((RemovableDescriptorUi) item);
                    })
            );
        }
        String folderId = requireArguments().getString(ARG_FOLDER_ID);
        if (destructiveNonEdit && item instanceof InFolderDescriptorUi && folderId != null) {
            addShortcut(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_remove_from_folder)
                    .setIconDrawableTintAttr(android.R.attr.colorAccent)
                    .setLabel(R.string.customize_item_remove_from_folder)
                    .setOnClickListener(v -> {
                        removeFromFolder((InFolderDescriptorUi) item, folderId);
                    })
            );
        }
    }

    private void startAppSettings(PackageDescriptor item, View bounds) {
        dismissWithResult();
        IntentUtils.safeStartAppSettings(requireContext(), item.getPackageName(), bounds);
    }

    private void requestRemoval(RemovableDescriptorUi item) {
        dismiss();
        Bundle result = new RequestRemovalContract().result(item.getDescriptor().getId());
        sendResult(result);
    }

    private void removeFromFolder(InFolderDescriptorUi item, String folderId) {
        dismiss();
        Bundle result = RemoveFromFolderContract.result(item.getDescriptor().getId(), folderId);
        sendResult(result);
    }

    public static class RequestRemovalContract extends DescriptorFragmentResultContract {
        public RequestRemovalContract() {
            super("request_removal");
        }
    }

    public static class RemoveFromFolderContract implements FragmentResultContract<RemoveFromFolderContract.Result> {
        private static final String KEY = "remove_from_folder";
        private static final String DESCRIPTOR_ID = "descriptor_id";
        private static final String FOLDER_ID = "folder_id";

        static Bundle result(String descriptorId, String folderId) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putString(DESCRIPTOR_ID, descriptorId);
            result.putString(FOLDER_ID, folderId);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(DESCRIPTOR_ID), result.getString(FOLDER_ID));
        }

        public static class Result {
            public final String descriptorId;
            public final String folderId;

            Result(String descriptorId, String folderId) {
                this.descriptorId = descriptorId;
                this.folderId = folderId;
            }
        }
    }
}
