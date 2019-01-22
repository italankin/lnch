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
import com.italankin.lnch.model.repository.store.json.model.AppModel;
import com.italankin.lnch.model.repository.store.json.model.DeepShortcutModel;
import com.italankin.lnch.model.repository.store.json.model.GroupModel;
import com.italankin.lnch.model.repository.store.json.model.IntentModel;
import com.italankin.lnch.model.repository.store.json.model.JsonModel;
import com.italankin.lnch.model.repository.store.json.model.PinnedShortcutModel;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class DescriptorJsonConverter implements JsonDeserializer<Descriptor>, JsonSerializer<Descriptor> {

    private static final Map<String, Class<? extends JsonModel>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(AppModel.TYPE, AppModel.class);
        MAPPING.put(GroupModel.TYPE, GroupModel.class);
        MAPPING.put(PinnedShortcutModel.TYPE, PinnedShortcutModel.class);
        MAPPING.put(DeepShortcutModel.TYPE, DeepShortcutModel.class);
        MAPPING.put(IntentModel.TYPE, IntentModel.class);
    }

    private final JsonModelConverter converter;

    DescriptorJsonConverter() {
        this(new JsonModelConverter());
    }

    DescriptorJsonConverter(JsonModelConverter converter) {
        this.converter = converter;
    }

    @Override
    public Descriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == JsonNull.INSTANCE) {
            throw new NullPointerException();
        }
        String type = ((JsonObject) json)
                .getAsJsonPrimitive(JsonModel.PROPERTY_TYPE)
                .getAsString();
        Class<? extends JsonModel> modelClass = MAPPING.get(type);
        if (modelClass == null) {
            throw new IllegalArgumentException("Unknown JsonModel type: " + type);
        }
        JsonModel jsonModel = context.deserialize(json, modelClass);
        return converter.fromJson(jsonModel);
    }

    @Override
    public JsonElement serialize(Descriptor src, Type typeOfSrc, JsonSerializationContext context) {
        JsonModel jsonModel = converter.toJson(src);
        return context.serialize(jsonModel);
    }
}
