package com.italankin.lnch.util.widget.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;

import com.italankin.lnch.R;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class PopupFragment extends Fragment {

    protected static final String ARG_ANCHOR = "anchor";

    private static final String BACKSTACK_NAME = "popup";
    private static final String TAG = "popup";

    protected PopupFrameView root;
    protected ArrowLayout containerRoot;
    protected ViewGroup itemsContainer;

    private int backstackId;

    @CallSuper
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup, container, false);
        root = view.findViewById(R.id.popup_root);
        containerRoot = view.findViewById(R.id.popup_container_root);
        itemsContainer = view.findViewById(R.id.popup_item_container);
        return view;
    }

    @CallSuper
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        itemsContainer.setClipToOutline(true);

        Rect anchor = requireArguments().getParcelable(ARG_ANCHOR);
        root.setAnchor(anchor);
        root.post(() -> {
            WindowInsets insets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
            root.setPaddingRelative(0, insets.getStableInsetTop(), 0, insets.getStableInsetBottom());
        });
        root.setOnClickListener(v -> dismiss());
    }

    public void show(FragmentManager fragmentManager) {
        backstackId = fragmentManager.beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.fragment_folder_out)
                .add(android.R.id.content, this, TAG)
                .addToBackStack(BACKSTACK_NAME)
                .commit();
    }

    public void dismiss() {
        if (backstackId >= 0) {
            getParentFragmentManager()
                    .popBackStack(backstackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            backstackId = -1;
        }
    }

    protected void show() {
        containerRoot.setScaleX(0.4f);
        containerRoot.setScaleY(0.4f);
        containerRoot.setAlpha(0);

        containerRoot.animate()
                .withStartAction(() -> {
                    containerRoot.setVisibility(View.VISIBLE);
                })
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(150)
                .start();
    }
}
