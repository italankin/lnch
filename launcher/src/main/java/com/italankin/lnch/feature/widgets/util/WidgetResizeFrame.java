package com.italankin.lnch.feature.widgets.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.widgets.host.LauncherAppWidgetHostView;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.CellSize;
import com.italankin.lnch.util.ResUtils;

import java.util.Collections;
import java.util.List;

public class WidgetResizeFrame extends FrameLayout implements GestureDetector.OnGestureListener {

    private static final float EXTEND_THRESHOLD = .7f;
    private static final float SHRINK_THRESHOLD = .4f;
    private static final int SWITCH_MODE_ANIM_DURATION = 250;

    private final WidgetSizeHelper widgetSizeHelper;

    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Rect frameMin = new Rect();
    private final Rect frameMax = new Rect();
    private final Rect frame = new Rect();
    private final Rect visualFrame = new Rect();

    private final int frameColor;
    private final int frameActiveColor;
    private final int handleRadius;
    private final int drawFrameInset;
    private final int handleTouchRadius;
    private final int resizeElevation;
    private CellSize cellSize;

    private final GestureDetector gestureDetector;

    private boolean resizeHorizontally = false;
    private boolean resizeVertically = false;
    private boolean forceResize = false;

    private final View deleteAction;
    private final View configureAction;
    private final ViewGroup actionsContainer;
    private LauncherAppWidgetHostView hostView;
    private final FrameView frameView;
    private AppWidget appWidget;

    private boolean triggerCommitAction;
    private Runnable commitAction;
    private OnStartDragListener startDragListener;

    private boolean resizeModeActive = false;
    private Handle activeDragHandle = null;
    private ValueAnimator switchModeAnimator;
    private final Rect exclusionRect = new Rect();
    private final List<Rect> exclusionRects = Collections.singletonList(exclusionRect);

