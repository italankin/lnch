package com.italankin.lnch.feature.home.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.util.widget.LceLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class WidgetsFragment extends AppFragment {

    private LceLayout lce;
    private ViewGroup widgetContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_widgets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        lce = view.findViewById(R.id.lce_widgets);
        widgetContainer = view.findViewById(R.id.widget_container);

        lce.error()
                .message(R.string.widgets_empty)
                .button(R.string.widgets_add, v -> {
                    // TODO
                })
                .show();
    }
}
