package tech.thatgravyboat.repolib.v2.builtin;

import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.BoolValue;
import tech.thatgravyboat.repolib.v2.expl.value.FunctionValue;
import tech.thatgravyboat.repolib.v2.expl.value.ImmutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.KeyValue;
import tech.thatgravyboat.repolib.v2.expl.value.MutableStructValue;
import tech.thatgravyboat.repolib.v2.expl.value.NumValue;
import tech.thatgravyboat.repolib.v2.expl.value.StrValue;
import tech.thatgravyboat.repolib.v2.expl.value.StructValue;
import tech.thatgravyboat.repolib.v2.expl.value.Value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public record Constants(Map<String, Value> map) implements StructValue {
    public Constants(Consumer<Builder> builder) {
        this(Builder.create(builder, ImmutableStructValue::new));
    }

    @Override
    public Value get(String field) {
        if (field.equals("this")) return this;
        return map.get(field);
    }

    @Override
    public MutableStructValue toMutable() {
        return new MutableStructValue(new HashMap<>(map));
    }

    @Override
    public boolean contains(String field) {
        return map.containsKey(field);
    }

    public static KeyValue.Mutable mutable(Consumer<Builder> builder) {
        return new MutableStructValue(Builder.create(builder, MutableStructValue::new));
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return map.entrySet().iterator();
    }

    public static class Builder {
        private java.util.function.Function<Map<String, Value>, KeyValue> struct;
        private final Map<String, Value> map = new HashMap<>();

        private Builder() {
        }

        private static Map<String, Value> create(Consumer<Builder> builderConsumer, java.util.function.Function<Map<String, Value>, KeyValue> struct) {
            var builder = new Builder();
            builder.struct = struct;
            builderConsumer.accept(builder);
            return builder.map;
        }

        public void constant(String name, String value) {
            field(name, new StrValue(value));
        }

        public void constant(String name, boolean value) {
            field(name, new BoolValue(value));
        }

        public void constant(String name, int value) {
            field(name, new NumValue(value));
        }

        public void constant(String name, long value) {
            field(name, new NumValue(value));
        }

        public void constant(String name, float value) {
            field(name, new NumValue(value));
        }

        public void constant(String name, double value) {
            field(name, new NumValue(value));
        }

        public void function(String name, Consumer<FunctionBuilder> function) {
            field(name, FunctionBuilder.create(function));
        }

        public void struct(String name, Consumer<Constants.Builder> builder) {
            field(name, struct.apply(Builder.create(builder, struct)));
        }

        public void mutableStruct(String name, Consumer<Constants.Builder> builder) {
            field(name, struct.apply(Builder.create(builder, MutableStructValue::new)));
        }

        public void immutableStruct(String name, Consumer<Constants.Builder> builder) {
            field(name, struct.apply(Builder.create(builder, ImmutableStructValue::new)));
        }

        public void field(String name, Value value) {
            map.putIfAbsent(name, value);
        }

        public static class FunctionBuilder {
            private int arity = 0;
            private boolean vararg = false;
            private BiFunction<Evaluator, List<Value>, Value> executor;

            public static FunctionValue create(Consumer<FunctionBuilder> builderConsumer) {
                var builder = new FunctionBuilder();
                builderConsumer.accept(builder);
                int arity = builder.arity;
                boolean vararg = builder.vararg;
                var executor = builder.executor;
                return ((evaluator, args) -> {
                    if ((vararg && args.size() < arity) || (!vararg && args.size() != arity)) {
                        evaluator.error("Arity mismatched, expected " + arity + " arguments but got " + args.size());
                        return NIL;
                    }

                    return executor.apply(evaluator, args);
                });
            }

            public void vararg(boolean isVararg) {
                this.vararg = isVararg;
            }

            public void arity(int arity) {
                if (arity < 0) {
                    throw new IllegalArgumentException("Arity " + arity + "out of range [0;[");
                }
                this.arity = arity;
            }

            public void execute(BiFunction<Evaluator, List<Value>, Value> executor) {
                this.executor = executor;
            }

            public void executeVoid(BiConsumer<Evaluator, List<Value>> executor) {
                this.executor = (evaluator, values) -> {
                    executor.accept(evaluator, values);
                    return NIL;
                };
            }

            public void executeArgless(java.util.function.Function<Evaluator, Value> executor) {
                this.executor = ((evaluator, values) -> executor.apply(evaluator));
            }

            public void executeArglessVoid(Consumer<Evaluator> executor) {
                this.executeVoid((evaluator, args) -> executor.accept(evaluator));
            }

            public void executeSimple(java.util.function.Function<List<Value>, Value> executor) {
                this.executor = ((evaluator, values) -> executor.apply(values));
            }

            public void executeSimpleVoid(Consumer<List<Value>> executor) {
                this.executeVoid((evaluator, args) -> executor.accept(args));
            }

            public void supply(Supplier<Value> executor) {
                this.executor = ((evaluator, values) -> executor.get());
            }

            public void runs(Runnable executor) {
                this.executeVoid((evaluator, values) -> executor.run());
            }
        }
    }
}
