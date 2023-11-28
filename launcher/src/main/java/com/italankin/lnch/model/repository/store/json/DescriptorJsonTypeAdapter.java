package com.italankin.lnch.model.repository.store.json;

import androidx.annotation.Keep;
import com.google.gson.*;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.store.json.model.*;
import timber.log.Timber;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Keep
public class DescriptorJsonTypeAdapter implements JsonDeserializer<Descriptor>, JsonSerializer<Descriptor> {

    private static final Map<String, Class<? extends DescriptorJson>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(AppDescriptorJson.TYPE, AppDescriptorJson.class);
        MAPPING.put(FolderDescriptorJson.TYPE, FolderDescriptorJson.class);
        MAPPING.put(FolderDescriptorJson.OLD_TYPE, FolderDescriptorJson.class);
        MAPPING.put(PinnedShortcutDescriptorJson.TYPE, PinnedShortcutDescriptorJson.class);
        MAPPING.put(DeepShortcutDescriptorJson.TYPE, DeepShortcutDescriptorJson.class);
        MAPPING.put(IntentDescriptorJson.TYPE, IntentDescriptorJson.class);
    }

    private final DescriptorJsonConverter converter = new DescriptorJsonConverter();

    @Override
    public Descriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            if (json == JsonNull.INSTANCE) {
                return null;
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
        } catch (Exception e) {
            Timber.e(e, "deserialize:");
            return null;
        }
    }

    @Override
    public JsonElement serialize(Descriptor src, Type typeOfSrc, JsonSerializationContext context) {
        DescriptorJson descriptorJson = converter.toJson(src);
        return context.serialize(descriptorJson);
    }
}
