package com.italankin.lnch.model.repository.descriptor;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.json.GsonDescriptorStore;
import com.italankin.lnch.model.repository.descriptor.json.OldDescriptorStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VersioningDescriptorStore implements DescriptorStore {

    private final DescriptorStore descriptorStore;
    private final List<DescriptorStore> repositories = new ArrayList<>(1);

    public VersioningDescriptorStore(GsonBuilder gsonBuilder) {
        descriptorStore = new GsonDescriptorStore(gsonBuilder);
        repositories.add(descriptorStore);
        repositories.add(new OldDescriptorStore());
    }

    @Override
    public List<Descriptor> read(File packagesFile) {
        for (DescriptorStore repository : repositories) {
            List<Descriptor> descriptors = repository.read(packagesFile);
            if (descriptors != null) {
                return descriptors;
            }
        }
        return null;
    }

    @Override
    public void write(File packagesFile, List<Descriptor> items) {
        descriptorStore.write(packagesFile, items);
    }
}
