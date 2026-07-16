package tech.thatgravyboat.repolib.v2.expl.value;

import tech.thatgravyboat.repolib.v2.builtin.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public non-sealed interface ArrayValue extends Value, KeyValue, Iterable<Value> {
    public static String prettyPrint(ArrayValue array, String prefix) {
        var values = getValues(array);
        var stringBuilder = new StringBuilder();

        stringBuilder.append("[").append("\n");
        for (var value : values) {
            stringBuilder.append(prefix).append(" ").append(Value.prettyPrint(value, prefix)).append(",\n");
        }
        stringBuilder.append(prefix).append("]");

        return stringBuilder.toString();
    }

    @Override
    default String type() {
        return "array";
    }

    static List<Value> flatten(List<Value> values) {
        List<Value> list = new LinkedList<>();
        for (var value : values) {
            if (value instanceof ArrayValue array) {
                list.addAll(getValues(array));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    static List<Value> getValues(ArrayValue array) {
        List<Value> list = new LinkedList<>();
        array.forEach(list::add);
        return list;
    }

    static List<Value> flatten(ArrayValue array) {
        return flatten(getValues(array));
    }

    static Value get(List<Value> entries, int index) {
        if ((index < 0 && -index > entries.size()) || (index >= entries.size())) {
            return Value.NIL;
        }

        return entries.get(Math.floorMod(index, entries.size()));
    }

    static KeyValue.Mutable createPrototype(List<Value> entries, boolean mutable, StructValue prototype) {
        var prototypes = prototype.iterator();

        return new KeyValue.Mutable.Forwarding() {
            final KeyValue.Mutable base = Constants.mutable(builder -> {
                prototypes.forEachRemaining(entry -> builder.field(entry.getKey(), entry.getValue()));

                builder.function("get", function -> {
                    function.arity(1);
                    function.execute((evaluator, args) -> ArrayValue.get(entries, (int)  evaluator.getNumberOrThrow(args.getFirst())));
                });

                builder.function("chunked", function -> {
                    function.arity(1);
                    function.execute((evaluator, values) -> {
                        var amount = evaluator.getNumberOrThrow(values.getFirst());

                        var array = MutableArrayValue.create();

                        var current = MutableArrayValue.create();
                        for (var entry : entries) {
                            if (current.entries().size() >= amount) {
                                array.add(entry);
                                current = MutableArrayValue.create();
                            }
                            current.add(entry);
                        }

                        if (!current.entries().isEmpty()) {
                            array.add(current);
                        }

                        return array;
                    });
                });

                if (!mutable) {
                    return;
                }
                builder.function(
                        "add", function -> {
                            function.arity(1);
                            function.executeSimpleVoid(args -> entries.add(args.getFirst()));
                        });

                builder.function(
                        "addFirst", function -> {
                            function.arity(1);
                            function.executeSimpleVoid(args -> entries.addFirst(args.getFirst()));
                        });

                builder.function("addAll", function -> {
                    function.arity(1);
                    function.executeVoid((evaluator, args) -> {
                        entries.addAll(ArrayValue.flatten(args));
                    });
                });

                builder.function(
                        "set", function -> {
                            function.arity(2);
                            function.executeVoid((evaluator, args) -> {
                                var index = evaluator.getNumberOrThrow(args.getFirst());
                                entries.set((int) index, args.get(1));
                            });
                        });

                builder.function(
                        "clear", function -> {
                            function.arity(0);
                            function.runs(entries::clear);
                        });
            });

            @Override
            public Mutable delegate() {
                return base;
            }

            @Override
            public Value get(String field) {
                return switch (field) {
                    case "length", "size" -> new NumValue(entries.size());
                    case null, default -> base.get(field);
                };
            }
        };
    }

    Value get(int index);

    KeyValue.Mutable toMutableArray();

    interface Mutable extends ArrayValue, KeyValue.Mutable {

        void set(int index, Value value);

        void add(Value value);

        ArrayValue toImmutableArray();

    }

}
