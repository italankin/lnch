package com.italankin.lnch.feature.home.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class EditModePanel extends LinearLayout {

    private static final int ANIM_DURATION = 300;
    private static final float ALPHA_DISABLED = 0.25f;

    private final TextView message;
    private final View undo;
    private final View save;

    private CoordinatorLayout parent;
    private boolean dismissed = false;

    public EditModePanel(Context context) {
        this(context, null);
    }

    public EditModePanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.widget_edit_mode_panel, this);
        setOrientation(VERTICAL);
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(ResUtils.resolveColor(context, R.attr.colorEditPanelBackground));
        message = findViewById(R.id.message);
        undo = findViewById(R.id.undo);
        save = findViewById(R.id.save);
    }

    public EditModePanel setMessage(CharSequence text) {
        message.setText(text);
        return this;
    }

    public EditModePanel setMessage(@StringRes int text) {
        message.setText(text);
        return this;
    }

    public EditModePanel setOnSaveActionClickListener(OnClickListener listener) {
        save.setOnClickListener(listener);
        return this;
    }

    public EditModePanel setOnUndoActionClickListener(OnClickListener listener) {
        undo.setOnClickListener(listener);
        undo.setVisibility(listener == null ? GONE : VISIBLE);
        return this;
    }

    public EditModePanel setUndoActionEnabled(boolean enabled) {
        undo.setAlpha(enabled ? 1 : ALPHA_DISABLED);
        undo.setEnabled(enabled);
        return this;
    }

    public EditModePanel setSaveActionEnabled(boolean enabled) {
        save.setAlpha(enabled ? 1 : ALPHA_DISABLED);
        save.setEnabled(enabled);
        return this;
    }

    public EditModePanel show(CoordinatorLayout layout) {
        if (dismissed) {
            return this;
        }

        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        (parent = layout).addView(this, params);

        int height = getResources().getDimensionPixelSize(R.dimen.edit_panel_size);
        setAlpha(0);
        setTranslationY(height);

        animate()
                .translationY(0)
                .alpha(1)
                .setDuration(ANIM_DURATION)
                .setListener(null)
                .start();
        return this;
    }

    public void dismiss() {
        if (dismissed) {
            return;
        }
        dismissed = true;
        animate()
                .translationY(getHeight())
                .alpha(0)
                .setDuration(ANIM_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        parent.removeView(EditModePanel.this);
                        parent = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        parent.removeView(EditModePanel.this);
                        parent = null;
                    }
                })
                .start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(0, 0, 0, insets.getStableInsetBottom());
        return insets;
    }
}
