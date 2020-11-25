package com.italankin.lnch.model.repository.store.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.store.json.model.AppDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.DescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.GroupDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.IntentDescriptorJson;
import com.italankin.lnch.model.repository.store.json.model.PinnedShortcutDescriptorJson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Keep;

@Keep
public class DescriptorJsonTypeAdapter implements JsonDeserializer<Descriptor>, JsonSerializer<Descriptor> {

    private static final Map<String, Class<? extends DescriptorJson>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(AppDescriptorJson.TYPE, AppDescriptorJson.class);
        MAPPING.put(GroupDescriptorJson.TYPE, GroupDescriptorJson.class);
        MAPPING.put(PinnedShortcutDescriptorJson.TYPE, PinnedShortcutDescriptorJson.class);
        MAPPING.put(DeepShortcutDescriptorJson.TYPE, DeepShortcutDescriptorJson.class);
        MAPPING.put(IntentDescriptorJson.TYPE, IntentDescriptorJson.class);
    }

    private final DescriptorJsonConverter converter = new DescriptorJsonConverter();

    @Override
    public Descriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == JsonNull.INSTANCE) {
            throw new NullPointerException();
        }
        String type = ((JsonObject) json)
                .getAsJsonPrimitive(DescriptorJson.PROPERTY_TYPE)
                .getAsString();
        Class<? extends DescriptorJson> modelClass = MAPPING.get(type);
        if (modelClass == null) {
            throw new IllegalArgumentException("Unknown JsonModel type: " + type);
        }
        DescriptorJson descriptorJson = context.deserialize(json, modelClass);
        return converter.fromJson(descriptorJson);
    }

    @Override
    public JsonElement serialize(Descriptor src, Type typeOfSrc, JsonSerializationContext context) {
        DescriptorJson descriptorJson = converter.toJson(src);
        return context.serialize(descriptorJson);
    }
}
