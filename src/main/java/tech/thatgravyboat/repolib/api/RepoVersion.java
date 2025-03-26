package tech.thatgravyboat.repolib.api;

public enum RepoVersion {
    V1_21_4("1_21_4"),
    V1_21_5("1_21_5"),
    ;

    private final String version;

    RepoVersion(String version) {
        this.version = version;
    }

    public String version() {
        return version;
    }
}
