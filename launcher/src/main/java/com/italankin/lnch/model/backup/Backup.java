package com.italankin.lnch.model.backup;

import com.google.gson.annotations.SerializedName;
import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;
import java.util.Map;

class Backup {

    @SerializedName("packages")
    public final List<Descriptor> descriptors;

    @SerializedName("preferences")
    public final Map<String, Object> preferences;

    Backup(List<Descriptor> descriptors, Map<String, Object> preferences) {
        this.descriptors = descriptors;
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "Backup{" +
                "descriptors=" + descriptors +
                ", preferences=" + preferences +
                '}';
    }
}
