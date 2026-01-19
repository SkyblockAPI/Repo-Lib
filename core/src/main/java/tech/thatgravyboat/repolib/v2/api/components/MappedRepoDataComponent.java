package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public record MappedRepoDataComponent<Parent, Mapped>(RepoDataComponent<Parent> parent,
                                                      Function<Parent, Mapped> converter)
        implements RepoDataComponent<Mapped> {
    @Override
    public Mapped get(RepoDataComponentGetter entry) {
        return converter.apply(parent.get(entry));
    }

    @Override
    public @Nullable Optional<Mapped> getOptional(RepoDataComponentGetter entry) {
        var key = parent.getOptional(entry);
        //noinspection OptionalAssignedToNull
        return key == null ? null : key.map(converter);
    }
}
