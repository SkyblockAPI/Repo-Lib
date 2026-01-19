package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RepoComponentPatch implements RepoDataComponentGetter {

    private final Map<RepoDataComponent<?>, Optional<?>> patch = new HashMap<>();

    public <Type> void add(RepoDataComponent<Type> type, @Nullable Type value) {
        patch.put(type, Optional.ofNullable(value));
    }

    public <Type> Type get(RepoDataComponentGetter prototype, RepoDataComponent<Type> type) {
        var patched = type.getOptional(this);
        return patched != null ? patched.orElse(null) : prototype.get(type);
    }

    @Override
    public @Nullable <Type> Type getUnsafe(RepoDataComponent<Type> component) {
        //noinspection unchecked
        return patch.containsKey(component) ? (Type) patch.get(component).orElse(null) : null;
    }

    @Override
    public @Nullable <Type> Optional<Type> getOptionalUnsafe(RepoDataComponent<Type> component) {
        //noinspection unchecked
        return (Optional<Type>) patch.get(component);
    }

    @Override
    public boolean contains(RepoDataComponent<?> component) {
        return patch.containsKey(component);
    }

    public Set<Map.Entry<RepoDataComponent<?>, Optional<?>>> entrySet() {
        return patch.entrySet();
    }

    public boolean isEmpty() {
        return this.patch.isEmpty();
    }
}
