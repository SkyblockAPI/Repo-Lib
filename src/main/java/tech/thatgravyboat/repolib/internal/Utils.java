package tech.thatgravyboat.repolib.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@ApiStatus.Internal
public class Utils {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new GsonBuilder().create();
    private static final String BASE_URL = "https://skyblock-repo.pages.dev/";

    @Nullable
    public static JsonElement getJsonFromApi(@NotNull String path) {
        try {
            var request = HttpRequest.newBuilder()
                    .header("User-Agent", "Repolib")
                    .uri(URI.create(BASE_URL + path))
                    .GET()
                    .build();

            var response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("Failed to fetch data from " + path + " (status code: " + response.statusCode() + ")");
            }

            return GSON.fromJson(response.body(), JsonElement.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static JsonElement getJsonFromFile(@NotNull Path path) throws Exception {
        if (!Files.exists(path)) {
            return null;
        }
        return GSON.fromJson(Files.readString(path), JsonElement.class);
    }

    @NotNull
    public static JsonElement getJsonFromResources(@NotNull String path) throws Exception {
        var stream = Utils.class.getResourceAsStream("/backup/" + path);
        if (stream == null) throw new IOException("Resource not found: " + path);
        try (var resource = stream) {
            return GSON.fromJson(new JsonReader(new InputStreamReader(resource)), JsonElement.class);
        } catch (IOException e) {
            throw new IOException("Failed to read resource: " + path, e);
        }
    }

    public static boolean isLoaded(@NotNull String className) {
        try {
            Class.forName(className, false, Utils.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static <I, O> O mapNotNull(@Nullable I value, Function<I, O> mapper) {
        if (value == null) return null;
        return mapper.apply(value);
    }
}
