package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RepoComponentPatch implements RepoDataComponentGetter {

    private final Map<RepoDataComponent<?>, Object> patch = new HashMap<>();

    public <Type> void add(RepoDataComponent<Type> type, Type value) {
        patch.put(type, value);
    }

    public <Type> @Nullable Type get(RepoDataComponentGetter prototype, RepoDataComponent<Type> type) {
        return type.getOptional(this).orElse(prototype.get(type));
    }

    @Override
    public @Nullable <Type> Type getBase(BaseRepoDataComponent<Type> component) {
        //noinspection unchecked
        return (Type) patch.get(component);
    }

    public Set<Map.Entry<RepoDataComponent<?>, Object>> entrySet() {
        return patch.entrySet();
    }

    public boolean isEmpty() {
        return this.patch.isEmpty();
    }
}
