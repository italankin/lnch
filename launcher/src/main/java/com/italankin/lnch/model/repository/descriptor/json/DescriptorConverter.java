package com.italankin.lnch.model.repository.descriptor.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.GroupDescriptor;
import com.italankin.lnch.model.descriptor.impl.ShortcutDescriptor;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DescriptorConverter implements JsonDeserializer<Descriptor>, JsonSerializer<Descriptor> {
    private static final String PROP_TYPE = "type";
    private static final String TYPE_APP = "app";
    private static final String TYPE_GROUP = "group";
    private static final String TYPE_SHORTCUT = "shortcut";

    private static final Map<String, Class<? extends Descriptor>> MAPPING;

    static {
        MAPPING = new HashMap<>();
        MAPPING.put(TYPE_APP, AppDescriptor.class);
        MAPPING.put(TYPE_GROUP, GroupDescriptor.class);
        MAPPING.put(TYPE_SHORTCUT, ShortcutDescriptor.class);
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public Descriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == JsonNull.INSTANCE) {
            return null;
        }
        JsonPrimitive typeObject = ((JsonObject) json).getAsJsonPrimitive(PROP_TYPE);
        Class<? extends Descriptor> type = MAPPING.get(typeObject.getAsString());
        if (type == null) {
            return null;
        }
        return gson.fromJson(json, type);
    }

    @Override
    public JsonElement serialize(Descriptor src, Type typeOfSrc, JsonSerializationContext context) {
        String type = getType(src);
        if (type == null) {
            return JsonNull.INSTANCE;
        }
        JsonObject element = (JsonObject) gson.toJsonTree(src);
        element.addProperty(PROP_TYPE, type);
        return element;
    }

    private String getType(Descriptor src) {
        Class<? extends Descriptor> classOfSrc = src.getClass();
        for (Map.Entry<String, Class<? extends Descriptor>> entry : MAPPING.entrySet()) {
            if (entry.getValue() == classOfSrc) {
                return entry.getKey();
            }
        }
        return null;
    }
}
