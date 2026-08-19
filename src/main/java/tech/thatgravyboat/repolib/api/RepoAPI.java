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
    private static final ReforgeStonesAPI reforgeStones = new ReforgeStonesAPI();
    private static final ParentsAPI parents = new ParentsAPI();
    private static final RunesAPI runes = new RunesAPI();
    private static final EnchantsAPI enchants = new EnchantsAPI();
    private static final AttributesAPI attributes = new AttributesAPI();
    private static final PotionsAPI potions = new PotionsAPI();
    private static final IdOverlaysAPI overlays = new IdOverlaysAPI();

    //region Setup

    private static void assertVersion(RepoVersion version) {
        if (RepoAPI.version != null && version != RepoAPI.version) {
            throw new IllegalStateException("RepoAPI has already been setup with a different version");
        } else if (!version.isSupported()) {
            throw new IllegalArgumentException("Version " + version.version() + " is no longer supported");
        }
    }

    public static void setup(RepoVersion version, Consumer<RepoStatus> listener) {
        RepoLibLogger.info("Initializing with version " + version.version());
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
        RepoLibLogger.debug("Setting up repo lib!");
        RepoAPI.setup = true;
        CompletableFuture.runAsync(() -> {
            try {
                load();
                RepoAPI.status = RepoStatus.SUCCESS;
            } catch (Throwable e) {
                RepoLibLogger.error("Failed to load data from the repo.", e);
                RepoAPI.status = RepoStatus.FAILED;
            }

            RepoLibLogger.debug("Dispatching listeners with status = " + RepoAPI.status);
            for (var listener : RepoAPI.listeners) {
                listener.accept(RepoAPI.status);
            }
            RepoAPI.listeners.clear();
        });
    }

    private static @NotNull JsonElement tryVersionedLoad(@Nullable JsonObject remote, @Nullable JsonObject local, String key, String path) throws Exception {
        JsonObject remoteVersioned = remote != null ? remote.getAsJsonObject(RepoAPI.version.version()) : null;
        JsonObject localVersioned = local != null ? local.getAsJsonObject(RepoAPI.version.version()) : null;
        RepoLibLogger.debug("Trying to load versioned " + RepoAPI.version.version() + "/" + key);
        return tryLoad(remoteVersioned, localVersioned, key, String.format("%s/%s", RepoAPI.version.version(), path));
    }

    private static @NotNull JsonElement tryLoad(@Nullable JsonObject remote, @Nullable JsonObject local, String key, String urlpath) throws Exception {
        RepoLibLogger.debug("Trying to load " + key);
        var loc = impl.getRepoPath().resolve(key + ".min.json");
        var shasMatch = local != null && remote != null && Objects.equals(local.get(key), remote.get(key));
        if (!shasMatch || !Files.exists(loc)) {
            RepoLibLogger.trace("Downloading " + key + " from remote!");
            JsonElement element = Utils.getJsonFromApi(urlpath);
            if (element != null) {
                Files.writeString(loc, element.toString());
                return element;
            }
        }

        RepoLibLogger.trace("Loading " + key + " from local cache!");
        var localElement = Utils.getJsonFromFile(loc);
        if (localElement != null) return localElement;
        RepoLibLogger.debug("Loading " + key + " from backup repo!");
        return Utils.getJsonFromResources(urlpath);
    }

    @Blocking
    private static void load() throws Exception {
        Files.createDirectories(impl.getRepoPath());
        RepoLibLogger.debug("Loading repo with path " + impl.getRepoPath().toString());

        JsonObject shas = Utils.mapNotNull(Utils.getJsonFromApi("shas.json"), JsonElement::getAsJsonObject);
        if (shas == null) {
            RepoLibLogger.warn("Unable to retrieve shas.json from remote!");
        }
        JsonObject localShas = Utils.mapNotNull(Utils.getJsonFromFile(impl.getShasFile()), JsonElement::getAsJsonObject);
        if (localShas == null) {
            RepoLibLogger.info("Unable to read shas.json from local cache!");
        }
        if (shas == null && localShas == null) {
            RepoLibLogger.warn("Both local and remote index are null!");
        }

        JsonObject constants = tryLoad(shas, localShas, "constants", "constants.min.json").getAsJsonObject();

        RepoAPI.pets.load(tryVersionedLoad(shas, localShas, "pets", "pets.min.json"), constants);
        RepoAPI.items.load(tryVersionedLoad(shas, localShas, "items", "items.min.json"));
        RepoAPI.recipes.load(tryVersionedLoad(shas, localShas, "recipes", "recipes.min.json"));
        RepoAPI.mobs.load(tryVersionedLoad(shas, localShas, "mobs", "mobs.min.json"));
        RepoAPI.runes.load(tryVersionedLoad(shas, localShas, "runes", "runes.min.json").getAsJsonObject());
        RepoAPI.enchants.load(tryVersionedLoad(shas, localShas, "enchantments", "enchantments.min.json"));
        RepoAPI.attributes.load(tryVersionedLoad(shas, localShas, "attributes", "attributes.min.json"));
        RepoAPI.potions.load(tryVersionedLoad(shas, localShas, "potions", "potions.min.json"));
        RepoAPI.overlays.load(tryVersionedLoad(shas, localShas, "id_overlays", "id_overlays.min.json"));

        // Constants
        RepoAPI.reforgeStones.load(tryLoad(shas, localShas, "reforge_stones", "constants/reforge_stones.min.json"));
        RepoAPI.parents.load(tryLoad(shas, localShas, "parents", "constants/parents.min.json"));

        if (shas != null) {
            RepoLibLogger.debug("Writing current index file!");
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
        return RepoAPI.reforgeStones;
    }

    public static ParentsAPI parents() {
        return RepoAPI.parents;
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

    public static PotionsAPI potions() {
        return RepoAPI.potions;
    }

    public static IdOverlaysAPI overlays() {
        return RepoAPI.overlays;
    }

}