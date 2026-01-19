package tech.thatgravyboat.repolib.v2.internal.variants;

import tech.thatgravyboat.repolib.v2.api.properties.IdProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class VariantSelector<T> {
    private final List<IdProperty<?>> properties;
    private final VariantTree<T> tree = VariantTree.create(this, 0);
    private final Map<IdProperty<?>, ?> defaults;

    public VariantSelector(Map<IdProperty<?>, ?> defaults) {
        this.defaults = defaults;
        this.properties = List.copyOf(defaults.keySet());
    }

    public List<T> collect() {
        return this.tree.collect();
    }

    public <Value> List<Value> collectMapped(Function<T, Value> mapper) {
        return this.tree.collectMapped(mapper);
    }

    private sealed interface VariantTreeNode<T> permits LeafNode, VariantTree {
        T select(Map<IdProperty<?>, ?> properties);

        default List<T> collect() {
            return collectMapped(Function.identity());
        }

        <Value> List<Value> collectMapped(Function<T, Value> mapper);
    }

    private static final class VariantTree<T> implements VariantTreeNode<T> {
        private final Map<Object, VariantTreeNode<T>> map = new HashMap<>();
        private final VariantSelector<T> owner;
        private final int depth;
        private final boolean isLastLayer;
        private final IdProperty<?> layerProperty;

        public VariantTree(VariantSelector<T> owner, IdProperty<?> layerProperty, int depth) {
            this.owner = owner;
            this.layerProperty = layerProperty;
            this.depth = depth;
            this.isLastLayer = this.depth + 1 >= this.owner.properties.size();
        }

        public static <T> VariantTree<T> create(VariantSelector<T> owner, int depth) {
            return new VariantTree<>(
                    owner,
                    owner.properties.get(depth),
                    depth + 1
            );
        }

        public void register(Map<IdProperty<?>, ?> properties, T value) {
            var property = properties.get(this.layerProperty);
            if (this.isLastLayer) {
                map.put(property, new LeafNode<>(value));
            } else {
                map.put(property, create(this.owner, this.depth));
            }
        }

        @Override
        public T select(Map<IdProperty<?>, ?> properties) {
            if (properties.containsKey(this.layerProperty)) {
                return this.map.get(properties.get(this.layerProperty)).select(properties);
            } else {
                return this.map.get(this.owner.defaults.get(this.layerProperty)).select(properties);
            }
        }

        @Override
        public <Value> List<Value> collectMapped(Function<T, Value> mapper) {
            return this.map.values().stream().flatMap(i -> i.collectMapped(mapper).stream()).toList();
        }
    }

    private record LeafNode<T>(T value) implements VariantTreeNode<T> {
        @Override
        public T select(Map<IdProperty<?>, ?> properties) {
            return value;
        }

        @Override
        public <Value> List<Value> collectMapped(Function<T, Value> mapper) {
            return List.of(mapper.apply(value));
        }
    }
}
