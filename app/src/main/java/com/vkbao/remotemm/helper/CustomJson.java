package com.vkbao.remotemm.helper;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Map;

public class CustomJson implements JsonDeserializer<Map<String, Object>> {

    @Override
    public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return read(json.getAsJsonObject());
    }

    private Map<String, Object> read(JsonObject jsonObject) {
        Map<String, Object> map = new java.util.HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                map.put(key, read(value.getAsJsonObject()));
            } else if (value.isJsonArray()) {
                map.put(key, readArray(value.getAsJsonArray()));
            } else if (value.isJsonPrimitive()) {
                map.put(key, readPrimitive(value.getAsJsonPrimitive()));
            } else if (value.isJsonNull()) {
                map.put(key, null);
            }
        }
        return map;
    }

    private Object readArray(JsonArray array) {
        java.util.List<Object> list = new java.util.ArrayList<>();
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                list.add(read(element.getAsJsonObject()));
            } else if (element.isJsonArray()) {
                list.add(readArray(element.getAsJsonArray()));
            } else if (element.isJsonPrimitive()) {
                list.add(readPrimitive(element.getAsJsonPrimitive()));
            } else if (element.isJsonNull()) {
                list.add(null);
            }
        }
        return list;
    }

    private Object readPrimitive(JsonPrimitive json) {
        if (json.isBoolean()) {
            return json.getAsBoolean();
        } else if (json.isString()) {
            return json.getAsString();
        } else {
            try {
                long longValue = json.getAsLong();
                if (longValue == (int) longValue) {
                    return (int) longValue;
                } else {
                    return longValue;
                }
            } catch (NumberFormatException e) {
                return json.getAsDouble();
            }
        }
    }
}
