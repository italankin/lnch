package com.italankin.lnch.feature.widgets.util;

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
import com.italankin.lnch.util.ResUtils;

import java.util.Arrays;
import java.util.List;

public class WidgetResizeFrame extends FrameLayout implements GestureDetector.OnGestureListener {

    private static final float EXTEND_THRESHOLD = .90f;
    private static final float SHRINK_THRESHOLD = .5f;

    private final WidgetSizeHelper widgetSizeHelper;

    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Rect frameMin = new Rect();
    private final Rect frameMax = new Rect();
    private final Rect frame = new Rect();
    private final Rect visualFrame = new Rect();

    private final int handleRadius;
    private final int drawFrameInset;
    private final int handleTouchRadius;
    private final int resizeElevation;
    private int cellSize;

    private final int frameColor;
    private final int frameOverlayColor;

    private final GestureDetector gestureDetector;

    private boolean resizeHorizontally = false;
    private boolean resizeVertically = false;
    private boolean forceResize = false;

    private final View deleteAction;
    private final View configureAction;
    private final List<View> actions;
    private LauncherAppWidgetHostView hostView;
    private AppWidget appWidget;

    private boolean triggerCommitAction;
    private Runnable commitAction;
    private OnStartDragListener startDragListener;

    private boolean resizeModeActive = false;
    private Handle handle = null;

    public WidgetResizeFrame(@NonNull Context context) {
        super(context);
        widgetSizeHelper = new WidgetSizeHelper(context);
        Resources res = context.getResources();
        frameColor = ContextCompat.getColor(context, R.color.widget_resize_frame);
        frameOverlayColor = ContextCompat.getColor(context, R.color.widget_resize_frame_overlay);
        framePaint.setColor(frameColor);
        framePaint.setStyle(Paint.Style.STROKE);
        int frameStrokeSize = res.getDimensionPixelSize(R.dimen.widget_resize_frame_stroke);
        framePaint.setStrokeWidth(frameStrokeSize);
        framePaint.setShadowLayer(frameStrokeSize / 2f, 0, 0,
                ContextCompat.getColor(context, R.color.widget_resize_frame_shadow));
        handleRadius = res.getDimensionPixelSize(R.dimen.widget_resize_frame_handle_radius);
        drawFrameInset = res.getDimensionPixelSize(R.dimen.widget_resize_frame_inset);
        resizeElevation = ResUtils.px2dp(getContext(), 10);
        handleTouchRadius = handleRadius * 3;
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setColor(frameColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{frameStrokeSize, frameStrokeSize}, 0));
        gridPaint.setStrokeWidth(frameStrokeSize);
        gridPaint.setColor(frameColor);
        gridPaint.setAlpha(80);

        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setIsLongpressEnabled(false);

        setClipChildren(false);
        setClipToPadding(false);

