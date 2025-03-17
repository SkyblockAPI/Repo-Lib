package tech.thatgravyboat.repolib.fabric;

import net.fabricmc.loader.api.FabricLoader;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;

public class FabricRepoImplementation implements RepoImplementation {

    @Override
    public Path getRepoPath() {
        return FabricLoader.getInstance().getGameDir().resolve(RepoImplementation.FOLDER);
    }
}
