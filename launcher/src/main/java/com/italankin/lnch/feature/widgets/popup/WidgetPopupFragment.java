package com.italankin.lnch.feature.widgets.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.util.widget.popup.ActionPopupFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WidgetPopupFragment extends ActionPopupFragment {

    public static WidgetPopupFragment newInstance(
            int appWidgetId,
            String requestKey,
            @Nullable Rect anchor) {
        WidgetPopupFragment fragment = new WidgetPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putInt(ARG_APP_WIDGET_ID, appWidgetId);
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_APP_WIDGET_ID = "app_widget_id";
    private static final String BACKSTACK_NAME = "widget_popup";
    private static final String TAG = "widget_popup";

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

        int appWidgetId = requireArguments().getInt(ARG_APP_WIDGET_ID);

        addShortcut(new ItemBuilder()
                .setLabel(R.string.widgets_app_info)
                .setOnClickListener(v -> {
                    dismiss();
                    Bundle result = new Bundle();
                    result.putString(FragmentResults.RESULT, FragmentResults.Widgets.AppInfo.KEY);
                    result.putInt(FragmentResults.Widgets.AppInfo.APP_WIDGET_ID, appWidgetId);
                    sendResult(result);
                })
                .setIconDrawableTintAttr(R.attr.colorAccent)
                .setIcon(R.drawable.ic_app_info));
        addShortcut(new ItemBuilder()
                .setLabel(R.string.widgets_remove)
                .setOnClickListener(v -> {
                    dismiss();
                    Bundle result = new Bundle();
                    result.putString(FragmentResults.RESULT, FragmentResults.Widgets.RemoveWidget.KEY);
                    result.putInt(FragmentResults.Widgets.AppInfo.APP_WIDGET_ID, appWidgetId);
                    sendResult(result);
                })
                .setIconDrawableTintAttr(R.attr.colorAccent)
                .setIcon(R.drawable.ic_action_delete));

        createItemViews();
        showPopup();
    }
}
