package com.sunrisedental.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>)
                    (value, type, context) -> new com.google.gson.JsonPrimitive(value.toString()))
            .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>)
                    (json, type, context) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class, (com.google.gson.JsonSerializer<LocalTime>)
                    (value, type, context) -> new com.google.gson.JsonPrimitive(value.toString()))
            .registerTypeAdapter(LocalTime.class, (com.google.gson.JsonDeserializer<LocalTime>)
                    (json, type, context) -> LocalTime.parse(json.getAsString()))
            .create();

    private JsonUtil() { }

    public static String toJson(Object value) {
        return GSON.toJson(value);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return GSON.fromJson(json, type);
    }

        public static <T> T fromJson(com.google.gson.JsonElement json, Class<T> type) {
                return GSON.fromJson(json, type);
        }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

        public static <T> List<T> fromJsonList(String json, Class<T> type) {
                return GSON.fromJson(json, com.google.gson.reflect.TypeToken.getParameterized(List.class, type).getType());
        }
}