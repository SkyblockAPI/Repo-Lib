package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.Nullable;
import tech.thatgravyboat.repolib.internal.Utils;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;
import tech.thatgravyboat.repolib.v2.internal.codec.struct.fields.StructKeyDispatchCodec;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface RepoComponentMap
        extends Iterable<TypedRepoDataComponent<?>>, RepoDataComponentGetter
        permits RepoComponentMap.Simple, PatchedRepoComponentMap {

    static RepoCodec<RepoComponentMap> codec(Set<BaseRepoDataComponent<?>> components) {
        var mapper = RepoCodec.<String, BaseRepoDataComponent<?>>idMapper();

        for (var component : components) {
            mapper.put(component.name(), component);
        }

        return new StructKeyDispatchCodec<>(
                mapper.dispatch(RepoCodec.STRING),
                Utils::unsafe
        ).map(RepoComponentMap::of, RepoComponentMap::asMap);
    }

    static RepoComponentMap of(Map<BaseRepoDataComponent<?>, ?> map) {
        return new Simple(map);
    }

    default Map<BaseRepoDataComponent<?>, Object> asMap() {
        HashMap<BaseRepoDataComponent<?>, Object> map = new HashMap<>();

        for (var entry : entrySet()) {
            map.put(entry.getKey(), Utils.unsafe(entry.getValue()));
        }

        return map;
    }

    @Override
    default Iterator<TypedRepoDataComponent<?>> iterator() {
        List<TypedRepoDataComponent<?>> iterator = keySet().stream().map(this::getTyped).filter(Objects::nonNull).collect(Collectors.toList());
        return iterator.iterator();
    }

    default Stream<TypedRepoDataComponent<?>> stream() {
        return StreamSupport.stream(
                Spliterators.spliterator(
                        this.iterator(), this.size(),
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE
                ), false
        );
    }

    Set<BaseRepoDataComponent<?>> keySet();

    default int size() {
        return keySet().size();
    }

    default boolean isEmpty() {
        return size() == 0;
    }


    default Set<Map.Entry<BaseRepoDataComponent<?>, ?>> entrySet() {
        var set = new HashSet<Map.Entry<BaseRepoDataComponent<?>, ?>>();
        for (var repoDataComponent : keySet()) {
            var value = this.getBase(repoDataComponent);
            if (value == null) continue;
            set.add(new AbstractMap.SimpleEntry<>(repoDataComponent, value));
        }

        return set;
    }

    record Simple(Map<BaseRepoDataComponent<?>, ?> map) implements RepoComponentMap {

        @Override
        public Set<BaseRepoDataComponent<?>> keySet() {
            return map.keySet();
        }

        @Override
        public <Type> @Nullable Type getBase(BaseRepoDataComponent<Type> component) {
            //noinspection unchecked
            return (Type) map.get(component);
        }

        @Override
        public Set<Map.Entry<BaseRepoDataComponent<?>, ?>> entrySet() {
            return Utils.unsafe(map.entrySet());
        }
    }
}
