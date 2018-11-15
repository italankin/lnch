package com.italankin.lnch.model.repository.store.json;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.repository.store.DescriptorStore;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@Deprecated
public class OldDescriptorStore implements DescriptorStore {
    @Override
    public List<Descriptor> read(File packagesFile) {
        try {
            List<Descriptor> descriptors = new ArrayList<>();
            JsonReader jsonReader = new Gson().newJsonReader(new FileReader(packagesFile));
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                int color = 0;
                Integer customColor = null;
                boolean hidden = false;
                String id = null;
                String label = null;
                String customLabel = null;
                String componentName = null;
                long versionCode = 0;

                jsonReader.beginObject();
                JsonToken token;
                while ((token = jsonReader.peek()) != JsonToken.END_OBJECT) {
                    switch (token) {
                        case NAME:
                            String name = jsonReader.nextName();
                            switch (name) {
                                case "color":
                                    color = jsonReader.nextInt();
                                    break;
                                case "hidden":
                                    hidden = jsonReader.nextBoolean();
                                    break;
                                case "id":
                                    id = jsonReader.nextString();
                                    break;
                                case "label":
                                    label = jsonReader.nextString();
                                    break;
                                case "versionCode":
                                    versionCode = jsonReader.nextLong();
                                    break;
                                case "customLabel":
                                    customLabel = jsonReader.nextString();
                                    break;
                                case "customColor":
                                    customColor = jsonReader.nextInt();
                                    break;
                                case "componentName":
                                    componentName = jsonReader.nextString();
                                    break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }
                jsonReader.endObject();

                Descriptor descriptor;
                if ("com.italankin.lnch.separator".equals(id)) {
                    GroupDescriptor groupDescriptor = new GroupDescriptor(label, color);
                    groupDescriptor.customColor = customColor;
                    groupDescriptor.customLabel = customLabel;
                    descriptor = groupDescriptor;
                } else {
                    AppDescriptor appDescriptor = new AppDescriptor(id);
                    appDescriptor.color = color;
                    appDescriptor.customColor = customColor;
                    appDescriptor.versionCode = versionCode;
                    appDescriptor.label = label;
                    appDescriptor.customLabel = customLabel;
                    appDescriptor.hidden = hidden;
                    appDescriptor.componentName = componentName;
                    descriptor = appDescriptor;
                }
                descriptors.add(descriptor);
            }
            jsonReader.endArray();
            return descriptors;
        } catch (Exception e) {
            Timber.e(e, "read:");
            return null;
        }
    }

    @Override
    public void write(File packagesFile, List<Descriptor> items) {
        throw new UnsupportedOperationException();
    }
}