        inflate(context, R.layout.widget_edit_actions, this);
        deleteAction = findViewById(R.id.widget_delete);
        configureAction = findViewById(R.id.widget_configure);
        actions = Arrays.asList(deleteAction, configureAction);
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        invalidate();
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
        this.forceResize = forceResize;
        updateResizeFlags();
    }

    public void bindAppWidget(AppWidget appWidget) {
        this.appWidget = appWidget;
        frameMin.set(0, 0, appWidget.size.minWidth, appWidget.size.minHeight);
        frameMax.set(0, 0, appWidget.size.maxWidth, appWidget.size.maxHeight);
        updateResizeFlags();
        setHostViewSize(appWidget.options, appWidget.size.width, appWidget.size.height);
    }

    private void updateResizeFlags() {
        AppWidgetProviderInfo info = hostView.getAppWidgetInfo();
        resizeHorizontally = forceResize || (info.resizeMode & AppWidgetProviderInfo.RESIZE_HORIZONTAL) != 0;
        resizeVertically = forceResize || (info.resizeMode & AppWidgetProviderInfo.RESIZE_VERTICAL) != 0;
    }

    public void setResizeMode(boolean resizeMode) {
        if (resizeModeActive != resizeMode) {
            resizeModeActive = resizeMode;
            deleteAction.setVisibility(resizeMode ? View.VISIBLE : View.INVISIBLE);
            configureAction.setVisibility(resizeMode && isReconfigurable() ? VISIBLE : GONE);
            handle = null;
            if (!resizeMode) {
                gestureDetector.setIsLongpressEnabled(false);
                setElevation(0f);
            }
            setWillNotDraw(!resizeMode);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (handle != null) {
            return;
        }
        updateFrame();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if (resizeModeActive && !visualFrame.isEmpty()) {
            handlePaint.setColor(frameOverlayColor);
            canvas.drawRect(visualFrame, handlePaint);
            if (cellSize > 0) {
                int l = frame.left + cellSize;
                while (l < frame.right) {
                    canvas.drawLine(l, visualFrame.top, l, visualFrame.bottom, gridPaint);
                    l += cellSize;
                }
                int t = frame.top + cellSize;
                while (t < frame.bottom) {
                    canvas.drawLine(visualFrame.left, t, visualFrame.right, t, gridPaint);
                    t += cellSize;
                }
            }
            canvas.drawRect(visualFrame, framePaint);
            handlePaint.setColor(frameColor);
            if (resizeVertically) {
                canvas.drawCircle(visualFrame.centerX(), visualFrame.bottom, handleRadius, handlePaint);
            }
            if (resizeHorizontally) {
                canvas.drawCircle(visualFrame.right, visualFrame.centerY(), handleRadius, handlePaint);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!resizeModeActive) {
            return false;
        }
        for (View action : actions) {
            if (event.getX() >= action.getLeft() && event.getX() <= action.getRight() &&
                    event.getY() >= action.getTop() && event.getY() <= action.getBottom()) {
                return false;
            }
        }
        resetFrame(event);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!resizeModeActive) {
            return super.onTouchEvent(event);
        }
        resetFrame(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handle = null;
            float x = event.getX(), y = event.getY();
            if (resizeVertically) {
                if (euclidean(x, y, visualFrame.centerX(), visualFrame.bottom) <= handleTouchRadius) {
                    handle = Handle.BOTTOM;
                }
            }
            if (handle == null && resizeHorizontally) {
                if (euclidean(x, y, visualFrame.right, visualFrame.centerY()) <= handleTouchRadius) {
                    handle = Handle.RIGHT;
                }
            }
            if (handle != null) {
                setElevation(resizeElevation);
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
        if (handle == null) {
            return false;
        }
        switch (handle) {
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
        invalidate();

        int width = frame.width();
        int height = frame.height();
        if (cellSize > 0) {
            if ((width % cellSize) > cellSize * EXTEND_THRESHOLD) {
                width += (cellSize - (width % cellSize));
            } else if ((width % cellSize) < cellSize * SHRINK_THRESHOLD) {
                width -= (width % cellSize);
            } else {
                return true;
            }
            if ((height % cellSize) > cellSize * EXTEND_THRESHOLD) {
                height += (cellSize - (height % cellSize));
            } else if ((height % cellSize) < cellSize * SHRINK_THRESHOLD) {
                height -= (height % cellSize);
            } else {
                return true;
            }
        }
        setHostViewSize(new Bundle(), width, height);
        triggerCommitAction = true;
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
            handle = null;
            updateFrame();
            invalidate();
            setElevation(0f);
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

    private void setHostViewSize(Bundle options, int width, int height) {
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

            widgetSizeHelper.resize(hostView.getAppWidgetId(), options, width, height);
        }
    }

    @Override
    public void onViewAdded(View child) {
        if (child instanceof LauncherAppWidgetHostView) {
            hostView = (LauncherAppWidgetHostView) child;
        }
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
}
