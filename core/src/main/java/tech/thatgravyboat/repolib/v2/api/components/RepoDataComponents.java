package tech.thatgravyboat.repolib.v2.api.components;

import com.google.gson.JsonElement;
import tech.thatgravyboat.repolib.v2.api.types.SkyBlockIdType;
import tech.thatgravyboat.repolib.v2.internal.codec.RepoCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

public class RepoDataComponents {
    private static final List<RepoDataComponent<?>> components = new ArrayList<>();
    private static final Map<SkyBlockIdType, List<RepoDataComponent<?>>> map = new HashMap<>();

    public static RepoDataComponent<JsonElement> NAME = json("name");
    public static RepoDataComponent<JsonElement> TOOLTIP = json("tooltip", SkyBlockIdType.ITEM);
    public static RepoDataComponent<String> ITEM_TYPE = string("item_type", SkyBlockIdType.ITEM);
    public static RepoDataComponent<String> MODEL = string("model", SkyBlockIdType.ITEM);

    public static RepoDataComponent<Boolean> SACKABLE = bool("sackable", SkyBlockIdType.ITEM);
    public static RepoDataComponent<Boolean> MUSEUMABLE = bool("museumable", SkyBlockIdType.ITEM);
    public static RepoDataComponent<Integer> COLOR = i32("color", SkyBlockIdType.ITEM);
    public static RepoDataComponent<Boolean> ENCHANTMENT_OVERRIDE = bool(
            "enchantment_override",
            SkyBlockIdType.ITEM);
    public static RepoDataComponent<String> CATEGORY = string("category", SkyBlockIdType.ITEM);
    public static RepoDataComponent<String> TIER = string("tier", SkyBlockIdType.ITEM);
    public static RepoDataComponent<JsonElement> CRAFTING_REQUIREMENTS = json(
            "crafting_requirements",
            SkyBlockIdType.ITEM);
    public static RepoDataComponent<String> POWER = string("power", SkyBlockIdType.ITEM);

    private RepoDataComponents() {
    }


    private static RepoDataComponent<Boolean> bool(String name) {
        return register(name, FilterBuilder::all, RepoCodec.BOOLEAN);
    }

    private static RepoDataComponent<Boolean> bool(String name, SkyBlockIdType... types) {
        return register(name, (builder) -> builder.allowAll(types), RepoCodec.BOOLEAN);
    }

    private static RepoDataComponent<String> string(String name) {
        return register(name, FilterBuilder::all, RepoCodec.STRING);
    }

    private static RepoDataComponent<String> string(String name, SkyBlockIdType... types) {
        return register(name, (builder) -> builder.allowAll(types), RepoCodec.STRING);
    }

    private static RepoDataComponent<JsonElement> json(String name) {
        return register(name, FilterBuilder::all, RepoCodec.JSON);
    }

    private static RepoDataComponent<JsonElement> json(String name, SkyBlockIdType... types) {
        return register(name, (builder) -> builder.allowAll(types), RepoCodec.JSON);
    }

    private static RepoDataComponent<Integer> i32(String name) {
        return register(name, FilterBuilder::all, RepoCodec.INTEGER);
    }

    private static RepoDataComponent<Integer> i32(String name, SkyBlockIdType... types) {
        return register(name, (builder) -> builder.allowAll(types), RepoCodec.INTEGER);
    }

    private static <Type> RepoDataComponent<Type> register(
            String name,
            RepoCodec<Type> converter,
            SkyBlockIdType... types
    ) {
        return register(name, (builder) -> builder.allowAll(types), converter);
    }

    private static <Type> RepoDataComponent<Type> register(
            String name,
            UnaryOperator<FilterBuilder> setup,
            RepoCodec<Type> converter
    ) {
        var component = new SimpleRepoDataComponent<>(name, setup.apply(new FilterBuilder()).build(), converter);
        for (var allowedType : component.allowedTypes()) {
            allowedType.registerComponent(component);
        }
        return component;
    }

    private static class FilterBuilder {
        private final Set<SkyBlockIdType> allowed = new HashSet<>();

        public FilterBuilder all() {
            allowed.addAll(SkyBlockIdType.types);
            return this;
        }

        public FilterBuilder allow(SkyBlockIdType type) {
            allowed.add(type);
            return this;
        }

        public FilterBuilder disallow(SkyBlockIdType type) {
            allowed.remove(type);
            return this;
        }

        public FilterBuilder snippet(FilterBuilder builder) {
            allowed.addAll(builder.allowed);
            return this;
        }

        public FilterBuilder allowAll(SkyBlockIdType... builder) {
            allowed.addAll(List.of(builder));
            return this;
        }

        public FilterBuilder disallowAll(SkyBlockIdType... builder) {
            List.of(builder).forEach(allowed::remove);
            return this;
        }

        public Set<SkyBlockIdType> build() {
            return allowed;
        }
    }

}
