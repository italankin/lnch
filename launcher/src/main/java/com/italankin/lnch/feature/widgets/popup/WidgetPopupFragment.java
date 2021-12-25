package com.italankin.lnch.feature.widgets.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultContract;
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
                    sendResult(AppInfoContract.result(appWidgetId));
                })
                .setIconDrawableTintAttr(R.attr.colorAccent)
                .setIcon(R.drawable.ic_app_info));
        addShortcut(new ItemBuilder()
                .setLabel(R.string.widgets_remove)
                .setOnClickListener(v -> {
                    dismiss();
                    sendResult(RemoveWidgetContract.result(appWidgetId));
                })
                .setIconDrawableTintAttr(R.attr.colorAccent)
                .setIcon(R.drawable.ic_action_delete));

        createItemViews();
        showPopup();
    }

    public static class AppInfoContract implements FragmentResultContract<Integer> {
        private static final String KEY = "widget_app_info";
        private static final String APP_WIDGET_ID = "app_widget_id";

        static Bundle result(int appWidgetId) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putInt(APP_WIDGET_ID, appWidgetId);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Integer parseResult(Bundle result) {
            return result.getInt(APP_WIDGET_ID);
        }
    }

    public static class RemoveWidgetContract implements FragmentResultContract<Integer> {
        private static final String KEY = "widget_remove";
        private static final String APP_WIDGET_ID = "app_widget_id";

        static Bundle result(int appWidgetId) {
            Bundle result = new Bundle();
            result.putString(RESULT_KEY, KEY);
            result.putInt(APP_WIDGET_ID, appWidgetId);
            return result;
        }

        @Override
        public String key() {
            return KEY;
        }

        @Override
        public Integer parseResult(Bundle result) {
            return result.getInt(APP_WIDGET_ID);
        }
    }
}
