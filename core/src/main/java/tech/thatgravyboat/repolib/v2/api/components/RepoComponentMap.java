package tech.thatgravyboat.repolib.v2.api.components;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface RepoComponentMap extends Iterable<TypedRepoDataComponent<?>>, RepoDataComponentGetter {
    default boolean has(RepoDataComponent<?> type) {
        return this.get(type) != null;
    }

    @Override
    default @NotNull Iterator<TypedRepoDataComponent<?>> iterator() {
        var iterator = keySet().iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public TypedRepoDataComponent<?> next() {
                return getTyped(iterator.next());
            }
        };
    }

    default Stream<TypedRepoDataComponent<?>> stream() {
        return StreamSupport.stream(
                Spliterators.spliterator(
                        this.iterator(), this.size(),
                        Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.NONNULL | Spliterator.IMMUTABLE
                ), false
        );
    }

    Set<RepoDataComponent<?>> keySet();

    default int size() {
        return keySet().size();
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    static RepoComponentMap of(Map<RepoDataComponent<?>, ?> map) {
        return new Simple(map);
    }

    record Simple(Map<RepoDataComponent<?>, ?> map) implements RepoComponentMap {
        @Override
        public Set<RepoDataComponent<?>> keySet() {
            return map.keySet();
        }

        @Override
        public <Type> @Nullable Type getUnsafe(RepoDataComponent<Type> component) {
            //noinspection unchecked
            return (Type) map.get(component);
        }

        @Override
        public boolean contains(RepoDataComponent<?> component) {
            return map.containsKey(component);
        }
    }
}
