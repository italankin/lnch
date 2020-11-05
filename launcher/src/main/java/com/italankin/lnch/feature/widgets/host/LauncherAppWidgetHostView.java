/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.italankin.lnch.feature.widgets.host;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RemoteViews;

import com.italankin.lnch.feature.widgets.util.CheckLongPressHelper;

import timber.log.Timber;

public class LauncherAppWidgetHostView extends AppWidgetHostView implements View.OnLongClickListener {

    private final CheckLongPressHelper mLongPressHelper = new CheckLongPressHelper(this, this);

    private boolean mIsScrollable;
    private float mSlop;

    private int maxWidth;
    private int maxHeight;

    private float mStartX;
    private float mStartY;

    LauncherAppWidgetHostView(Context context) {
        super(context);
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        super.setMinimumWidth(minWidth);
    }

    public void setDimensionsConstraints(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        setMinimumWidth(minWidth);
        setMinimumHeight(minHeight);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        requestLayout();
        invalidate();
    }

    @Override
    public boolean onLongClick(View view) {
        if (mIsScrollable) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        view.performLongClick();
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Just in case the previous long press hasn't been cleared, we make sure to start fresh
        // on touch down.
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mLongPressHelper.cancelLongPress();
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mStartY = ev.getY();
                if (mIsScrollable) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                mLongPressHelper.postCheckForLongPress();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (!pointInView(this, x, y, mSlop) || Math.abs(x - mStartX) >= mSlop || Math.abs(y - mStartY) >= mSlop) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // If the widget does not handle touch, then cancel
        // long press when we release the touch
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getX();
                float y = ev.getY();
                if (!pointInView(this, x, y, mSlop) || Math.abs(x - mStartX) >= mSlop || Math.abs(y - mStartY) >= mSlop) {
                    mLongPressHelper.cancelLongPress();
                }
                break;
        }
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                constrain(widthMeasureSpec, getMinimumWidth(), maxWidth),
                constrain(heightMeasureSpec, getMinimumHeight(), maxHeight)
        );
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    protected View getErrorView() {
        // TODO
        return super.getErrorView();
    }

    public void switchToErrorView() {
        // Update the widget with 0 Layout id, to reset the view to error view.
        updateAppWidget(new RemoteViews(getAppWidgetInfo().provider.getPackageName(), 0));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        try {
            super.onLayout(changed, left, top, right, bottom);
        } catch (RuntimeException e) {
            Timber.e(e);
            post(this::switchToErrorView);
        }
        mIsScrollable = checkScrollableRecursively(this);
    }

    private static int constrain(int spec, int min, int max) {
        if (max <= 0) {
            return spec;
        }
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(min, MeasureSpec.AT_MOST);
            case MeasureSpec.AT_MOST:
                if (size > max) {
                    return MeasureSpec.makeMeasureSpec(max, MeasureSpec.AT_MOST);
                }
                break;
            case MeasureSpec.EXACTLY:
                if (size > max) {
                    return MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
                }
                break;
        }
        return spec;
    }

    private boolean checkScrollableRecursively(ViewGroup viewGroup) {
        if (viewGroup instanceof AdapterView) {
            return true;
        } else {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    if (checkScrollableRecursively((ViewGroup) child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) && localY < (v.getHeight() + slop);
    }
}
