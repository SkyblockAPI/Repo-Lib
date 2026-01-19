package tech.thatgravyboat.repolib.v2.internal.types;

import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.GlobalSkyBlockBase;
import tech.thatgravyboat.repolib.v2.api.components.LoadingRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.entry.BaseRepoEntry;
import tech.thatgravyboat.repolib.v2.api.entry.RepoEntry;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.internal.RepoLibService;
import tech.thatgravyboat.repolib.v2.internal.RepoLibLoadingContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GenericSkyblockIdType implements InternalSkyBlockIdType, RepoLibService {

    private final String name;
    private final Set<LoadingRepoDataComponent<?>> allowedComponents = new HashSet<>();
    private final Map<GlobalSkyBlockBase, BaseRepoEntry> entries = new HashMap<>();
    private final Map<GlobalSkyBlockId, RepoEntry> entryMap = new HashMap<>();

    public GenericSkyblockIdType(String name) {
        this.name = name;
    }

    @Override
    public Iterable<IdProperty<?>> getProperties(GlobalSkyBlockId globalSkyBlockId) {
        var entry = entryMap.get(globalSkyBlockId);
        if (entry == null) return Collections.emptyList();
        return entry.base().properties();
    }

    @Override
    public Iterable<GlobalSkyBlockId> getVariants(GlobalSkyBlockId globalSkyBlockId) {
        var entry = entries.get(globalSkyBlockId.base());
        if (entry == null) return Collections.emptyList();
        return entry.variants().collectMapped(RepoEntry::id);
    }

    @Override
    public <T> void addComponent(LoadingRepoDataComponent<T> component) {
        this.allowedComponents.add(component);
    }

    @Override
    public CompletableFuture<Void> load(RepoLibLoadingContext settings) {
        var components = allowedComponents;

        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (GenericSkyblockIdType) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.allowedComponents, that.allowedComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, allowedComponents);
    }

    @Override
    public String toString() {
        return "GenericSkyblockIdType[" +
                "name=" + name + ", " +
                "allowedComponents=" + allowedComponents + ']';
    }
}
