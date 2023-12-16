package com.italankin.lnch.util.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.content.res.AppCompatResources;
import com.italankin.lnch.R;
import com.italankin.lnch.util.ViewUtils;

import java.util.HashMap;

public class LceLayout extends FrameLayout {

    private static final int DELAY_HIDE_LOADING = 500;

    private final HashMap<Layer, View> addedViews = new HashMap<>(1);
    private Layer visibleLayer = Layer.CONTENT;
    private Runnable delayedRunnable;

    private final StateView empty;
    private final StateView error;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public LceLayout(Context context) {
        this(context, null, 0);
    }

    public LceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        addLayer(Layer.ERROR, error = new StateView(inflater));
        addLayer(Layer.EMPTY, empty = new StateView(inflater));
        addLayer(Layer.LOADING, inflater.inflate(R.layout.state_loading, this, false));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public API
    ///////////////////////////////////////////////////////////////////////////

    public void showLoading() {
        showLayer(Layer.LOADING);
    }

    public void showContent() {
        showLayer(Layer.CONTENT);
    }

    @CheckResult(suggest = "show()")
    public StateBuilder error() {
        return new StateBuilder(error, Layer.ERROR);
    }

    @CheckResult(suggest = "show()")
    public StateBuilder empty() {
        return new StateBuilder(empty, Layer.EMPTY);
    }

    public View getVisibleLayerView() {
        return addedViews.get(visibleLayer);
    }

    public Layer getVisibleLayer() {
        return visibleLayer;
    }

    @NonNull
    public View getLoadingView() {
        return addedViews.get(Layer.LOADING);
    }

    @NonNull
    public View getErrorView() {
        return addedViews.get(Layer.ERROR);
    }

    @NonNull
    public View getEmptyView() {
        return addedViews.get(Layer.EMPTY);
    }

