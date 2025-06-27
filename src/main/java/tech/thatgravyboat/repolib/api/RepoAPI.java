package tech.thatgravyboat.repolib.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.internal.RepoImplementation;
import tech.thatgravyboat.repolib.internal.Utils;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class RepoAPI {

    private static final RepoImplementation impl = RepoImplementation.getImplementation();
    private static final List<Consumer<RepoStatus>> listeners = new ArrayList<>();

    private static boolean setup = false;
    private static RepoStatus status = null;

    private static RepoVersion version;

    private static PetsAPI pets;
    private static ItemsAPI items;
    private static RecipesAPI recipes;
    private static MobsAPI mobs;
    private static ReforgeStonesAPI refogeStones;
    private static RunesAPI runes;

    //region Setup

    private static void assertVersion(RepoVersion version) {
        if (RepoAPI.version != null && version != RepoAPI.version) {
            throw new IllegalStateException("RepoAPI has already been setup with a different version");
        }
    }

    public static void setup(RepoVersion version, Consumer<RepoStatus> listener) {
        assertVersion(version);
        if (RepoAPI.status != null) {
            listener.accept(RepoAPI.status);
        } else {
            RepoAPI.listeners.add(listener);
            RepoAPI.version = version;
            setup(version);
        }
    }

    public static void setup(RepoVersion version) {
        assertVersion(version);
        RepoAPI.version = version;
        RepoAPI.setup();
    }

    //endregion

    //region Loading

    private static void setup() {
        if (RepoAPI.setup) return;
        RepoAPI.setup = true;
        CompletableFuture.runAsync(() -> {
            try {
                load();
                RepoAPI.status = RepoStatus.SUCCESS;
            } catch (Throwable e) {
                System.out.println("Failed to load data from the repo");
                e.printStackTrace();
                RepoAPI.status = RepoStatus.FAILED;
            }

            for (var listener : RepoAPI.listeners) {
                listener.accept(RepoAPI.status);
            }
            RepoAPI.listeners.clear();
        });
    }

    private static @NotNull JsonElement tryVersionedLoad(@Nullable JsonObject remote, @Nullable JsonElement local, String key, String path) throws Exception {
        JsonObject remoteVersioned = remote != null ? remote.getAsJsonObject(RepoAPI.version.version()) : null;
        JsonElement localVersioned = local instanceof JsonObject obj ? obj.getAsJsonObject(RepoAPI.version.version()) : null;
        return tryLoad(remoteVersioned, localVersioned, key, String.format("%s/%s", RepoAPI.version.version(), path));
    }

    private static @NotNull JsonElement tryLoad(@Nullable JsonObject remote, @Nullable JsonElement local, String key, String urlpath) throws Exception {
        var loc = impl.getRepoPath().resolve(key + ".min.json");
        var shasMatch = local instanceof JsonObject obj && remote != null && Objects.equals(obj.get(key), remote.get(key));
        if (!shasMatch || !Files.exists(loc)) {
            JsonElement element = Utils.getJsonFromApi(urlpath);
            if (element != null) {
                Files.writeString(loc, element.toString());
                return element;
            }
        }

        var localElement = Utils.getJsonFromFile(loc);
        if (localElement != null) return localElement;
        return Utils.getJsonFromResources(urlpath);
    }

    @Blocking
    private static void load() throws Exception {
        Files.createDirectories(impl.getRepoPath());

        JsonObject shas = Utils.mapNotNull(Utils.getJsonFromApi("shas.json"), JsonElement::getAsJsonObject);
        JsonElement localShas = Utils.getJsonFromFile(impl.getShasFile());
        JsonObject constants = tryLoad(shas, localShas, "constants", "constants.min.json").getAsJsonObject();

        RepoAPI.pets = PetsAPI.load(tryVersionedLoad(shas, localShas, "pets", "pets.min.json"), constants);
        RepoAPI.items = ItemsAPI.load(tryVersionedLoad(shas, localShas, "items", "items.min.json"));
        RepoAPI.recipes = RecipesAPI.load(tryVersionedLoad(shas, localShas, "recipes", "recipes.min.json"));
        RepoAPI.mobs = MobsAPI.load(tryVersionedLoad(shas, localShas, "mobs", "mobs.min.json"));
        RepoAPI.runes = RunesAPI.load(tryVersionedLoad(shas, localShas, "runes", "runes.min.json").getAsJsonObject());

        // Constants
        RepoAPI.refogeStones = ReforgeStonesAPI.load(tryLoad(shas, localShas, "reforge_stones", "constants/reforge_stones.min.json"));

        if (shas != null) {
            Files.writeString(impl.getShasFile(), shas.toString());
        }
    }

    //endregion

    public static boolean isInitialized() {
        return RepoAPI.status == RepoStatus.SUCCESS;
    }

    public static PetsAPI pets() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.pets;
    }

    public static ItemsAPI items() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.items;
    }

    public static RecipesAPI recipes() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.recipes;
    }

    public static MobsAPI mobs() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.mobs;
    }

    public static ReforgeStonesAPI reforgeStones() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.refogeStones;
    }

    public static RunesAPI runes() {
        if (!RepoAPI.isInitialized()) throw new IllegalStateException("RepoAPI has not been initialized yet");
        return RepoAPI.runes;
    }

}
