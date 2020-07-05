package com.italankin.lnch.util.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

public abstract class BasePopupWindow extends PopupWindow {

    private static final float MAX_WIDTH_FACTOR = 0.66f;
    private static final float MAX_HEIGHT_FACTOR = 0.8f;

    private ViewGroup contentView;

    private final int arrowSize;

    private final int darkArrowColor;
    private final int lightArrowColor;

    @SuppressLint("InflateParams")
    public BasePopupWindow(Context context) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Resources res = context.getResources();

        contentView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.widget_base_popup, null);

        arrowSize = res.getDimensionPixelSize(R.dimen.popup_window_arrow_size);
        darkArrowColor = ResUtils.resolveColor(context, R.attr.colorPopupActionsBackground);
        lightArrowColor = ResUtils.resolveColor(context, R.attr.colorPopupBackground);

        onCreateView(contentView);
        setContentView(contentView);

        setOutsideTouchable(true);
        setFocusable(true);
        setElevation(res.getDimensionPixelSize(R.dimen.popup_window_elevation));

        contentView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                boolean arrowTop = contentView.getChildAt(0).getId() == R.id.arrow;
                Rect rect = new Rect(view.getPaddingLeft(),
                        0,
                        contentView.getWidth() - contentView.getPaddingRight(),
                        contentView.getHeight() - arrowSize);
                if (arrowTop) {
                    rect.offset(0, arrowSize);
                }
                float radius = ResUtils.px2dp(contentView.getContext(), 8);
                outline.setRoundRect(rect, radius);
            }
        });
    }

    protected abstract void onCreateView(ViewGroup parent);

    @SuppressLint("RtlHardcoded")
    public void showAtAnchor(View anchorView, Rect bounds) {
        int maxWidth = (int) (bounds.width() * MAX_WIDTH_FACTOR);
        int maxHeight = (int) (bounds.height() * MAX_HEIGHT_FACTOR);
        contentView.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST));

        int[] tmp = new int[2];
        anchorView.getLocationOnScreen(tmp);

        int contentWidth = contentView.getMeasuredWidth();
        int anchorWidth = anchorView.getMeasuredWidth();
        int xOffset = (anchorWidth - contentWidth) / 2;
        boolean beyondLeft = false, beyondRight = false;
        if (tmp[0] + xOffset < 0) {
            xOffset = 0;
            beyondLeft = true;
        } else {
            int contentRight = tmp[0] + xOffset + contentWidth;
            if (contentRight > bounds.right) {
                xOffset -= (contentRight - bounds.right);
                beyondRight = true;
            }
        }

        int anchorHeight = anchorView.getMeasuredHeight();
        int additionalVerticalOffset = (anchorView.getPaddingTop() + anchorView.getPaddingTop()) / 2;
        int yOffset = -additionalVerticalOffset;

        int arrowCenter;
        if (beyondLeft) {
            arrowCenter = anchorWidth / 2 - contentView.getPaddingLeft();
        } else if (beyondRight) {
            arrowCenter = Math.abs(xOffset) + anchorWidth / 2 - contentView.getPaddingRight();
        } else {
            arrowCenter = contentWidth / 2 - contentView.getPaddingLeft();
        }
        View arrowView = new View(contentView.getContext());
        arrowView.setId(R.id.arrow);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(arrowSize, arrowSize);
        lp.setMarginStart(arrowCenter - arrowSize / 2);
        int anchorViewBottom = tmp[1] + anchorHeight;
        int contentHeight = contentView.getMeasuredHeight() + arrowSize;
        if (bounds.bottom - anchorViewBottom < contentHeight + additionalVerticalOffset) {
            yOffset = -contentHeight - anchorHeight + additionalVerticalOffset;
            arrowView.setBackground(new ArrowDrawable(lightArrowColor, arrowSize, true));
            contentView.addView(arrowView, lp);
        } else {
            int color = isDarkArrow() ? darkArrowColor : lightArrowColor;
            arrowView.setBackground(new ArrowDrawable(color, arrowSize, false));
            contentView.addView(arrowView, 0, lp);
        }

        setWidth(contentWidth);
        showAsDropDown(anchorView, xOffset, yOffset, Gravity.TOP | Gravity.LEFT);
    }

    protected abstract boolean isDarkArrow();

    private static class ArrowDrawable extends Drawable {

        private static final float HEIGHT_FACTOR = .66f;

        private final Paint paint;
        private final Path path;

        private ArrowDrawable(@ColorInt int color, @Px int size, boolean pointDown) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            path = new Path();
            int height = (int) (size * HEIGHT_FACTOR);
            if (pointDown) {
                path.moveTo(0, 0);
                path.lineTo(size, 0);
                path.lineTo(size / 2f, height);
            } else {
                path.moveTo(size / 2f, size - height);
                path.lineTo(size, size);
                path.lineTo(0, size);
            }
            path.close();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }
}
