package com.italankin.lnch.feature.home.apps;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class EmptySpaceGestureHandler extends RecyclerView.SimpleOnItemTouchListener {

    private final GestureDetector gestureDetector;
    private boolean startedOnEmptySpace;

    EmptySpaceGestureHandler(Context context, Listener listener) {
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
            startedOnEmptySpace = recyclerView.findChildViewUnder(event.getX(), event.getY()) == null;
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

    interface Listener {

        void onTap(@NonNull MotionEvent event);

        void onLongTap(@NonNull MotionEvent event);

        void onDoubleTap(@NonNull MotionEvent event);
    }
}
