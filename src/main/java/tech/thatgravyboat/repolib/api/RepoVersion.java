package tech.thatgravyboat.repolib.api;

import org.jetbrains.annotations.Nullable;

public enum RepoVersion {
    V1_21_4("1_21_4"),
    V1_21_5("1_21_5"),
    V1_21_6("1_21_5"),
    V1_21_7("1_21_5"),
    ;

    private final String version;

    RepoVersion(String version) {
        this.version = version;
    }

    public String version() {
        return version;
    }

    public static @Nullable RepoVersion fromName(String name) {
        name = name.replace(".", "_").replace("-", "_").toUpperCase();
        for (RepoVersion version : values()) {
            if (version.version().equals(name)) {
                return version;
            }
        }
        return null;
    }
}
