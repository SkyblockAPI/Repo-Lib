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

    private static final PetsAPI pets = new PetsAPI();
    private static final ItemsAPI items = new ItemsAPI();
    private static final RecipesAPI recipes = new RecipesAPI();
    private static final MobsAPI mobs = new MobsAPI();
    private static final ReforgeStonesAPI refogeStones = new ReforgeStonesAPI();
    private static final RunesAPI runes = new RunesAPI();
    private static final EnchantsAPI enchants = new EnchantsAPI();
    private static final AttributesAPI attributes = new AttributesAPI();

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

        RepoAPI.pets.load(tryVersionedLoad(shas, localShas, "pets", "pets.min.json"), constants);
        RepoAPI.items.load(tryVersionedLoad(shas, localShas, "items", "items.min.json"));
        RepoAPI.recipes.load(tryVersionedLoad(shas, localShas, "recipes", "recipes.min.json"));
        RepoAPI.mobs.load(tryVersionedLoad(shas, localShas, "mobs", "mobs.min.json"));
        RepoAPI.runes.load(tryVersionedLoad(shas, localShas, "runes", "runes.min.json").getAsJsonObject());
        RepoAPI.enchants.load(tryVersionedLoad(shas, localShas, "enchantments", "enchantments.min.json"));
        RepoAPI.attributes.load(tryVersionedLoad(shas, localShas, "attributes", "attributes.min.json"));

        // Constants
        RepoAPI.refogeStones.load(tryLoad(shas, localShas, "reforge_stones", "constants/reforge_stones.min.json"));

        if (shas != null) {
            Files.writeString(impl.getShasFile(), shas.toString());
        }
    }

    //endregion

    public static boolean isInitialized() {
        return RepoAPI.status == RepoStatus.SUCCESS;
    }

    public static PetsAPI pets() {
        return RepoAPI.pets;
    }

    public static ItemsAPI items() {
        return RepoAPI.items;
    }

    public static RecipesAPI recipes() {
        return RepoAPI.recipes;
    }

    public static MobsAPI mobs() {
        return RepoAPI.mobs;
    }

    public static ReforgeStonesAPI reforgeStones() {
        return RepoAPI.refogeStones;
    }

    public static RunesAPI runes() {
        return RepoAPI.runes;
    }

    public static AttributesAPI attributes() {
        return RepoAPI.attributes;
    }

    public static EnchantsAPI enchantments() {
        return RepoAPI.enchants;
    }

}
