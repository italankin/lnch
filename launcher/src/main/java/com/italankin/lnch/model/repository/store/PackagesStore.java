package com.italankin.lnch.model.repository.store;

import java.io.InputStream;
import java.io.OutputStream;

public interface PackagesStore {

    InputStream input();

    OutputStream output();

    void clear();
}