    @NonNull
    public View getContentView() {
        return addedViews.get(Layer.CONTENT);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Layer
    ///////////////////////////////////////////////////////////////////////////

    public enum Layer {
        CONTENT(0),
        EMPTY(1),
        ERROR(2),
        LOADING(3);

        private final int index;

        Layer(int index) {
            this.index = index;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // StateBuilder
    ///////////////////////////////////////////////////////////////////////////

    public final class StateBuilder {
        private final StateView stateView;
        private final Context context;
        private final Layer layer;

        private CharSequence message;
        private Drawable icon;
        private CharSequence buttonTitle;
        private OnClickListener buttonClickListener;

        StateBuilder(StateView stateView, Layer layer) {
            this.stateView = stateView;
            this.context = stateView.getContext();
            this.layer = layer;
        }

        @CheckResult(suggest = "show()")
        public StateBuilder message(@Nullable CharSequence message) {
            this.message = message;
            return this;
        }

        @CheckResult(suggest = "show()")
        public StateBuilder message(@StringRes int message) {
            return message(context.getText(message));
        }

        @CheckResult(suggest = "show()")
        public StateBuilder icon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        @CheckResult(suggest = "show()")
        public StateBuilder icon(@DrawableRes int icon) {
            return icon(AppCompatResources.getDrawable(context, icon));
        }

        @CheckResult(suggest = "show()")
        public StateBuilder button(@Nullable CharSequence buttonTitle, @Nullable OnClickListener listener) {
            this.buttonTitle = buttonTitle;
            this.buttonClickListener = listener;
            return this;
        }

        @CheckResult(suggest = "show()")
        public StateBuilder button(@Nullable OnClickListener listener) {
            return button(R.string.retry, listener);
        }

        @CheckResult(suggest = "show()")
        public StateBuilder button(@StringRes int title, @Nullable OnClickListener listener) {
            return button(context.getText(title), listener);
        }

        public void show() {
            stateView.setMessage(message, icon);
            stateView.setRetryButtonClickListener(buttonTitle, buttonClickListener);
            showLayerDelayed(layer);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void showLayer(Layer layer) {
        if (layer == visibleLayer) {
            return;
        }
        final View entering = addedViews.get(layer);
        visibleLayer = layer;
        bringChildToFront(entering);
        syncLayerVisibility();
    }

    private void syncLayerVisibility() {
        View child;
        LceLayoutParams lp;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            lp = (LceLayoutParams) child.getLayoutParams();
            child.setVisibility(visibleLayer == lp.layer ? VISIBLE : GONE);
        }
    }

    private void addLayer(Layer layer, @NonNull View child) {
        LceLayoutParams lp = new LceLayoutParams(layer);
        addView(child, -1, lp);
    }

    private void replaceLayer(Layer layer, @NonNull View child) {
        if (addedViews.containsKey(layer)) {
            View view = addedViews.get(layer);
            addedViews.remove(layer);
            removeView(view);
        }
        addLayer(layer, child);
    }

    private void showLayerDelayed(Layer layer) {
        if (layer != visibleLayer) {
            if (visibleLayer == Layer.LOADING) {
                removeDelayedRunnable();
                delayedRunnable = new ShowLayerRunnable(this, layer);
                postDelayed(delayedRunnable, DELAY_HIDE_LOADING);
            } else {
                showLayer(layer);
            }
        }
    }

    private void removeDelayedRunnable() {
        if (delayedRunnable != null) {
            removeCallbacks(delayedRunnable);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeDelayedRunnable();
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        LceLayoutParams lp = (LceLayoutParams) params;
        if (addedViews.containsKey(lp.layer)) {
            throw new IllegalArgumentException("Duplicate layer: " + lp.layer);
        }
        if (addedViews.size() >= 4) {
            throw new AssertionError("LceLayout can hold up to 4 children");
        }
        addedViews.put(lp.layer, child);
        if (visibleLayer == lp.layer) {
            child.setVisibility(VISIBLE);
        } else {
            child.setVisibility(GONE);
        }
        index = Math.min(lp.layer.index, getChildCount());
        super.addView(child, index, lp);
        syncLayerVisibility();
    }

    ///////////////////////////////////////////////////////////////////////////
    // LceLayoutParams
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LceLayoutParams;
    }

    @Override
    public LceLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LceLayoutParams();
    }

    @Override
    protected LceLayoutParams generateDefaultLayoutParams() {
        return new LceLayoutParams();
    }

    @Override
    protected LceLayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LceLayoutParams(p);
    }

    public static class LceLayoutParams extends FrameLayout.LayoutParams {
        public final Layer layer;

        public LceLayoutParams() {
            this(Layer.CONTENT);
        }

        public LceLayoutParams(Layer layer) {
            super(MATCH_PARENT, MATCH_PARENT);
            this.layer = layer;
        }

        public LceLayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            layer = ((LceLayoutParams) source).layer;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility
    ///////////////////////////////////////////////////////////////////////////

    private static class StateView extends LinearLayout {

        private final TextView text;
        private final Button button;

        StateView(LayoutInflater inflater) {
            super(inflater.getContext());
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            ViewUtils.setPaddingDp(this, 32);
            inflater.inflate(R.layout.state_view, this, true);
            text = findViewById(R.id.text);
            button = findViewById(R.id.button);
        }

        void setMessage(@Nullable CharSequence message, @Nullable Drawable icon) {
            text.setText(message);
            text.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        }

        void setRetryButtonClickListener(@Nullable CharSequence title, @Nullable OnClickListener listener) {
            if (title != null) {
                button.setText(title);
            }
            button.setOnClickListener(listener);
            button.setVisibility(listener != null ? VISIBLE : GONE);
        }
    }

    private static class ShowLayerRunnable implements Runnable {
        private final LceLayout layout;
        private final Layer layer;

        public ShowLayerRunnable(LceLayout layout, Layer layer) {
            this.layout = layout;
            this.layer = layer;
        }

        @Override
        public void run() {
            layout.showLayer(layer);
        }
    }
}
