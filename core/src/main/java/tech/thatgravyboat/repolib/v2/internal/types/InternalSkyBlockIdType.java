package tech.thatgravyboat.repolib.v2.internal.types;

import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.LoadingRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;

public interface InternalSkyBlockIdType {

    String name();

    Iterable<IdProperty<?>> getProperties(GlobalSkyBlockId globalSkyBlockId);

    Iterable<GlobalSkyBlockId> getVariants(GlobalSkyBlockId globalSkyBlockId);

    <T> void addComponent(LoadingRepoDataComponent<T> component);
}
