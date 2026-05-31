package tech.thatgravyboat.repolib.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ApiStatus.Internal
public class JsonHelper {

    public static <T> List<T> getList(@NotNull JsonElement array, Function<JsonElement, T> mapper) {
        List<T> list = new ArrayList<>();
        for (JsonElement element : array.getAsJsonArray()) {
            list.add(mapper.apply(element));
        }
        return list;
    }

    public static <T> List<T> getList(@NotNull JsonObject json, @NotNull String key, Function<JsonElement, T> mapper) {
        return getList(json.getAsJsonArray(key), mapper);
    }

    public static int getInt(@NotNull JsonObject json, @NotNull String key, int fallback) {
        if (json.get(key) instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsInt();
        }
        return fallback;
    }

    public static float getFloat(@NotNull JsonObject json, @NotNull String key, float fallback) {
        if (json.get(key) instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsFloat();
        }
        return fallback;
    }

    @Contract("_,_,null->_;_,_,!null->!null")
    public static String getString(@NotNull JsonObject json, @NotNull String key, String fallback) {
        if (json.get(key) instanceof JsonPrimitive primitive && primitive.isString()) {
            return primitive.getAsString();
        }
        return fallback;
    }

    public static boolean getBoolean(@NotNull JsonObject json, @NotNull String key, boolean fallback) {
        if (json.get(key) instanceof JsonPrimitive primitive && primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        return fallback;
    }
}
