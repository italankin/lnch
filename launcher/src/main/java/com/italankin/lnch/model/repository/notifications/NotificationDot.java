package com.italankin.lnch.model.repository.notifications;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NotificationDot {

    final Set<Integer> ids;

    NotificationDot(Integer... ids) {
        this(new HashSet<>(Arrays.asList(ids)));
    }

    NotificationDot(Set<Integer> ids) {
        this.ids = Collections.unmodifiableSet(ids);
    }

    public int getCount() {
        return ids.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NotificationDot that = (NotificationDot) o;
        return ids.equals(that.ids);
    }

    @Override
    public int hashCode() {
        return ids.hashCode();
    }

    @Override
    public String toString() {
        return "NotificationBadge{count=" + getCount() + "}";
    }
}
