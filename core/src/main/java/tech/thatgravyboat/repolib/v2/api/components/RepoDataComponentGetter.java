package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface RepoDataComponentGetter {

    <Type> @Nullable Type getUnsafe(RepoDataComponent<Type> component);
    default <Type> @Nullable Optional<Type> getOptionalUnsafe(RepoDataComponent<Type> component) {
        return Optional.ofNullable(get(component));
    }

    default <Type> @Nullable Type get(RepoDataComponent<Type> component) {
        return component.get(this);
    }
    default <Type> @Nullable Optional<Type> getOptional(RepoDataComponent<Type> component) {
        return component.getOptional(this);
    }

    default <Type> @NotNull TypedRepoDataComponent<Type> getTyped(RepoDataComponent<Type> component) {
        return new TypedRepoDataComponent<>(component, get(component));
    }

    boolean contains(RepoDataComponent<?> component);

    @Contract("_, !null -> !null; _, null -> _")
    default <Type> Type getOrDefault(RepoDataComponent<Type> component, Type defaultValue) {
        var data = get(component);
        if (data == null) {
            return defaultValue;
        } else {
            return data;
        }
    }

}
