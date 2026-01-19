package tech.thatgravyboat.repolib.v2.api.entry;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.RepoComponentMap;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponentGetter;

public record RepoEntry(
        BaseRepoEntry base,
        GlobalSkyBlockId id,
        RepoComponentMap map
) implements RepoDataComponentGetter {
    @Override
    public @Nullable <Type> Type getUnsafe(RepoDataComponent<Type> component) {
        return this.map.getUnsafe(component);
    }

    @Override
    public boolean contains(RepoDataComponent<?> component) {
        return this.map.contains(component);
    }
}
