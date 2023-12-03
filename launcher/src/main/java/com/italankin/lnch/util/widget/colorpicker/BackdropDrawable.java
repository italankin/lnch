package com.italankin.lnch.util.widget.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import com.italankin.lnch.R;

/**
 * Draws transparency grid and color on top of it
 */
public class BackdropDrawable extends ColorDrawable {
    private final int gridSize;
    private final Paint paint = new Paint();

    public BackdropDrawable(Context context) {
        this(context.getResources().getDimensionPixelSize(R.dimen.backdrop_grid_size));
    }

    public BackdropDrawable(@Px int gridSize) {
        if (gridSize <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        this.gridSize = gridSize;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int bw = getBounds().width();
        int width = (bw + gridSize - bw % gridSize) / gridSize;
        int bh = getBounds().height();
        int height = (bh + gridSize - bh % gridSize) / gridSize;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                paint.setColor(w % 2 == h % 2 ? Color.WHITE : Color.LTGRAY);
                canvas.drawRect(gridSize * w, gridSize * h,
                        gridSize * w + gridSize, gridSize * h + gridSize,
                        paint);
            }
        }
        super.draw(canvas);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
