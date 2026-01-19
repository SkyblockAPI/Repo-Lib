package tech.thatgravyboat.repolib.v2.api.components;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record SimpleRepoDataComponent<Type>(String name, Set<SkyBlockIdType> allowedTypes,
                                            Function<JsonElement, Type> converter)
        implements LoadingRepoDataComponent<Type> {

    public Type get(RepoDataComponentGetter entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Optional<Type> getOptional(RepoDataComponentGetter entry) {
        return Optional.empty();
    }

    @Override
    public Type load(JsonElement element) {
        return null;
    }
}
