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
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.italankin.lnch.R;
import timber.log.Timber;

public class LauncherAppWidgetHostView extends AppWidgetHostView {

    private static final int[] LOC = new int[2];

    private View scrollableView;
    private float touchStartX;
    private float touchStartY;

    LauncherAppWidgetHostView(Context context) {
        super(context);
    }

    @Override
    public void setAppWidget(int appWidgetId, AppWidgetProviderInfo info) {
        super.setAppWidget(appWidgetId, info);
        int p = getResources().getDimensionPixelSize(R.dimen.widget_padding);
        setPadding(p, p, p, p);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (scrollableView != null) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                touchStartX = ev.getX();
                touchStartY = ev.getY();
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                if (!isInBounds(scrollableView, ev.getRawX(), ev.getRawY())) {
                    // gesture is not within scrollable view bounds, intercept events
                    return true;
                }
                float dx = touchStartX - ev.getX();
                float dy = touchStartY - ev.getY();
                if (Math.abs(dx) > Math.abs(dy)) {
                    // horizontal scroll, do not pass to widget
                    return true;
                }
                int d = (int) ((int) dy == 0 ? Math.signum(dy) : dy);
                if (scrollableView.canScrollVertically(d)) {
                    // do not intercept further events, widget can scroll
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        }
        return false;
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
        scrollableView = checkScrollableRecursively(this);
    }

    private static boolean isInBounds(View view, float x, float y) {
        view.getLocationOnScreen(LOC);
        return x >= LOC[0] && y >= LOC[1] && x <= (LOC[0] + view.getWidth()) && y <= (LOC[1] + view.getHeight());
    }

    private View checkScrollableRecursively(ViewGroup viewGroup) {
        if (viewGroup.canScrollVertically(1) || viewGroup.canScrollVertically(-1)) {
            return viewGroup;
        } else {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof ViewGroup) {
                    View scrollableView = checkScrollableRecursively((ViewGroup) child);
                    if (scrollableView != null) {
                        return scrollableView;
                    }
                }
            }
        }
        return null;
    }
}
