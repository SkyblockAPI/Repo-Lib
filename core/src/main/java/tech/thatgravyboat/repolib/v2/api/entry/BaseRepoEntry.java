package tech.thatgravyboat.repolib.v2.api.entry;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.v2.api.components.RepoComponentMap;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.components.RepoDataComponentGetter;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.variants.VariantSelector;

import java.util.List;

public record BaseRepoEntry(
        List<IdProperty<?>> properties,
        RepoComponentMap defaults,
        VariantSelector<RepoEntry> variants
) implements RepoDataComponentGetter {
    @Override
    public @Nullable <Type> Type getUnsafe(RepoDataComponent<Type> component) {
        return this.defaults.get(component);
    }

    @Override
    public boolean contains(RepoDataComponent<?> component) {
        return this.defaults.contains(component);
    }
}
