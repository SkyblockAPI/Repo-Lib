package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

import java.util.Optional;

public interface BaseRepoDataComponent<Type> extends RepoDataComponent<Type>, RepoCodec<Type> {

    String name();

    default @Nullable Type get(RepoDataComponentGetter entry) {
        return entry.getBase(this);
    }

    @Override
    default Optional<Type> getOptional(RepoDataComponentGetter entry) {
        return entry.getOptionalBase(this);
    }

    @Override
    default boolean has(RepoDataComponentGetter entry) {
        return entry.hasBase(this);
    }
}
