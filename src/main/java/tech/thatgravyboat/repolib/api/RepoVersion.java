package tech.thatgravyboat.repolib.api;

import org.jetbrains.annotations.Nullable;

public enum RepoVersion {
    V1_21_4("1_21_4", "1.21.4"),
    V1_21_5("1_21_5", "1.21.5"),
    V1_21_6("1_21_5", "1.21.6"),
    V1_21_7("1_21_5", "1.21.7"),
    ;

    private final String version;
    private final String mcVersion;

    RepoVersion(String version, String mcVersion) {
        this.version = version;
        this.mcVersion = mcVersion;
    }

    public String version() {
        return version;
    }

    public static @Nullable RepoVersion fromName(String name) {
        name = name.replace("_", ".").replace("-", ".").toUpperCase();
        for (RepoVersion version : values()) {
            if (version.mcVersion.equals(name)) {
                return version;
            }
        }
        return null;
    }
}
