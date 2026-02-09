package tech.thatgravyboat.repolib.v2.internal;

import tech.thatgravyboat.repolib.v2.internal.types.InternalSkyBlockIdType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;

public record RepoLibLoadingContext(
        Path path,
        Executor executor
) {

    public List<Path> getEntries(String directory) {
        try (var files = Files.list(path.resolve(directory))){
            return files.toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
