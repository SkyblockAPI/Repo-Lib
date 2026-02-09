package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

import java.util.HashSet;
import java.util.Set;

public record PatchedRepoComponentMap(
        RepoComponentMap prototype,
        RepoComponentMap patch
) implements RepoComponentMap {
    public static RepoCodec<PatchedRepoComponentMap> codec(RepoComponentMap prototype, Set<BaseRepoDataComponent<?>> components) {
        return RepoComponentMap.codec(components).map(
                patch -> new PatchedRepoComponentMap(prototype, patch),
                patched -> patched.patch
        );
    }

    @Override
    public Set<BaseRepoDataComponent<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        } else {
            Set<BaseRepoDataComponent<?>> components = new HashSet<>(this.prototype.keySet());

            for (var entry : this.patch.entrySet()) {
                var key = entry.getKey();
                components.add(key);
            }

            return components;
        }
    }

    @Override
    public <Type> @Nullable Type getBase(BaseRepoDataComponent<Type> component) {
        @Nullable var value = patch.get(component);
        return value == null ? prototype.getBase(component) : value;
    }
}
