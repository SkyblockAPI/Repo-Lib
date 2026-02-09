package tech.thatgravyboat.repolib.v2.api.types;

import org.jetbrains.annotations.ApiStatus;
import tech.thatgravyboat.repolib.v2.api.id.BaseSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.RepoLibService;
import tech.thatgravyboat.repolib.v2.internal.RepoLibServices;
import tech.thatgravyboat.repolib.v2.internal.codec.CodecResult;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.types.GenericSkyblockIdType;
import tech.thatgravyboat.repolib.v2.internal.types.InternalSkyBlockIdType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *The backing type of any global skyblock globalId, its used as an abstraction layer for better implementations.
 */
public sealed interface SkyBlockIdType permits Wrapper {

    @ApiStatus.Internal
    Map<String, SkyBlockIdType> map = new HashMap<>();

    RepoCodec<SkyBlockIdType> CODEC = RepoCodec.STRING.flatMap(
            type -> {
                var value = map.get(type);
                if (value == null) {
                    return CodecResult.failure("Unknown globalId type '" + type + "'");
                }

                return CodecResult.success(value);
            },
            type -> CodecResult.success(type.name())
    );

    List<SkyBlockIdType> types = new ArrayList<>();

    SkyBlockIdType ITEM = register(new GenericSkyblockIdType("item"));

    InternalSkyBlockIdType internal();

    private static <T extends InternalSkyBlockIdType & RepoLibService> SkyBlockIdType register(T type) {
        RepoLibServices.register(type);
        var apiType = new Wrapper(type);
        types.add(apiType);
        if (map.put(apiType.name(), apiType) != null) {
            throw new IllegalArgumentException("Duplicate skyblock globalId with name '" + apiType.name() + "'");
        }
        return apiType;
    }

    /**
     * @return The name of this type.
     */
    String name();

    /**
     * {@code null} if the global skyblock globalId doesn't belong to this type, else always returns a value.
     */
    Collection<IdProperty> getProperties(BaseSkyBlockId globalSkyBlockId);

    /**
     * {@code null} if the global skyblock globalId doesn't belong to this type, else always returns a value.
     */
    Collection<GlobalSkyBlockId> getVariants(BaseSkyBlockId globalSkyBlockId);

    /**
     * Internal use only! Used to allow a data component on the globalId type.
     */
    @ApiStatus.Internal
    <T> void registerComponent(BaseRepoDataComponent<T> component);
}
