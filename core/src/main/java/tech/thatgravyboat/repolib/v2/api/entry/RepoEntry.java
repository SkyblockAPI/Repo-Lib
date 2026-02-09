package tech.thatgravyboat.repolib.v2.api.entry;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.components.PatchedRepoComponentMap;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponentGetter;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyblockIdRepresentable;

public record RepoEntry(
        BaseRepoEntry base,
        GlobalSkyBlockId globalId,
        PatchedRepoComponentMap map
) implements RepoDataComponentGetter, GlobalSkyblockIdRepresentable {
    @Override
    public <Type> @Nullable Type getBase(BaseRepoDataComponent<Type> component) {
        return this.map.getBase(component);
    }
}
