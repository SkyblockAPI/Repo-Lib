package tech.thatgravyboat.repolib.v2.expl.value;

import tech.thatgravyboat.repolib.v2.builtin.Constants;

import java.util.LinkedList;
import java.util.List;

public non-sealed interface Array extends Value, KeyValue, Iterable<Value> {

    static List<Value> flatten(List<Value> values) {
        List<Value> list = new LinkedList<>();
        for (var value : values) {
            if (value instanceof Array array) {
                list.addAll(flatten(array));
            } else {
                list.add(value);
            }
        }
        return list;
    }

    static List<Value> flatten(Array array) {
        List<Value> list = new LinkedList<>();
        array.forEach(list::add);
        return flatten(list);
    }

    static Value get(List<Value> entries, int index) {
        if ((index < 0 && -index > entries.size()) || (index >= entries.size())) {
            return Value.NIL;
        }

        return entries.get(Math.floorMod(index, entries.size()));
    }

    static KeyValue.Mutable createPrototype(List<Value> entries, boolean mutable, Struct prototype) {
        var prototypes = prototype.iterator();

        return new KeyValue.Mutable.Forwarding() {
            final KeyValue.Mutable base = Constants.mutable(builder -> {
                prototypes.forEachRemaining(entry -> builder.field(entry.getKey(), entry.getValue()));

                builder.function("get", function -> {
                    function.arity(1);
                    function.execute((evaluator, args) -> Array.get(entries, (int)  evaluator.getNumberOrThrow(args.getFirst())));
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
                    case "length", "size" -> new Num(entries.size());
                    case null, default -> base.get(field);
                };
            }
        };
    }

    Value get(int index);

    KeyValue.Mutable toMutableArray();

    interface Mutable extends Array, KeyValue.Mutable {

        void set(int index, Value value);

        void add(Value value);

        Array toImmutableArray();

    }

}
