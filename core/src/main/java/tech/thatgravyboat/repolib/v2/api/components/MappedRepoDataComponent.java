package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public record MappedRepoDataComponent<Parent, Mapped>(
        RepoDataComponent<Parent> parent,
        Function<Parent, Mapped> converter
) implements RepoDataComponent<Mapped> {

    @Override
    public @Nullable Mapped get(RepoDataComponentGetter entry) {
        return getOptional(entry).orElse(null);
    }

    @Override
    public Optional<Mapped> getOptional(RepoDataComponentGetter entry) {
        return parent.getOptional(entry).map(converter);
    }

    @Override
    public boolean has(RepoDataComponentGetter entry) {
        return parent.has(entry);
    }
}
