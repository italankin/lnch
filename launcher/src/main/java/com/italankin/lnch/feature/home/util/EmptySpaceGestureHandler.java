package com.italankin.lnch.feature.home.util;

import android.content.Context;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EmptySpaceGestureHandler extends RecyclerView.SimpleOnItemTouchListener {

    private final GestureDetector gestureDetector;
    private boolean startedOnEmptySpace;

    public EmptySpaceGestureHandler(Context context, Listener listener) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent event) {
                listener.onTap(event);
                return true;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent event) {
                listener.onLongTap(event);
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent event) {
                listener.onDoubleTap(event);
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            startedOnEmptySpace = recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE &&
                    recyclerView.findChildViewUnder(event.getX(), event.getY()) == null;
            if (!startedOnEmptySpace) {
                cancelGesture();
            }
        }
        if (startedOnEmptySpace) {
            gestureDetector.onTouchEvent(event);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP ||
                event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            startedOnEmptySpace = false;
        }
        return false;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            startedOnEmptySpace = false;
            cancelGesture();
        }
    }

    private void cancelGesture() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        gestureDetector.onTouchEvent(cancel);
        cancel.recycle();
    }

    public interface Listener {

        void onTap(@NonNull MotionEvent event);

        void onLongTap(@NonNull MotionEvent event);

        void onDoubleTap(@NonNull MotionEvent event);
    }
}
