package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface RepoDataComponentGetter {

    <Type> @Nullable Type getBase(BaseRepoDataComponent<Type> component);

    default <Type> Optional<Type> getOptionalBase(BaseRepoDataComponent<Type> component) {
        return Optional.ofNullable(getBase(component));
    }

    default <Type> boolean hasBase(BaseRepoDataComponent<Type> component) {
        return getBase(component) != null;
    }

    default <Type> @Nullable Type get(RepoDataComponent<Type> component) {
        return component.get(this);
    }
    default <Type> Optional<Type> getOptional(RepoDataComponent<Type> component) {
        return component.getOptional(this);
    }

    default <Type> @Nullable TypedRepoDataComponent<Type> getTyped(RepoDataComponent<Type> component) {
        @Nullable var value = get(component);
        return value == null ? null : new TypedRepoDataComponent<>(component, value);
    }

   default boolean has(RepoDataComponent<?> component) {
        return component.has(this);
   }

    @Contract("_, !null -> !null; _, null -> _")
    default <Type> Type getOrDefault(RepoDataComponent<Type> component, Type defaultValue) {
        @Nullable var data = get(component);
        return data == null ? defaultValue : data;
    }

}
