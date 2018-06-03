package com.italankin.lnch.util.rx;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Function;

public class ListMapper<T, R> implements Function<List<T>, List<R>> {

    public static <T, R> Function<List<T>, List<R>> create(@NonNull Function<T, R> mapper) {
        return new ListMapper<>(mapper);
    }

    private final Function<T, R> mapper;

    private ListMapper(@NonNull Function<T, R> mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<R> apply(List<T> data) throws Exception {
        List<R> list = new ArrayList<>(data.size());
        for (T entry : data) {
            list.add(mapper.apply(entry));
        }
        return list;
    }
}
