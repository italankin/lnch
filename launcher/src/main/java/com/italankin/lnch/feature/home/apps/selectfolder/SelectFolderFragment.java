package com.italankin.lnch.feature.home.apps.selectfolder;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.impl.FolderDescriptorUi;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.widget.popup.PopupFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

public class SelectFolderFragment extends PopupFragment {

    public static SelectFolderFragment newInstance(String requestKey,
            InFolderDescriptorUi item,
            List<FolderDescriptorUi> folders,
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
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDERS = "folders";
    private static final String ARG_REQUEST_KEY = "request_key";

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

        containerRoot.setArrowColors(ResUtils.resolveColor(requireContext(), R.attr.colorPopupBackground));

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
                String requestKey = args.getString(ARG_REQUEST_KEY);
                Bundle result = new Bundle();
                result.putString(FragmentResults.RESULT, FragmentResults.SelectFolder.KEY);
                result.putString(FragmentResults.SelectFolder.FOLDER_ID, folder.id);
                result.putString(FragmentResults.SelectFolder.DESCRIPTOR_ID, args.getString(ARG_DESCRIPTOR_ID));
                getParentFragmentManager().setFragmentResult(requestKey, result);
                dismiss();
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
}
