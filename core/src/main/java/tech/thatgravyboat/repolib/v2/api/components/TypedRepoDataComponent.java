package tech.thatgravyboat.repolib.v2.api.components;

public record TypedRepoDataComponent<Type>(
        RepoDataComponent<Type> component,
        Type value
) {
}
