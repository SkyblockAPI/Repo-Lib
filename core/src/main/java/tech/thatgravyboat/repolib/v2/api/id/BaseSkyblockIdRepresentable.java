package tech.thatgravyboat.repolib.v2.api.id;

import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;

import java.util.Collection;

public interface BaseSkyblockIdRepresentable {

    BaseSkyBlockId baseId();

    default SkyBlockIdType type() {
        return baseId().type();
    }
    default String id() {
        return baseId().id();
    }

    default Collection<GlobalSkyBlockId> variants() {
        return type().getVariants(baseId());
    }

    default Collection<IdProperty> allowedProperties() {
        return type().getProperties(baseId());
    }

}
