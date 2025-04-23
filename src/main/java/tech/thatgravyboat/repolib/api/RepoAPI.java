package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Blocking;
import tech.thatgravyboat.repolib.internal.RepoImplementation;
import tech.thatgravyboat.repolib.internal.Utils;

import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class RepoAPI {

    private static boolean setup = false;
    private static boolean initialized = false;
    private static final RepoImplementation impl = RepoImplementation.getImplementation();

    private static RepoVersion version;

    private static PetsAPI pets;
    private static ItemsAPI items;
    private static RecipesAPI recipes;
    private static MobsAPI mobs;

    public static void setup(RepoVersion version) {
        if (RepoAPI.version != null && version != RepoAPI.version) {
            throw new IllegalStateException("RepoAPI has already been setup with a different version");
        }

        if (RepoAPI.setup) return;
        RepoAPI.version = version;
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

    private static JsonElement tryVersionedLoad(JsonObject remote, JsonElement local, String key, String path) throws Exception {
        JsonObject remoteVersioned = remote.getAsJsonObject(RepoAPI.version.version());
        JsonElement localVersioned = local instanceof JsonObject obj ? obj.getAsJsonObject(RepoAPI.version.version()) : null;
        return tryLoad(remoteVersioned, localVersioned, key, String.format("%s/%s", RepoAPI.version.version(), path));
    }

    private static JsonElement tryLoad(JsonObject remote, JsonElement local, String key, String path) throws Exception {
        var loc = impl.getRepoPath().resolve(key + ".min.json");
        var shasMatch = local instanceof JsonObject obj && Objects.equals(obj.get(key), remote.get(key));
        if (!shasMatch || !Files.exists(loc)) {
            JsonElement element = Utils.getJsonFromApi(path);
            Files.writeString(loc, element.toString());
            return element;
        } else {
            return Utils.getJsonFromFileOrThrow(loc);
        }
    }

    @Blocking
    private static void load() throws Exception {
        Files.createDirectories(impl.getRepoPath());

        JsonObject shas = Utils.getJsonFromApi("shas.json").getAsJsonObject();
        JsonElement localShas = Utils.getJsonFromFile(impl.getShasFile());
        JsonObject constants = tryLoad(shas, localShas, "constants", "constants.min.json").getAsJsonObject();

        RepoAPI.pets = PetsAPI.load(tryVersionedLoad(shas, localShas, "pets", "pets.min.json"), constants);
        RepoAPI.items = ItemsAPI.load(tryVersionedLoad(shas, localShas, "items", "items.min.json"));
        RepoAPI.recipes = RecipesAPI.load(tryVersionedLoad(shas, localShas, "recipes", "recipes.min.json"));
        RepoAPI.mobs = MobsAPI.load(tryVersionedLoad(shas, localShas, "mobs", "mobs.min.json"));

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

    public static MobsAPI mobs() {
        if (!RepoAPI.initialized) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.mobs;
    }

}
