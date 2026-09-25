package tech.thatgravyboat.repolib.v2.builtin;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import tech.thatgravyboat.repolib.v2.expl.Evaluator;
import tech.thatgravyboat.repolib.v2.expl.value.*;

import java.util.*;
import java.util.function.*;

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
    public MutableStructValue toMutableStruct() {
        return new MutableStructValue(new HashMap<>(map));
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean contains(String field) {
        return map.containsKey(field);
    }

    public static Mutable mutable(Consumer<Builder> builder) {
        return new MutableStructValue(Builder.create(builder, MutableStructValue::new));
    }

    public static StructValue kvFromJson(JsonObject jsonObject) {
        Map<String, Value> structKeys = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            var jsonEntry = jsonObject.get(key);
            structKeys.put(key, valueFromJson(jsonEntry));
        }
        return new ImmutableStructValue(structKeys);
    }

    private static Value valueFromJson(JsonElement element) {
        return switch (element) {
            case JsonObject subObject: {
                yield kvFromJson(subObject);
            }
            case JsonArray jsonArray: {
                List<Value> array = new ArrayList<>(jsonArray.size());
                for (var arrayElement : jsonArray) {
                    array.add(valueFromJson(arrayElement));
                }
                yield MutableArrayValue.create(array).toImmutableArray();
            }
            case JsonPrimitive primitive: {
                if (primitive.isNumber()) {
                    yield new NumValue(primitive.getAsDouble());
                } else if (primitive.isBoolean()) {
                    yield BoolValue.wrap(primitive.getAsBoolean());
                } else if (primitive.isString()) {
                    yield new StrValue(primitive.getAsString());
                }
                yield Value.NIL;
            }
            case JsonNull ignored: {
                yield Value.NIL;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + element);
        };
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Map<String, KeyValue> sourceMap() {
        Map<String, KeyValue> result = new HashMap<>(map.size());
        for (String key : keySet()) {
            result.put(key, this);
        }
        return result;
    }

    @Override
    public @NotNull Iterator<Map.Entry<String, Value>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    public static class Builder {
        private Function<Map<String, Value>, KeyValue> struct;
        private final Map<String, Value> map = new HashMap<>();

        private Builder() {
        }

        private static Map<String, Value> create(Consumer<Builder> builderConsumer, Function<Map<String, Value>, KeyValue> struct) {
            var builder = new Builder();
            builder.struct = struct;
            builderConsumer.accept(builder);
            return builder.map;
        }

        public void constant(String name, String value) {
            field(name, new StrValue(value));
        }

        public void constant(String name, boolean value) {
            field(name, BoolValue.wrap(value));
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

        public void struct(String name, Consumer<Builder> builder) {
            field(name, struct.apply(Builder.create(builder, struct)));
        }

        public void mutableStruct(String name, Consumer<Builder> builder) {
            field(name, struct.apply(Builder.create(builder, MutableStructValue::new)));
        }

        public void immutableStruct(String name, Consumer<Builder> builder) {
            field(name, struct.apply(Builder.create(builder, ImmutableStructValue::new)));
        }

        public void field(String name, Value value) {
            map.putIfAbsent(name, value);
        }

        public static class FunctionBuilder {
            private int arityMin = 0;
            private int arityMax = Short.MAX_VALUE;
            private boolean vararg = false;
            private BiFunction<Evaluator, List<Value>, Value> executor;

            public static FunctionValue create(Consumer<FunctionBuilder> builderConsumer) {
                var builder = new FunctionBuilder();
                builderConsumer.accept(builder);
                int arityMin = builder.arityMin;
                int arityMax = builder.arityMax;
                boolean vararg = builder.vararg;
                var executor = builder.executor;
                return new LambdaFunctionValue((evaluator, args) -> {
                    if ((vararg && (args.size() > arityMax || args.size() < arityMin)) || (!vararg && args.size() != arityMin)) {
                        evaluator.error("Arity mismatched, expected " + arityMin + (arityMin != arityMax ?
                                " - " + arityMax :
                                "") + " arguments but got " + args.size());
                        return NIL;
                    }

                    return executor.apply(evaluator, args);
                }, vararg, arityMin, arityMax);
            }

            public void vararg(boolean isVararg) {
                this.vararg = isVararg;
            }

            public void arity(int arity) {
                if (arity < 0) {
                    throw new IllegalArgumentException("Arity " + arity + "out of range [0;[");
                }
                arityMin = arity;
                arityMax = Short.MAX_VALUE;
            }

            public void arity(int arityMin, int arityMax) {
                if (arityMax <= arityMin) {
                    throw new IllegalArgumentException("Max arity is below (or equal) min arity!");
                }
                if (arityMax < 0 || arityMin < 0) {
                    throw new IllegalArgumentException("Arity is out of range");
                }
                this.arityMin = arityMin;
                this.arityMax = arityMax;
                vararg = true;
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

            public void executeArgless(Function<Evaluator, Value> executor) {
                this.executor = ((evaluator, _) -> executor.apply(evaluator));
            }

            public void executeArglessVoid(Consumer<Evaluator> executor) {
                this.executeVoid((evaluator, _) -> executor.accept(evaluator));
            }

            public void executeSimple(Function<List<Value>, Value> executor) {
                this.executor = ((_, values) -> executor.apply(values));
            }

            public void executeSimpleVoid(Consumer<List<Value>> executor) {
                this.executeVoid((_, args) -> executor.accept(args));
            }

            public void supply(Supplier<Value> executor) {
                this.executor = ((_, _) -> executor.get());
            }

            public void runs(Runnable executor) {
                this.executeVoid((_, _) -> executor.run());
            }

            public int getArityMin() {
                return arityMin;
            }

            public int getArityMax() {
                return arityMax;
            }

            public boolean isVararg() {
                return vararg;
            }

            public BiFunction<Evaluator, List<Value>, Value> getExecutor() {
                return executor;
            }
        }
    }
}
