package com.italankin.lnch.model.repository.descriptors.json;

import com.google.gson.GsonBuilder;
import com.italankin.lnch.model.repository.descriptors.Descriptor;
import com.italankin.lnch.model.repository.descriptors.DescriptorRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VersioningDescriptorRepository implements DescriptorRepository {

    private final DescriptorRepository descriptorRepository;
    private final List<DescriptorRepository> repositories = new ArrayList<>(1);

    public VersioningDescriptorRepository(GsonBuilder gsonBuilder) {
        descriptorRepository = new GsonDescriptorRepository(gsonBuilder);
        repositories.add(descriptorRepository);
        repositories.add(new OldDescriptorRepository());
    }

    @Override
    public List<Descriptor> read(File packagesFile) {
        for (DescriptorRepository repository : repositories) {
            List<Descriptor> descriptors = repository.read(packagesFile);
            if (descriptors != null) {
                return descriptors;
            }
        }
        return null;
    }

    @Override
    public void write(File packagesFile, List<Descriptor> items) {
        descriptorRepository.write(packagesFile, items);
    }
}
