package tech.thatgravyboat.repolib.v2.internal.types.item;

import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.variants.VariantSelector;

import java.util.List;
import java.util.Map;

public class RepoItemEntry {

    public BaseRepoItemStack base;
    public List<IdProperty<?>> properties;
    public Map<IdProperty<?>, ?> defaults;
    public VariantSelector<BaseRepoItemStack> variants;

}
