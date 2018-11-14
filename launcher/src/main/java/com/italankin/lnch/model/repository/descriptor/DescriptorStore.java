package com.italankin.lnch.model.repository.descriptor;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.io.File;
import java.util.List;

public interface DescriptorStore {

    List<Descriptor> read(File packagesFile);

    void write(File packagesFile, List<Descriptor> items);

}
