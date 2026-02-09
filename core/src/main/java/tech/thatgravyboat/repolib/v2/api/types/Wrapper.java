package tech.thatgravyboat.repolib.v2.api.types;

import tech.thatgravyboat.repolib.v2.api.id.BaseSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.types.InternalSkyBlockIdType;

import java.util.Collection;

record Wrapper(
        InternalSkyBlockIdType internalSkyBlockIdType
) implements SkyBlockIdType {
    @Override
    public InternalSkyBlockIdType internal() {
        return internalSkyBlockIdType;
    }

    @Override
    public String name() {
        return this.internalSkyBlockIdType.name();
    }

    @Override
    public Collection<IdProperty> getProperties(BaseSkyBlockId globalSkyBlockId) {
        if (globalSkyBlockId.type() != this) throw new IllegalArgumentException("Type of skyblock globalId '" + globalSkyBlockId + "' is not equal to self ('" + name() + "'");
        return this.internalSkyBlockIdType.getProperties(globalSkyBlockId);
    }

    @Override
    public Collection<GlobalSkyBlockId> getVariants(BaseSkyBlockId globalSkyBlockId) {
        if (globalSkyBlockId.type() != this) throw new IllegalArgumentException("Type of skyblock globalId '" + globalSkyBlockId + "' is not equal to self ('" + name() + "'");
        return this.internalSkyBlockIdType.getVariants(globalSkyBlockId);
    }

    @Override
    public <T> void registerComponent(BaseRepoDataComponent<T> component) {
        this.internalSkyBlockIdType.addComponent(component);
    }
}
