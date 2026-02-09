package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public interface RepoDataComponent<Type> {

    Type get(RepoDataComponentGetter entry);
    Optional<Type> getOptional(RepoDataComponentGetter entry);
    boolean has(RepoDataComponentGetter entry);

}
