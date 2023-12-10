package com.italankin.lnch.feature.widgets;

import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.util.ListUtils;

import java.util.*;

class WidgetItemsState {

    private final List<AppWidget> appWidgets = new ArrayList<>();
    private boolean resizeMode = false;

    public void setResizeMode(boolean resizeMode, boolean forceResize) {
        this.resizeMode = resizeMode;
        for (AppWidget appWidget : appWidgets) {
            appWidget.resizeMode = resizeMode;
            appWidget.forceResize = resizeMode && forceResize;
        }
    }

    public void addWidget(AppWidget appWidget) {
        appWidget.resizeMode = resizeMode;
        appWidgets.add(appWidget);
    }

    public void clearWidgets() {
        appWidgets.clear();
        resizeMode = false;
    }

    public void removeWidgetById(int appWidgetId) {
        Iterator<AppWidget> iterator = appWidgets.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().appWidgetId == appWidgetId) {
                iterator.remove();
                break;
            }
        }
    }

    public void swapWidgets(int from, int to) {
        ListUtils.move(appWidgets, from, to);
    }

    public boolean isResizeMode() {
        return resizeMode;
    }

    public List<AppWidget> getItems() {
        return appWidgets;
    }

    public void setWidgetsOrder(List<Integer> order) {
        if (order.isEmpty()) {
            return;
        }
        Map<Integer, AppWidget> map = new LinkedHashMap<>(appWidgets.size());
        for (AppWidget appWidget : appWidgets) {
            map.put(appWidget.appWidgetId, appWidget);
        }
        appWidgets.clear();
        for (Integer id : order) {
            AppWidget appWidget = map.remove(id);
            if (appWidget != null) {
                appWidgets.add(appWidget);
            }
        }
        appWidgets.addAll(map.values());
    }
}
