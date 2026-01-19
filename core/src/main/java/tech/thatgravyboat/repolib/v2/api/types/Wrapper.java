package tech.thatgravyboat.repolib.v2.api.types;

import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.LoadingRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.types.InternalSkyBlockIdType;

record Wrapper(
        InternalSkyBlockIdType internalSkyBlockIdType
) implements SkyBlockIdType {
    @Override
    public String name() {
        return this.internalSkyBlockIdType.name();
    }

    @Override
    public Iterable<IdProperty<?>> getProperties(GlobalSkyBlockId globalSkyBlockId) {
        if (globalSkyBlockId.type() != this) return null;
        return this.internalSkyBlockIdType.getProperties(globalSkyBlockId);
    }

    @Override
    public Iterable<GlobalSkyBlockId> getVariants(GlobalSkyBlockId globalSkyBlockId) {
        if (globalSkyBlockId.type() != this) return null;
        return this.internalSkyBlockIdType.getVariants(globalSkyBlockId);
    }

    @Override
    public <T> void registerComponent(LoadingRepoDataComponent<T> component) {
        this.internalSkyBlockIdType.addComponent(component);
    }
}
