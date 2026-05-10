package tech.thatgravyboat.repolib.v2;

import java.nio.file.Path;

public record LoadingErrors(Path file, String reason) {
    public LoadingErrors(Path file, Exception reason) {
        this(file, reason.getMessage());
    }
}
