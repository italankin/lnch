package com.italankin.lnch.model.repository.descriptors;

import java.io.File;
import java.util.List;

public interface DescriptorRepository {

    List<Descriptor> read(File packagesFile);

    void write(File packagesFile, List<Descriptor> items);

}