    public WidgetResizeFrame(@NonNull Context context) {
        super(context);
        widgetSizeHelper = new WidgetSizeHelper(context);
        Resources res = context.getResources();
        frameColor = ContextCompat.getColor(context, R.color.widget_resize_frame);
        frameActiveColor = ContextCompat.getColor(context, R.color.widget_resize_frame_active);
        framePaint.setColor(frameColor);
        framePaint.setStyle(Paint.Style.STROKE);
        int frameStrokeSize = res.getDimensionPixelSize(R.dimen.widget_resize_frame_stroke);
        framePaint.setStrokeWidth(frameStrokeSize);
        framePaint.setShadowLayer(frameStrokeSize / 2f, 0, 0,
                ContextCompat.getColor(context, R.color.widget_resize_frame_shadow));
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(frameColor);
        handlePaint.setShadowLayer(frameStrokeSize / 2f, 0, 0,
                ContextCompat.getColor(context, R.color.widget_resize_frame_shadow));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(frameStrokeSize / 2f);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.widget_resize_frame_grid));
        overlayPaint.setColor(ContextCompat.getColor(context, R.color.widget_resize_frame_overlay));
        overlayPaint.setStyle(Paint.Style.FILL);
        handleRadius = res.getDimensionPixelSize(R.dimen.widget_resize_frame_handle_radius);
        drawFrameInset = res.getDimensionPixelSize(R.dimen.widget_resize_frame_inset);
        resizeElevation = ResUtils.px2dp(context, 20);
        handleTouchRadius = handleRadius * 3;

        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setIsLongpressEnabled(false);

        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawingOrderEnabled(true);

        inflate(context, R.layout.widget_edit_actions, this);
        deleteAction = findViewById(R.id.widget_delete);
        configureAction = findViewById(R.id.widget_configure);
        actionsContainer = findViewById(R.id.actions_container);

        frameView = new FrameView(context);
        addView(frameView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        frameView.setVisibility(View.INVISIBLE);
        frameView.setAlpha(0f);
    }

    public void setCellSize(CellSize cellSize) {
        this.cellSize = cellSize;
        float interval = cellSize.width / 16f;
        gridPaint.setPathEffect(new DashPathEffect(new float[]{interval, interval}, drawFrameInset / 2f));
        frameView.invalidate();
    }

    public void setDeleteAction(OnClickListener onClickListener) {
        deleteAction.setOnClickListener(onClickListener);
    }

    public void setConfigureAction(OnClickListener onClickListener) {
        configureAction.setOnClickListener(onClickListener);
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        startDragListener = listener;
    }

    public void setCommitAction(Runnable action) {
        this.commitAction = action;
    }

    public void setForceResize(boolean forceResize) {
        if (this.forceResize != forceResize) {
            this.forceResize = forceResize;
            updateWidgetFlags();
            updateMinMaxFrames();
        }
    }

    public void bindAppWidget(AppWidget appWidget, LauncherAppWidgetHostView hostView) {
        this.appWidget = appWidget;
        this.hostView = hostView;
        addView(hostView, 0, new LayoutParams(appWidget.size.width, appWidget.size.height));
        updateMinMaxFrames();
        updateWidgetFlags();
        setHostViewSize(appWidget.options, appWidget.size.width, appWidget.size.height);
    }

    public void setResizeMode(boolean resizeMode, boolean animated) {
        if (resizeModeActive != resizeMode) {
            resizeModeActive = resizeMode;
            activeDragHandle = null;
            if (!resizeMode) {
                gestureDetector.setIsLongpressEnabled(false);
                setElevation(0f);
            }
            if (switchModeAnimator != null && switchModeAnimator.isRunning()) {
                switchModeAnimator.cancel();
            }
            if (!animated) {
                frameView.setVisibility(resizeMode ? View.VISIBLE : View.INVISIBLE);
                frameView.setAlpha(resizeMode ? 1 : 0);
                actionsContainer.setVisibility(resizeMode ? View.VISIBLE : View.INVISIBLE);
                actionsContainer.setAlpha(resizeMode ? 1 : 0);
                return;
            }
            if (resizeMode) {
                switchModeAnimator = ValueAnimator.ofFloat(frameView.getAlpha(), 1);
                switchModeAnimator.setDuration(SWITCH_MODE_ANIM_DURATION);
                switchModeAnimator.addUpdateListener(animation -> {
                    float alpha = (float) animation.getAnimatedValue();
                    frameView.setAlpha(alpha);
                    actionsContainer.setAlpha(alpha);
                });
                switchModeAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        frameView.setVisibility(View.VISIBLE);
                        actionsContainer.setVisibility(View.VISIBLE);
                    }
                });
                switchModeAnimator.start();
            } else {
                switchModeAnimator = ValueAnimator.ofFloat(frameView.getAlpha(), 0);
                switchModeAnimator.setDuration(SWITCH_MODE_ANIM_DURATION);
                switchModeAnimator.addUpdateListener(animation -> {
                    float alpha = (float) animation.getAnimatedValue();
                    frameView.setAlpha(alpha);
                    actionsContainer.setAlpha(alpha);
                });
                switchModeAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        frameView.setVisibility(View.INVISIBLE);
                        actionsContainer.setVisibility(View.INVISIBLE);
                    }
                });
                switchModeAnimator.start();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (switchModeAnimator != null) {
            switchModeAnimator.cancel();
            switchModeAnimator = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (activeDragHandle != null) {
            return;
        }
        updateFrame();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // if gesture system navigation enabled, we need to
            // allow dragging right handle from the edge of the screen
            exclusionRect.set(visualFrame.right - handleTouchRadius, visualFrame.centerY() - handleTouchRadius,
                    visualFrame.right + handleTouchRadius, visualFrame.centerY() + handleTouchRadius);
            setSystemGestureExclusionRects(exclusionRects);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!resizeModeActive) {
            return false;
        }
        if (event.getX() >= actionsContainer.getLeft() && event.getX() <= actionsContainer.getRight() &&
                event.getY() >= actionsContainer.getTop() && event.getY() <= actionsContainer.getBottom()) {
            return false;
        }
        resetFrame(event);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!resizeModeActive) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP && activeDragHandle != null) {
            setElevation(0f);
        }
        resetFrame(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            activeDragHandle = null;
            float x = event.getX(), y = event.getY();
            if (resizeVertically) {
                if (euclidean(x, y, visualFrame.centerX(), visualFrame.bottom) <= handleTouchRadius) {
                    activeDragHandle = Handle.BOTTOM;
                }
            }
            if (activeDragHandle == null && resizeHorizontally) {
                if (euclidean(x, y, visualFrame.right, visualFrame.centerY()) <= handleTouchRadius) {
                    activeDragHandle = Handle.RIGHT;
                }
            }
            if (activeDragHandle != null) {
                setElevation(resizeElevation);
                frameView.invalidate();
                gestureDetector.setIsLongpressEnabled(false);
                getParent().requestDisallowInterceptTouchEvent(true);
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            } else {
                gestureDetector.setIsLongpressEnabled(resizeModeActive);
            }
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        triggerCommitAction = false;
        return true;
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        if (activeDragHandle == null) {
            return false;
        }
        switch (activeDragHandle) {
            case RIGHT:
                frame.right = (int) e2.getX();
                if (frame.right > frameMax.right) {
                    frame.right = frameMax.right;
                } else if (frame.width() < frameMin.width()) {
                    frame.right = frame.left + frameMin.width();
                }
                break;
            case BOTTOM:
                frame.bottom = (int) e2.getY();
                if (frame.bottom > frameMax.bottom) {
                    frame.bottom = frameMax.bottom;
                } else if (frame.height() < frameMin.height()) {
                    frame.bottom = frame.top + frameMin.height();
                }
                break;
        }
        visualFrame.set(frame);
        visualFrame.inset(drawFrameInset, drawFrameInset);
        frameView.invalidate();

        int width = frame.width();
        int height = frame.height();
        if (cellSize.isNotEmpty()) {
            if ((width % cellSize.width) > cellSize.width * EXTEND_THRESHOLD) {
                width += (cellSize.width - (width % cellSize.width));
            } else if ((width % cellSize.width) < cellSize.width * SHRINK_THRESHOLD) {
                width -= (width % cellSize.width);
            } else {
                return true;
            }
            if ((height % cellSize.height) > cellSize.height * EXTEND_THRESHOLD) {
                height += (cellSize.height - (height % cellSize.height));
            } else if ((height % cellSize.height) < cellSize.height * SHRINK_THRESHOLD) {
                height -= (height % cellSize.height);
            } else {
                return true;
            }
        }
        triggerCommitAction = setHostViewSize(new Bundle(), width, height);
        appWidget.size.width = width;
        appWidget.size.height = height;
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if (startDragListener != null) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            startDragListener.onStartDrag(this);
        }
    }

    private void updateMinMaxFrames() {
        if (forceResize) {
            frameMin.set(0, 0, cellSize.width, cellSize.height);
        } else {
            frameMin.set(0, 0, appWidget.size.minWidth, appWidget.size.minHeight);
        }
        frameMax.set(0, 0, appWidget.size.maxWidth, appWidget.size.maxHeight);
    }

    private void updateWidgetFlags() {
        AppWidgetProviderInfo info = hostView.getAppWidgetInfo();
        resizeHorizontally = forceResize || (info.resizeMode & AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0 &&
                appWidget.size.minWidth != appWidget.size.maxWidth;
        resizeVertically = forceResize || (info.resizeMode & AppWidgetProviderInfo.RESIZE_VERTICAL) != 0 &&
                appWidget.size.minHeight != appWidget.size.maxHeight;
        configureAction.setVisibility(isReconfigurable() ? VISIBLE : INVISIBLE);
    }

    private boolean isReconfigurable() {
        AppWidgetProviderInfo info = hostView.getAppWidgetInfo();
        if (info.configure == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return (info.widgetFeatures & AppWidgetProviderInfo.WIDGET_FEATURE_RECONFIGURABLE) != 0;
        }
        return false;
    }

    private void resetFrame(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            activeDragHandle = null;
            updateFrame();
            frameView.invalidate();
            if (triggerCommitAction && commitAction != null) {
                commitAction.run();
            }
        }
    }

    private void updateFrame() {
        ViewGroup.LayoutParams lp = hostView.getLayoutParams();
        frame.set(0, 0, lp.width, lp.height);
        visualFrame.set(frame);
        visualFrame.inset(drawFrameInset, drawFrameInset);
    }

    private boolean setHostViewSize(Bundle options, int width, int height) {
        ViewGroup.LayoutParams lp = hostView.getLayoutParams();
        if (width != lp.width || height != lp.height) {
            lp.width = width;
            lp.height = height;
            hostView.setLayoutParams(lp);

            ViewGroup.LayoutParams plp = getLayoutParams();
            plp.width = width;
            plp.height = height;
            setLayoutParams(plp);

            requestLayout();
            invalidate();
            frameView.invalidate();

            widgetSizeHelper.resize(hostView.getAppWidgetId(), options, width, height, true);
            return true;
        }
        return false;
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int drawingPosition) {
        View child = getChildAt(drawingPosition);
        if (child == hostView) {
            return 0;
        } else if (child == frameView) {
            return 1;
        } else if (child == actionsContainer) {
            return 2;
        }
        return drawingPosition;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    private static float euclidean(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private enum Handle {
        RIGHT,
        BOTTOM
    }

    public interface OnStartDragListener {
        void onStartDrag(WidgetResizeFrame resizeFrame);
    }

    private class FrameView extends View {

        FrameView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (visualFrame.isEmpty()) {
                return;
            }
            canvas.drawRect(visualFrame, overlayPaint);
            if (cellSize.isNotEmpty()) {
                int l = frame.left + cellSize.width;
                while (l < frame.right) {
                    canvas.drawLine(l, visualFrame.top, l, visualFrame.bottom, gridPaint);
                    l += cellSize.width;
                }
                int t = frame.top + cellSize.height;
                while (t < frame.bottom) {
                    canvas.drawLine(visualFrame.left, t, visualFrame.right, t, gridPaint);
                    t += cellSize.height;
                }
            }
            if (activeDragHandle != null) {
                framePaint.setColor(frameActiveColor);
                handlePaint.setColor(frameActiveColor);
            } else {
                framePaint.setColor(frameColor);
                handlePaint.setColor(frameColor);
            }
            canvas.drawRect(visualFrame, framePaint);
            if (resizeVertically) {
                canvas.drawCircle(visualFrame.centerX(), visualFrame.bottom, handleRadius, handlePaint);
            }
            if (resizeHorizontally) {
                canvas.drawCircle(visualFrame.right, visualFrame.centerY(), handleRadius, handlePaint);
            }
        }
    }
}
