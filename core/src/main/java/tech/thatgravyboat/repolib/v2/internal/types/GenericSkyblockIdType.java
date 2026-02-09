package tech.thatgravyboat.repolib.v2.internal.types;

import tech.thatgravyboat.repolib.v2.api.id.GlobalSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.id.BaseSkyBlockId;
import tech.thatgravyboat.repolib.v2.api.components.BaseRepoDataComponent;
import tech.thatgravyboat.repolib.v2.api.entry.BaseRepoEntry;
import tech.thatgravyboat.repolib.v2.api.entry.RepoEntry;
import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.RepoLibService;
import tech.thatgravyboat.repolib.v2.internal.RepoLibLoadingContext;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class GenericSkyblockIdType implements InternalSkyBlockIdType, RepoLibService {

    private final String name;
    public SkyBlockIdType publicType;
    private final Set<BaseRepoDataComponent<?>> allowedComponents = new HashSet<>();
    private final Map<BaseSkyBlockId, BaseRepoEntry> entries = new HashMap<>();

    public GenericSkyblockIdType(String name) {
        this.name = name;
    }

    @Override
    public Collection<IdProperty> getProperties(BaseSkyBlockId globalSkyBlockId) {
        var entry = entries.get(globalSkyBlockId);
        if (entry == null) return Collections.emptyList();
        return entry.properties();
    }

    @Override
    public Collection<GlobalSkyBlockId> getVariants(BaseSkyBlockId globalSkyBlockId) {
        var entry = entries.get(globalSkyBlockId);
        if (entry == null) return Collections.emptyList();
        return entry.variantSelector().collectMapped(RepoEntry::globalId);
    }

    @Override
    public <T> void addComponent(BaseRepoDataComponent<T> component) {
        this.allowedComponents.add(component);
    }

    @Override
    public Set<BaseRepoDataComponent<?>> components() {
        return this.allowedComponents;
    }

    private RepoCodec<BaseRepoEntry> codec() {
        return BaseRepoEntry.codec(publicType);
    }

    @Override
    public CompletableFuture<Void> load(RepoLibLoadingContext settings) {
        return CompletableFuture.supplyAsync(() -> {
            var files = settings.getEntries(name);

            for (var directory : files) {
                try {
                    var object = JsonUtils.parseObject(Files.readString(directory, StandardCharsets.UTF_8));
                    var type = BaseRepoEntry.codec(this.publicType).decode(object).orElseThrow();

                    entries.put(type.baseId(), type);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return null;
        }, settings.executor());
    }

    @Override
    public void save(RepoLibLoadingContext settings) {
        for (var entry : entries.entrySet()) {

        }
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
