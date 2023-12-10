package com.italankin.lnch.feature.home.apps.selectfolder;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.color.MaterialColors;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.util.widget.popup.PopupFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectFolderFragment extends PopupFragment {

    public static SelectFolderFragment newInstance(String requestKey,
            InFolderDescriptorUi item,
            List<FolderDescriptorUi> folders,
            boolean move,
            @Nullable Rect anchor) {
        SelectFolderFragment fragment = new SelectFolderFragment();
        Bundle args = new Bundle();
        ArrayList<Folder> f = new ArrayList<>(folders.size());
        for (FolderDescriptorUi folder : folders) {
            f.add(new Folder(folder));
        }
        args.putSerializable(ARG_FOLDERS, f);
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, item.getDescriptor().getId());
        args.putBoolean(ARG_MOVE, move);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDERS = "folders";
    private static final String ARG_MOVE = "move";

    private static final String BACKSTACK_NAME = "folder_select";
    private static final String TAG = "folder_select";

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        containerRoot.setArrowColors(MaterialColors.getColor(view, com.google.android.material.R.attr.colorSurfaceContainer));

        populate();
        showPopup();
    }

    @SuppressWarnings("unchecked")
    private void populate() {
        Bundle args = requireArguments();
        ArrayList<Folder> folders = (ArrayList<Folder>) args.getSerializable(ARG_FOLDERS);
        LayoutInflater inflater = getLayoutInflater();
        for (Folder folder : folders) {
            TextView folderView = (TextView) inflater.inflate(R.layout.item_folder_select, itemsContainer, false);
            folderView.setText(folder.label);
            folderView.setOnClickListener(v -> {
                dismiss();
                Bundle result = AddToFolderContract.result(args.getString(ARG_DESCRIPTOR_ID), folder.id, args.getBoolean(ARG_MOVE));
                sendResult(result);
            });
            TextViewCompat.setCompoundDrawableTintList(folderView, ColorStateList.valueOf(folder.color));
            itemsContainer.addView(folderView);
        }
    }

    private static class Folder implements Serializable {
        final String id;
        final String label;
        @ColorInt
        final int color;

        Folder(FolderDescriptorUi folder) {
            this.id = folder.getDescriptor().id;
            this.label = folder.getVisibleLabel();
            this.color = folder.getVisibleColor();
        }
    }

    public static class AddToFolderContract implements FragmentResultContract<AddToFolderContract.Result> {
        private static final String KEY = "select_folder";
        private static final String DESCRIPTOR_ID = "descriptor_id";
        private static final String FOLDER_ID = "folder_id";
        private static final String MOVE = "move";

        static Bundle result(String descriptorId, String folderId, boolean move) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putString(DESCRIPTOR_ID, descriptorId);
            result.putString(FOLDER_ID, folderId);
            result.putBoolean(MOVE, move);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Result parseResult(Bundle result) {
            return new Result(result.getString(DESCRIPTOR_ID), result.getString(FOLDER_ID), result.getBoolean(MOVE));
        }

        public static class Result {
            public final String descriptorId;
            public final String folderId;
            public final boolean move;

            public Result(String descriptorId, String folderId, boolean move) {
                this.descriptorId = descriptorId;
                this.folderId = folderId;
                this.move = move;
            }
        }
    }
}
