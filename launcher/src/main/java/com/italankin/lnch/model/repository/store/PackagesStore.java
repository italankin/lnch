package com.italankin.lnch.model.repository.store;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public interface PackagesStore {

    @Nullable
    File input();

    @NonNull
    File output();

    void clear();
}
