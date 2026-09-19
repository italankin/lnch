package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;
import com.italankin.lnch.util.widget.popup.PopupFrameView;

public class EmptySpacePopupFragment extends ActionPopupFragment {

    public static EmptySpacePopupFragment newInstance(String requestKey, @NonNull Rect anchor) {
        EmptySpacePopupFragment fragment = new EmptySpacePopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String BACKSTACK_NAME = "empty_space_popup";
    private static final String POPUP_TAG = "empty_space_popup";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDrawArrow(false);
        setPopupLocations(PopupFrameView.Location.TOP, PopupFrameView.Location.BOTTOM);
        buildPopup();
        createItemViews();
        showPopup();
    }

    private void buildPopup() {
        addAction(new ItemBuilder()
                .setIcon(R.drawable.ic_action_rename)
                .setLabel(R.string.customize_hint)
                .setOnClickListener(v -> {
                    dismiss();
                    sendResult(new StartCustomizeContract().result());
                })
        );
        addAction(new ItemBuilder()
                .setIcon(R.drawable.ic_settings)
                .setLabel(R.string.settings_title)
                .setOnClickListener(v -> {
                    dismiss();
                    sendResult(new ShowSettingsContract().result());
                })
        );
    }

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return POPUP_TAG;
    }

    public static class StartCustomizeContract extends SignalFragmentResultContract {
        public StartCustomizeContract() {
            super("start_customize");
        }
    }

    public static class ShowSettingsContract extends SignalFragmentResultContract {
        public ShowSettingsContract() {
            super("show_settings");
        }
    }
}
