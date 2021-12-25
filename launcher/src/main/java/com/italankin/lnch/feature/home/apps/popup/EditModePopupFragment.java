package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.model.repository.prefs.Preferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditModePopupFragment extends ActionPopupFragment {

    public static EditModePopupFragment newInstance(
            String requestKey,
            @Nullable Rect anchor) {
        EditModePopupFragment fragment = new EditModePopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String BACKSTACK_NAME = "edit_mode_popup";
    private static final String TAG = "edit_mode_popup";

    private Preferences preferences;

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = LauncherApp.daggerService.main().preferences();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addShortcut(new ItemBuilder()
                .setIcon(R.drawable.ic_action_add_new_folder)
                .setLabel(R.string.edit_add_folder)
                .setOnClickListener(v -> {
                    dismiss();
                    Bundle result = new Bundle();
                    result.putString(FragmentResults.RESULT, FragmentResults.Customize.AddFolder.KEY);
                    sendResult(result);
                }));
        if (preferences.get(Preferences.EXPERIMENTAL_INTENT_FACTORY)) {
            addShortcut(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_intent_edit)
                    .setLabel(R.string.edit_add_intent)
                    .setOnClickListener(v -> {
                        dismiss();
                        Bundle result = new Bundle();
                        result.putString(FragmentResults.RESULT, FragmentResults.Customize.CreateIntent.KEY);
                        sendResult(result);
                    }));
        }

        createItemViews();
        showPopup();
    }
}
