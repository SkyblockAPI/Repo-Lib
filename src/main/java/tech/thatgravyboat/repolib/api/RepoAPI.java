package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Blocking;
import tech.thatgravyboat.repolib.internal.RepoImplementation;
import tech.thatgravyboat.repolib.internal.ThrowingBiFunction;
import tech.thatgravyboat.repolib.internal.Utils;

import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class RepoAPI {

    private static boolean setup = false;
    private static boolean initialized = false;
    private static final RepoImplementation impl = RepoImplementation.getImplementation();

    private static PetsAPI pets;
    private static ItemsAPI items;
    private static RecipesAPI recipes;

    public static void setup() {
        if (RepoAPI.setup) return;
        RepoAPI.setup = true;

        CompletableFuture.runAsync(() -> {
            try {
                load();
                RepoAPI.initialized = true;
            } catch (Exception e) {
                System.out.println("Failed to load data from the repo");
                e.printStackTrace();
            }
        });
    }

    public static boolean isInitialized() {
        return RepoAPI.initialized;
    }

    @Blocking
    private static void load() throws Exception {
        Files.createDirectories(impl.getRepoPath());

        JsonObject shas = Utils.getJsonFromApi("shas.json").getAsJsonObject();
        JsonElement localShas = Utils.getJsonFromFile(impl.getShasFile());
        ThrowingBiFunction<String, String, JsonElement> getData = (key, path) -> {
            var loc = impl.getRepoPath().resolve(path);
            var shasMatch = localShas instanceof JsonObject obj && Objects.equals(obj.get(key), shas.get(key));
            if (!shasMatch || !Files.exists(loc)) {
                JsonElement element = Utils.getJsonFromApi(path);
                Files.writeString(loc, element.toString());
                return element;
            } else {
                return Utils.getJsonFromFileOrThrow(loc);
            }
        };

        RepoAPI.pets = PetsAPI.load(getData.apply("pets", "pets.min.json"));
        RepoAPI.items = ItemsAPI.load(getData.apply("items", "items.min.json"));
        RepoAPI.recipes = RecipesAPI.load(getData.apply("recipes", "recipes.min.json"));

        Files.writeString(impl.getShasFile(), shas.toString());
    }

    public static PetsAPI pets() {
        if (!RepoAPI.initialized) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.pets;
    }

    public static ItemsAPI items() {
        if (!RepoAPI.initialized) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.items;
    }

    public static RecipesAPI recipes() {
        if (!RepoAPI.initialized) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.recipes;
    }

}
