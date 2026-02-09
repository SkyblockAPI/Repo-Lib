package tech.thatgravyboat.repolib.v2.internal.types;

import tech.thatgravyboat.repolib.v2.api.id.BaseSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;

import java.util.Collection;
import java.util.Set;

public sealed  interface InternalSkyBlockIdType permits GenericSkyblockIdType {

    String name();

    Collection<IdProperty> getProperties(BaseSkyBlockId globalSkyBlockId);

    Collection<GlobalSkyBlockId> getVariants(BaseSkyBlockId globalSkyBlockId);

    <T> void addComponent(BaseRepoDataComponent<T> component);

    Set<BaseRepoDataComponent<?>> components();
}
