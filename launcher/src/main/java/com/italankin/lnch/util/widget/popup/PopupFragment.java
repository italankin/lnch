package com.italankin.lnch.util.widget.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.italankin.lnch.R;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public abstract class PopupFragment extends Fragment {

    protected static final String ARG_ANCHOR = "anchor";

    protected PopupFrameView root;
    protected ArrowLayout containerRoot;
    protected LinearLayout itemsContainer;

    private int backstackId;

    private boolean showAfterInsetsApplied = false;
    private boolean insetsApplied = false;

    protected abstract String getPopupBackstackName();

    protected abstract String getPopupTag();

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
        WindowInsets initialInsets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
        if (initialInsets == null) {
            root.post(() -> {
                WindowInsets insets = requireActivity().getWindow().getDecorView().getRootWindowInsets();
                applyInsets(insets);
                if (showAfterInsetsApplied) {
                    showPopupInternal();
                }
            });
        } else {
            applyInsets(initialInsets);
        }
        root.setOnClickListener(v -> dismiss());
    }

    public void show(FragmentManager fragmentManager) {
        backstackId = fragmentManager.beginTransaction()
                .setCustomAnimations(0, 0, 0, R.animator.fragment_popup_out)
                .add(android.R.id.content, this, getPopupTag())
                .addToBackStack(getPopupBackstackName())
                .commit();
    }

    public void dismiss() {
        if (backstackId >= 0) {
            getParentFragmentManager()
                    .popBackStack(backstackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            backstackId = -1;
        }
    }

    protected void showPopup() {
        if (insetsApplied) {
            showPopupInternal();
        } else {
            showAfterInsetsApplied = true;
        }
    }

    private void applyInsets(WindowInsets insets) {
        root.setPaddingRelative(root.getPaddingStart(), insets.getStableInsetTop(),
                root.getPaddingEnd(), insets.getStableInsetBottom());
        insetsApplied = true;
    }

    private void showPopupInternal() {
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
