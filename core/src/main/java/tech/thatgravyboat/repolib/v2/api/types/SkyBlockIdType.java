package tech.thatgravyboat.repolib.v2.api.types;

import org.jetbrains.annotations.ApiStatus;
import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.LoadingRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.RepoLibService;
import tech.thatgravyboat.repolib.v2.internal.RepoLibServices;
import tech.thatgravyboat.repolib.v2.internal.types.GenericSkyblockIdType;
import tech.thatgravyboat.repolib.v2.internal.types.InternalSkyBlockIdType;

import java.util.ArrayList;
import java.util.List;

/**
 *The backing type of any global skyblock id, its used as an abstraction layer for better implementations.
 */
public sealed interface SkyBlockIdType permits Wrapper {

    List<SkyBlockIdType> types = new ArrayList<>();

    SkyBlockIdType ITEM = register(new GenericSkyblockIdType("item"));

    private static <T extends InternalSkyBlockIdType & RepoLibService> SkyBlockIdType register(T type) {
        RepoLibServices.register(type);
        var apiType = new Wrapper(type);
        types.add(apiType);
        return apiType;
    }

    /**
     * @return The name of this type.
     */
    String name();

    /**
     * {@code null} if the global skyblock id doesn't belong to this type, else always returns a value.
     */
    Iterable<IdProperty<?>> getProperties(GlobalSkyBlockId globalSkyBlockId);

    /**
     * {@code null} if the global skyblock id doesn't belong to this type, else always returns a value.
     */
    Iterable<GlobalSkyBlockId> getVariants(GlobalSkyBlockId globalSkyBlockId);

    /**
     * Internal use only! Used to allow a data component on the id type.
     */
    @ApiStatus.Internal
    <T> void registerComponent(LoadingRepoDataComponent<T> component);
}
