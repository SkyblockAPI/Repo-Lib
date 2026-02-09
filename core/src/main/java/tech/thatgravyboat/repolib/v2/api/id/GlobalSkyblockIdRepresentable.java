package tech.thatgravyboat.repolib.v2.api.id;

public interface GlobalSkyblockIdRepresentable extends BaseSkyblockIdRepresentable {

    GlobalSkyBlockId globalId();

    @Override
    default BaseSkyBlockId baseId() {
        return globalId().base();
    }
}
