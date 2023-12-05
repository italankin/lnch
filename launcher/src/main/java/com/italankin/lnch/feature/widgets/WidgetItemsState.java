package com.italankin.lnch.feature.widgets;

import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.NoWidgetsItem;
import com.italankin.lnch.feature.widgets.model.WidgetAdapterItem;
import com.italankin.lnch.util.ListUtils;

import java.util.*;

class WidgetItemsState {

    private final NoWidgetsItem noWidgetsItem = new NoWidgetsItem();
    private final List<AppWidget> appWidgets = new ArrayList<>();
    private boolean resizeMode = false;
    private final List<WidgetAdapterItem> items = new ArrayList<>();

    public void setResizeMode(boolean resizeMode, boolean forceResize) {
        this.resizeMode = resizeMode;
        for (AppWidget appWidget : appWidgets) {
            appWidget.resizeMode = resizeMode;
            appWidget.forceResize = forceResize;
        }
    }

    public void addWidget(AppWidget appWidget) {
        appWidget.resizeMode = resizeMode;
        appWidgets.add(appWidget);
        rebuild();
    }

    public void clearWidgets() {
        appWidgets.clear();
        rebuild();
    }

    public void removeWidgetById(int appWidgetId) {
        Iterator<AppWidget> iterator = appWidgets.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().appWidgetId == appWidgetId) {
                iterator.remove();
                rebuild();
                break;
            }
        }
    }

    public void swapWidgets(int from, int to) {
        AppWidget widgetFrom = (AppWidget) items.get(from);
        AppWidget widgetTo = (AppWidget) items.get(to);
        int fromIndex = appWidgets.indexOf(widgetFrom);
        int toIndex = appWidgets.indexOf(widgetTo);
        ListUtils.move(appWidgets, fromIndex, toIndex);
        ListUtils.move(items, from, to);
    }

    public boolean isResizeMode() {
        return resizeMode;
    }

    public List<WidgetAdapterItem> getItems() {
        return items;
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
        rebuild();
    }

    public List<Integer> getWidgetsOrder() {
        List<Integer> result = new ArrayList<>(appWidgets.size());
        for (AppWidget appWidget : appWidgets) {
            result.add(appWidget.appWidgetId);
        }
        return result;
    }

    private void rebuild() {
        int size = appWidgets.size();
        items.clear();
        if (size > 0) {
            items.addAll(appWidgets);
        } else {
            items.add(noWidgetsItem);
        }
    }
}
