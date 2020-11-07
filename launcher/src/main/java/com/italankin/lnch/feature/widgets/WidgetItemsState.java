package com.italankin.lnch.feature.widgets;

import com.italankin.lnch.feature.widgets.model.AddWidgetItem;
import com.italankin.lnch.feature.widgets.model.AppWidget;
import com.italankin.lnch.feature.widgets.model.NoWidgetsItem;
import com.italankin.lnch.feature.widgets.model.WidgetAdapterItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class WidgetItemsState {

    private final AddWidgetItem addWidgetItem = new AddWidgetItem();
    private final NoWidgetsItem noWidgetsItem = new NoWidgetsItem();
    private final List<AppWidget> appWidgets = new ArrayList<>();

    public void addWidget(AppWidget appWidget) {
        appWidgets.add(appWidget);
    }

    public void removeWidget(AppWidget appWidget) {
        appWidgets.remove(appWidget);
    }

    public void clearWidgets() {
        appWidgets.clear();
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

    public List<WidgetAdapterItem> getItems() {
        int size = appWidgets.size();
        ArrayList<WidgetAdapterItem> items = new ArrayList<>(size + 1);
        items.add(addWidgetItem);
        if (size > 0) {
            items.addAll(appWidgets);
        } else {
            items.add(noWidgetsItem);
        }
        return items;
    }
}
