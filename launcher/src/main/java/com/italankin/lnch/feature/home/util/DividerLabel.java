package com.italankin.lnch.feature.home.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class DividerLabel extends AppCompatTextView {
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DividerLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setHorizontallyScrolling(false);
        linePaint.setStrokeWidth(getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // TextView may scroll its content internally. Draw the rules in viewport coordinates.
        int saveCount = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        drawLines(canvas);
        canvas.restoreToCount(saveCount);
    }

    private void drawLines(Canvas canvas) {
        linePaint.setColor(getCurrentTextColor());
        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float centerY = getPaddingTop() + (getHeight() - getPaddingTop() - getPaddingBottom()) / 2f;
        Layout layout = getLayout();
        if (getText().length() == 0 || layout == null) {
            canvas.drawLine(left, centerY, right, centerY, linePaint);
            return;
        }
        float gap = 8 * getResources().getDisplayMetrics().density;
        float textWidth = layout.getLineWidth(0);
        float centerX = (left + right) / 2f;
        float lineEnd = centerX - textWidth / 2f - gap;
        float lineStart = centerX + textWidth / 2f + gap;
        if (lineEnd > left) {
            canvas.drawLine(left, centerY, lineEnd, centerY, linePaint);
        }
        if (lineStart < right) {
            canvas.drawLine(lineStart, centerY, right, centerY, linePaint);
        }
    }
}
