package tech.thatgravyboat.repolib.neoforge;

import net.neoforged.fml.loading.FMLLoader;
import tech.thatgravyboat.repolib.internal.RepoImplementation;

import java.nio.file.Path;

public class NeoForgeRepoImplementation implements RepoImplementation {

    @Override
    public Path getRepoPath() {
        return FMLLoader.getGamePath().resolve(RepoImplementation.FOLDER);
    }
}
