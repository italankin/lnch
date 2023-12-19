package com.italankin.lnch.feature.home.adapter.shimmer;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.italankin.lnch.R;

import java.util.Random;

public class ShimmerDrawable extends Drawable {

    private final Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect bounds = new Rect();
    private final RectF frameBounds = new RectF();
    private final float[] intervals = new float[2];
    private final float radius;
    private final float phaseAdvance;
    private int padding;

    private float phase;

    public ShimmerDrawable(Context context) {
        dashPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.shimmer_stroke_width));
        int color = Color.WHITE;
        dashPaint.setColor(color);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setStrokeCap(Paint.Cap.ROUND);
        dashPaint.setStrokeJoin(Paint.Join.ROUND);
        bgPaint.setColor(0x80ffffff & color);
        bgPaint.setStyle(Paint.Style.FILL);
        radius = context.getResources().getDimension(R.dimen.shimmer_round_radius);
        phaseAdvance = context.getResources().getDimension(R.dimen.shimmer_phase_advance);
    }

    public void setPadding(int padding) {
        if (this.padding != padding) {
            this.padding = padding;
            update();
            invalidateSelf();
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(frameBounds, radius, radius, bgPaint);
        canvas.drawRoundRect(frameBounds, radius, radius, dashPaint);
        advancePhase();
    }

    @Override
    public void setAlpha(int alpha) {
        dashPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        dashPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        this.bounds.set(bounds);
        update();
    }

    private void update() {
        frameBounds.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        frameBounds.inset(padding, padding);

        intervals[0] = frameBounds.width() / 2f;
        intervals[1] = frameBounds.width() + frameBounds.height() + intervals[0];
        phase = new Random().nextFloat() * intervals[1];
        dashPaint.setPathEffect(new DashPathEffect(intervals, phase));
    }

    private void advancePhase() {
        phase -= phaseAdvance;
        dashPaint.setPathEffect(new DashPathEffect(intervals, phase));
        invalidateSelf();
    }
}
