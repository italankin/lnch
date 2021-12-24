package com.italankin.lnch.util.widget.popup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

public class PopupFrameView extends ViewGroup {

    private static final boolean DEBUG = false;

    private static final float MAX_WIDTH_FACTOR = .85f;
    private static final float MAX_HEIGHT_FACTOR = .85f;

    private final Rect container = new Rect();
    private final Rect out = new Rect();
    private final int[] tmp = new int[2];

    private final List<Location> locations = Arrays.asList(Location.BOTTOM, Location.TOP);
    private Rect anchor;
    private float maxWidthFactor = MAX_WIDTH_FACTOR;
    private float maxHeightFactor = MAX_HEIGHT_FACTOR;

    private boolean keepLocationOnShrink;
    private int keptMeasuredWidth = -1;
    private int keptMeasuredHeight = -1;
    private Location keptLocation;

    public PopupFrameView(Context context) {
        super(context);
    }

    public PopupFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(!DEBUG);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setAnchor(Rect anchor) {
        this.anchor = anchor;
        requestLayout();
        invalidate();
    }

    public void setMaxWidthFactor(float factor) {
        this.maxWidthFactor = factor;
        requestLayout();
        invalidate();
    }

    public void setMaxHeightFactor(float factor) {
        this.maxHeightFactor = factor;
        requestLayout();
        invalidate();
    }

    public void setKeepLocationOnShrink(boolean keepLocationOnShrink) {
        this.keepLocationOnShrink = keepLocationOnShrink;
    }

    public void setLocations(List<Location> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount > 1) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " can host only one view");
        }

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));

        if (childCount == 0) {
            return;
        }

        if (!(getChildAt(0) instanceof ArrowLayout)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " can only host " + ArrowLayout.class.getSimpleName());
        }

        int maxWidth = (int) ((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) * maxWidthFactor);
        int maxHeight = (int) ((getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) * maxHeightFactor);
        int widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        int heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        measureChildren(widthSpec, heightSpec);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);

        if (DEBUG) {
            Paint p = new Paint();
            p.setColor(Color.RED);
            p.setAlpha(64);
            canvas.drawRect(container, p);
            p.setColor(Color.BLUE);
            p.setAlpha(64);
            canvas.drawRect(out, p);
            p.setColor(Color.GREEN);
            p.setAlpha(64);
            canvas.drawRect(anchor, p);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0) {
            return;
        }
        container.set(l + getPaddingLeft(),
                t + getPaddingTop(),
                r - getPaddingRight(),
                b - getPaddingBottom());
        Child child = (View & Child) getChildAt(0);
        int childWidth = child.getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        if (anchor != null) {
            Location popupLocation = null;
            if (keepLocationOnShrink && keptLocation != null
                    && keptMeasuredWidth >= childWidth && keptMeasuredHeight >= childHeight) {
                out.set(0, 0, childWidth, childHeight);
                keptLocation.apply(container, childWidth, childHeight, anchor, tmp, out);
                popupLocation = keptLocation;
            }
            if (popupLocation == null) {
                for (Location location : locations) {
                    out.set(0, 0, childWidth, childHeight);
                    if (location.apply(container, childWidth, childHeight, anchor, tmp, out)) {
                        popupLocation = location;
                        break;
                    }
                }
                if (keepLocationOnShrink) {
                    keptLocation = popupLocation;
                    keptMeasuredWidth = childWidth;
                    keptMeasuredHeight = childHeight;
                }
            }
            int anchorX = tmp[0];
            int anchorY = tmp[1];
            int childAnchorX = anchorX - out.left;
            int childAnchorY = anchorY - out.top;
            if (!container.contains(out)) {
                tmp[0] = 0;
                tmp[1] = 0;
                placeInside(out, container, tmp);
                if (tmp[0] != 0) {
                    // shifted along x axis
                    childAnchorX = anchorX - out.left;
                }
                if (tmp[1] != 0) {
                    // shifted along y axis
                    childAnchorY = anchorY - out.top;
                }
            }
            child.setAnchorPoint(childAnchorX, childAnchorY, popupLocation);
            child.setPivotX(anchorX - out.left);
            child.setPivotY(anchorY - out.top);
        } else {
            Gravity.apply(Gravity.CENTER, childWidth, childHeight, container, out);
            child.clearAnchorPoint();
        }
        child.layout(out.left, out.top, out.right, out.bottom);
    }

    private static void placeInside(Rect target, Rect bounds, int[] shifts) {
        if (target.left < bounds.left) {
            int dx = bounds.left - target.left;
            target.offset(dx, 0);
            shifts[0] = dx;
        }
        if (target.top < bounds.top) {
            int dy = bounds.top - target.top;
            target.offset(0, dy);
            shifts[1] = dy;
        }
        if (target.right > bounds.right) {
            int dx = bounds.right - target.right;
            target.offset(dx, 0);
            shifts[0] = dx;
        }
        if (target.bottom > bounds.bottom) {
            int dy = bounds.bottom - target.bottom;
            target.offset(0, dy);
            shifts[1] = dy;
        }
    }

    public interface Child {

        int getMeasuredWidth();

        int getMeasuredHeight();

        void setAnchorPoint(int ax, int ay, Location location);

        void clearAnchorPoint();

        void layout(int left, int top, int right, int bottom);

        void setPivotX(float x);

        void setPivotY(float y);
    }

    public enum Location {
        LEFT {
            @Override
            boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                    int[] outAnchors, Rect childOut) {
                int anchorX = anchor.left;
                int anchorY = anchor.centerY();
                childOut.offsetTo(anchorX - childOut.width(), anchorY - childOut.height() / 2);
                outAnchors[0] = anchorX;
                outAnchors[1] = anchorY;
                return childOut.left >= container.left;
            }
        },
        TOP {
            @Override
            boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                    int[] outAnchors, Rect childOut) {
                int anchorX = anchor.centerX();
                int anchorY = anchor.top;
                childOut.offsetTo(anchorX - childOut.width() / 2, anchorY - childOut.height());
                outAnchors[0] = anchorX;
                outAnchors[1] = anchorY;
                return childOut.top >= container.top;
            }
        },
        RIGHT {
            @Override
            boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                    int[] outAnchors, Rect childOut) {
                int anchorX = anchor.right;
                int anchorY = anchor.centerY();
                childOut.offsetTo(anchorX, anchorY - childOut.height() / 2);
                outAnchors[0] = anchorX;
                outAnchors[1] = anchorY;
                return childOut.right <= container.right;
            }
        },
        BOTTOM {
            @Override
            boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                    int[] outAnchors, Rect childOut) {
                int anchorX = anchor.centerX();
                int anchorY = anchor.bottom;
                childOut.offsetTo(anchorX - childOut.width() / 2, anchorY);
                outAnchors[0] = anchorX;
                outAnchors[1] = anchorY;
                return childOut.bottom <= container.bottom;
            }
        },
        CENTER {
            @Override
            boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                    int[] outAnchors, Rect outChild) {
                int anchorX = anchor.centerX();
                int anchorY = anchor.centerY();
                outChild.offsetTo(anchorX - outChild.width() / 2, anchorY - outChild.height() / 2);
                outAnchors[0] = anchorX;
                outAnchors[1] = anchorY;
                return true;
            }
        };

        abstract boolean apply(Rect container, int childWidth, int childHeight, Rect anchor,
                int[] outAnchors, Rect childOut);
    }
}
