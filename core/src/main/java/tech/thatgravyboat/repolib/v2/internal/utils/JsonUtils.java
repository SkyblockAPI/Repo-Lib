package tech.thatgravyboat.repolib.v2.internal.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtils {
    public static final Gson GSON = new GsonBuilder().create();

    public static JsonObject parseObject(String element) {
        return GSON.fromJson(element, JsonObject.class);
    }

    private JsonUtils() {}

    public static String type(JsonElement element) {
        return switch (element) {
            case JsonObject o -> "object";
            case JsonArray a -> "array";
            case JsonPrimitive p -> {
                if (p.isBoolean()) {
                    yield "boolean";
                } else if (p.isString()) {
                    yield "string";
                } else if (p.isNumber()) {
                    yield "number";
                } else {
                    yield "unknown";
                }
            }
            default -> "unknown";
        };
    }

    public static String format(String field, String expected, JsonElement actual) {
        return "Expected %s to be of type %s but got %s (%s)".formatted(field, expected, actual, type(actual));
    }

    public static boolean isString(JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isString();
    }

    public static boolean isBoolean(JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isBoolean();
    }

    public static boolean isNumber(JsonElement element) {
        return element instanceof JsonPrimitive primitive && primitive.isNumber();
    }

}
