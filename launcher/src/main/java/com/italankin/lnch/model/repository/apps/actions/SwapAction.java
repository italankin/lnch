package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.bean.AppItem;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.Collections;
import java.util.List;

public class SwapAction implements AppsRepository.Editor.Action {
    private final int from;
    private final int to;

    public SwapAction(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void apply(List<AppItem> items) {
        swap(items, from, to);
    }

    public static <T> void swap(List<T> items, int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
    }
}
