package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PatchedRepoComponentMap implements RepoComponentMap {
    private final RepoComponentMap prototype;
    private final RepoComponentPatch patch;

    public PatchedRepoComponentMap(RepoComponentMap prototype, RepoComponentPatch patch) {
        this.prototype = prototype;
        this.patch = patch;
    }

    @Override
    public Set<RepoDataComponent<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        } else {
            Set<RepoDataComponent<?>> components = new HashSet<>(this.prototype.keySet());

            for (var entry : this.patch.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();

                if (value.isEmpty()) {
                    components.remove(key);
                } else {
                    components.add(key);
                }
            }

            return components;
        }
    }

    @Override
    public @Nullable <Type> Type getUnsafe(RepoDataComponent<Type> component) {
        var value = patch.get(component);
        return value == null ? prototype.getUnsafe(component) : value;
    }

    @Override
    public boolean contains(RepoDataComponent<?> component) {
        var value = this.patch.getOptional(component);
        if (value != null) {
            return value.isPresent();
        }

        return this.prototype.contains(component);
    }
}
