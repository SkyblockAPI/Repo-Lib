package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public interface RepoDataComponent<Type> {

    Type get(RepoDataComponentGetter entry);
    @Nullable Optional<Type> getOptional(RepoDataComponentGetter entry);
    default <Mapped> RepoDataComponent<Mapped> map(Function<Type, Mapped> function) {

    }

}
