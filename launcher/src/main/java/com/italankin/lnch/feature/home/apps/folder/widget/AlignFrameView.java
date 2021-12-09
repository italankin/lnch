package com.italankin.lnch.feature.home.apps.folder.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

public class AlignFrameView extends ViewGroup {

    private static final float MAX_WIDTH_FACTOR = .85f;
    private static final float MAX_HEIGHT_FACTOR = .85f;

    private final Rect container = new Rect();
    private final Rect out = new Rect();

    private int anchorX = -1;
    private int anchorY = -1;

    public AlignFrameView(Context context) {
        super(context);
    }

    public AlignFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAnchorPoint(int x, int y) {
        anchorX = x;
        anchorY = y;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childCount = getChildCount();
        if (childCount > 1) {
            throw new IllegalStateException(AlignFrameView.class.getSimpleName() + " can hold only 1 root view");
        }

        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));

        if (childCount == 0) {
            return;
        }

        int maxWidth = (int) ((getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) * MAX_WIDTH_FACTOR);
        int maxHeight = (int) ((getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) * MAX_HEIGHT_FACTOR);
        int widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
        int heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        measureChildren(widthSpec, heightSpec);
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
        View child = getChildAt(0);
        int childMeasuredWidth = child.getMeasuredWidth();
        int childMeasuredHeight = child.getMeasuredHeight();
        if (anchorX < 0 || anchorY < 0) {
            Gravity.apply(Gravity.CENTER, childMeasuredWidth, childMeasuredHeight, container, out);
        } else {
            out.left = anchorX - childMeasuredWidth / 2;
            out.top = anchorY;
            out.right = anchorX + childMeasuredWidth / 2;
            out.bottom = anchorY + childMeasuredHeight;
            if (!container.contains(out)) {
                if (out.left < container.left) {
                    int dx = container.left - out.left;
                    out.offset(dx, 0);
                }
                if (out.top < container.top) {
                    int dy = container.top - out.top;
                    out.offset(0, dy);
                }
                if (out.right > container.right) {
                    int dx = container.right - out.right;
                    out.offset(dx, 0);
                }
                if (out.bottom > container.bottom) {
                    int dy = container.bottom - out.bottom;
                    out.offset(0, dy);
                }
            }
            child.setPivotX(anchorX - out.left);
            child.setPivotY(anchorY - out.top);
        }
        child.layout(out.left, out.top, out.right, out.bottom);
    }
}
