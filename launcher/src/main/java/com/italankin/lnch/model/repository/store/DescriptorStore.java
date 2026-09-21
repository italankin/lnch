package com.italankin.lnch.model.repository.store;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface DescriptorStore {

    List<Descriptor> read(InputStream in);

    /** Writes and flushes descriptors without closing the caller-owned output stream. */
    void write(OutputStream out, List<Descriptor> items);
}
