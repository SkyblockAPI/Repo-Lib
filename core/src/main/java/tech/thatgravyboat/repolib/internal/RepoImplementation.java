package tech.thatgravyboat.repolib.internal;

import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

public interface RepoImplementation {

    String PROPERTY = "repo.lib.implementation";
    String FOLDER = "skyblock-repo-cache";

    Path getRepoPath();

    default Path getShasFile() {
        return getRepoPath().resolve("shas.json");
    }

    @ApiStatus.Internal
    static RepoImplementation getImplementation() {
        String impl = System.getProperty(PROPERTY, "");
        if (impl.isEmpty()) {
            if (Utils.isLoaded("net.neoforged.fml.loading.FMLLoader")) {
                impl = "tech.thatgravyboat.repolib.neoforge.NeoForgeRepoImplementation";
            } else if (Utils.isLoaded("net.fabricmc.loader.api.FabricLoader")) {
                impl = "tech.thatgravyboat.repolib.fabric.FabricRepoImplementation";
            } else {
                throw new IllegalStateException("No implementation found for RepoLib");
            }
        }

        try {
            return (RepoImplementation) Class.forName(impl).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RepoLib implementation", e);
        }
    }
}
