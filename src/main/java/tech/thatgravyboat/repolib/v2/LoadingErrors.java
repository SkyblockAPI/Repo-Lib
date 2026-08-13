package tech.thatgravyboat.repolib.v2;

import java.nio.file.Path;

public record LoadingErrors(Path file, Throwable reason) {
    public LoadingErrors(Path file, String reason) {
        this(file, new RuntimeException(reason));
    }
}
